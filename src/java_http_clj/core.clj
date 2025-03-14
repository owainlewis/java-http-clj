(ns java-http-clj.core
  "Core functionality for the Java HTTP client wrapper.
   Provides a Clojure-friendly interface to the Java 11+ HttpClient."
  (:refer-clojure :exclude [get])
  (:require [clojure.string :as str]
            [java-http-clj.client :as client]
            [java-http-clj.util :as util])
  (:import [java.net URI]
           [java.net.http
            HttpClient
            HttpRequest
            HttpRequest$Builder
            HttpRequest$BodyPublishers
            HttpResponse
            HttpResponse$BodyHandlers]
           [java.util.concurrent CompletableFuture]
           [java.nio.charset StandardCharsets]
           [java.util Base64]))

;; Request building

(defn- add-headers
  "Add headers to an HttpRequest$Builder."
  [^HttpRequest$Builder builder headers]
  (reduce-kv
   (fn [builder k v]
     (.header builder (name k) (str v)))
   builder
   headers))

(defn- add-basic-auth
  "Add Basic Authentication header to an HttpRequest$Builder."
  [^HttpRequest$Builder builder [username password]]
  (let [auth-string (str username ":" password)
        encoded (.encodeToString (Base64/getEncoder) (.getBytes auth-string StandardCharsets/UTF_8))]
    (.header builder "Authorization" (str "Basic " encoded))))

(defn- body-publisher
  "Convert a body value to an appropriate BodyPublisher."
  [body]
  (cond
    (nil? body)
    (HttpRequest$BodyPublishers/noBody)
    
    (string? body)
    (HttpRequest$BodyPublishers/ofString body)
    
    (bytes? body)
    (HttpRequest$BodyPublishers/ofByteArray body)
    
    (instance? java.io.InputStream body)
    (HttpRequest$BodyPublishers/ofInputStream #(body))
    
    (instance? java.nio.file.Path body)
    (HttpRequest$BodyPublishers/ofFile body)
    
    :else
    (HttpRequest$BodyPublishers/ofString (str body))))

(defn- build-request
  "Build an HttpRequest from a request map."
  ^HttpRequest [{:keys [method url headers body basic-auth timeout] :as req}]
  (let [method-str (-> method name str/upper-case)
        uri (URI/create url)
        builder (-> (HttpRequest/newBuilder)
                    (.uri uri)
                    (.method method-str (body-publisher body)))]
    (cond-> builder
      headers    (add-headers headers)
      basic-auth (add-basic-auth basic-auth)
      timeout    (.timeout (util/convert-timeout timeout))
      :always    (.build))))

;; Response processing

(defn- process-response
  "Process an HttpResponse into a Clojure map."
  [^HttpResponse response]
  {:status (.statusCode response)
   :headers (into {} (.map (.headers response)))
   :body (.body response)
   :version (util/version->sym (.version response))
   :uri (.uri response)})

;; Public API

(defn request
  "Make an HTTP request and return the response.
   
   Request map options:
   - :method - HTTP method (:get, :post, :put, :delete, etc.)
   - :url - URL string
   - :headers - Map of headers
   - :body - Request body (String, byte[], InputStream, or Path)
   - :basic-auth - Vector of [username password]
   - :timeout - Request timeout in milliseconds
   
   Client options:
   - :client - HttpClient instance to use
   - :timeout - Connection timeout in milliseconds
   - :follow-redirects - Redirect policy (:always, :never, :normal)
   - :version - HTTP version (:http1.1, :http2)
   - :as - Response body type (:string, :bytes, :stream, :discard)"
  ([req]
   (request req {}))
  ([{:keys [method url] :as req} {:keys [client as] :or {as :string} :as opts}]
   (let [http-client (or client @client/default-client)
         http-request (build-request req)
         body-handler (util/get-body-handler as)
         response (.send http-client http-request body-handler)]
     (process-response response))))

(defn async-request
  "Make an asynchronous HTTP request.
   
   Returns a CompletableFuture that will complete with the response.
   If a callback function is provided, it will be called with the response.
   
   See `request` for options."
  ([req]
   (async-request req {} nil))
  ([req opts]
   (async-request req opts nil))
  ([{:keys [method url] :as req} {:keys [client as] :or {as :string} :as opts} callback]
   (let [http-client (or client @client/default-client)
         http-request (build-request req)
         body-handler (util/get-body-handler as)
         future (.sendAsync http-client http-request body-handler)]
     (if callback
       (.thenApply future
                   (util/body-handler->function
                    (comp callback process-response)))
       (.thenApply future
                   (util/body-handler->function
                    process-response))))))

;; Convenience methods for common HTTP methods

(defn get
  "Make a GET request.
   
   url - URL string
   opts - Optional map of request options
   
   See `request` for available options."
  ([url]
   (get url {}))
  ([url opts]
   (request (assoc opts :method :get :url url) opts)))

(defn post
  "Make a POST request.
   
   url - URL string
   body - Request body
   opts - Optional map of request options
   
   See `request` for available options."
  ([url body]
   (post url body {}))
  ([url body opts]
   (request (assoc opts :method :post :url url :body body) opts)))

(defn put
  "Make a PUT request.
   
   url - URL string
   body - Request body
   opts - Optional map of request options
   
   See `request` for available options."
  ([url body]
   (put url body {}))
  ([url body opts]
   (request (assoc opts :method :put :url url :body body) opts)))

(defn delete
  "Make a DELETE request.
   
   url - URL string
   opts - Optional map of request options
   
   See `request` for available options."
  ([url]
   (delete url {}))
  ([url opts]
   (request (assoc opts :method :delete :url url) opts)))

(defn head
  "Make a HEAD request.
   
   url - URL string
   opts - Optional map of request options
   
   See `request` for available options."
  ([url]
   (head url {}))
  ([url opts]
   (request (assoc opts :method :head :url url) opts)))

(defn options
  "Make an OPTIONS request.
   
   url - URL string
   opts - Optional map of request options
   
   See `request` for available options."
  ([url]
   (options url {}))
  ([url opts]
   (request (assoc opts :method :options :url url) opts)))

(defn patch
  "Make a PATCH request.
   
   url - URL string
   body - Request body
   opts - Optional map of request options
   
   See `request` for available options."
  ([url body]
   (patch url body {}))
  ([url body opts]
   (request (assoc opts :method :patch :url url :body body) opts)))

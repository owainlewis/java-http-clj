(ns java-http-clj.core
  (:refer-clojure :exclude [get])
  (:require [clojure.string :as str]
            [java-http-clj.client :refer [default-client]]
            [java-http-clj.util :refer [convert-timeout convert-version version->sym]])
  (:import [java.net URI]
           [java.net.http
            HttpClient
            HttpRequest
            HttpRequest$BodyPublishers
            HttpRequest$Builder
            HttpResponse
            HttpResponse$BodyHandlers]
           [java.time Duration]
           [java.util.concurrent CompletableFuture Executor]
           [java.util.function Function Supplier]
           [javax.net.ssl SSLContext SSLParameters]))

(defn- convert-body-publisher [body]
  (letfn [(input-stream-supplier [s]
            (reify Supplier
              (get [this] s)))]
    (let [byte-array-class (Class/forName "[B")]
      (cond
        (nil? body)
        (HttpRequest$BodyPublishers/noBody)
        (string? body)
        (HttpRequest$BodyPublishers/ofString body)
        (instance? java.io.InputStream body)
        (HttpRequest$BodyPublishers/ofInputStream (input-stream-supplier body))
        (instance? byte-array-class body)
        (HttpRequest$BodyPublishers/ofByteArray body)))))

(def ^:private convert-headers-xf
  (mapcat
   (fn [[k v :as p]]
     (if (sequential? v)
       (interleave (repeat k) v)
       p))))

(defn params->query-string
  "Converts a map of query parameter options into a URL encoded query string that
   can be added to a URI"
  [m]
  (str/join "&"
            (for [[k v] m]
              (str (name k) "=" (java.net.URLEncoder/encode v)))))

(defn build-uri [uri query-parameters]
  (if (nil? query-parameters)
    (URI/create uri))
  (let [qs (params->query-string query-parameters)]
    (URI/create (str uri "?" qs))))

(defn request-builder
  ^HttpRequest$Builder [opts]
  (let [{:keys [expect-continue?
                headers
                query-parameters
                method
                timeout
                url
                version
                body]} opts]
    (cond-> (HttpRequest/newBuilder)
      (some? expect-continue?) (.expectContinue expect-continue?)
      (seq headers)            (.headers (into-array String (eduction convert-headers-xf headers)))
      method                   (.method (str/upper-case (name method)) (convert-body-publisher body))
      timeout                  (.timeout (convert-timeout timeout))
      url                      (.uri (build-uri url query-parameters))
      version                  (.version (convert-version version)))))

(defn build-request
  ([] (.build (request-builder {})))
  ([req-map]
   (.build (request-builder req-map))))

(defn response->map [^HttpResponse resp]
  (letfn [(version-string->version-keyword [version]
            (case (.name version)
              "HTTP_1_1" :http1.1
              "HTTP_2"   :http2))]
    {:status (.statusCode resp)
     :body (.body resp)
     :version (-> resp .version version-string->version-keyword)
     :headers (into {}
                    (map
                     (fn [[k v]]
                       [k (if (> (count v) 1)
                            (vec v)
                            (first v))]))
                    (.map (.headers resp)))}))

(defmacro fn->java-function
  "Turns a Clojure fn form into a Java function"
  ^Function
  [f]
  `(reify Function
     (apply [_# x#] (~f x#))))

(defn- convert-request
  [req]
  (cond
    (map? req)
    (build-request req)
    (string? req)
    (build-request {:url req})
    (instance? HttpRequest req)
    req))

(defn- convert-body-handler
  "Maps a clojure body handler keyword to a Java form"
  [mode]
  (case mode
    nil (HttpResponse$BodyHandlers/ofString)
    :string (HttpResponse$BodyHandlers/ofString)
    :input-stream (HttpResponse$BodyHandlers/ofInputStream)
    :byte-array (HttpResponse$BodyHandlers/ofByteArray)))

;; ***************************************
;; Public API
;; ***************************************

(defonce ^:private NANOSECOND_MILLIS 1000000.0)

(defn request
  "Sends a HTTP request and blocks until a response is returned or the request
   takes longer than the specified `timeout`.
   If the request times out, a [HttpTimeoutException]
   (https://docs.oracle.com/en/java/javase/11/docs/api/java.net.http/java/net/http/HttpTimeoutException.html) is thrown.

   The request map takes the following keys:
   - `:method`  - HTTP method as a keyword (e.g `:get`, `:put`, `:post`)
   - `:url`     - the request url
   - `:headers` - HTTP headers as a map where keys are strings and values are strings or a list of strings
   - `:body`    - the request body. Can be a string, a primitive Java byte array or a java.io.InputStream.
   - `:timeout` - the request timeout in milliseconds or a `java.time.Duration`
   - `:version` - the HTTP protocol version, one of `:http1.1` or `:http2`

   The `opts` param is a map containing one of the following keys:
   - `:as` - converts the response body to one of the following formats:
     - `:string`       - a java.lang.String (default)
     - `:byte-array`   - a Java primitive byte array.
     - `:input-stream` - a java.io.InputStream.
   - `:client` - the [HttpClient](https://docs.oracle.com/en/java/javase/11/docs/api/java.net.http/java/net/http/HttpClient.html)
                 to use for the request. If not provided the [[default-client]] will be used.
   - `:raw?`   - if true, skip the Ring format conversion and return the
                 raw [HttpResponse](https://docs.oracle.com/en/java/javase/11/docs/api/java.net.http/java/net/http/HttpResponse.html"
  ([req]
   (request req {}))
  ([req {:keys [as client raw?] :as opts}]
   (let [^HttpClient client (or client @default-client)
         req' (convert-request req)
         start (. System (nanoTime))
         resp (.send client req' (convert-body-handler as))]
     (let [request-time-ms (/ (double (- (. System (nanoTime)) start)) NANOSECOND_MILLIS)]
       (if raw? resp
           (with-meta
             (response->map resp)
             {:request-time-ms request-time-ms}))))))

(defn async-request
  ([req]
   (async-request req {} nil nil))
  ([req opts]
   (async-request req opts nil nil))
  ([req opts callback]
   (async-request req opts callback nil))
  ([req {:keys [as client raw?]} callback ex-handler]
   (let [^HttpClient client (or client @default-client)
         req' (convert-request req)]
     (cond-> (.sendAsync client req' (convert-body-handler as))
       (not raw?)  (.thenApply (fn->java-function response->map))
       callback    (.thenApply (fn->java-function callback))
       ex-handler  (.exceptionally (fn->java-function ex-handler))))))

(defn get
  "Like #'request, but sets the :method and :url as appropriate."
  [url & [req opts]]
  (request (merge req {:method :get :url url}) (or opts {})))

(defn put
  "Like #'request, but sets the :method and :url as appropriate."
  [url & [req opts]]
  (request (merge req {:method :put :url url}) (or opts {})))

(defn patch
  "Like #'request, but sets the :method and :url as appropriate."
  [url & [req opts]]
  (request (merge req {:method :patch :url url}) (or opts {})))

(defn post
  "Like #'request, but sets the :method and :url as appropriate."
  [url & [req opts]]
  (request (merge req {:method :post :url url}) (or opts {})))

(defn delete
  "Like #'request, but sets the :method and :url as appropriate."
  [url & [req opts]]
  (request (merge req {:method :delete :url url}) (or opts {})))

(defn head
  "Like #'request, but sets the :method and :url as appropriate."
  [url & [req opts]]
  (request (merge req {:method :head :url url}) (or opts {})))

(defn options
  "Like #'request, but sets the :method and :url as appropriate."
  [url & [req opts]]
  (request (merge req {:method :options :url url}) (or opts {})))

(defn get-async
  "Like #'async-request, but sets the :method and :url as appropriate."
  [url & [req opts callback exception-handler]]
  (async-request (merge req {:method :get :url url}) (or opts {}) callback exception-handler))

(defn put-async
  "Like #'async-request, but sets the :method and :url as appropriate."
  [url & [req opts callback exception-handler]]
  (async-request (merge req {:method :put :url url}) (or opts {}) callback exception-handler))

(defn patch-async
  "Like #'async-request, but sets the :method and :url as appropriate."
  [url & [req opts callback exception-handler]]
  (async-request (merge req {:method :patch :url url}) (or opts {}) callback exception-handler))

(defn post-async
  "Like #'async-request, but sets the :method and :url as appropriate."
  [url & [req opts callback exception-handler]]
  (async-request (merge req {:method :post :url url}) (or opts {}) callback exception-handler))

(defn delete-async
  "Like #'async-request, but sets the :method and :url as appropriate."
  [url & [req opts callback exception-handler]]
  (async-request (merge req {:method :delete :url url}) (or opts {}) callback exception-handler))

(defn head-async
  "Like #'async-request, but sets the :method and :url as appropriate."
  [url & [req opts callback exception-handler]]
  (async-request (merge req {:method :head :url url}) (or opts {}) callback exception-handler))

(defn options-async
  "Like #'async-request, but sets the :method and :url as appropriate."
  [url & [req opts callback exception-handler]]
  (async-request (merge req {:method :options :url url}) (or opts {}) callback exception-handler))

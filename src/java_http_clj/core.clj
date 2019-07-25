(ns java-http-clj.core
  (:refer-clojure :exclude [get])
  (:require [clojure.string :as str])
  (:import [java.net URI]
           [java.net.http
              HttpClient
              HttpClient$Builder
              HttpClient$Redirect
              HttpClient$Version
              HttpRequest
              HttpRequest$BodyPublishers
              HttpRequest$Builder
              HttpResponse
              HttpResponse$BodyHandlers]
           [java.time Duration]
           [java.util.concurrent CompletableFuture Executor]
           [java.util.function Function Supplier]
           [javax.net.ssl SSLContext SSLParameters]))

(defn convert-timeout [t]
  (if (integer? t)
    (Duration/ofMillis t)
    t))

(defn client-builder
  (^HttpClient$Builder []
   (client-builder {}))
  (^HttpClient$Builder [opts]
   (letfn [(version-keyword->version-enum [version]
            (case version
              :http1.1 HttpClient$Version/HTTP_1_1
              :http2   HttpClient$Version/HTTP_2))
          (follow-redirect [redirect]
            (case redirect
              :always HttpClient$Redirect/ALWAYS
              :never HttpClient$Redirect/NEVER
              :normal HttpClient$Redirect/NORMAL))])
   (let [{:keys [connect-timeout
                 cookie-handler
                 executor
                 follow-redirects
                 priority
                 proxy
                 ssl-context
                 ssl-parameters
                 version]} opts]
     (cond-> (HttpClient/newBuilder)
       connect-timeout  (.connectTimeout (convert-timeout connect-timeout))
       cookie-handler   (.cookieHandler cookie-handler)
       executor         (.executor executor)
       follow-redirects (.followRedirects (follow-redirect follow-redirects))
       priority         (.priority priority)
       proxy            (.proxy proxy)
       ssl-context      (.sslContext ssl-context)
       ssl-parameters   (.sslParameters ssl-parameters)
       version          (.version (version-keyword->version-enum version))))))

(defn build-client
  ([] (.build (client-builder)))
  ([opts] (.build (client-builder opts))))

(def ^HttpClient default-client
  (delay (HttpClient/newHttpClient)))

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

(defn request-builder ^HttpRequest$Builder [opts]
  (let [{:keys [expect-continue?
                headers
                method
                timeout
                uri
                version
                body]} opts]
    (cond-> (HttpRequest/newBuilder)
      (some? expect-continue?) (.expectContinue expect-continue?)
      (seq headers)            (.headers (into-array String (eduction convert-headers-xf headers)))
      method                   (.method (str/upper-case (name method)) (convert-body-publisher body))
      timeout                  (.timeout (convert-timeout timeout))
      uri                      (.uri (URI/create uri))
      version                  (.version (version-keyword->version-enum version)))))

(defn build-request
  ([] (.build (request-builder {})))
  ([req-map] (.build (request-builder req-map))))

(defn response->map [^HttpResponse resp]
  (letfn [(version-string->version-keyword [version]
            (case (.name version)
              "HTTP_1_1" :http1.1
              "HTTP_2"   :http2))]
  {:status (.statusCode resp)
   :body (.body resp)
   :version (-> resp .version version-string->version-keyword)
   :headers (into {}
                  (map (fn [[k v]] [k (if (> (count v) 1) (vec v) (first v))]))
                  (.map (.headers resp)))}))

(defmacro clj-fn->function ^Function [f]
  `(reify Function
    (apply [_# x#] (~f x#))))

(defn- convert-request [req]
  (cond
    (map? req) (build-request req)
    (string? req) (build-request {:uri req})
    (instance? HttpRequest req) req))

(def ^:private bh-of-string (HttpResponse$BodyHandlers/ofString))
(def ^:private bh-of-input-stream (HttpResponse$BodyHandlers/ofInputStream))
(def ^:private bh-of-byte-array (HttpResponse$BodyHandlers/ofByteArray))

(defn- convert-body-handler [mode]
  (case mode
    nil bh-of-string
    :string bh-of-string
    :input-stream bh-of-input-stream
    :byte-array bh-of-byte-array))

;; ***************************************
;; Public API
;; ***************************************

(defn send-request
  ([req]
   (send-request req {}))
  ([req {:keys [as client raw?] :as opts}]
   (let [^HttpClient client (or client @default-client)
         req' (convert-request req)
         resp (.send client req' (convert-body-handler as))]
     (if raw? resp (response->map resp)))))

(defn async
  ([req]
   (async req {} nil nil))
  ([req opts]
   (async req opts nil nil))
  ([req {:keys [as client raw?] :as opts} callback ex-handler]
   (let [^HttpClient client (or client @default-client)
         req' (convert-request req)]
     (cond-> (.sendAsync client req' (convert-body-handler as))
       (not raw?)  (.thenApply (clj-fn->function response->map))
       callback    (.thenApply (clj-fn->function callback))
       ex-handler  (.exceptionally (clj-fn->function ex-handler))))))

(defn- shorthand-docstring [method]
  (str "Sends a " (str/upper-case (name method)) " request to `uri`.
   See [[send]] for a description of `req-map` and `opts`."))

(defn- defshorthand [method]
  `(defn ~(symbol (name method))
     ~(shorthand-docstring method)
     (~['uri]
       (send-request ~{:uri 'uri :method method} {}))
     (~['uri 'req-map]
       (send-request (merge ~'req-map ~{:uri 'uri :method method}) {}))
     (~['uri 'req-map 'opts]
       (send-request (merge ~'req-map ~{:uri 'uri :method method}) ~'opts))))

(def ^:private shorthands [:get :head :post :put :delete])

(defmacro ^:private def-all-shorthands []
  `(do ~@(map defshorthand shorthands)))

(def-all-shorthands)

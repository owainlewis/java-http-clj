(ns java-http-clj.client
  "HTTP client functionality wrapping Java 11+ HttpClient."
  (:refer-clojure :exclude [get])
  (:require [clojure.string :as str]
            [java-http-clj.util :as util])
  (:import [java.net URI]
           [java.net.http
            HttpClient
            HttpClient$Builder
            HttpClient$Redirect
            HttpClient$Version]
           [java.time Duration]))

(defn client-builder
  "Creates a new HttpClient builder with optional configuration.
   
   Options:
   - :connect-timeout - Connection timeout in milliseconds or Duration
   - :cookie-handler - CookieHandler instance
   - :executor - Executor for async requests
   - :follow-redirects - Redirect policy (:always, :never, :normal)
   - :priority - Request priority
   - :proxy - ProxySelector instance
   - :ssl-context - SSLContext instance
   - :ssl-parameters - SSLParameters instance
   - :version - HTTP version (:http1.1, :http2)"
  (^HttpClient$Builder []
   (client-builder {}))
  (^HttpClient$Builder [opts]
   (letfn [(convert-redirect [redirect]
             (case redirect
               :always HttpClient$Redirect/ALWAYS
               :never HttpClient$Redirect/NEVER
               :normal HttpClient$Redirect/NORMAL
               HttpClient$Redirect/NORMAL))]
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
         connect-timeout  (.connectTimeout (util/convert-timeout connect-timeout))
         cookie-handler   (.cookieHandler cookie-handler)
         executor         (.executor executor)
         follow-redirects (.followRedirects (convert-redirect follow-redirects))
         priority         (.priority priority)
         proxy            (.proxy proxy)
         ssl-context      (.sslContext ssl-context)
         ssl-parameters   (.sslParameters ssl-parameters)
         version          (.version (util/convert-version version)))))))

(defn build-client
  "Builds and returns a configured HttpClient instance.
   
   With no arguments, returns a default client.
   With options map, configures the client according to the options.
   See client-builder for available options."
  (^HttpClient [] (.build (client-builder)))
  (^HttpClient [opts] (.build (client-builder opts))))

(def ^HttpClient default-client
  "Default HttpClient instance for use when no custom configuration is needed."
  (delay (HttpClient/newHttpClient)))

(defn client-with-timeout
  "Creates a new HttpClient with the specified timeout in milliseconds."
  ^HttpClient [timeout-ms]
  (build-client {:connect-timeout timeout-ms}))

(defn client-with-version
  "Creates a new HttpClient with the specified HTTP version (:http1.1 or :http2)."
  ^HttpClient [version]
  (build-client {:version version}))

(defn client-with-redirect-policy
  "Creates a new HttpClient with the specified redirect policy (:always, :never, :normal)."
  ^HttpClient [redirect-policy]
  (build-client {:follow-redirects redirect-policy}))

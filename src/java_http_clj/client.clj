(ns java-http-clj.client
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
  (^HttpClient$Builder []
   (client-builder {}))
  (^HttpClient$Builder [opts]
   (letfn [(convert-redirect [redirect]
             (case redirect
               :always HttpClient$Redirect/ALWAYS
               :never HttpClient$Redirect/NEVER
               :normal HttpClient$Redirect/NORMAL))]
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
  ([] (.build (client-builder)))
  ([opts] (.build (client-builder opts))))

(def ^HttpClient default-client
  (delay (HttpClient/newHttpClient)))

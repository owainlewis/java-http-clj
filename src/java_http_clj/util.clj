(ns java-http-clj.util
  (:import [java.net.http HttpClient$Version]
           [java.time Duration]))

(defn convert-timeout [t]
  (if (integer? t)
    (Duration/ofMillis t)
    t))

(defn convert-version [version]
  (case version
    :http1.1 HttpClient$Version/HTTP_1_1
    :http2   HttpClient$Version/HTTP_2
    HttpClient$Version/HTTP_1_1))

(defn version->sym [version]
  (case (.name version)
    "HTTP_1_1" :http1.1
    "HTTP_2"   :http2
    :http1.1))
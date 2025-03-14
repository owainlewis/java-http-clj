(ns java-http-clj.util
  "Utility functions for the Java HTTP client wrapper."
  (:import [java.net.http HttpClient$Version HttpResponse$BodyHandlers]
           [java.time Duration]
           [java.nio.charset StandardCharsets]
           [java.util.function Function]
           [java.io InputStream]))

(defn convert-timeout
  "Converts a timeout value to a Duration.
   If the input is already a Duration, returns it unchanged.
   If the input is an integer, treats it as milliseconds."
  ^Duration [t]
  (if (integer? t)
    (Duration/ofMillis t)
    t))

(defn convert-version
  "Converts a keyword version to a HttpClient$Version.
   Supported versions: :http1.1, :http2
   Default is HTTP_1_1 if not specified or recognized."
  ^HttpClient$Version [version]
  (case version
    :http1.1 HttpClient$Version/HTTP_1_1
    :http2   HttpClient$Version/HTTP_2
    HttpClient$Version/HTTP_1_1))

(defn version->sym
  "Converts a HttpClient$Version to a keyword symbol.
   Returns :http1.1 or :http2.
   If version is nil, returns :http1.1 as default."
  [^HttpClient$Version version]
  (if (nil? version)
    :http1.1
    (case (.name version)
      "HTTP_1_1" :http1.1
      "HTTP_2"   :http2
      :http1.1)))

(defn string->input-stream
  "Converts a string to an InputStream using UTF-8 encoding."
  ^InputStream [^String s]
  (-> s
      (.getBytes StandardCharsets/UTF_8)
      (java.io.ByteArrayInputStream.)))

(defn input-stream->string
  "Converts an InputStream to a String using UTF-8 encoding."
  ^String [^InputStream is]
  (slurp is :encoding "UTF-8"))

(defn body-handler->function
  "Converts a body handler function to a Java Function for use with the HTTP client."
  ^Function [handler-fn]
  (reify Function
    (apply [_ response]
      (handler-fn response))))

(defn get-body-handler
  "Returns the appropriate BodyHandler based on the :as option.
   Supported options:
   - :string (default) - Returns response body as a String
   - :bytes - Returns response body as a byte array
   - :stream - Returns response body as an InputStream
   - :discard - Discards the response body
   - function - Custom handler function"
  [as]
  (case as
    :string  (HttpResponse$BodyHandlers/ofString)
    :bytes   (HttpResponse$BodyHandlers/ofByteArray)
    :stream  (HttpResponse$BodyHandlers/ofInputStream)
    :discard (HttpResponse$BodyHandlers/discarding)
    (if (fn? as)
      (HttpResponse$BodyHandlers/ofString)
      (HttpResponse$BodyHandlers/ofString))))
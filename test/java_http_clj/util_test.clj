(ns java-http-clj.util-test
  (:require [clojure.test :refer :all]
            [java-http-clj.util :as util])
  (:import [java.net.http HttpClient$Version HttpResponse$BodyHandlers]
           [java.time Duration]
           [java.nio.charset StandardCharsets]
           [java.io InputStream ByteArrayInputStream]))

(deftest test-convert-timeout
  (testing "Convert integer to Duration"
    (let [timeout 5000
          duration (util/convert-timeout timeout)]
      (is (instance? Duration duration))
      (is (= 5000 (.toMillis duration)))))
  
  (testing "Pass through Duration"
    (let [original (Duration/ofSeconds 10)
          result (util/convert-timeout original)]
      (is (identical? original result)))))

(deftest test-convert-version
  (testing "Convert :http1.1 to HTTP_1_1"
    (let [version (util/convert-version :http1.1)]
      (is (= HttpClient$Version/HTTP_1_1 version))))
  
  (testing "Convert :http2 to HTTP_2"
    (let [version (util/convert-version :http2)]
      (is (= HttpClient$Version/HTTP_2 version))))
  
  (testing "Default to HTTP_1_1 for unknown version"
    (let [version (util/convert-version :unknown)]
      (is (= HttpClient$Version/HTTP_1_1 version)))))

(deftest test-version->sym
  (testing "Convert HTTP_1_1 to :http1.1"
    (let [sym (util/version->sym HttpClient$Version/HTTP_1_1)]
      (is (= :http1.1 sym))))
  
  (testing "Convert HTTP_2 to :http2"
    (let [sym (util/version->sym HttpClient$Version/HTTP_2)]
      (is (= :http2 sym))))
  
  (testing "Default to :http1.1 for nil version"
    (let [sym (util/version->sym nil)]
      (is (= :http1.1 sym)))))

(deftest test-string->input-stream
  (testing "Convert string to input stream"
    (let [input-stream (util/string->input-stream "Hello, world!")]
      (is (instance? InputStream input-stream))
      (is (= "Hello, world!" (slurp input-stream :encoding "UTF-8"))))))

(deftest test-input-stream->string
  (testing "Convert input stream to string"
    (is (= "Hello, world!" 
           (util/input-stream->string 
            (ByteArrayInputStream. 
             (.getBytes "Hello, world!" StandardCharsets/UTF_8)))))))

(deftest test-body-handler->function
  (testing "Convert handler function to Java Function"
    (let [handler-fn (fn [x] (str "Handled: " x))
          java-fn (util/body-handler->function handler-fn)
          result (.apply java-fn "test")]
      (is (= "Handled: test" result)))))

(deftest test-get-body-handler
  (testing "Get string body handler"
    (let [handler (util/get-body-handler :string)]
      (is (= (HttpResponse$BodyHandlers/ofString) handler))))
  
  (testing "Get bytes body handler"
    (let [handler (util/get-body-handler :bytes)]
      (is (= (HttpResponse$BodyHandlers/ofByteArray) handler))))
  
  (testing "Get stream body handler"
    (let [handler (util/get-body-handler :stream)]
      (is (= (HttpResponse$BodyHandlers/ofInputStream) handler))))
  
  (testing "Get discard body handler"
    (let [handler (util/get-body-handler :discard)]
      (is (= (HttpResponse$BodyHandlers/discarding) handler))))
  
  (testing "Default to string body handler for unknown type"
    (let [handler (util/get-body-handler :unknown)]
      (is (= (HttpResponse$BodyHandlers/ofString) handler))))
  
  (testing "Default to string body handler for function"
    (let [handler (util/get-body-handler (fn [_] nil))]
      (is (= (HttpResponse$BodyHandlers/ofString) handler))))) 
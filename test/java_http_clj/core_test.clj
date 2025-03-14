(ns java-http-clj.core-test
  (:require [clojure.test :refer :all]
            [java-http-clj.core :as http]
            [java-http-clj.client :as client]
            [clojure.data.json :as json])
  (:import [java.net.http HttpClient HttpClient$Version]))

;; Tests for HTTP methods
(deftest test-http-methods
  (testing "GET request"
    (let [response (http/get "https://httpbin.org/get")]
      (is (= 200 (:status response)))
      (is (map? (json/read-str (:body response))))))
  
  (testing "POST request"
    (let [body "{\"key\":\"value\"}"
          response (http/post "https://httpbin.org/post" body)]
      (is (= 200 (:status response)))
      (let [parsed (json/read-str (:body response))]
        (is (= body (get parsed "data"))))))
  
  (testing "PUT request"
    (let [body "{\"key\":\"updated\"}"
          response (http/put "https://httpbin.org/put" body)]
      (is (= 200 (:status response)))
      (let [parsed (json/read-str (:body response))]
        (is (= body (get parsed "data"))))))
  
  (testing "DELETE request"
    (let [response (http/delete "https://httpbin.org/delete")]
      (is (= 200 (:status response)))))
  
  (testing "HEAD request"
    (let [response (http/head "https://httpbin.org/get")]
      (is (= 200 (:status response)))
      (is (= "" (:body response)))))
  
  (testing "OPTIONS request"
    (let [response (http/options "https://httpbin.org/get")]
      (is (= 200 (:status response)))))
  
  (testing "PATCH request"
    (let [body "{\"key\":\"patched\"}"
          response (http/patch "https://httpbin.org/patch" body)]
      (is (= 200 (:status response)))
      (let [parsed (json/read-str (:body response))]
        (is (= body (get parsed "data")))))))

;; Tests for error handling
(deftest test-error-handling
  (testing "404 Not Found"
    (let [response (http/get "https://httpbin.org/status/404")]
      (is (= 404 (:status response)))))
  
  (testing "500 Server Error"
    (let [response (http/get "https://httpbin.org/status/500")]
      (is (= 500 (:status response))))))

;; Tests for request options
(deftest test-request-options
  (testing "Custom headers"
    (let [response (http/get "https://httpbin.org/headers" 
                             {:headers {"X-Custom-Header" "test-value"}})]
      (is (= 200 (:status response)))
      (let [parsed (json/read-str (:body response))
            headers (get parsed "headers")]
        (is (= "test-value" (get headers "X-Custom-Header"))))))
  
  (testing "Basic authentication"
    (let [response (http/request {:method :get
                                  :url "https://httpbin.org/basic-auth/user/pass"
                                  :basic-auth ["user" "pass"]})]
      (is (= 200 (:status response)))
      (let [parsed (json/read-str (:body response))]
        (is (true? (get parsed "authenticated")))))))

;; Tests for client configuration
(deftest test-client-configuration
  (testing "Client builder with options"
    (let [client-builder (client/client-builder {:version :http2
                                                 :follow-redirects :never
                                                 :connect-timeout 5000})]
      (is (instance? java.net.http.HttpClient$Builder client-builder))))
  
  (testing "Build client with options"
    (let [client (client/build-client {:version :http2
                                       :follow-redirects :never
                                       :connect-timeout 5000})]
      (is (instance? java.net.http.HttpClient client))))
  
  (testing "Client with timeout"
    (let [client (client/client-with-timeout 5000)]
      (is (instance? java.net.http.HttpClient client))))
  
  (testing "Client with version"
    (let [client (client/client-with-version :http2)]
      (is (instance? java.net.http.HttpClient client))))
  
  (testing "Client with redirect policy"
    (let [client (client/client-with-redirect-policy :always)]
      (is (instance? java.net.http.HttpClient client)))))

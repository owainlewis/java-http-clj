(ns java-http-clj.examples
  "Example usage of the java-http-clj library."
  (:require [java-http-clj.core :as http]
            [clojure.pprint :as pp]))

;; Basic GET request
(defn request-example
  "Example of a simple GET request."
  []
  (let [response (http/request {:method :get 
                                :url "https://postman-echo.com/get?foo1=bar1&foo2=bar2"})]
    (println "Status:" (:status response))
    (println "Headers:")
    (pp/pprint (:headers response))
    (println "Body:")
    (pp/pprint (:body response))
    response))

;; Asynchronous request
(defn async-example
  "Example of an asynchronous GET request."
  []
  (http/async-request
   {:method :get
    :url "http://ip.jsontest.com/"}
   {}
   (fn [response] 
     (println "Async response received:")
     (println "Status:" (:status response))
     (println "Body:" (:body response)))))

;; POST request with JSON body
(defn post-json-example
  "Example of a POST request with a JSON body."
  []
  (let [response (http/request 
                  {:method :post
                   :url "https://postman-echo.com/post"
                   :headers {"Content-Type" "application/json"}
                   :body "{\"name\": \"John\", \"age\": 30}"})]
    (println "POST response status:" (:status response))
    (pp/pprint (:body response))
    response))

;; Request with custom timeout
(defn timeout-example
  "Example of a request with a custom timeout."
  []
  (let [response (http/request 
                  {:method :get
                   :url "https://postman-echo.com/delay/2"}
                  {:timeout 5000})] ; 5 second timeout
    (println "Response with custom timeout:")
    (println "Status:" (:status response))
    response))

;; Request with basic authentication
(defn basic-auth-example
  "Example of a request with basic authentication."
  []
  (let [response (http/request 
                  {:method :get
                   :url "https://postman-echo.com/basic-auth"
                   :basic-auth ["postman" "password"]})]
    (println "Basic auth response:")
    (println "Status:" (:status response))
    (println "Body:" (:body response))
    response))

;; Download a file
(defn download-example
  "Example of downloading a file."
  [output-path]
  (let [response (http/request 
                  {:method :get
                   :url "https://httpbin.org/image/jpeg"
                   :as :bytes})]
    (when (= 200 (:status response))
      (with-open [out (java.io.FileOutputStream. output-path)]
        (.write out (:body response)))
      (println "File downloaded to" output-path))
    response))

;; Run all examples
(defn run-all-examples
  "Run all example functions."
  []
  (println "\n=== Running request-example ===")
  (request-example)
  
  (println "\n=== Running async-example ===")
  (async-example)
  
  (println "\n=== Running post-json-example ===")
  (post-json-example)
  
  (println "\n=== Running timeout-example ===")
  (timeout-example)
  
  (println "\n=== Running basic-auth-example ===")
  (basic-auth-example)
  
  (println "\n=== Running download-example ===")
  (download-example "downloaded-image.jpg")
  
  (println "\nAll examples completed."))

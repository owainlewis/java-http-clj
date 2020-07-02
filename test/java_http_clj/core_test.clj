(ns java-http-clj.core-test
  (:require [java-http-clj.core :as http]
            [clojure.data.json :as json]
            [clojure.test :refer [deftest is]]))

(defmethod clojure.test/report :begin-test-var [m]
  (println "- Running test" (-> m :var meta :name)))

(deftest get-request []
  (is (= 200 (-> (http/get "https://postman-echo.com/get") :status))))

(deftest request-time-meta []
  (let [response (http/get "https://postman-echo.com/get")]
    (is (contains? (meta response) :request-time-ms))))

(deftest get-requests-with-query-params
  (let [response (http/get "https://postman-echo.com/get" {:query-parameters {:foo "bar"}})]
    (is (= {:foo "bar"}
           (-> response :body (json/read-str :key-fn keyword) :args)))))

(deftest post-request
  (let [body "Hello, World!"
        response (http/post "https://postman-echo.com/post" {:headers {"Content-Type" "application/json"}
                                                             :body body})]
    (is (= body
           (-> response :body (json/read-str :key-fn keyword) :data)))))

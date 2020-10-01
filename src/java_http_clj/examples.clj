(ns java-http-clj.examples
  (:require [java-http-clj.core :as http]))

(defn request-example []
  (http/request {:method :get :url "https://postman-echo.com/get?foo1=bar1&foo2=bar2"}))

(defn async-example []
  (http/async-request
   {:method :get
    :url "http://ip.jsontest.com/"}
   {}
   (fn [r] (println (:status r)))))

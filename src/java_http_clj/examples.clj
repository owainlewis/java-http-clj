(ns java-http-clj.examples
  (:require [java-http-clj.core :as http]))

(defn request-example []
  (http/request :get "https://postman-echo.com/get?foo1=bar1&foo2=bar2"))

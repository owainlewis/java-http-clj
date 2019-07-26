(ns java-http-clj.core-test
  (:refer-clojure :exclude [get])
  (:require [clojure.test :refer :all]
            [java-http-clj.core :refer :all])
  (:import [java.net.http
            HttpClient$Redirect
            HttpClient$Version
            HttpHeaders
            HttpRequest$BodyPublisher
            HttpRequest$BodyPublishers
            HttpResponse]
           [java.time Duration])
  (:require [clojure.test :refer :all]
            [java-http-clj.core :refer :all]))

(deftest build-client-test
  (let [opts {:connect-timeout 2000
              :follow-redirects :always}
        client (build-client opts)]
    (is (= (Duration/ofMillis 2000) (-> client .connectTimeout .get)))
    (is (= HttpClient$Redirect/ALWAYS (-> client .followRedirects)))))

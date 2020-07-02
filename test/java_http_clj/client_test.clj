(ns java-http-clj.client-test
  (:refer-clojure :exclude [get])
  (:require [clojure.test :refer [deftest is]]
            [java-http-clj.client :as client])
  (:import [java.net.http
            HttpClient$Redirect
            HttpClient$Version]
           [java.time Duration]))

(defmethod clojure.test/report :begin-test-var [m]
  (println "- Running test" (-> m :var meta :name)))

(deftest build-client-test
  (let [opts {:connect-timeout 2000
              :follow-redirects :always}
        c (client/build-client opts)]
    (is (= (Duration/ofMillis 2000) (-> c .connectTimeout .get)))
    (is (= HttpClient$Redirect/ALWAYS (-> c .followRedirects)))))

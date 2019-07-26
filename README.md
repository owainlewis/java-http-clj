# java-http-clj

A Clojure HTTP client based on the Java 11 native HTTP client. This is a drop in replacement for clojure-http and http-kit.

## Getting started

[com.owainlewis/java-http-clj "0.1.0"]

[![CircleCI](https://circleci.com/gh/owainlewis/java-http-clj.svg?style=svg)](https://circleci.com/gh/owainlewis/java-http-clj)

## Intro

The [HTTP Client](https://openjdk.java.net/groups/net/httpclient/intro.html) was added in Java 11. It can be used to request HTTP resources over the network. It supports HTTP/1.1 and HTTP/2, both synchronous and asynchronous programming models, handles request and response bodies as reactive-streams, and follows the familiar builder pattern.

This library exposes this Java client via Clojure and supports both sync and async requests. It can be used as a light weight, drop-in replacement for http-kit and clojure-http.

**This library requires Java 11+**

### Making Requests

### GET

```clojure
;; If you don't specify any options defaults are used
(http/get "http://www.google.com")

;; With request options
(http/get "https://www.google.com"
  {:headers {"Accept" "application/json" "Accept-Encoding" ["gzip" "deflate"]}
   :timeout 2000})
```

### Query Parameters

You can add query parameters to your request using the following form:

```clojure
(get "https://postman-echo.com/get" {:query-parameters {:foo "bar"}})
```

### Async Requests

There are async implementations of all core HTTP methods.
When using the Async client, you can provide an optional callback and an optional error handler.

```clojure
;; With callback
(async-get "https://github.com"
  (fn [r] (println (:status r))))

;; With callback and error handler
(async-get "https://github.com"
  (fn [r] (println (:status r)))
  (fn [e] (println e)))
```

## License

Copyright © 2019 Owain Lewis

Released under the MIT License: http://www.opensource.org/licenses/mit-license.php

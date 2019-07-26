# java-http-clj

A Clojure HTTP client based on the Java 11 native HTTP client. This is a drop in replacement for clojure-http and http-kit.

## Usage

This library supports both sync and async HTTP request.

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

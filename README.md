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

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.

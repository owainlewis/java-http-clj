# java-http-clj

[![Clojars Project](https://img.shields.io/clojars/v/com.owainlewis/java-http-clj.svg)](https://clojars.org/com.owainlewis/java-http-clj)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

A Clojure HTTP client library that wraps the Java 11+ HTTP client. This library provides a simple, idiomatic Clojure interface to the modern Java HTTP client while leveraging its powerful features.

## Features

- Simple, idiomatic Clojure API
- Synchronous and asynchronous requests
- Support for all HTTP methods (GET, POST, PUT, DELETE, etc.)
- Customizable client configuration (timeouts, redirects, etc.)
- Support for different response body types (string, bytes, stream)
- Basic authentication support
- Built on the modern Java 11+ HTTP client for performance and reliability

## Requirements

- Java 11 or higher
- Clojure 1.10 or higher

## Installation

Add the following dependency to your project.clj:

```clojure
[com.owainlewis/java-http-clj "0.3.1-SNAPSHOT"]
```

Or with deps.edn:

```clojure
{:deps {com.owainlewis/java-http-clj {:mvn/version "0.3.1-SNAPSHOT"}}}
```

## Quick Start

```clojure
(require '[java-http-clj.core :as http])

;; Simple GET request
(http/get "https://api.example.com/users")

;; POST request with JSON body
(http/post "https://api.example.com/users" 
           "{\"name\": \"John\", \"email\": \"john@example.com\"}" 
           {:headers {"Content-Type" "application/json"}})

;; Asynchronous request with callback
(http/async-request 
  {:method :get :url "https://api.example.com/users"} 
  {} 
  (fn [response] (println "Got response:" (:status response))))
```

## Usage Examples

### Basic Requests

```clojure
(require '[java-http-clj.core :as http])

;; GET request
(http/get "https://api.example.com/users")

;; POST request with body
(http/post "https://api.example.com/users" "Hello, world!")

;; PUT request
(http/put "https://api.example.com/users/123" "{\"name\": \"Updated Name\"}")

;; DELETE request
(http/delete "https://api.example.com/users/123")

;; HEAD request
(http/head "https://api.example.com/users")

;; OPTIONS request
(http/options "https://api.example.com/users")

;; PATCH request
(http/patch "https://api.example.com/users/123" "{\"name\": \"Patched Name\"}")
```

### Request with Headers

```clojure
(http/get "https://api.example.com/users" 
          {:headers {"Authorization" "Bearer token123"
                     "Accept" "application/json"}})
```

### Basic Authentication

```clojure
(http/get "https://api.example.com/protected" 
          {:basic-auth ["username" "password"]})

;; Or with the request map
(http/request {:method :get
               :url "https://api.example.com/protected"
               :basic-auth ["username" "password"]})
```

### Custom Timeout

```clojure
;; Set request timeout (in milliseconds)
(http/get "https://api.example.com/slow-endpoint" 
          {:timeout 5000})
```

### Response Body Handling

```clojure
;; Get response as string (default)
(http/get "https://api.example.com/users" 
          {:as :string})

;; Get response as byte array
(http/get "https://api.example.com/image.jpg" 
          {:as :bytes})

;; Get response as input stream
(http/get "https://api.example.com/large-file" 
          {:as :stream})

;; Discard response body
(http/head "https://api.example.com/users" 
           {:as :discard})
```

### Asynchronous Requests

```clojure
;; With callback
(http/async-request 
  {:method :get :url "https://api.example.com/users"} 
  {} 
  (fn [response] 
    (println "Status:" (:status response))
    (println "Body:" (:body response))))

;; Without callback (returns CompletableFuture)
(def future-response 
  (http/async-request 
    {:method :get :url "https://api.example.com/users"}))

;; Later, get the result
(def response (.get future-response))
```

### Custom Client Configuration

```clojure
(require '[java-http-clj.client :as client])

;; Create a custom client
(def custom-client 
  (client/build-client 
    {:connect-timeout 5000
     :follow-redirects :always
     :version :http2}))

;; Use the custom client
(http/get "https://api.example.com/users" 
          {:client custom-client})

;; Convenience functions for common configurations
(def timeout-client (client/client-with-timeout 5000))
(def http2-client (client/client-with-version :http2))
(def redirect-client (client/client-with-redirect-policy :always))
```

## API Documentation

### Core Functions

#### `request`

```clojure
(request req)
(request req opts)
```

Makes an HTTP request and returns the response.

Request map options:
- `:method` - HTTP method (`:get`, `:post`, `:put`, `:delete`, etc.)
- `:url` - URL string
- `:headers` - Map of headers
- `:body` - Request body (String, byte[], InputStream, or Path)
- `:basic-auth` - Vector of [username password]
- `:timeout` - Request timeout in milliseconds

Client options:
- `:client` - HttpClient instance to use
- `:timeout` - Connection timeout in milliseconds
- `:follow-redirects` - Redirect policy (`:always`, `:never`, `:normal`)
- `:version` - HTTP version (`:http1.1`, `:http2`)
- `:as` - Response body type (`:string`, `:bytes`, `:stream`, `:discard`)

#### `async-request`

```clojure
(async-request req)
(async-request req opts)
(async-request req opts callback)
```

Makes an asynchronous HTTP request. Returns a CompletableFuture that will complete with the response. If a callback function is provided, it will be called with the response.

#### HTTP Method Helpers

```clojure
(get url)
(get url opts)

(post url body)
(post url body opts)

(put url body)
(put url body opts)

(delete url)
(delete url opts)

(head url)
(head url opts)

(options url)
(options url opts)

(patch url body)
(patch url body opts)
```

### Client Functions

#### `client-builder`

```clojure
(client-builder)
(client-builder opts)
```

Creates a new HttpClient builder with optional configuration.

Options:
- `:connect-timeout` - Connection timeout in milliseconds or Duration
- `:cookie-handler` - CookieHandler instance
- `:executor` - Executor for async requests
- `:follow-redirects` - Redirect policy (`:always`, `:never`, `:normal`)
- `:priority` - Request priority
- `:proxy` - ProxySelector instance
- `:ssl-context` - SSLContext instance
- `:ssl-parameters` - SSLParameters instance
- `:version` - HTTP version (`:http1.1`, `:http2`)

#### `build-client`

```clojure
(build-client)
(build-client opts)
```

Builds and returns a configured HttpClient instance.

#### Convenience Client Builders

```clojure
(client-with-timeout timeout-ms)
(client-with-version version)
(client-with-redirect-policy redirect-policy)
```

## Response Format

All HTTP responses are returned as Clojure maps with the following keys:

- `:status` - HTTP status code (e.g., 200, 404, 500)
- `:headers` - Map of response headers
- `:body` - Response body (format depends on the `:as` option)
- `:version` - HTTP version used (`:http1.1` or `:http2`)
- `:uri` - Request URI

## Development

### Running Tests

```bash
lein test
```

### Building

```bash
lein jar
```

## License

Copyright © 2023 Owain Lewis

Distributed under the MIT License.

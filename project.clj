(defproject com.owainlewis/java-http-clj "0.1.0"
  :description "Clojure HTTP client based on the Java 11+ HTTP client"
  :url "http://github.com/owainlewis/http-java-clj"
  :license {:name "MIT"
            :url "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [cheshire "5.8.1"]]
  :profiles {:dev {:dependencies [[org.clojure/data.json "1.0.0"]]}}
  :source-paths ["src" "examples" "test"]
  :plugins [[lein-cljfmt "0.6.8"]]
  :repl-options {:init-ns java-http-clj.core})

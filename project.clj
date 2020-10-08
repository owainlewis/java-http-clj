(defproject com.owainlewis/java-http-clj "0.3.1-SNAPSHOT"
  :description "Clojure HTTP client based on the Java 11+ HTTP client"
  :url "http://github.com/owainlewis/java-http-clj"
  :license {:name "MIT"
            :url "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.10.0"]]
  :profiles {:dev {:dependencies [[org.clojure/data.json "1.0.0"]]}}
  :source-paths ["src" "examples" "test"]
  :plugins [[lein-cljfmt "0.6.8"]]
  :deploy-repositories
  [["clojars"
    {:url "https://repo.clojars.org"
     :username :env/clojars_username
     :password :env/clojars_password
     :sign-releases false}]]
  :repl-options {:init-ns java-http-clj.core})

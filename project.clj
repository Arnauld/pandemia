(defproject pandemia "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.0"]
                 [org.clojure/tools.reader "0.7.4"]
                 [org.clojure/tools.logging "0.2.6"]

                 [ch.qos.logback/logback-classic "1.0.13"]
                  
                  ; https://github.com/ptaoussanis/carmine
                  ; Clojure Redis client & message queue 
                 [com.taoensso/carmine "1.7.0"] 
                  
                  ; https://github.com/clojure/data.json
                  ; JSON parser/generator to/from Clojure data structures.
                  ; [org.clojure/data.json "0.2.2"]
                  
                  ; https://github.com/dakrone/cheshire
                  ; clojure-json had really nice features (custom encoders), 
                  ; but was slow; clj-json had no features, but was fast. 
                  ; Cheshire encodes JSON fast, with added support for more 
                  ; types and the ability to use custom encoders.
                 [cheshire "5.1.2"]

                  ; http://http-kit.org/
                  ; HTTP client/server for Clojure
                 [http-kit "2.1.2"]

                  ; https://github.com/weavejester/compojure
                  ; A concise routing DSL for Ring/Clojure 
                 [compojure "1.1.5"]

                  ; https://github.com/fhd/clostache
                  ; {{ mustache }} for Clojure
                 [de.ubercode.clostache/clostache "1.3.1"]
                ]
  :plugins [[lein-cucumber "1.0.2"]]
  :cucumber-feature-paths ["features/"]
  :source-paths ["src/"]
  :test-paths ["test/"]
  :resource-paths ["resources/"] ; non-code files included in classpath/jar
  )

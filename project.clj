(defproject pandemia "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.0"]]
  :plugins [[lein-cucumber "1.0.2"]]
  :cucumber-feature-paths ["src/features/"]
  :source-paths ["src/clojure"]
  :test-paths ["src/test"]
  :resource-paths ["src/resources"] ; non-code files included in classpath/jar
  )

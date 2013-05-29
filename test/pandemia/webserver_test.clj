(ns pandemia.webserver-test
    (:use clojure.test
          pandemia.webserver))

;; --- DUMMY

(deftest a-test
  (testing "FIXME, If I fail."
    (is (= 1 1))))

;; --- DUMMY

(deftest run-server-test
  (testing "Run server"
      (start-server {:port 8000})
      (println "Server started?")
      (is (= 1 1))))
(ns pandemia.event-store-test
  (:use clojure.test
        pandemia.core
        pandemia.event-store))

(defrecord DummyEvent [id data])

;; --- DUMMY

(deftest a-test
  (testing "FIXME, If I fail."
    (is (= 1 1))))

;; --- 

(defn- append-dummy [store aggregate-id data] 
	(let [event-stream (retrieve-event-stream store aggregate-id)
          old-events (flatten (:transactions event-stream))
          new-events [(->DummyEvent aggregate-id data)]]
    ;(println new-events)
    (append-events store aggregate-id event-stream new-events))
    (load-events aggregate-id store))

(deftest duplicate-are-possible-through-different-memory-store
  (testing "Make sure different memory store are isolated"
  	(let [store1 (new-in-memory-event-store)
  		  store2 (new-in-memory-event-store)
  		  e1 (append-dummy store1 1 {:what 1})
  		  e2 (append-dummy store2 1 {:what 1})]
  		(is (= 1 (count e1)))
  		(is (= 1 (count e2))))))


(ns pandemia.web-test
  (:use clojure.test)
  (:require [pandemia.web :as web]))


(defn mark [req res dir id]
	[req (assoc res (str "" dir "_" id) true)])

(defn h1 [req res dir] (mark req res dir 1))
(defn h2 [req res dir] (mark req res dir 2))
(defn h3 [req res dir] (mark req res dir 3))
(defn h4 [req res dir] (mark req (web/ok res "ok!") dir 4))
(defn herr [req res dir] (mark req (web/err res "yeahh!" 500) dir :err))

(deftest ok-chain
	(testing "One handler (terminal handler is only caller downstream)"
		(let [c (web/handler-chain [h1])
			  [req res] (c {:method "GET"})]
			  (is (contains? res ":down_1"))
			  (is (not (contains? res ":up_1")))
			))
	(testing "Chain of 4 handlers"
		(let [c (web/handler-chain [h1 h2 h3 h4])
			  [req res] (c {:method "GET"})]
			  (is (contains? res ":down_1"))
			  (is (contains? res ":down_2"))
			  (is (contains? res ":down_3"))
			  (is (contains? res ":down_4"))
			  (is (not (contains? res ":up_4")))
			  (is (contains? res ":up_3"))
			  (is (contains? res ":up_2"))
			  (is (contains? res ":up_1"))
			)))

(deftest chain-with-err
	(testing "h1 -> h2 -> herr [-x> h4]"
		(let [c (web/handler-chain [h1 h2 herr h4])
			  [req res] (c {:method "GET"})]
			  (is (contains? res ":down_1"))
			  (is (contains? res ":down_2"))
			  (is (contains? res ":down_:err"))
			  (is (not (contains? res ":down_4")))
			  (is (not (contains? res ":up_4")))
			  (is (not (contains? res ":up_:err")))
			  (is (contains? res ":up_2"))
			  (is (contains? res ":up_1"))
			)))
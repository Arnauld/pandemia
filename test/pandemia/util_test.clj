(ns pandemia.util-test
  (:use clojure.test
        pandemia.util))


;; --- 
(def values {
  :mail "james.bond@mi6.gk"
  :first_name "James"
  :last_name "Bind"
  })

;;
;; --- (delta values changeset)
;;

(deftest test-delta
  (testing "delta with empty changeset"
    (is (= 
        {} 
        (delta values {}))))

  (testing "delta with empty values"
    (is (= 
        values 
        (delta {} values))))

  (testing "delta with same values"
    (is (= 
        {} 
        (delta values values))))

  (testing "delta with one change"
    (is (= 
        {:last_name "Bond"} 
        (delta values {:last_name "Bond"}))))

  (testing "delta with one change and one new value"
    (is (= 
        {:last_name "Bond"
         :car "Aston Martin"
        } 
        (delta values {:last_name "Bond"
                 :first_name "James"
                 :car "Aston Martin"
                })))))
;;
;; --- (split nb coll)
;;

(deftest test-split
  (testing "split with empty coll"
    (is (= 
        [[] [] []]
        (split 3 #{}))))

  (testing "split with not enough elements"
    (let [orig #{:a :b}
          parts (split 3 orig)]
      (is (= 1 (count (nth parts 0))))
      (is (= 1 (count (nth parts 1))))
      (is (= 0 (count (nth parts 2))))
      (is (= orig (set (flatten parts))))))

  (testing "split just enough elements"
    (let [orig #{:a :b :c :d :e :f}
          parts (split 3 orig)]
      (is (= 2 (count (nth parts 0))))
      (is (= 2 (count (nth parts 1))))
      (is (= 2 (count (nth parts 2))))
      (is (= orig (set (flatten parts))))))

  (testing "split not enough elements for equal size"
    (let [orig #{:a :b :c :d :e :f :g}
          parts (split 3 orig)]
      (is (= 3 (count (nth parts 0))))
      (is (= 2 (count (nth parts 1))))
      (is (= 2 (count (nth parts 2))))
      (is (= orig (set (flatten parts)))))))

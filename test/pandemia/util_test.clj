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

(deftest delta-empty-changeset
  (testing "delta with empty changeset"
    (is (= 
    		{} 
    		(delta values {})))))

(deftest delta-empty-values
  (testing "delta with empty values"
    (is (= 
    		values 
    		(delta {} values)))))

(deftest delta-same-values
  (testing "delta with same values"
    (is (= 
    		{} 
    		(delta values values)))))

(deftest delta-one-change
  (testing "delta with one change"
    (is (= 
    		{:last_name "Bond"} 
    		(delta values {:last_name "Bond"})))))

(deftest delta-one-change-one-new-value
  (testing "delta with one change and one new value"
    (is (= 
    		{:last_name "Bond"
    		 :car "Aston Martin"
    		} 
    		(delta values {:last_name "Bond"
    					   :first_name "James"
    					   :car "Aston Martin"
    						})))))

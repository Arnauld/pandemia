(ns pandemia.city-test
  (:use clojure.test
        pandemia.city)
  (:require [pandemia.graph :as graph]))


(deftest color-of-test
  (testing "color-of some cities"
    (is (= (color-of :Paris) :blue))
    (is (= (color-of :London) :blue))
    (is (= (color-of :Khartoum) :yellow))
    (is (= (color-of :Manila) :orange))
    (is (= (color-of :HongKong) :orange))
    (is (= (color-of :Istanbul) :black))))

(deftest city-graph-test
  (testing "city-graph basic behavior"
    (let [g city-graph]
        (is (contains? (graph/nodes g :Paris) :Milan))
        (is (contains? (graph/nodes g :Paris) :Madrid))
        (is (contains? (graph/nodes g :Paris) :Algiers))
        (is (contains? (graph/nodes g :Paris) :London))
        (is (contains? (graph/nodes g :Paris) :Essen))
        ;;
        (is (contains? (graph/nodes g :Milan) :Paris))
        (is (contains? (graph/nodes g :Madrid) :Paris))
        (is (contains? (graph/nodes g :Algiers) :Paris))
        (is (contains? (graph/nodes g :London) :Paris))
        (is (contains? (graph/nodes g :Essen) :Paris)))))
    
    
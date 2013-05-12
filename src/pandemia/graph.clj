(ns pandemia.graph
    (:require [clojure.set :as set]))

;;
;; Initially inspired from
;; https://groups.google.com/d/msg/clojure/h1m6Qjuh3wA/pRqNY5HlYJEJ
;; 
;; Remove orientated behavior: prev/next set of nodes by node,
;; in favor of set of linked nodes.


(def the-graph {}) 

(defn add-node [g n] 
  (if (g n) 
    g 
    (assoc g n {:links #{}}))) 

(defn add-edge [g n1 n2] 
  (-> g 
      (add-node n1) 
      (add-node n2) 
      (update-in [n1 :links] conj n2) 
      (update-in [n2 :links] conj n1))) 

(defn remove-edge [g n1 n2] 
  (-> g 
      (add-node n1) 
      (add-node n2) 
      (update-in [n1 :links] disj n2) 
      (update-in [n2 :links] disj n1))) 

; (defn remove-node [g n] 
;   (if-let [{:keys [next prev]} (g n)] 
;     ((comp 
;       #(dissoc % n) 
;       #(reduce (fn [g* n*] (remove-edge g* n* n)) % prev) 
;       #(reduce (fn [g* n*] (remove-edge g* n n*)) % next)) 
;      g) 
;     g)) 

(defn contains-node? [g n] 
  (g n)) 

(defn contains-edge? [g n1 n2] 
  (get-in g [n1 :links n2])) 

(defn nodes [g n] 
  (get-in g [n :links]))


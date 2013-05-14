(ns pandemia.util)

(defn system-id [obj]
  (Integer/toHexString (System/identityHashCode obj)))

(defn delta [actual changeset]
    (reduce 
        (fn [res [k v]] 
            ;(println (str "1: " res ", 2: " k ", " v))
            (if 
                (or 
                    (not  (k actual)) ;; not already there
                    (not= (k actual) v)) ;; or different
                (assoc res k v) 
                res))
        {} changeset))

(defn split [nb-parts coll]
    (let [sz (count coll)
          pile-sz  (int (/ sz nb-parts))
          extra-n  (mod sz nb-parts)
          splitted (reduce (fn [pred i]
            (let [available (:cards pred)
                  extra (:extra pred)
                  delta (if (< 0 extra) 1 0)
                  amount (+ pile-sz delta)
                  selected (take amount available)
                  remaining (drop amount available)]
                {:extra (- extra delta)
                 :parts (conj (:parts pred) selected)
                 :cards remaining})) 
            {:extra extra-n
             :parts [] 
             :cards coll} (range nb-parts))]
        (:parts splitted)))

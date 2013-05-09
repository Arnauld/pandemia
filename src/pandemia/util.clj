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
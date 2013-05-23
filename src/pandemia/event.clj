(ns pandemia.event)

(defn camel-to-dash
  [s]
  (let [b (StringBuilder.)
          f (fn [pred c]
              (if (and (Character/isUpperCase c)
                       (< 0 (.length pred)))
                  (.append pred \-))
              (.append pred (Character/toLowerCase c)))]
          (.toString (reduce f b s))))

;;
;; http://david-mcneil.com/post/765563763/enhanced-clojure-records
;;

(defn create-event [event-name fields values]
    (let [initial {:event-name event-name}
          kv (map (fn [k v] [k v]) fields values)]
        (println "create-event" kv)
        (reduce (fn [pred [k v]] (assoc pred k v)) initial kv)))

(defn map->create-event [event-name fields value-map]
    (let [initial {:event-name event-name}]
        (reduce (fn [pred k] (assoc pred k (k value-map))) initial fields)))


(defmacro defevent0
    "Defines a new type of event and set up constructor functions"
    [event-name fields]
        (let [fields-kw (map (fn [k] (keyword k)) fields)
              event-kw  (keyword event-name)]
         (println ">>" fields-kw)
        `(defn ~event-name [& value-map#]
                (println "kw:" ~@fields-kw)
                (println "He:" value-map#)
                (create-event ~event-kw (list ~@fields-kw) value-map#))))

(defmacro defevent
    "Defines a new type of event and set up constructor functions"
    [event-name fields]
        (let [fields-kw (map (fn [k] (keyword k)) fields)
              params    (map (fn [k] (symbol (str k "#"))) fields)
              event-kw  (keyword event-name)
              event-name-map (symbol (str 'map-> event-name))]
         (println ">> kw " fields-kw)
         (println ">> kw " params)
        `(do
	        (defn ~event-name [~@params]
	                (println "kw:" ~@fields-kw)
	                (println "He:" ~@params)
	                (create-event ~event-kw (list ~@fields-kw) (list ~@params)))
	        (defn ~event-name-map [value-map#]
	                (println "map" value-map#)
	                (map->create-event ~event-kw (list ~@fields-kw) value-map#)))))


(defevent E [x y])
(E 1 2 3)
(E 1 2)
(map->E {:x 1 :y 5})

;        `(defn ~ctor-name [value-map#]
;            (-> {:event-type ~(symbol ~ctor-name)}
;                (set-event-fields ~fields value-map#))
;            )
;        )

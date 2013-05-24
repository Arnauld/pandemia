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

(defn create-event [event-type fields values]
    (let [initial {:event-type event-type}
          kv (map (fn [k v] [k v]) fields values)]
        (reduce (fn [pred [k v]] (assoc pred k v)) initial kv)))

(defn map->create-event [event-type fields value-map]
    (let [initial {:event-type event-type}]
        (reduce (fn [pred k] (assoc pred k (k value-map))) initial fields)))

(defn full-type [event] 
	(:event-type event))

(defn simple-type [event] 
	(let [full (str (full-type event))
		  pos  (.lastIndexOf full ".")
		  sub  (if (>= pos 0)
				  	(.substring full (+ 1 pos))
				  	full)]
		  (keyword sub)))


(defmacro defevent
    "Defines a new type of event and set up constructor functions.
     Body is unused at this point.
     Generate two constructor functions, eg. 
         
        (defevent MailSent [from to text])

     will gives
         
        (defn    ->MailSent [from to text] ...)
        (defn map->MailSent [values-as-map] ...)
     "
    [event-type fields & body]
        (let [fields-kw (map (fn [k] (keyword k)) fields)
              params    (map (fn [k] (symbol (str k "#"))) fields)
              event-kw  (keyword (str *ns* '. event-type))
              ctor-name (symbol (str '-> event-type))
              ctor-name-map (symbol (str 'map-> event-type))]
        `(do
            (defn ~ctor-name [~@params]
                    (create-event ~event-kw (list ~@fields-kw) (list ~@params)))
            (defn ~ctor-name-map [value-map#]
                    (map->create-event ~event-kw (list ~@fields-kw) value-map#)))))
        

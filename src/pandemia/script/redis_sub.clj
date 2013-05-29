(ns pandemia.script.redis-sub
  (:require [taoensso.carmine :as redis]
            [clojure.tools.reader.edn :as edn]))

(defn to-edn [obj] (pr-str obj))
(defn from-edn [str] (edn/read-string str))

(defn -main [& args]
	(let [channel "events"
          pool (redis/make-conn-pool)
          spec (redis/make-conn-spec)]
		(redis/with-new-pubsub-listener 
  			spec 
  			{channel (fn f1 [msg] (println "<<< received [" channel "] " msg))}
  			(redis/subscribe channel))))
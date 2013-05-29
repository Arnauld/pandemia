(ns pandemia.redis-test
  (:use clojure.test)
  (:require [pandemia.util :as util]
            [pandemia.core :as core]
            [pandemia.event :as event]
            [taoensso.carmine :as redis]
            [clojure.tools.reader.edn :as edn]))

(defn to-edn [obj] (pr-str obj))
(defn from-edn [str] (edn/read-string str))


(def pool         (redis/make-conn-pool)) ; See docstring for additional options
(def spec-server1 (redis/make-conn-spec)) ; ''

(defmacro wredis [& body] `(redis/with-conn pool spec-server1 ~@body))

(defrecord RedisEventBus [pool spec])
(extend-protocol core/EventBus
	RedisEventBus
	
	(publish [this event]
		(let [channel "events"
			  spec (:spec this)
			  pool (:pool this)
			  edn (to-edn event)]
			(redis/with-conn pool spec
				(println ">>> publishing [" channel "] " edn)
				(redis/publish channel edn))))

  	(subscribe [this listener]
  		(let [channel "events"
  			  spec (:spec this)
  			  handlr (redis/with-new-pubsub-listener 
			  			spec {channel (fn f1 [msg] 
			  				(println "<<< received [" channel "] " msg)
			  				(apply listener (from-edn msg)))}
			  			(redis/subscribe channel))]
  			  handlr))

  	(unsubscribe [this listener-ref]
  		(let [channel "events"]
  				(redis/close-listener listener-ref))))


(deftest event->edn->redis
  (testing "FIXME, If I fail."
      (println "ppol: " pool)
      (println "spec-server1: " spec-server1)
      (let [uuid (util/uuid)
            event {:event-type :pandemia.game.CityInfectedEvent
                   :aggregate-id "game-1"
                   :color :blue
                   :nb-cubes 1
                   :city-id :Madrid}
            serialized (to-edn event)
            ; publish the event in edn format
            ; then read it back
            redis-res (wredis 
                          (redis/ping)
                          (redis/rpush uuid serialized)
                          (redis/lrange uuid 0 -1))
            event-list (nth redis-res 2)
            retrieved (first event-list)]
            ; make sure once back the event is the same...
        (is (= event (from-edn retrieved))))))

(deftest publish-event-redis-event-bus
	(testing "with-event-bus publish/subscribe event"
		(let [eb (RedisEventBus. pool spec-server1)
			  event {:event-type :pandemia.game.CityInfectedEvent
                   :aggregate-id "game-1"
                   :color :blue
                   :nb-cubes 1
                   :city-id :Madrid}
              listener (fn [evt] (println "received:" evt))
			  listener-ref (core/with-event-bus eb
								(core/subscribe-event listener))]
			(Thread/sleep 500)
			(println "!")
			(core/with-event-bus eb
				(core/publish-event event))
			(println "?")
			(Thread/sleep 2000)
			(core/with-event-bus eb
				(core/unsubscribe-event listener-ref))
			(println ".")
			)))

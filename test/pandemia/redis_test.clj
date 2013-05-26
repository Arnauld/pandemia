(ns pandemia.redis-test
  (:use clojure.test)
  (:require [pandemia.util :as util]
            [pandemia.event :as event]
            [taoensso.carmine :as redis]
            [clojure.tools.reader.edn :as edn]))

(defn to-edn [obj] (pr-str obj))
(defn from-edn [str] (edn/read-string str))


(def pool         (redis/make-conn-pool)) ; See docstring for additional options
(def spec-server1 (redis/make-conn-spec)) ; ''

(defmacro wredis [& body] `(redis/with-conn pool spec-server1 ~@body))

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

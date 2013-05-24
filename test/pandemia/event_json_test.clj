(ns pandemia.event-json-test
  (:use clojure.test)
  (:require [cheshire.core :as json]))

(defn to-json [event]
	(json/generate-string event))

(defn from-json [event-json]
	(json/parse-string event-json true)) ; 'true' to get keywords back

;; --- Discovery tests
(def event1 {:color :orange, 
			 :nb-cubes 1, 
			 :city-id :Shanghai, 
			 :aggregate-id "game-1", 
			 :event-type :pandemia.game.CityInfectedEvent})


(deftest cheshire-usages-test
  (testing "event1 -> json"
    (is (not= "1" (to-json event1))))

  (testing "json -> event1 (what about keywords?)"
  	(let [parsed (from-json (to-json event1))]
  		(is (not= event1 parsed)))))

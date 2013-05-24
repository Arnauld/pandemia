(ns pandemia.event-edn-test
  (:use clojure.test
      	pandemia.game)
  (:require [pandemia.event :as event]
  			[clojure.tools.reader.edn :as edn]))

(defn to-edn [obj] (pr-str obj))
(defn from-edn [str] (edn/read-string str))


(deftest event->edn->event
  (testing "FIXME, If I fail."
  	(let [event {:event-type :pandemia.game.CityInfectedEvent
  				 :aggregate-id "game-1"
  				 :color :blue
  				 :nb-cubes 1
  				 :city-id :Madrid}
  		  str (to-edn event)]
	    (is (instance? String str))
	    (is (= event (from-edn str))))))

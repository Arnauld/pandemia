(ns pandemia.city
	(:use pandemia.graph)
    (:require [clojure.set :as set]))

(def black-cities #{
    :Algiers
    :Baghdad
    :Cairo
    :Chennai
    :Delhi
    :Istanbul
    :Karachi
    :Kolkata
    :Moscow
    :Mumbai
    :Riyadh
    :Tehran
    })

(def blue-cities #{
    :Atlanta
    :Chicago
    :Essen
    :London
    :Madrid
    :Milan
    :NewYork
    :Paris
    :SanFrancisco
    :SaintPetersburg
    :Toronto
    :Washington
    })

(def orange-cities #{
    :Bangkok
    :Beijing
    :HoChiMinhCity
    :HongKong
    :Jakarta
    :Manila
    :Osaka
    :Seoul
    :Shanghai
    :Sydney
    :Taipei
    :Tokyo
    })

(def yellow-cities #{
    :Bogota
    :BuenosAires
    :Johannesburg
    :Khartoum
    :Kinshasa
    :Lagos
    :Lima
    :LosAngeles
    :MexicoCity
    :Miami
    :Santiago
    :SaoPaulo
    })

(def all-cities (set/union black-cities blue-cities orange-cities yellow-cities))

(defn color-of [city] 
	(cond (contains? black-cities city) :black
		  (contains? blue-cities  city) :blue
		  (contains? orange-cities city) :orange
		  (contains? yellow-cities city) :yellow
		  :else (throw (IllegalArgumentException. (str "Unknown city " city)))))

;;
;; basic graph support
;;


(def city-graph 
	(let [g the-graph]
	(-> g
		;; --- Blue links
		(add-edge :SanFrancisco :Tokyo)
        (add-edge :SanFrancisco :Manila)
        (add-edge :SanFrancisco :LosAngeles)
        (add-edge :SanFrancisco :Chicago)
        (add-edge :Chicago :SanFrancisco)
        (add-edge :Chicago :LosAngeles)
        (add-edge :Chicago :MexicoCity)
        (add-edge :Chicago :Atlanta)
        (add-edge :Chicago :Toronto)
        (add-edge :Atlanta :Chicago)
        (add-edge :Atlanta :Miami)
        (add-edge :Atlanta :Washington)
        (add-edge :Toronto :Chicago)
        (add-edge :Toronto :Washington)
        (add-edge :Toronto :NewYork)
        (add-edge :Washington :NewYork)
        (add-edge :Washington :Toronto)
        (add-edge :Washington :Atlanta)
        (add-edge :Washington :Miami)
        (add-edge :NewYork :Toronto)
        (add-edge :NewYork :Washington)
        (add-edge :NewYork :Madrid)
        (add-edge :NewYork :London)
        (add-edge :Madrid :NewYork)
        (add-edge :Madrid :SaoPaulo)
        (add-edge :Madrid :Algiers)
        (add-edge :Madrid :Paris)
        (add-edge :Madrid :London)
        (add-edge :London :NewYork)
        (add-edge :London :Madrid)
        (add-edge :London :Paris)
        (add-edge :London :Essen)
        (add-edge :Paris :London)
        (add-edge :Paris :Madrid)
        (add-edge :Paris :Algiers)
        (add-edge :Paris :Milan)
        (add-edge :Paris :Essen)
        (add-edge :Essen :London)
        (add-edge :Essen :Paris)
        (add-edge :Essen :Milan)
        (add-edge :Essen :SaintPetersburg)
        (add-edge :Milan :Essen)
        (add-edge :Milan :Paris)
        (add-edge :Milan :Istanbul)
        (add-edge :SaintPetersburg :Essen)
        (add-edge :SaintPetersburg :Istanbul)
        (add-edge :SaintPetersburg :Moscow)

        ;; Black links
        (add-edge :Algiers :Madrid)
        (add-edge :Algiers :Paris)
        (add-edge :Algiers :Istanbul)
        (add-edge :Algiers :Cairo)
        (add-edge :Istanbul :Algiers)
        (add-edge :Istanbul :Milan)
        (add-edge :Istanbul :SaintPetersburg)
        (add-edge :Istanbul :Moscow)
        (add-edge :Istanbul :Baghdad)
        (add-edge :Istanbul :Cairo)
        (add-edge :Moscow :SaintPetersburg)
        (add-edge :Moscow :Tehran)
        (add-edge :Moscow :Istanbul)
        (add-edge :Tehran :Moscow)
        (add-edge :Tehran :Baghdad)
        (add-edge :Tehran :Karachi)
        (add-edge :Tehran :Delhi)
        (add-edge :Delhi :Tehran)
        (add-edge :Delhi :Karachi)
        (add-edge :Delhi :Mumbai)
        (add-edge :Delhi :Chennai)
        (add-edge :Delhi :Kolkata)
        (add-edge :Kolkata :Delhi)
        (add-edge :Kolkata :HongKong)
        (add-edge :Kolkata :Bangkok)
        (add-edge :Kolkata :Chennai)
        (add-edge :Chennai :Kolkata)
        (add-edge :Chennai :Bangkok)
        (add-edge :Chennai :Jakarta)
        (add-edge :Chennai :Mumbai)
        (add-edge :Chennai :Delhi)
        (add-edge :Mumbai :Karachi)
        (add-edge :Mumbai :Delhi)
        (add-edge :Mumbai :Chennai)
        (add-edge :Karachi :Baghdad)
        (add-edge :Karachi :Tehran)
        (add-edge :Karachi :Delhi)
        (add-edge :Karachi :Mumbai)
        (add-edge :Karachi :Riyadh)
        (add-edge :Baghdad :Istanbul)
        (add-edge :Baghdad :Tehran)
        (add-edge :Baghdad :Karachi)
        (add-edge :Baghdad :Riyadh)
        (add-edge :Baghdad :Cairo)
        (add-edge :Cairo :Algiers)
        (add-edge :Cairo :Istanbul)
        (add-edge :Cairo :Baghdad)
        (add-edge :Cairo :Riyadh)
        (add-edge :Cairo :Khartoum)
        (add-edge :Riyadh :Cairo)
        (add-edge :Riyadh :Baghdad)
        (add-edge :Riyadh :Karachi)

	)))



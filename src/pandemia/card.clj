(ns pandemia.card
    (:use pandemia.util
    	  pandemia.city)
    (:require [clojure.set :as set]))

;;
;;
;;

(def infection-cards all-cities)

;;
;;
;;
(def epidemic {:type :epidemic})

(def city-player-cards (map (fn [city] {:type :city 
										:city city}) all-cities))

(def default-special-player-cards (map (fn [kind] {:type :special :kind kind}) 
	#{
		; Move a pawn (yours or another player's) to any city.
     	; You must have a player's permission to move their pawn.
    	:Airlift
    
	    ;Examine the top 6 cards of the Infection Draw pile,
	    ;rearrange them in the order oy your choice, then place
	    ;them back on the pile.
	    :Forecast

	    ;Add a Research Station to any city for free.
	    :GovernmentGrant,
	    
	    ;The next player to begin the Playing The Infector phase
	    ;of their turn may skip that phase entirely.
	    :OneQuietNight,
	    
	    ;Take a card from the Infection Discard Pile and remove
	    ;it from the game.
	    :ResilientPopulation}))
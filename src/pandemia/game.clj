(ns pandemia.game
    (:use pandemia.core
          pandemia.user
          pandemia.util)
    (:require [clojure.set :as set]
              [pandemia.card :as card]
              [pandemia.city :as city]))

;;
;; Commands
;;

(defrecord CreateGameCommand [game-id user-id difficulty ruleset])
(defrecord ChangeGameDifficultyCommand [game-id user-id difficulty])
(defrecord StartGameCommand [game-id])
(defrecord JoinGameCommand [game-id user-id])

;;
;; Events
;;

(defrecord GameCreatedEvent [aggregate-id creator-id difficulty ruleset])
(defrecord GameDifficultyChangedEvent [aggregate-id user-id difficulty])
(defrecord GameStartedEvent [aggregate-id])
(defrecord GameJoinedEvent  [aggregate-id user-id])
(defrecord GameUserRoleDefinedEvent [aggregate-id user-id role]
  Object
  (toString [this] (str "GameUserRoleDefinedEvent@" (system-id this) "{"
                        "aggregate-id: " aggregate-id 
                        ", user-id: " user-id 
                        ", role: " role "}")))
(defrecord PlayerCardAddedToHandEvent [aggregate-id user-id cards]
  Object
  (toString [this] (str "PlayerCardAddedToHandEvent@" (system-id this) "{"
                        "aggregate-id: " aggregate-id 
                        ", user-id: " user-id 
                        ", cards: " cards "}")))
(defrecord PlayerDrawPileInitializedEvent [aggregate-id cards]
  Object
  (toString [this] (str "PlayerDrawPileInitializedEvent@" (system-id this) "{"
                        "aggregate-id: " aggregate-id 
                        ", cards: " cards "}")))
(defrecord InfectionDrawPileInitializedEvent [aggregate-id cards discarded]
  Object
  (toString [this] (str "InfectionDrawPileInitializedEvent@" (system-id this) "{"
                        "aggregate-id: " aggregate-id 
                        ", cards: " cards 
                        ", discarded: " discarded "}")))

(defrecord CityInfectedEvent [aggregate-id city-id nb-cubes color])

;;
;; Entities
;;

(defrecord Game [])
(defrecord Player [user-id role])

(defn load-game [game-id]
    (load-aggregate game-id (Game.)))

;;
;; Constants
;;
(def all-default-roles #{:dispatcher :medic :operationsExpert :researcher :scientist})
(def all-difficulties #{:introduction :normal :heroic})

(def all-ruleset 
    {:default 
      {:roles all-default-roles
       :infection-cards card/infection-cards
       :distributable-player-cards (concat card/city-player-cards card/default-special-player-cards)
       :nb-cards-per-player (fn [players] 
                                (let [nb (- 6 (count players))]
                                    (map (fn [player] {:nb nb :player-id (:user-id player)}) players)))
       :nb-pandemic-for-difficulty (fn [difficulty]
                                      (cond (= :introduction difficulty) 4
                                            (= :normal difficulty) 5
                                            (= :heroic difficulty) 6
                                            :else (throw (Exception. (str "Unsupported difficulty " difficulty)))))
       :nb-cubes-outbreak-threshold 3}})

(defn get-ruleset [game]
  ((:ruleset game) all-ruleset))


;;
;; Game methods
;;

(defn get-roles [game]
  (:roles (get-ruleset game)))

;;
;; Handle Events
;;

(defmulti game-apply-event (fn [game event] (class event)))
(defmethod game-apply-event GameCreatedEvent [game event]
    (assoc game 
      :game-id (:aggregate-id event)
      :state :created
      :creator-id (:creator-id event)
      :ruleset (:ruleset event)
      :difficulty (:difficulty event)
      :players []))
(defmethod game-apply-event GameDifficultyChangedEvent [game event]
    (assoc game 
      :difficulty (:difficulty event)))
(defmethod game-apply-event GameStartedEvent [game event]
    (assoc game 
      :state :started))
(defmethod game-apply-event GameJoinedEvent [game event]
    (assoc game 
      :players (conj (:players game) (Player. (:user-id event) :random))))


;;
(extend-protocol EventHandler
    Game
    (apply-event [this event]
        (game-apply-event this event)))

;;
;; Command helpers
;;
(defn- used-roles [players]
  (let [players-with-role (filter #(not= :random (:role %)) players)]
    map #(:role %) players-with-role))

(defn- events-for-player-without-role [game]
  (let [game-id (:game-id game)
        players (:players game)
        players-without-role (filter #(= :random (:role %)) players)
        used-roles (used-roles players)
        remaining-roles (shuffle (set/difference (get-roles game) (set used-roles)))]
        (map (fn [player role] 
              (->GameUserRoleDefinedEvent game-id (:user-id player) role)) 
              players-without-role 
              remaining-roles)))

(defn- complete-for-difficulty [game player-cards]
  (let [ruleset (get-ruleset game)
        difficulty (:difficulty game)
        nb-cards (apply (:nb-pandemic-for-difficulty ruleset) [difficulty])
        ; Divide the remaining Player cards into 'nbEpidemicCards' *equal* (or at least close to) piles.
        list-of-piles (split nb-cards player-cards)]
        (reduce 
          (fn [pred pile]
            ; Shuffle 1 Epidemic card (face down) into each pile.
            ; Stack the piles together to form the Player Draw Pile.
            (concat pred 
                (shuffle (conj pile card/epidemic))))
            [] list-of-piles)))

(defn initialize-player-cards [game]
  (let [game-id (:game-id game)
        players (:players game)
        ruleset (get-ruleset game)
        distributable-cards (shuffle (:distributable-player-cards ruleset))
        nb-cards-per-player (apply (:nb-cards-per-player ruleset) [players])
        reduced (reduce 
                  (fn [pred pl] 
                    (let [old-remainings (:remainings pred)
                          new-remainings (drop (:nb pl) old-remainings)
                          cards (take (:nb pl) old-remainings)]
                        {:remainings new-remainings
                         :events (conj (:events pred) (PlayerCardAddedToHandEvent. game-id (:player-id pl) (seq cards)))}))
                  {:remainings distributable-cards :events []} nb-cards-per-player)
        player-draw-pile (complete-for-difficulty game (:remainings reduced))]
      (conj (:events reduced) (PlayerDrawPileInitializedEvent. game-id (seq player-draw-pile)))))

(defn number-of-cubes [game city-id color]
  (let [nb (get-in game [:cities city-id color])]
    (if (nil? nb) 0 nb)))

(defn trigger-outbreak 
  ([game city-id color]
      (trigger-outbreak game city-id color city/city-graph {:outbreaked {city-id 0}} 0))
  ([game city-id color city-graph state generation]
      (let [ruleset (get-ruleset game)
            nb-cubes-outbreak (:nb-cubes-outbreak-threshold ruleset)
            adjacents (city/adjacent-cities-of city-graph city-id)
            calculated (reduce 
              (fn [pred adj-city]
                  (let [infestors (get-in pred [:infestors adj-city])
                        old-generation (city-id infestors)
                        new-generation (if (nil? old-generation) generation (min generation old-generation))
                        next-state (update-in pred [:infestors adj-city] assoc city-id new-generation)
                        nb-cubes (+ (number-of-cubes game adj-city color) 
                                    (count (get-in next-state [:infestors adj-city])))
                        next-gen (+ 1 generation)]
                    (println "nb-cubes: " nb-cubes ", nb-cubes-outbreak: " nb-cubes-outbreak)
                    ;; City can outbreak only once
                    ;; outbreak is propagated only if the generation is lower than the 
                    ;; one already propagated. So that all infestors should be lowered.
                    (if (> nb-cubes nb-cubes-outbreak)
                      (trigger-outbreak adj-city color city-graph next-state next-gen)
                      next-state)))
              state adjacents)]
            calculated)))

;
; Infect the city with the given number of cubes for the specified disease.
; That is add the given number of cubes to the existings ones (if any).
;
; *Note that the eradication status is not checked by this method,
; that is the following rule is not verified:*
;
; > If, however, the pictured city is of a color that has been
; > eradicated, do not add a cube.
;
; *Whereas the outbreak rule is handled:*
; 
; > If a city already has 3 cubes in it of the color being added, instead of
; > adding a cube to the city, an outbreak occurs in that color.
;
; @param cityId city to infect
; @param nbCubes number of cube to add
; @param disease disease the cubes belongs to
(defn infect-city 
  ([game city-id nb-cubes color]
    (let [game-id (:game-id game)
          ruleset (get-ruleset game)
          nb-cubes-outbreak (:nb-cubes-outbreak-threshold ruleset)
          nb-cubes-old (number-of-cubes game city-id color)
          nb-cubes-new (+ nb-cubes-old nb-cubes)]
        (if (> nb-cubes-new nb-cubes-outbreak)
            (trigger-outbreak game city-id color)
            [(CityInfectedEvent. game-id city-id nb-cubes color)])))
  ([game city-id nb-cubes]
    (infect-city game city-id nb-cubes (city/color-of city-id))))

(defn initialize-infection-cards [game]
  (let [game-id (:game-id game)
        ruleset (get-ruleset game)
        nbCubes [3 3 3 2 2 2 1 1 1]
        nbCardsDiscarded (apply + nbCubes)
        infection-cards (shuffle (:infection-cards ruleset))
        distribution (reduce 
                          (fn [pred nb]
                            (let [old-remainings (:remainings pred)
                                  new-remainings (drop 1 old-remainings)
                                  card (first old-remainings)
                                  new-events (concat (:events pred) (infect-city game card nb))]
                              {:remainings new-remainings
                               :events new-events}))
                          {:remainings infection-cards :events []} nbCubes)
        cards-remaining (:remainings distribution)
        cards-discarded (take nbCardsDiscarded infection-cards)
        infected-cities-events (:events distribution)]
        (conj infected-cities-events
              (InfectionDrawPileInitializedEvent. game-id cards-remaining cards-discarded))))


;;
;; Handle Commands
;;

(extend-protocol CommandHandler
    CreateGameCommand
    (perform [command context]
        (let [game-id (:game-id command)
              user-id (:user-id command)
              difficulty (:difficulty command)
              ruleset (:ruleset command)
              user (load-user user-id)
              game (load-game game-id)]
            (when (:state game)
                (throw (Exception. "Already created")))
            (when-not (:state user)
                (throw (Exception. (str "No user found with id: " user-id))))
            (when (playing? user)
                (throw (IllegalStateException. (str "User " user-id " is already engaged in a game"))))
            (when-not (contains? all-difficulties difficulty)
                (throw (Exception. (str "Unsupported difficulty: " difficulty " " (seq all-difficulties)))))
            (when-not (contains? all-ruleset ruleset)
                (throw (Exception. (str "Unsupported ruleset: " ruleset))))
        [(->GameCreatedEvent game-id user-id difficulty ruleset) 
         (->GameJoinedEvent game-id user-id)
         (->UserGameJoinedEvent user-id game-id)]))

    JoinGameCommand
    (perform [command context]
        (let [game-id (:game-id command)
              user-id (:user-id command)
              user (load-user user-id)
              game (load-game game-id)
              state (:state game)]
            (when-not (= state :created)
                (throw (Exception. (str "Incorrect state: " state))))
            (when-not (:state user)
                (throw (Exception. (str "No user found with id: " user-id))))
            (when (playing? user)
                (throw (IllegalStateException. (str "User " user-id " is already engaged in a game"))))
        [(->GameJoinedEvent game-id user-id)
         (->UserGameJoinedEvent user-id game-id)]))

    ChangeGameDifficultyCommand
    (perform [command context]
        (let [game-id (:game-id command)
              user-id (:user-id command)
              user (load-user user-id)
              game (load-game game-id)
              state (:state game)]
            (when-not (= state :created)
                (throw (Exception. (str "Incorrect state: " state))))
            (when-not (:state user)
                (throw (Exception. (str "No user found with id: " user-id))))
        [(->GameDifficultyChangedEvent game-id user-id (:difficulty command))]))

    StartGameCommand
    (perform [command context]
        (let [game-id (:game-id command)
              game (load-game game-id)
              state (:state game)
              players (:players game)]
            (when-not (= state :created)
                (throw (Exception. (str "Incorrect state: " state))))
            (when (< (count players) 2)
                (throw (Exception. (str "Insufficient number of player: " (count players)))))
            (concat 
                ;; assign role to all players that don't have one yet
                (events-for-player-without-role game)
                ;; prepare the draw-piles
                (initialize-player-cards game)
                (initialize-infection-cards game)
                ;; finally trigger the Start game event
                [(->GameStartedEvent game-id)]
                ))))



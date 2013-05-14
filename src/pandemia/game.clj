(ns pandemia.game
    (:use pandemia.core
          pandemia.user
          pandemia.util)
    (:require [clojure.set :as set]
              [pandemia.card :as card]))

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
(defrecord InfectionDrawPileInitializedEvent [aggregate-id cards]
  Object
  (toString [this] (str "InfectionDrawPileInitializedEvent@" (system-id this) "{"
                        "aggregate-id: " aggregate-id 
                        ", cards: " cards "}")))

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
                                            :else (throw (Exception. (str "Unsupported difficulty " difficulty)))))}})



;;
;; Game methods
;;

(defn get-roles [game]
  (:roles (:ruleset game)))

;;
;; Handle Events
;;

(defmulti game-apply-event (fn [game event] (class event)))
(defmethod game-apply-event GameCreatedEvent [game event]
    (assoc game 
      :game-id (:aggregate-id event)
      :state :created
      :creator-id (:creator-id event)
      :ruleset ((:ruleset event) all-ruleset)
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
  (let [ruleset (:ruleset game)
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
        ruleset (:ruleset game)
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

(defn initialize-infection-cards [game]
  (let [game-id (:game-id game)
        ruleset (:ruleset game)
        infection-cards (shuffle (:infection-cards ruleset))]
    [(InfectionDrawPileInitializedEvent. game-id (seq infection-cards))]))

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



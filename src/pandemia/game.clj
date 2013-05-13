(ns pandemia.game
    (:use pandemia.core
          pandemia.user
          pandemia.util)
    (:require [clojure.set :as set]))

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
(def all-difficulties #{:easy :normal :hard :nightmare})

(def all-ruleset 
    {:default {:roles all-default-roles}})



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

(defn- events-for-player-without-role [game game-id]
  (let [players (:players game)
        players-without-role (filter #(= :random (:role %)) players)
        used-roles (used-roles players)
        remaining-roles (shuffle (set/difference (get-roles game) (set used-roles)))]
        (map (fn [player role] 
              (->GameUserRoleDefinedEvent game-id (:user-id player) role)) 
              players-without-role 
              remaining-roles)))

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
                (throw (Exception. (str "Unsupported difficulty: " difficulty))))
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
                [(->GameStartedEvent game-id)]
                ;; assign role to all players that don't have one yet
                (events-for-player-without-role game game-id)
                ;; prepare the draw-piles

                ))))



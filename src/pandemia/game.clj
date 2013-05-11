(ns pandemia.game
    (:use pandemia.core
          pandemia.user)
    (:require [clojure.set :as set]))

;;
;; Commands
;;

(defrecord CreateGameCommand [game-id user-id ])
(defrecord ChangeGameDifficultyCommand [game-id user-id difficulty])
(defrecord StartGameCommand [game-id])
(defrecord JoinGameCommand [game-id user-id])

;;
;; Events
;;

(defrecord GameCreatedEvent [aggregate-id creator-id])
(defrecord GameDifficultyChangedEvent [aggregate-id user-id difficulty])
(defrecord GameStartedEvent [aggregate-id])
(defrecord GameJoinedEvent  [aggregate-id user-id])
(defrecord GameUserRoleDefinedEvent [aggregate-id user-id role])

;;
;; Entities
;;

(defrecord Game [])
(defrecord Player [user-id role])


(defn load-game [game-id]
    (load-aggregate game-id (Game.)))

;;
;;

(defn players-with-role [players]
  (filter (fn [player] (not= :random (:role player))) players))

(defn players-without-role [players]
  (filter (fn [player] (= :random (:role player))) players))

(def all-roles #{:dispatcher :medic :operationsExpert :researcher :scientist})

;;
;; Handle Events
;;

(defmulti game-apply-event (fn [game event] (class event)))
(defmethod game-apply-event GameCreatedEvent [game event]
    (assoc game 
      :state :created
      :creator-id (:creator-id event)
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
;; Handle Commands
;;

(extend-protocol CommandHandler
    CreateGameCommand
    (perform [command context]
        (let [game-id (:game-id command)
              user-id (:user-id command)
              user (load-user user-id)
              game (load-game game-id)]
            (when (:state game)
                (throw (Exception. "Already created")))
            (when-not (:state user)
                (throw (Exception. (str "No user found with id: " user-id))))
            (when (playing? user)
                (throw (IllegalStateException. (str "User " user-id " is already engaged in a game"))))
        [(->GameCreatedEvent game-id user-id) 
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
            (let [players-without-role (players-without-role players)
                  used-roles (map #(:role %) (players-with-role players))
                  remaining-roles (shuffle (set/difference all-roles (set used-roles)))]
                  (concat 
                      [(->GameStartedEvent game-id)]
                      ;; assign role to all players that miss one
                      (map (fn [player role] 
                          (->GameUserRoleDefinedEvent game-id (:user-id player) role)) 
                          players-without-role 
                          remaining-roles))))))



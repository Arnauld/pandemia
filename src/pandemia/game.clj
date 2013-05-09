(ns pandemia.game
    (:use pandemia.core
          pandemia.user))

;;
;; Commands
;;

(defrecord CreateGameCommand [game-id user-id ])
(defrecord ChangeGameDifficultyCommand [game-id user-id difficulty])
(defrecord StartGameCommand [game-id])

;;
;; Events
;;

(defrecord GameCreatedEvent [aggregate-id creator-id])
(defrecord GameDifficultyChangedEvent [aggregate-id user-id difficulty])
(defrecord GameStartedEvent [aggregate-id])


;;
;; Entity
;;

(defrecord Game [])

(defn load-game [game-id]
    (load-aggregate game-id (Game.)))


;;
;; Handle Events
;;

(defmulti game-apply-event (fn [game event] (class event)))
(defmethod game-apply-event GameCreatedEvent [game event]
    (assoc game 
      :state :created
      :creator-id (:creator-id event)))
(defmethod game-apply-event GameDifficultyChangedEvent [game event]
    (assoc game 
      :difficulty (:difficulty event)))
(defmethod game-apply-event GameStartedEvent [game event]
    (assoc game 
      :state :started))

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
        [(->GameCreatedEvent game-id user-id)]))

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

    GameStartedEvent
    (perform [command context]
        (let [game-id (:game-id command)
              game (load-game game-id)
              state (:state game)]
            (when-not (= state :created)
                (throw (Exception. (str "Incorrect state: " state))))
        [(->GameStartedEvent (:aggregate-id command))])))



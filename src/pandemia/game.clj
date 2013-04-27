(ns pandemia.game
	(:use pandemia.core
		  pandemia.event))

(defrecord CreateGameCommand [aggregate-id player-id ])
(defrecord ChangeGameDifficultyCommand [aggregate-id player-id difficulty])
(defrecord StartGameCommand [aggregate-id])

(defrecord GameCreatedEvent [game-id creator-id])
(defrecord GameDifficultyChangedEvent [game-id player-id difficulty])
(defrecord GameStartedEvent [game-id])

;;
;; Handle Commands
;;

(extend-protocol CommandHandler
  CreateGameCommand
  (perform [command state]
    (when (:state state)
      (throw (Exception. "Already created")))
    [(->GameCreatedEvent (:aggregate-id command) (:player-id command))])

  ChangeGameDifficultyCommand
  (perform [command state]
    (when-not (= (:state state) :created)
      (throw (Exception. (str "Incorrect state: " state))))
    [(->GameDifficultyChangedEvent (:aggregate-id command) (:player-id command) (:difficulty command))])

  GameStartedEvent
  (perform [command state]
  	(when-not (= (:state state) :created)
      (throw (Exception. (str "Incorrect state: " state))))
	[(->GameStartedEvent (:aggregate-id command))]))

;;
;; Handle Events
;;

(defmulti apply-event (fn [state event] (class event)))
(defmethod apply-event GameCreatedEvent [state event]
  (assoc state 
    :state :created
    :creator-id (:creator-id event)))

(defmethod apply-event ChangeGameDifficultyCommand [state event]
  (assoc state 
    :move (:move event)))

(defmethod apply-event GameStartedEvent [state event]
  (assoc state 
    :state :started))

;;
;; Infrastructure
;;
(defn apply-events [state events] 
  (reduce apply-event state events))

;;
;;
;;
(defn handle-command [command event-store]
  (let [event-stream (retrieve-event-stream event-store (:aggregate-id command))
        old-events (flatten (:transactions event-stream))
        current-state (apply-events {} old-events)
        new-events (perform command current-state)]
    (println new-events)
    (append-events event-store (:aggregate-id command) event-stream new-events)))

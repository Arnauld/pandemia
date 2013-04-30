(ns pandemia.game
	(:use pandemia.core))

;;
;; Commands
;;

(defrecord CreateGameCommand [aggregate-id player-id ])
(defrecord ChangeGameDifficultyCommand [aggregate-id player-id difficulty])
(defrecord StartGameCommand [aggregate-id])

;;
;; Events
;;

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

(extend-protocol EventHandler
  GameCreatedEvent
  (apply-event [event state]
    (assoc state 
      :state :created
      :creator-id (:creator-id event)))

  GameDifficultyChangedEvent
  (apply-event [event state]
    (assoc state 
      :difficulty (:difficulty event)))

  GameStartedEvent
  (apply-event [event state]
    (assoc state 
      :state :started)))

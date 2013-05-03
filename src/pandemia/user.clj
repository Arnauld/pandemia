(ns pandemia.user
	(:use pandemia.core
		  pandemia.util))

;;
;; Commands
;;

(defrecord CreateUserCommand [aggregate-id pseudo])
(defrecord ChangeUserInfosCommand [aggregate-id infos])

;;
;; Events
;;

(defrecord UserCreatedEvent [user-id pseudo])
(defrecord UserInfosChangedEvent [user-id delta])

;;
;; Handle Commands
;;

(extend-protocol CommandHandler
	CreateUserCommand
	(perform [command state]
		(when (:state state)
	      (throw (Exception. "Already created")))
		[(->UserCreatedEvent (:aggregate-id command) (:pseudo command))])

	ChangeUserInfosCommand
	(perform [command state]
		(when-not (= (:state state) :created)
			(throw (Exception. (str "Incorrect state: " state))))
		(let [delta (delta state (:infos command))]
			(if (empty? delta)
				[] ;; command does not change anything...
				[(->UserInfosChangedEvent (:aggregate-id command) delta)]))))

;;
;; Handle Events
;;

(extend-protocol EventHandler
  UserCreatedEvent
  (apply-event [event state]
    (assoc state 
      :state :created))

  UserInfosChangedEvent
  (apply-event [event state]
  	(assoc state (:delta event))))

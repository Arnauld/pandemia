(ns pandemia.user
    (:use pandemia.core
          pandemia.util))

;;
;; Commands
;;

(defrecord CreateUserCommand [user-id pseudo])
(defrecord ChangeUserInfosCommand [user-id infos])

;;
;; Events
;;

(defrecord UserCreatedEvent [aggregate-id pseudo])
(defrecord UserInfosChangedEvent [aggregate-id delta])
(defrecord UserGameJoinedEvent [aggregate-id game-id])

;;
;; Entity
;;

(defrecord User [])

(defn load-user [user-id]
    (load-aggregate user-id (User.)))


;;
;; Handle Events
;;

(defmulti user-apply-event (fn [user event] (class event)))
(defmethod user-apply-event UserCreatedEvent [user event]
    (assoc user 
        :state :created))

(defmethod user-apply-event UserInfosChangedEvent [user event]
    (assoc user
        (:delta event)))

(extend-protocol EventHandler
    User
    (apply-event [this event]
        (user-apply-event this event)))

;;
;; Handle Commands
;;

(extend-protocol CommandHandler
    CreateUserCommand
    (perform [command context]
        (let [user-id (:user-id command)
              user (load-user user-id)]
          (when (:state user)
            (throw (Exception. "Already created")))
          [(->UserCreatedEvent user-id (:pseudo command))]))

    ChangeUserInfosCommand
    (perform [command context]
        (let [user-id (:user-id command)
              user (load-user user-id)
              state (:state user)]
          (when-not (= state :created)
            (throw (Exception. (str "Incorrect state: " state))))
          (let [delta (delta state (:infos command))]
            (if (empty? delta)
                [] ;; command does not change anything...
                [(->UserInfosChangedEvent user-id delta)])))))



(ns pandemia.user
    (:use pandemia.core
          pandemia.util)
    (:use [pandemia.event :only [defevent]])
    (:require [pandemia.event :as event]))

;;
;; Commands
;;

(defrecord CreateUserCommand [user-id pseudo])
(defrecord ChangeUserInfosCommand [user-id infos])


;;
;; Events
;;

(defevent UserCreatedEvent [aggregate-id pseudo])
(defevent UserInfosChangedEvent [aggregate-id delta])
(defevent UserGameJoinedEvent [aggregate-id game-id])

;;
;; Entity
;;

(defrecord User [])

(defn load-user [user-id]
    (load-aggregate user-id (User.)))


;;
;; Handle Events
;;

(defmulti user-apply-event (fn [user event] (event/simple-type event)))
(defmethod user-apply-event :UserCreatedEvent [user event]
    (assoc user 
        :state :created))

(defmethod user-apply-event :UserInfosChangedEvent [user event]
    (assoc user
        (:delta event)))

(defmethod user-apply-event :UserGameJoinedEvent [user event]
    (assoc user
        :current-game-id (:game-id event)))


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

;;
;;
;;
(defn playing? [user] 
    (not= (:current-game-id user) nil))

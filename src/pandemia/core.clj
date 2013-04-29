(ns pandemia.core
	(:use pandemia.core))


;;
;;
;;
(defprotocol CommandHandler
  (perform [command state]))

;;
;;
;;

(defprotocol EventHandler
  (apply-event [state event]))


;;
;; Event Store
;;

(defprotocol EventStore
  (retrieve-event-stream [this aggregate-id])
  (append-events [this aggregate-id previous-event-stream events]))

(defrecord EventStream [version transactions])


;;
;; Infrastructure
;;
(defn apply-events [state events] 
  (reduce 
  	(fn [current-state event] (apply-event event current-state)) 
  	state events))

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

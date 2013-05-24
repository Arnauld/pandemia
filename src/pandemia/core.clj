(ns pandemia.core
    (:use pandemia.util))

;;
;;
;;

(defprotocol CommandHandler
  (perform [command context]))

;;
;;
;;

(defprotocol EventHandler
  (apply-event [event state]))

(defprotocol EventStore
  (retrieve-event-stream [this aggregate-id])
  (append-events [this aggregate-id previous-event-stream events]))

(defrecord EventStream [version events]
  Object
  (toString [this] (str "EventStream@" (system-id this) "{v: " version ", events: " events "}")))


;;
;; Event Storage
;;

(declare ^:dynamic *event-store*)

(defn get-event-store [] 
  (let [store @*event-store*]
    (when (= nil store) (throw (IllegalStateException. "No event store bound!")))
    store))

(defmacro with-event-store [store & body]
  `(binding [*event-store* (atom ~store)]
      (do ~@body)))

(defn- save-event [event]
  (let [aggregate-id (:aggregate-id event)
        event-store  (get-event-store)
        event-stream (retrieve-event-stream event-store aggregate-id)]
    (append-events event-store aggregate-id event-stream [event])))

(defn- save-events [unit-of-work]
  (dorun (map save-event (:events unit-of-work))))

;;
;;
;;

(defn publish-event [event]
  (println (str "Publishing " event)))


;;
;; Unit Of Work
;;

(declare ^:dynamic *unit-of-work*)

(defn get-unit-of-work []
  (let [uow @*unit-of-work*]
  uow))


(defn- add-events-to-unit-of-work! [events]
  (swap! *unit-of-work* 
    (fn [old new-events]
      (let [evts (concat (:events old) new-events)
            uow (assoc old :events evts)]
        uow))
    events))


(defn commit-unit-of-work! []
  (let [uow (get-unit-of-work)]
    (save-events uow)
    (dorun (map publish-event (:events uow)))))

(defmacro with-unit-of-work [& body]
  `(binding [*unit-of-work* (atom {:events [] :aggregate-versions {}})]
     (let [result# ~@body]
       (commit-unit-of-work!)
       result#)))

;;
;;
;;

(defn execute-command [cmd & args]
    (with-unit-of-work
        (let [new-events (perform cmd args)]
            (add-events-to-unit-of-work! new-events))))



(defn load-events [aggregate-id event-store]
    (let [event-stream (retrieve-event-stream event-store aggregate-id)
            events (flatten (:events event-stream))]
        events))

;;
;; Infrastructure
;;

(defn apply-events [state events] 
  (reduce 
      (fn [current-state event] (apply-event current-state event)) 
      state events))

;;
;;
;;

(defn load-aggregate [aggregate-id initial-state]
  (let [store (get-event-store)
        stream (retrieve-event-stream store aggregate-id)
        events (flatten (:events stream))
        state (apply-events initial-state events)]
    state))




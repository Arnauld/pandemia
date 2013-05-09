(ns pandemia.in-memory-event-store
  (:use pandemia.core
        pandemia.util))


(import java.util.concurrent.ConcurrentHashMap)
(import java.util.ConcurrentModificationException)

(defrecord MemoryStore [streams]
  Object
     (toString [this] (str "MemoryStore@" (system-id this) "{streams: " streams "}")))

(def empty-stream (->EventStream 0 []))

(extend-protocol EventStore
  MemoryStore
  (retrieve-event-stream [this aggregate-id]
    (let [streams (:streams this)]
      (when (= nil aggregate-id) (throw (IllegalArgumentException. "No id provided")))
      (let [r 
        (if (.putIfAbsent streams aggregate-id empty-stream)
            (.get streams aggregate-id)
            empty-stream)]
        r)))

  (append-events [this aggregate-id previous-es events]
    (let [streams (:streams this)
          next-es (->EventStream (+ (:version previous-es) (count events))
                                 (concat (:events previous-es) events))
          replaced (.replace streams aggregate-id previous-es next-es)]
      (when-not replaced (throw (ConcurrentModificationException.))))))

(defn new-in-memory-event-store [] 
  (->MemoryStore (ConcurrentHashMap.)))

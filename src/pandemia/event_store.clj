(ns pandemia.event-store
  (:use pandemia.core))


(import java.util.concurrent.ConcurrentHashMap)
(import java.util.ConcurrentModificationException)

(defrecord MemoryStore [streams])

(def empty-stream (->EventStream 0 []))

(extend-protocol EventStore
  MemoryStore
  (retrieve-event-stream [this aggregate-id]
    (let [streams (:streams this)]
      (if (.putIfAbsent streams aggregate-id empty-stream)
          (.get streams aggregate-id)
          empty-stream)))

  (append-events [this aggregate-id previous-es events]
    (let [streams (:streams this)
          next-es (->EventStream (inc (:version previous-es))
                                 (conj (:transactions previous-es)
                                 events))
          replaced (.replace streams aggregate-id previous-es next-es)]
      (when-not replaced (throw (ConcurrentModificationException.))))))

(defn new-in-memory-event-store [] (->MemoryStore (ConcurrentHashMap.)))

(ns pandemia.event)

(defprotocol EventStore
  (retrieve-event-stream [this aggregate-id])
  (append-events [this aggregate-id previous-event-stream events]))

(defrecord EventStream [version transactions])


(import java.util.concurrent.ConcurrentHashMap)
(import java.util.ConcurrentModificationException)

(def in-memory-event-store
  (let [streams (ConcurrentHashMap.)
        empty-stream (->EventStream 0 [])]
    (reify EventStore
      (retrieve-event-stream [this aggregate-id]
        (if (.putIfAbsent streams aggregate-id empty-stream)
          (.get streams aggregate-id)
          empty-stream))

      (append-events [this aggregate-id previous-es events]
        (let [next-es (->EventStream (inc (:version previous-es))
                                     (conj (:transactions previous-es)
                                     events))
              replaced (.replace streams aggregate-id previous-es next-es)]
          (when-not replaced (throw (ConcurrentModificationException.))))))))
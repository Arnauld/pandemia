(ns pandemia.redis-test
  (:require [pandemia.util :as util]
            [pandemia.core :as core]))

(import java.util.concurrent.CopyOnWriteArrayList)

(defrecord MemoryEventBus [listeners])
(extend-protocol core/EventBus
	MemoryEventBus
	(publish [this event]
		(doseq [listener (:listeners this)]
			(apply listener event)))

  	(subscribe [this listener]
  		(.add (:listeners this) listener)
  		listener)

  	(unsubscribe [this listener-ref]
  		(.remove (:listeners this) listener-ref)))

(defn new-in-memory-event-bus []
	(->MemoryEventBus (CopyOnWriteArrayList.)))

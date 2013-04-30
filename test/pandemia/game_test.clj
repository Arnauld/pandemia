(ns pandemia.game-test
  (:use clojure.test
  		pandemia.core
        pandemia.game
        pandemia.event-store))

(def aggreage-id 1)

(deftest a-test
  (testing "FIXME, If I fail."
  	(let [store in-memory-event-store
  		  e1 (handle-command (->CreateGameCommand aggreage-id :ply1) store)
  		  e2 (handle-command (->ChangeGameDifficultyCommand aggreage-id :ply2 :normal) store)
  		  old-events (load-events aggreage-id store)
  		  [tx1 tx2] old-events] ; destructuring ~ pattern matching to retrieve 'transactions' elements
  		  (println old-events)
		  (is (= "GameCreatedEvent" (.getSimpleName (class tx1))))
		  (is (= :ply1 (:creator-id tx1)))
		  (is (= "GameDifficultyChangedEvent" (.getSimpleName (class tx2))))
		  (is (= :normal  (:difficulty tx2))))))

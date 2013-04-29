(ns pandemia.game-test
  (:use clojure.test
  		pandemia.core
        pandemia.game
        pandemia.event-store))

(deftest a-test
  (testing "FIXME, If I fail."
  	(let [e1 (handle-command (->CreateGameCommand 1 :ply1) in-memory-event-store)
  		  e2 (handle-command (->ChangeGameDifficultyCommand 1 :ply2 :normal) in-memory-event-store)
  		  e3 (retrieve-event-stream in-memory-event-store 1)]
  		  (println e3) )
    (is (= 1 1))))

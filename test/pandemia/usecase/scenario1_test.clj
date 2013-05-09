(ns pandemia.usecase.scenario1-test
  (:use clojure.test
        pandemia.core
        pandemia.game
        pandemia.user
        pandemia.in-memory-event-store))

;; --- DUMMY

(deftest a-test
  (testing "FIXME, If I fail."
    (is (= 1 1))))

;; --- 

(deftest scenario-1
  (testing "Create a game and change its difficulty"
    (let [store (new-in-memory-event-store)
          userId "user-1"
          gameId "game-1"]
        (with-event-store store
          (execute-command (->CreateUserCommand userId "Gregory"))
          (execute-command (->CreateGameCommand gameId userId))
          (execute-command (->ChangeGameDifficultyCommand gameId userId :normal)))
        (let [game-events (load-events gameId store)
              user-events (load-events userId store)
              [ge1 ge2] game-events
              [ue1 ue2] user-events]
            ;(println user-events "\n---\n" game-events)
            (is (instance? pandemia.game.GameCreatedEvent ge1))
            (is (instance? pandemia.game.GameDifficultyChangedEvent ge2))
            (is (instance? pandemia.user.UserCreatedEvent ue1))
            (is (instance? pandemia.user.UserGameJoinedEvent ue2)) 
            (is (= userId (:creator-id ge1)))
            (is (= :normal  (:difficulty ge2)))))))

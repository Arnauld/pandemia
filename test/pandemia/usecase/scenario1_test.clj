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
          userId1 "user-1"
          userId2 "user-2"
          gameId "game-1"]
        (with-event-store store
          (execute-command (->CreateUserCommand userId1 "Gregory"))
          (execute-command (->CreateUserCommand userId2 "Wilson"))
          (execute-command (map->CreateGameCommand {:game-id gameId 
                                                    :user-id userId1 
                                                    :difficulty :heroic
                                                    :ruleset :default}))
          (execute-command (->ChangeGameDifficultyCommand gameId userId1 :normal))
          (execute-command (->JoinGameCommand gameId userId2))
          (execute-command (->StartGameCommand gameId)))
        (let [game-events (load-events gameId store)
              user1-events (load-events userId1 store)
              user2-events (load-events userId2 store)
              [ge1 ge2 ge3] game-events
              [ue1 ue2] user1-events]
            (println (str "user1-events: " (reduce #(str %1 "\n    " %2) user1-events)
                          "\n---\n" 
                          "user2-events: " (reduce #(str %1 "\n    " %2) user2-events)
                          "\n---\n"
                          "game-events : " (reduce #(str %1 "\n    " %2) game-events)))
            (is (instance? pandemia.game.GameCreatedEvent ge1))
            (is (instance? pandemia.game.GameJoinedEvent ge2))
            (is (instance? pandemia.game.GameDifficultyChangedEvent ge3))
            (is (instance? pandemia.user.UserCreatedEvent ue1))
            (is (instance? pandemia.user.UserGameJoinedEvent ue2)) 
            (is (= userId1 (:creator-id ge1)))
            (is (= :normal  (:difficulty ge3)))))))

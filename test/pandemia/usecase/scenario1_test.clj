(ns pandemia.usecase.scenario1-test
  (:use clojure.test
        pandemia.core
        pandemia.game
        pandemia.user
        pandemia.event-store))

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
      (handle-command (->CreateUserCommand userId "Gregory") store)
      (handle-command (->CreateGameCommand gameId userId) store)
    (handle-command (->ChangeGameDifficultyCommand gameId userId :normal) store)
      (let [game-events (load-events gameId store)
          user-events (load-events userId store)
          [ge1 ge2] game-events
          [ue1 ue2] user-events]
        ;(println user-events "\n---\n" game-events)
        ;; TODO how to reference a class, instead of using getSimpleName...
      (is (= "GameCreatedEvent" (.getSimpleName (class ge1)))) 
      (is (= "GameDifficultyChangedEvent" (.getSimpleName (class ge2))))
      (is (= "UserCreatedEvent" (.getSimpleName (class ue1)))) 
      (is (= "UserGameJoinedEvent" (.getSimpleName (class ue2))))
      (is (= userId (:creator-id ge1)))
      (is (= :normal  (:difficulty ge2)))))))

(ns pandemia.game-test
  (:use clojure.test
      pandemia.core
        pandemia.game
        pandemia.user
        pandemia.in-memory-event-store))

(def aggregate-id 1)

(deftest a-test
  (testing "Create a game and change its difficulty"
    (let [store (new-in-memory-event-store)
          gameId "game-1"
          userId "user-1"]
        (with-event-store store
          (execute-command (->CreateUserCommand userId "McCallum"))
          (execute-command (map->CreateGameCommand {:game-id gameId 
                                                    :user-id userId
                                                    :difficulty :heroic 
                                                    :ruleset :default}))
          (execute-command (->ChangeGameDifficultyCommand gameId userId :normal)))
        (let [old-events (load-events gameId store)
              [tx1 tx2 tx3] old-events] ; destructuring ~ pattern matching to retrieve 'transactions' elements
          ;(println old-events)
          (is (instance? pandemia.game.GameCreatedEvent tx1))
          (is (= userId (:creator-id tx1)))
          (is (instance? pandemia.game.GameJoinedEvent tx2))
          (is (= userId (:user-id tx2)))
          (is (instance? pandemia.game.GameDifficultyChangedEvent tx3))
          (is (= :normal (:difficulty tx3)))))))

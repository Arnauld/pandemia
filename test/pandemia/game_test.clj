(ns pandemia.game-test
  (:use clojure.test
      pandemia.core
        pandemia.game
        pandemia.user
        pandemia.in-memory-event-store)
  (:require [pandemia.event :as event]))

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
          (is (= :pandemia.game.GameCreatedEvent (event/full-type tx1)))
          (is (= userId (:creator-id tx1)))
          (is (= :pandemia.game.GameJoinedEvent (event/full-type tx2)))
          (is (= userId (:user-id tx2)))
          (is (= :pandemia.game.GameDifficultyChangedEvent (event/full-type tx3)))
          (is (= :normal (:difficulty tx3)))))))

(deftest test-number-of-cubes
  (testing "Number of cubes - city and color defined"
    (let [game {:cities {:NewYork {:blue 1}
                         :London  {:blue 3}
                         :Paris   {:blue 1}
                         :Essen   {:blue 2}}}]
          (is (= 1 (number-of-cubes game :Paris :blue)))))
  
  (testing "Number of cubes - city defined but no color"
    (let [game {:cities {:NewYork {:blue 1}
                         :London  {:blue 3}
                         :Paris   {:blue 1}
                         :Essen   {:blue 2}}}]
          (is (= 0 (number-of-cubes game :Paris :black)))))

  (testing "Number of cubes - city not yet defined"
    (let [game {:cities {:NewYork {:blue 1}
                         :London  {:blue 3}
                         :Paris   {:blue 1}
                         :Essen   {:blue 2}}}]
          (is (= 0 (number-of-cubes game :Milan :black))))))


;;    Toronto-----N.Y------------London------Essen---SaintPetersbourg
;;         \      / \               | \       /  |
;;      Washington   \             /   `-Paris    \
;;                    `-----Madrid------/   | `--Milan
;;                          /    \          |
;;                  SaoPaulo      `-------Algiers

(deftest test-calculate-outbreak-chain 
  (testing "One outbreak: no chain"
    (let [game {:ruleset :default
                :cities {:NewYork {:blue 1}
                         :London  {:blue 3}
                         :Paris   {:blue 1}
                         :Essen   {:blue 2}}}
          chain (calculate-outbreak-chain  game :London :blue)]
        ; (println "calculate-outbreak-chain : " chain "\n" game)
        )))

(deftest test-calculate-outbreak-chain-chaining 
  (testing "Multiple outbreak: chained in different generation"
    (let [game {:ruleset :default
                :cities {:NewYork {:blue 1}
                         :London  {:blue 3}
                         :Madrid  {:blue 3}
                         :Paris   {:blue 2}
                         :Essen   {:blue 2}}}
          chain (calculate-outbreak-chain  game :London :blue)]
        ; (println "calculate-outbreak-chain : " chain "\n" game)
        ))

  (testing "Multiple outbreak: chained within same generation"
    (let [game {:ruleset :default
                :cities {:NewYork {:blue 3}
                         :London  {:blue 3}
                         :Paris   {:blue 3}
                         :Madrid  {:blue 2}
                         :Essen   {:blue 2}}}
          chain (calculate-outbreak-chain  game :London :blue)
          payload (reduce-outbreak-chain game chain)]
        (doseq [p (:generations payload)] (println " > " p)))))

(deftest test-reduce-outbreak-chain
  (testing "Reduce outbreak chain : simple case"
      (let [game {:ruleset :default
                  :cities {:Paris {:blue 1}
                           :NewYork {:blue 1}
                           :London {:blue 3}
                           :Essen {:blue 2}}}
            chain {:infections {:Essen {:London 0}
                                :NewYork {:London 0}
                                :Paris {:London 0}
                                :Madrid {:London 0}}
                   :outbreaks {:London 0}}
            payload (reduce-outbreak-chain game chain)]
          (println "reduce-outbreak-chain  " chain "\n\t => " payload ))))






(ns pandemia.game-ext-test
  (:use clojure.test
      pandemia.core
        pandemia.game
        pandemia.user
        pandemia.in-memory-event-store))

(def aggregate-id 1)

(deftest test-calculate-outbreak-chain-chaining 
  (testing "Multiple outbreak: chained in different generation"
    (let [game {:ruleset :default
                :cities {:NewYork {:blue 1}
                         :London  {:blue 3}
                         :Madrid  {:blue 3}
                         :Paris   {:blue 2}
                         :Essen   {:blue 2}}}
          chain (calculate-outbreak-chain  game :London :blue)]
        (println "calculate-outbreak-chain : " chain "\n" game))))

(deftest test-calculate-outbreak-chain-chaining-same-gen 
  (testing "Multiple outbreak: chained within same generation"
    (let [game {:ruleset :default
                :cities {:NewYork {:blue 3}
                         :London  {:blue 3}
                         :Paris   {:blue 3}
                         :Madrid  {:blue 2}
                         :Essen   {:blue 2}}}
          chain (calculate-outbreak-chain  game :London :blue)
          payload (reduce-outbreak-chain game chain)]
        (println "\n\ncalculate-outbreak-chain : " chain)
        (println ":: " payload "\ngenerations:\n\n")
        (doseq [p (:generations payload)] (println " > " p))
        (println "•••"))))


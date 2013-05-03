(ns pandemia.user-test
  (:use clojure.test
  		  pandemia.core
        pandemia.user
        pandemia.event-store))

(def aggregate-id 1)

(deftest a-test
  (testing "Create a user and change its infos"
  	(let [store (new-in-memory-event-store)]
        (handle-command (->CreateUserCommand aggregate-id "Carmen") store)
        (handle-command (->ChangeUserInfosCommand aggregate-id {:first_name "Carmen" :last_name "McCallum"}) store)
  		  (let [events (load-events aggregate-id store)
              [tx1 tx2] events]
            ;; TODO how to reference a class, instead of using getSimpleName...
            (is (= "UserCreatedEvent" (.getSimpleName (class tx1)))) 
            (is (= "UserInfosChangedEvent" (.getSimpleName (class tx2))))))))


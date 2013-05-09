(ns pandemia.user-test
  (:use clojure.test
        pandemia.core
        pandemia.user
        pandemia.in-memory-event-store))

(def user-id 1)

(deftest a-test
  (testing "Create a user and change its infos"
    (let [store (new-in-memory-event-store)]
        (with-event-store store
            (execute-command (->CreateUserCommand user-id "Carmen"))
            (execute-command (->ChangeUserInfosCommand user-id {:first_name "Carmen" :last_name "McCallum"}))
            )
        (let [events (load-events user-id store)
              [tx1 tx2] events]
            (is (instance? pandemia.user.UserCreatedEvent tx1))
            (is (instance? pandemia.user.UserInfosChangedEvent tx2))))))


(ns pandemia.webcontroller)

(defrecord Ok  [body])
(defrecord Err [code-err body])

(defn ok [body]
	(->Ok body))
(defn not-found [body]
	(->Err 404 body))


(defn update-userinfo [req]          ;; ordinary clojure function
  (let [user-id (-> req :params :id)    ; param from uri
        password (-> req :params :password)] ; form param
      (println "Updating user infos (" user-id "/" password ")")
      (ok {:status :ok})))

(defn get-user-by-id [req]
  (let [user-id (-> req :params :id)]    ; param from uri
      (println "Retrieving user (" user-id ")")
      (not-found {:name "Bob"})))





(ns pandemia.webcontroller
	(:use [clojure.tools.logging :only (debug info error)])
	(:require [pandemia.web :as web]))



(defn update-userinfo [req]          ;; ordinary clojure function
  (let [user-id (-> req :params :id)    ; param from uri
        password (-> req :params :password)] ; form param
      (debug "Updating user infos (" user-id "/" password ")")
      (web/ok "{:status :ok}")))

(defn get-user-by-id [req]
  (let [user-id (-> req :params :id)]    ; param from uri
      (debug "Retrieving user (" user-id ")")
      (web/not-found "{:name \"Bob\"}")))





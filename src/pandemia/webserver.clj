(ns pandemia.webserver
    (:use [compojure.handler :only [site]] ; form, query params decode; cookie; session, etc
          [compojure.core :only [defroutes GET POST DELETE ANY context]]
          org.httpkit.server)
    (:require [cheshire.core   :as json]
              [compojure.route :as route] ; [files not-found]
              )) 

;; ---

(def default-conf {:port 5001})

(defn json-response [body]
    {:status  200
     :headers {"Content-Type" "application/json"}
     :body    (json/generate-string body)})

;; --- ~Controller

(defn update-userinfo [req]          ;; ordinary clojure function
  (let [user-id (-> req :params :id)    ; param from uri
        password (-> req :params :password)] ; form param
        (println "Updating user infos (" user-id "/" password ")")
        (json-response {:status :ok})
    ))

(defn get-user-by-id [req]
    (json-response {:name "Bob"}))

;; ---

(defn show-landing-page [req]
    {:status  200
     :headers {"Content-Type" "text/html"}
     :body    "hello HTTP!"})

(defn ws-handler [req]
  (with-channel req channel ; get the channel
    (if (websocket? channel)
      (println "WebSocket channel")
      (println "HTTP channel"))
    ;; communicate with client using method defined above
    (on-close channel (fn [status] (println "channel closed")))
    (on-receive channel (fn [data] ; data received from client
           ;; An optional param can pass to send!: close-after-send?
           ;; When unspecified, `close-after-send?` defaults to true for HTTP channels
           ;; and false for WebSocket.  (send! channel data close-after-send?)
           (send! channel data))))) ; data is sent directly to the client

(defn redirect-to [location]
    (fn [req]
        {:status  301
         :headers {"Location" location
                   "Content-Type" "text/html"}
         :body    (str "<p>Moved to " location "</p>")}))

(defroutes all-routes
  (GET "/"   [] (redirect-to "/index.html"))
  (GET "/ws" [] ws-handler)     ;; websocket
  (context "/user/:id" []
           (GET / [] get-user-by-id)
           (POST / [] update-userinfo))
  (route/files "/static/") ;; static file url prefix /static, in `public` folder
  (route/not-found "<p>Page not found.</p>")) ;; all other, return 404

(defn start-server [conf]
    (let [conf-to-use (merge default-conf conf)
          port (:port conf-to-use)]
        (println "Starting server on port " port)
        (run-server (site #'all-routes) {:port port})
        (println "Server started")))


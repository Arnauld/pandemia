(ns pandemia.webserver
    (:use [compojure.handler :only [site]] ; form, query params decode; cookie; session, etc
          [compojure.core :only [defroutes GET POST DELETE ANY context]]
          [clojure.tools.logging :only (debug info error)]
          org.httpkit.server
          [ring.util.response :only (file-response)])
    (:require [cheshire.core   :as json]
              [compojure.route :as route] ; [files not-found]
              [pandemia.webcontroller :as controller]
              [pandemia.web :as web])) 

;; ---

(def default-conf {:port 5001})

;; ---

;; TODO check something :)
(defn secure [req]
	(let [cookies (:cookies req)]
		(debug "Secure cookies: " cookies)
		req))

;; ---

(defn redirect-to [location]
    (fn [req]
        {:status  301
         :headers {"Location" location
                   "Content-Type" "text/html"}
         :body    (str "<p>Moved to " location "</p>")}))

;; ---

(defn render-json [response]
  (cond
    (web/err? response)
      {:status  (:code-err response)
       :headers {"Content-Type" "application/json"}
       :body    (json/generate-string (:body response))}
    :else
      {:status  200
       :headers {"Content-Type" "application/json"}
       :body    (json/generate-string (:body response))}
    ))

;; ---

(defn as-json [handlers]
    (let [chain (web/handler-chain handlers)]
      (fn [req]
        (render-json (apply chain [req])))))

;; ---

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

(def static-dir "client")

(defroutes all-routes
  (GET "/"   [] (redirect-to "/index.html"))
  (GET "/index.html" [] (fn [req] 
  							(file-response "/index.html" {:root static-dir})))
  (GET "/ws" []  ws-handler)     ;; websocket
  (context "/user" []
           (GET  "/:id" [] (as-json [secure controller/get-user-by-id]))
           (POST "/:id" [] (as-json [secure controller/update-userinfo])))
  (route/files "/static/" {:root static-dir}) ;; static file url prefix /static, in `public` folder
  (route/not-found "<p>Page not found.</p>")) ;; all other, return 404

(defn start-server [conf]
    (let [conf-to-use (merge default-conf conf)
          port (:port conf-to-use)]
        (run-server (site #'all-routes) {:port port})
        (info "Server started on port " port)))


(ns pandemia.webserver
    (:use [compojure.handler :only [site]] ; form, query params decode; cookie; session, etc
          [compojure.core :only [defroutes GET POST DELETE ANY context]]
          [clojure.tools.logging :only (info error)]
          org.httpkit.server
          [ring.util.response :only (file-response)])
    (:require [cheshire.core   :as json]
              [compojure.route :as route] ; [files not-found]
              [pandemia.webcontroller :as controller])) 

;; ---

(def default-conf {:port 5001})

;; ---

(defn redirect-to [location]
    (fn [req]
        {:status  301
         :headers {"Location" location
                   "Content-Type" "text/html"}
         :body    (str "<p>Moved to " location "</p>")}))

(defn apply-chain [req handlers]
    (let [res (reduce (fn [pred handler] 
                            (apply handler [pred])) req handlers)]
        res))

;; ---

(defprotocol JsonRender
    (render-json [this request]))

(extend-protocol JsonRender
    pandemia.webcontroller.Ok
    (render-json [this request]
        {:status  200
         :headers {"Content-Type" "application/json"}
         :body    (json/generate-string (:body this))})

    pandemia.webcontroller.Err
    (render-json [this request]
        {:status  (:code-err this)
         :headers {"Content-Type" "application/json"}
         :body    (json/generate-string (:body this))}))

;; ---

(defn as-json [handlers]
    (fn [req]
        (let [res (apply-chain req handlers)]
            (render-json res req))))

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
           (GET  "/:id" [] (as-json [controller/get-user-by-id]))
           (POST "/:id" [] (as-json [controller/update-userinfo])))
  (route/files "/static/" {:root static-dir}) ;; static file url prefix /static, in `public` folder
  (route/not-found "<p>Page not found.</p>")) ;; all other, return 404

(defn start-server [conf]
    (let [conf-to-use (merge default-conf conf)
          port (:port conf-to-use)]
        (run-server (site #'all-routes) {:port port})
        (info "Server started on port " port)))


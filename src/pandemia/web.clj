(ns pandemia.web)

(defn ok [response body]
  (assoc response
      :body body
      :web-status :ok
      :code-http 200))

(defn err [response body code-http]
    (assoc response
      :body body
      :web-status :err
      :code-http code-http))

(defn not-found [body]
    (err body 404))

(defn web-status [data] (:web-status data))

(defn err? [data] (= :err (web-status data)))

(defn down? [data]
    (cond
        (err? data) false
        :else true))

(defn handler-chain [handlers]
    (fn [initial-request]
        (loop [head (first handlers) 
               downstream (rest handlers) 
               upstream () 
               request initial-request
               response {}
               direction :down]
            ;--- 
            (let [
                  [req res] (apply head [request response direction])]
            	(if (and (= :down direction) (seq downstream) (down? res))
                    (recur (first downstream) (rest downstream) (cons head upstream) req res :down)
                    (if (seq upstream)
                    	(recur (first upstream) () (rest upstream) req res :up)
                    	[req res]))
                ; - let
                ))))

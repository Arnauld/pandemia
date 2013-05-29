(ns pandemia.script.run-server
	(:use pandemia.webserver))

(defn -main [& args]
	(start-server {:port 8000}))
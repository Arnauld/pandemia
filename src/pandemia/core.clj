(ns pandemia.core)


(defprotocol CommandHandler
  (perform [command state]))



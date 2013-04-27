(use 'pandemia.hello-test)
(use 'clojure.test)

(Given #"^I have (\d+) apples? in my bag$" [nbApples]
	(say nbApples))
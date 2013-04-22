(use 'pandemia.hello_test)
(use 'clojure.test)

(Given #"^I have (\d+) apples? in my bag$" [nbApples]
	(say nbApples))
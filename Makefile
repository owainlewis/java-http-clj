all: test

.PHONY: test
test:
	lein test

.PHONY: deploy
deploy:
	lein deploy clojars

cljs:
	shadow-cljs -A:f3-dev:rad-dev:i18n-dev server

report:
	npx shadow-cljs run shadow.cljs.build-report main report.html

release:
	TIMBRE_LEVEL=:warn npx shadow-cljs release main

server:
	clj -A:dev:datomic -M -m com.example.components.server

uberjar:
	time clj -X:uberjar :aliases '[:datomic]' :aot true :verbose true :jar ./target/feedly-reader-standalone.jar \
			:main-class com.example.components.server

runuberjar:
	java -jar target/feedly-reader-standalone.jar	
# get all the npm deps
init:
	yarn install

cljs:
	shadow-cljs -A:f3-dev:rad-dev:i18n-dev server

cljs-watch:
	shadow-cljs -A:f3-dev:rad-dev:i18n-dev watch main

cljs-compile:
	shadow-cljs watch main

report:
	npx shadow-cljs run shadow.cljs.build-report main report.html

release:
	TIMBRE_LEVEL=:warn npx shadow-cljs release main

server:
	clj -A:dev:datomic -M -m com.example.components.server

uberjar:
	time clojure -X:uberjar :aliases '[:datomic #_:nocljs]' :aot true :jar ./target/feedly-reader-standalone.jar \
			:main-class com.example.components.server

runuberjar:
	PORT=3001 java -jar target/feedly-reader-standalone.jar	

# --machine-type=N1_HIGHCPU_8 \
#--machine-type=N1_HIGHCPU_32 \
# e2-highcpu-8
# normal is e2-medium (1vCPU, 4GB)

googlecloudbuild:
	time gcloud builds submit \
	--machine-type=e2-highcpu-8 \
	--timeout=10m \
	--config k8s/cloudbuild.yaml .


deploycloudrun:
	time gcloud run deploy feedly-reader --image us.gcr.io/booktracker-1208/feedly-reader:latest \
		--region us-central1 --platform managed --port 3000

cljs-build-prod:
	time shadow-cljs -A:f3-dev:rad-dev:i18n-dev release main


native-image:
	echo $(PATH)
	time native-image \
	         --report-unsupported-elements-at-runtime \
			 --no-fallback \
             -jar ./target/feedly-reader-standalone.jar \
             --initialize-at-build-time=com.fasterxml.jackson,org.h2 \
             --initialize-at-build-time=clojure,cheshire \
             --initialize-at-build-time=. \
             --trace-object-instantiation=com.sun.jmx.mbeanserver.JmxMBeanServer \
             --trace-class-initialization=com.sun.jmx.mbeanserver.JmxMBeanServer \
             --trace-object-instantiation=java.lang.Thread \
             --initialize-at-run-time=com.sun.jmx.mbeanserver.JmxMBeanServer,org.h2 \
             -H:IncludeResources=config/defaults.edn \
             -H:IncludeResources=config/prod.edn \
             -H:IncludeResources=public/.* \
             --allow-incomplete-classpath \
             -H:+ReportExceptionStackTraces \
             -H:+StaticExecutableWithDynamicLibC



run-uberjar-with-agent:
# 	java -agentlib:native-image-agent=caller-filter-file=filter.json,config-output-dir=. -cp $(clojure -Spath):classes refl.main
	PORT=3000 java -agentlib:native-image-agent=caller-filter-file=filter.json,config-output-dir=. \
			-jar target/feedly-reader-standalone.jar

jib-deploy:
	time clojure -T:jib-deploy jib-deploy

cloudrundeploy:
	time gcloud run deploy feedly-reader-exe --image us.gcr.io/booktracker-1208/feedly-reader-exe:latest \
		--region us-west1 --platform managed

docker-pull:
	docker pull us.gcr.io/booktracker-1208/feedly-reader-exe:latest

docker-run:
	docker run us.gcr.io/booktracker-1208/feedly-reader-exe:latest

docker-shell:
	docker run --rm -it --entrypoint /busybox/sh us.gcr.io/booktracker-1208/feedly-reader-exe
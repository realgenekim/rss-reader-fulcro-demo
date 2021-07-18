cljs:
	shadow-cljs -A:f3-dev:rad-dev:i18n-dev server

report:
	npx shadow-cljs run shadow.cljs.build-report main report.html

release:
	TIMBRE_LEVEL=:warn npx shadow-cljs release main

server:
	clj -A:dev:datomic -M -m com.example.components.server

uberjar:
	time clj -X:uberjar :aliases '[:datomic]' :aot true :jar ./target/feedly-reader-standalone.jar \
			:main-class com.example.components.server

runuberjar:
	java -jar target/feedly-reader-standalone.jar	

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
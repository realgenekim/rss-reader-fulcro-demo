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

googlecloudbuild:
	gcloud builds submit \
	 --config k8s/cloudbuild.yaml .


deploycloudrun:
	gcloud run deploy feedly-reader --image us.gcr.io/booktracker-1208/feedly-reader:latest \
		--region us-central1 --platform managed --port 3000

gcp:
	gcloud compute instances create-with-container vm2 \
    --image-family cos-dev \
    --image-project cos-cloud \
    --container-image us.gcr.io/booktracker-1208/feedly-reader:latest
(ns jib
  ;(:require [clojure.tools.build.api :as b])
  (:import
    (com.google.cloud.tools.jib.api Jib
                                    DockerDaemonImage
                                    Containerizer
                                    TarImage
                                    RegistryImage
                                    ImageReference CredentialRetriever Credential)
    (com.google.cloud.tools.jib.api.buildplan AbsoluteUnixPath)
    (com.google.cloud.tools.jib.frontend
      CredentialRetrieverFactory)
    (java.util.function Consumer)
    (java.nio.file Paths)
    (java.io File)
    (java.util List ArrayList Optional)))

; from lein-jib-build: https://github.com/vehvis/lein-jib-build
(defn- get-path [filename]
  (Paths/get (.toURI (File. ^String filename))))

(defn- into-list
  [& args]
  (ArrayList. ^List args))

(defn- to-imgref [image-config]
  (ImageReference/parse image-config))

; from JUXT pack: https://github.com/juxt/pack.alpha/blob/master/src/mach/pack/alpha/jib.clj
(defn make-logger [verbose]
  (reify Consumer
    (accept [this log-event]
      (when verbose
        (println (.getMessage log-event))))))

(def logger (make-logger true))
(def image-name "us.gcr.io/booktracker-1208/feedly-reader-exe:latest")

(def base-image-with-creds
  ; we can't run distroless, because we need /bin/bash and entrypoint.sh, until we can figure out how
  ; to set file modes to executable via jib
  ; "debug" label gives us busybox
  ;(-> (RegistryImage/named "gcr.io/distroless/base-debian11:debug"))
  (-> (RegistryImage/named "gcr.io/distroless/java:debug")
  ;(-> (RegistryImage/named "gcr.io/google-appengine/debian11"))
  ;(-> (RegistryImage/named "us.gcr.io/google-containers/alpine-with-bash:1.0")
    (.addCredentialRetriever
      (-> (CredentialRetrieverFactory/forImage
            (to-imgref image-name)
            logger)
        (.dockerConfig)))))
        ;(.wellKnownCredentialHelpers)))))

(def local-standalone-jar-path "./feedly-reader-standalone")

(def app-layer [(into-list (get-path local-standalone-jar-path))
                (AbsoluteUnixPath/get "/")])

(def entrypoint ["/busybox/sh" "entrypoint.sh"])

(def arguments local-standalone-jar-path)

;(def image-name "us.gcr.io/booktracker-1208/pubsub-web-jib-test:latest")
;(def image-name "us.gcr.io/booktracker-1208/pubsub-web:latest")


(defn jib-deploy [_]
  (time (-> (Jib/from base-image-with-creds)
          ; keys
          (.addLayer (into-list (get-path "./bin/entrypoint.sh"))
                     (AbsoluteUnixPath/get "/"))
          ; jar file
          (.addLayer (first app-layer) (second app-layer))
          (.setEntrypoint (apply into-list entrypoint))
          (.setProgramArguments (into-list arguments))
          (.containerize
            (Containerizer/to
              (->
                (RegistryImage/named
                  (to-imgref image-name))
                (.addCredentialRetriever
                  (-> (CredentialRetrieverFactory/forImage
                        (to-imgref image-name)
                        logger),
                    ;(.dockerCredentialHelper "/Users/genekim/software/google-cloud-sdk/bin/docker-credential-gcloud")
                    (.dockerConfig)))))))))
                    ;(wellKnownCredentialHelpers)))))))))



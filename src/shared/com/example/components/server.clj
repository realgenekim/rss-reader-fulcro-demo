(ns com.example.components.server
  (:require
    [com.example.components.config :refer [config]]
    [com.example.components.ring-middleware :refer [middleware]]
    [mount.core :as mount :refer [defstate]]
    [org.httpkit.server :refer [run-server]]
    [ring.util.response :as response]
    [taoensso.timbre :as log])
  (:gen-class))

(require 'ring.middleware.multipart-params.temp_file) ;; preload for native-image

;; graalvm has its own resource scheme. if the content length is zero, we assume
;; it's a directory and return nil, like ring's own jar resource handling
(defmethod response/resource-data :resource
  [^java.net.URL url]
  ;; GraalVM resource scheme
  (let [resource (.openConnection url)
        len (#'ring.util.response/connection-content-length resource)]
    (when (pos? len)
      {:content        (.getInputStream resource)
       :content-length len
       :last-modified  (#'ring.util.response/connection-last-modified resource)})))

(defstate http-server
  :start
  (let [cfg     (get config :org.httpkit.server/config)
        stop-fn (run-server middleware cfg)]
    (log/info "Starting webserver with config " cfg)
    {:stop stop-fn})
  :stop
  (let [{:keys [stop]} http-server]
    (when stop
      (stop))))

;; This is a separate file for the uberjar only. We control the server in dev mode from src/dev/user.clj
(defn -main [& args]
  (mount/start-with-args {:config "config/prod.edn"}))

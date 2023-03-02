(ns dev
  (:require
    [clojure.test :refer :all]
    ;[nextjournal.clerk :as clerk]
    [flow-storm.api :as fs-api]
    [portal.api :as p]))


(comment
  (do
    ;(plog/configure-logging!)
    ;(def po (p/open))
    (def po (p/open {:launcher :intellij}))
    (add-tap #'p/submit)

    (tap> {:a 1}))


  (do
    (fs-api/local-connect)
    0)


 0)


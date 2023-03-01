(ns genek.core-test
  (:require
    [clojure.test :as t :refer :all]
    [com.example.membrane-ui.client :as client]
    [clojure.spec.alpha :as s]))

(deftest a-test
  (testing "FIXME, I fail."
    (is (= 1 1))))

(deftest client-http

  (let [app (client/create-client)]

    (def app app)

    (is (fn?
          (-> app :com.fulcrologic.fulcro.application/runtime-atom deref :com.fulcrologic.fulcro.application/remotes
            :remote :transmit!))))



  0)
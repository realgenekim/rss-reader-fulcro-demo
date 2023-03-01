(ns genek.core-test
  (:require
    [clojure.test :as t :refer :all]
    [clojure.spec.alpha :as s]
    [com.example.membrane-ui.client :as client]
    [com.example.ui.stories-forms :as stories]
    [com.fulcrologic.fulcro.data-fetch :as df]))

(deftest a-test
  (testing "FIXME, I fail."
    (is (= 1 1))))

(deftest client-http

  (let [app (client/create-client)]
    (def app app)

    (is (fn?
          (-> app :com.fulcrologic.fulcro.application/runtime-atom deref :com.fulcrologic.fulcro.application/remotes
            :remote :transmit!)))

    (let [retval (df/load! app :story/first-page-stories stories/Story
                   {:target        [:component/id ::StoriesMain :ui/all-stories]})]

      (println retval)))



  0)
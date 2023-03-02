(ns com.example.membrane-ui.ui
  (:require
    [com.fulcrologic.fulcro.application :as app]
    [com.example.membrane-ui.client :as c]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.mutations :refer [defmutation] :as mut]
    [com.fulcrologic.fulcro.algorithms.lookup :as ah]
    [com.fulcrologic.fulcro.algorithms.indexing :as indexing]
    [com.fulcrologic.fulcro.dom :as dom]
    [com.fulcrologic.fulcro.inspect.inspect-client :as inspect]
    [com.fulcrologic.fulcro.algorithms.merge :as merge]
    [com.fulcrologic.fulcro.algorithms.denormalize :as fdn]
    [com.fulcrologic.fulcro.algorithms.tx-processing.synchronous-tx-processing :as stx]
    [membrane.ui :as ui]
    [membrane.basic-components :as basic]
    membrane.component
    [membrane.fulcro :as mf
     :refer [uuid
             component->view
             show!
             show-sync!]]
    [membrane.skia :as skia]))


(comment
  c/app

  0)
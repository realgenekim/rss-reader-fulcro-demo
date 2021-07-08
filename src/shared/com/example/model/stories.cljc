(ns com.example.model.stories
  (:require
    [com.fulcrologic.rad.attributes :refer [defattr]]
    [com.fulcrologic.rad.attributes-options :as ao]
    [com.fulcrologic.fulcro.components :refer [defsc]]
    #?(:clj [com.example.components.database-queries :as queries])
    [com.fulcrologic.rad.report-options :as ro]
    [com.wsscode.pathom.connect :as pc]
    [taoensso.timbre :as log]))


(defattr id :story/id :string
  {ao/identity? true})

(defattr title :story/title :string
  {ao/required?                                          true
   ao/identities                                         #{:story/id}})

(defattr author :story/author :string
  {ao/required?                                          true
   ao/identities                                         #{:story/id}})

(defattr content :story/content :string
  {ao/required?                                          true
   ao/identities                                         #{:story/id}})

(defattr pos :story/pos :number
  {ao/required?                                          true
   ao/identities                                         #{:story/id}})


(def attributes [id title author content])


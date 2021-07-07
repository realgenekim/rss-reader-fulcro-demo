(ns com.example.model.stories
  (:require
    [com.fulcrologic.rad.attributes :refer [defattr]]
    [com.fulcrologic.rad.attributes-options :as ao]
    [com.fulcrologic.fulcro.components :refer [defsc]]
    #?(:clj [com.example.components.database-queries :as queries])
    [com.fulcrologic.rad.report-options :as ro]
    [com.wsscode.pathom.connect :as pc]))


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


(defattr all-stories :story-list/all-stories :ref
  {ao/target     :story/id
   ao/pc-output  [{:story-list/all-stories [:story-list/id]}]
   ao/pc-resolve (fn [{:keys [query-params] :as env} _]
                   #?(:clj
                      {:story-list/all-stories (queries/get-all-stories env query-params)}))})

(def attributes [id title author content all-stories])


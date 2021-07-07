(ns com.example.model.story-list
  (:require
    [com.fulcrologic.rad.attributes :refer [defattr]]
    [com.fulcrologic.rad.attributes-options :as ao]
    [com.fulcrologic.fulcro.components :refer [defsc]]
    #?(:clj [com.example.components.database-queries :as queries])
    [com.fulcrologic.rad.report-options :as ro]
    [com.wsscode.pathom.connect :as pc]))


(defattr id :story-list/id :string
  {ao/identity? true})

(defattr title :story-list/title :string
  {ao/required?                                          true
   ao/identities                                         #{:story-list/id}})

(defattr author :story-list/author :string
  {ao/required?                                          true
   ao/identities                                         #{:story-list/id}})

(defattr content :story-list/content :string
  {ao/required?                                          true
   ao/identities                                         #{:story-list/id}})

(defattr all-stories :story-list/all-stories :ref
  {ao/target     :story/id
   ao/pc-output  [{:story-list/all-stories [:story-list/id]}]
   ao/pc-resolve (fn [{:keys [query-params] :as env} _]
                   #?(:clj
                      {:story-list/all-stories (queries/get-all-stories env query-params)}))})

#?(:clj
   (do
     (pc/defresolver story-id-resolver [env {:story-list/keys [id]}]
       {::pc/input  #{:story-list/id}
        ::pc/output [:story/id :story/author :story/content]}
       (println "*** story-id-resolver: ")
       (dissoc (queries/get-story-by-id env id)
               :content))

     (pc/defresolver full-story-resolver [env {:full-story/keys [id]}]
       {::pc/input  #{:full-story/id}
        ::pc/output [:full-story/id :full-story/author :full-story/content :full-story/title]}
       (println "*** full-story-resolver: ")
       (let [retval (queries/get-full-story-by-id env id)]
         (println retval)
         retval))))


(def attributes [id title author content all-stories])

#?(:clj
   (def resolvers [story-id-resolver full-story-resolver]))
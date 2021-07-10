(ns com.example.model.story-list
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

(defattr all-stories :story/all-stories :ref
  {ao/target     :story/id
   ao/pc-output  [{:story/all-stories [:story/id]}]
   ao/pc-resolve (fn [{:keys [query-params] :as env} _]
                   #?(:clj
                      (let [stories (->> (queries/get-all-stories env query-params))]
                        (log/warn ":story/all-stories" stories)
                        (log/warn stories)
                        {:story/all-stories stories})))})

#?(:clj
   (do
     ;(pc/defresolver story-id-resolver [env {:story/keys [id]}]
     ;  {::pc/input  #{:story/id}
     ;   ::pc/output [:story/id :story/author :story/content]}
     ;  (println "*** story-id-resolver: ")
     ;  (dissoc (queries/get-story-by-id env id)
     ;          :content))

     (pc/defresolver story-resolver [env {:story/keys [id]}]
       {::pc/input  #{:story/id}
        ::pc/output [:story/id :story/author :story/content :story/title]}
       (println "*** story-resolver: ")
       (let [retval (queries/get-full-story-by-id env id)]
         (println retval)
         retval))))


(def attributes [id title author content pos all-stories])

#?(:clj
   (def resolvers [story-resolver])) ;full-story-resolver]))
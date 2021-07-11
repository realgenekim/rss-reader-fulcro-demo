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



(defn query-output-shape [env]
  (let [out (:com.wsscode.pathom.core/parent-query env)]
    (log/debug "examine query: " (:com.wsscode.pathom.core/parent-query env))
    out))


(defattr all-stories :story/all-stories :ref
  {ao/target     :story/id
   ao/pc-output  [{:story/all-stories [:story/id :story/author :story/title :story/published]}]
   ao/pc-resolve (fn [{:keys [query-params] :as env} _]
                   #?(:clj
                      (let [stories (->> (queries/get-all-stories env query-params))]
                        (log/warn ":story/all-stories")
                        ;(log/warn stories)
                        {:story/all-stories stories})))})


#?(:clj
   (do
     ;(pc/defresolver story-resolver [env {:story/keys [id]}]
     ;  {::pc/input  #{:story/id}
     ;   ::pc/output [:story/id :story/author :story/title]}
     ;  (log/warn "*** story-resolver: ")
     ;  (let [retval (->> (queries/get-story-by-id env id))]
     ;    ;(println retval)
     ;    retval))



     (pc/defresolver story-content-resolver [env input]
       {::pc/input  #{:story/id}
        ::pc/output [:story/id :story/author :story/title :story/published :story/content]
        ::pc/batch? true}
       ;(log/warn "*** story-content-resolver: ")
       ;(log/warn "*** story-content-resolver: " (:ast env))
       (let [output-shape (query-output-shape env)]
         ;(tap> env)
         ;(log/warn "*** story-content-resolver: ENV: " (with-out-str (clojure.pprint/pprint env)))
         ; [:story/id :story/author :story/title :story/content]
         ;(log/warn "*** story-content-resolver: pc-out: " output-shape)
         ;(let [retval (queries/get-full-story-by-id env id)]
         ;  ;(println retval)
         ;  retval)
         (if (sequential? input)
           (do
             ;(println retval)
             (log/warn "***     story-content-resolver: BATCH")
             (mapv (fn [v]
                     (let [retval (queries/get-story-by-id-shaped env (:story/id v) output-shape)]
                       (log/spy :warn v)
                       retval))
                   input))
           ; else
           (do
             (log/warn "***     story-content-resolver: single")
             (let [retval (queries/get-story-by-id-shaped env (:story/id input) output-shape)]
               (log/debug retval)
               retval)))))))


(def attributes [id title author content pos all-stories])

#?(:clj
   (def resolvers [ story-content-resolver])) ; story-resolver full-story-resolver]))
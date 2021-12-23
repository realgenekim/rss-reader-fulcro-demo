(ns com.example.model.story-list
  (:require
    [com.fulcrologic.rad.attributes :refer [defattr]]
    [com.fulcrologic.rad.attributes-options :as ao]
    [com.fulcrologic.fulcro.components :refer [defsc]]
    #?(:clj [com.example.components.database-queries :as queries])
    [com.fulcrologic.rad.report-options :as ro]
    [com.wsscode.pathom.core :as p]
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

(defattr first-page-stories :story/first-page-stories :ref
  {ao/target     :story/id
   ao/pc-output  [{:story/first-page-stories [:story/id :story/author :story/title :story/published]}]
   ao/pc-resolve (fn [{:keys [query-params] :as env} _]
                   #?(:clj
                      (let [stories (->> (queries/get-all-stories env query-params)
                                         (take 30))]
                        (log/warn :story/first-page-stories)
                        ;(log/warn stories)
                        {:story/first-page-stories stories})))})


#?(:clj
   (do

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
         (time
          (if (sequential? input)
            (do
              ;(println retval)
              (log/warn "***     story-content-resolver: BATCH")
              (mapv (fn [v]
                      (let [retval (queries/get-story-by-id-shaped env (:story/id v) output-shape)]
                        (log/spy :debug v)
                        retval))
                input))
            ; else
            (do
              (log/warn "***     story-content-resolver: single")
              (let [retval (queries/get-story-by-id-shaped env (:story/id input) output-shape)]
                (log/debug retval)
                retval))))))

     (defn contains-string?
       [text substr]
       (if-not text
         false
         (boolean (re-find (re-pattern (str "(?i)" substr)) text))))

     (comment
       (contains-string? "bandband" "BAND")
       ,)

     (defn story-matches?
       [story search-text]
       (or
         (contains-string? (:story/author story) search-text)
         (contains-string? (:story/title story) search-text)
         (contains-string? (:story/content story) search-text)))

     (comment
       (story-matches? {:story/author "Gene" :story/title "clojure" :story/content "abc"}
         "ABC")

       ()
       ,)

     (defn handle-search
       " pull out of resolver, so we don't need to restart server "
       [env]
       (tap> env)
       (let [search-text     (-> env :ast :params :search/search-query)
             _               (log/warn "handle-search! search-text2: " search-text)
             parser          (resolve 'com.example.components.parser/parser)
             config          (resolve 'com.example.components.config/config)
             all-stories     (->> (parser @config
                                    [{:story/all-stories [:story/id :story/author :story/title :story/content]}])
                               :story/all-stories)
             ;_ (println all-stories)
             ;_ (tap> all-stories)
             matched-stories (->> all-stories
                               (filter #(story-matches? % search-text)))
             matched         (count matched-stories)
             _               (log/warn "count: matched: " matched)
             retval          {:search-results/matched matched
                              :search-results/search-text search-text
                              :search-results/stories matched-stories}]
         (tap> retval)
         retval))

     (pc/defresolver story-search-resolver [env _]
       {; no ::pc/input
        ::pc/output [;:search-results/search-text
                     ;:search-results/matched
                     {:search-results/stories
                      [:story/id :story/title :story/author :story/content]}]}
       (do
         (log/warn "*** story-search-resolver")
         (handle-search env)))))

(comment

  (com.example.components.parser/parser com.example.components.config/config
    [{[:story/id "K3Y7GLlRfaBDsUWYD0WuXjH/byGbQnwaMWp+PEBoUZw=_13ef0cdbc18:15c0fac:70d63bab"]
      [:story/author :story/id :story/title :story/published]}])

  (com.example.components.parser/parser com.example.components.config/config
    [:story/all-stories])

  (com.example.components.parser/parser com.example.components.config/config
    [{:story/all-stories [:story/id :story/author :story/title :story/content]}])

  (com.example.components.parser/parser com.example.components.config/config
    [:search-results/stories])

  (com.example.components.parser/parser com.example.components.config/config
    ['(:search-results/stories {:search/search-query "gene kim"})])

  (com.example.components.parser/parser com.example.components.config/config
    [{'(:search-results/stories {:search/search-query "search text abc!"})
      [:search-results/matched :search-results/search-text]}])

  (com.example.components.parser/parser com.example.components.config/config
    ['(:search-results/stories {:search/search-query "re-frame"})
     [:search-results/matched :search-results/search-text
      {:search-results/stories [:story/author]}]])

  (->> (com.example.components.parser/parser com.example.components.config/config
         [{'(:search-results/stories {:search-text "gene kim"})
           [:story/id :story/title]}])
       :search-results/stories
       count)


  ,)



(def attributes [id title author content pos
                 all-stories first-page-stories])

#?(:clj
   (def resolvers [ story-content-resolver story-search-resolver])) ; story-resolver full-story-resolver]))
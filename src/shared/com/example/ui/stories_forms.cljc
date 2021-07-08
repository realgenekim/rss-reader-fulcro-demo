(ns com.example.ui.stories-forms
  (:require
    [com.example.model.story-list :as story-list]
    [com.example.model.stories :as story]
    [com.fulcrologic.rad.form-options :as fo]
    [com.fulcrologic.rad.form :as form]
    [com.fulcrologic.rad.report :as report]
    [com.fulcrologic.rad.report-options :as ro]
    #?(:clj  [com.fulcrologic.fulcro.dom-server :as dom :refer [div label input]]
       :cljs [com.fulcrologic.fulcro.dom :as dom :refer [div label input]])
    [com.fulcrologic.fulcro.mutations :as mutation :refer [declare-mutation]]
    [com.fulcrologic.fulcro.components :as comp]
    [com.example.model.mutations :as m]
    [com.fulcrologic.fulcro.data-fetch :as df]
    [com.fulcrologic.fulcro.algorithms.denormalize :as fdn]
    [com.fulcrologic.fulcro.algorithms.merge :as merge]))


(declare-mutation set-story 'com.example.model.mutations/set-story)
(declare-mutation get-story 'com.example.model.mutations/get-story)



(comp/defsc FullStory [_ {:full-story/keys [id author title content]
                          :as props}]
  {:query [:full-story/id :full-story/author :full-story/content :full-story/title :full-story/pos]
   :ident :full-story/id}
  ;(println "FullStory: props: " props)
  ;(println "FullStory: content: " content)
  (dom/div :.ui.segment
    (dom/h2 "Full Current Story: ")
    (dom/h3 author)
    (dom/h3 title)
    ;(dom/h3 id)
    ;(dom/p  content)
    (dom/div {:dangerouslySetInnerHTML
              {:__html content}}))) ;"<strong> hello! </strong"}})))


(def ui-full-story (comp/factory FullStory {:keyfn :story/id}))

#?(:cljs
   (def format goog.string.format))


(defn save-current-story-pos!
  [this {:story-list/keys [id pos]
         :as story}]
  (println "save-current-story-pos! story:" story)
  (let [state* (->> com.example.client/app
                    (:com.fulcrologic.fulcro.application/state-atom))]
    (reset! state* (assoc-in @state* [:ui/current-position] pos))))

(defn load-full-story!
  [this {:story-list/keys [id pos]
         :as story}]
  (println "load-full-story! story: " story)
  (df/load! this [:full-story/id id]
            FullStory {:target [:current-story]}))


(comp/defsc Story [this {:story-list/keys [id author title pos]
                         :as params}]
  {:query [:story-list/id :story-list/author :story-list/title :story-list/pos]
   :ident :story-list/id}
  (dom/li
    (println params)
    (dom/a {:href "#!"
            :onClick (fn [x]
                       (println "Story: link: id: " id)
                       (load-full-story! this params)
                       (save-current-story-pos! this params))}
      (format "%d. %s (%s)" pos title author))))

(def ui-story (comp/factory Story {:keyfn :story-list/id}))

(comment
  (reset! state)
  (reset! state* (assoc-in @state* [:ui/current-position] 5))

  (fdn/db->tree SelectedStory {:a [:b 1]})
  (fdn/db->tree  [:story-list/id] state state)
  (fdn/db->tree  [:story-list/id :story-list/author] state state)

  (merge/merge!
    com.example.client/app
    {:current-story {:full-story/id      "abc"
                     :full-story/author  "Gene"
                     :full-story/title   "title"
                     :full-story/content "XXS"}}
    (comp/get-query FullStory))

  (merge/merge!
    com.example.client/app
    {:current-story [{[:full-story/id "K3Y7GLlRfaBDsUWYD0WuXjH/byGbQnwaMWp+PEBoUZw=_13ef0cdbc18:15c0fac:70d63bab"]
                      [:story/id :story/author :story]}]}
    (comp/get-query FullStory))

  ; these work!  setting the :full-story/id triggers all the pathom resolvers!

  (df/load! com.example.client/app [:full-story/id "K3Y7GLlRfaBDsUWYD0WuXjH/byGbQnwaMWp+PEBoUZw=_13ef0cdbc18:15c0fac:70d63bab"]
            FullStory {:target [:current-story]})
  (df/load! com.example.client/app [:full-story/id "K3Y7GLlRfaBDsUWYD0WuXjH/byGbQnwaMWp+PEBoUZw=_16f2e23f4d5:155f3ef:69b9f616"]
            FullStory {:target [:current-story]})


  ,)




(report/defsc-report StoriesCustom [this {:ui/keys [current-rows parameters]
                                          :as props}]
  {ro/title            "Stories List"
   ro/source-attribute :story-list/all-stories
   ; this is a link query
   ro/query-inclusions [{[:current-story '_] (comp/get-query FullStory)}]
   ;ro/query-inclusions [{:current-story (comp/get-query FullStory)}]
   ro/row-pk           story-list/id
   ro/columns          [story-list/id story-list/author story-list/title story-list/pos]

   ro/run-on-mount?    true
   ro/route            "stories"}
  (dom/div
    (dom/div
      (dom/p "Current story: " (:current-story-pos/pos props)))
    (dom/div
      ;(println current-rows)
      ;(println "current story: " (:current-story props))
      (println "props: " props)
      ;(println (keys props))
      (println this))

    (dom/div :.ui.grid
      (div :.row
        (div :.eight.wide.column
          (dom/ul :.ui.segment
            (map ui-story current-rows)))
        (div :.eight.wide.column
          (ui-full-story (:current-story props)))))))


(comment
  (comp/get-query SelectedStory)
  (comp/get-query StoriesCustom)

  ,)




;(comp/defsc Root7 [this {stories :stories
;                         current-story :current-story
;                         :as props}]
;       {:query [{:stories (comp/get-query Story)}
;                {:current-story (comp/get-query FullStory)}
;                [df/marker-table :teams]]}
;       (dom/div
;         (dom/button {:type    "button"
;                      :onClick (fn [x]
;                                 (println "df/load! the data from here")
;                                 (comp/transact! this '[(get-story {:story/id 1})])
;                                 (df/load! this :current-story Story {:story-list/id 1}))}
;                 "Load Mutation...")
;         (let [loading? (get props [df/marker-table :teams])]                                  ; scaffolding for TASK 5
;           (cond
;             loading? (dom/p "Loading...")
;             ;; ...
;             :else
;             (comp/fragment (dom/p ""))))
;
;         (map ui-story stories)
;
;         (ui-full-story current-story)))

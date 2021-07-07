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

;(form/defsc-form AddressForm [this props]
;  {fo/id           address/id
;   fo/attributes   [address/street address/city address/state address/zip]
;   fo/cancel-route ["landing-page"]
;   fo/route-prefix "address"
;   fo/title        "Edit Address"
;   fo/layout       [[:address/street]
;                    [:address/city :address/state :address/zip]]})

(report/defsc-report StoryReport [this props]
  {ro/title            "Story"
   ro/source-attribute :story/id
   ro/row-pk           story/id
   ro/columns          [story/author story/title story/content]
   ;ro/column-headings  {:invoice/id "Invoice Number"}

   ;ro/form-links       {:invoice/id InvoiceForm}
   ;ro/controls         {:account/id {:type   :uuid
   ;                                  :local? true
   ;                                  :label  "Account"}}
   ;; No control layout...we don't actually let the user control it

   ro/run-on-mount?    true
   ro/route            "story"})
  ;(dom/div :.ui.container.grid
  ;    "Hello!"
  ;    "hello"
  ;     this))
      ;(-> this comp/props )))



(report/defsc-report StoriesListReport [this props]
  {ro/title             "Stories List"
   ro/source-attribute  :story-list/all-stories
   ro/row-pk            story-list/id
   ro/columns           [story-list/id story-list/author story-list/title]
   ro/column-formatters {:story-list/id
                         (fn [this v]
                           (dom/a {:onClick (fn [x]
                                              (println (-> this comp/props))
                                              (form/edit! this
                                                          StoryReport
                                                          {:story/id (-> this comp/props :story-list/id)}))}
                                  (subs (str v) 0 4)))}
   ;ro/column-headings  {:invoice/id "Invoice Number"}

   ;ro/form-links       {:invoice/id InvoiceForm}
   ;ro/controls         {:account/id {:type   :uuid
   ;                                  :local? true
   ;                                  :label  "Account"}}
   ;; No control layout...we don't actually let the user control it

   ro/run-on-mount?     true
   ro/route             "story-list"})

(declare-mutation set-story 'com.example.model.mutations/set-story)
(declare-mutation get-story 'com.example.model.mutations/get-story)

(comp/defsc FullStory [_ {:full-story/keys [id author title content]
                          :as props}]
  {:query [:full-story/id :full-story/author :full-story/content  :full-story/title]
   :ident :full-story/id}
  ;{:query [:current-story]
  ; :ident :current-story}
  ;(println "FullStory: current-story: " current-story)
  (println "FullStory: props: " props)
  (println "FullStory: content: " content)
  (dom/div (dom/h2 "Full Current Story: " (:full-story/content props)
                   (dom/p (str (:full-story/id props)
                               (:full-story/author props)
                               (:full-story/title props)
                               (:full-story/content props)))
                   (dom/div {:dangerouslySetInnerHTML {:__html "<strong> hello! </strong"}}))))

(def ui-full-story (comp/factory FullStory {:keyfn :story/id}))

(comp/defsc SelectedStory [_ params]
  {:query [:full-story/id :story/id :story/author :story/title :story/content]
   :ident :full-story/id}
  ;{:query [:current-story]
  ; :ident :current-story}
  ;(println "FullStory: current-story: " current-story)
  (println "SelectedStory: params: " params)
  (dom/div (dom/h2 "SelectedStory Current Story: " (:story/content params)
                   (dom/p (str (:story/id params)
                               (:story/author params)
                               (:story/title params)
                               (:story/content params)))
                   (dom/div {:dangerouslySetInnerHTML {:__html "<strong> hello! </strong"}}))))

(def ui-selected-story (comp/factory SelectedStory {:keyfn :story/id}))


(comp/defsc Story [this {:story-list/keys [id author title]
                         :as params}]
       {:query [:story-list/id :story-list/author :story-list/title]
        :ident :story-list/id}
       (dom/div (dom/h2 "Story: " title)
            (dom/p (str id author title))
            (dom/p (str params))
            (dom/button {:type    "button"
                         :onClick (fn [x]
                                    ;(comp/transact! this [(set-story {:story-list/id (:story-list/id params)})])
                                    (df/load! this :current-story SelectedStory {:full-story/id (:story-list/id params)}))}
                    "Set Current Story")))

(def ui-story (comp/factory Story {:keyfn :story-list/id}))

(comment
  (df/load! com.example.client/app [{[:full-story/id "K3Y7GLlRfaBDsUWYD0WuXjH/byGbQnwaMWp+PEBoUZw=_13ef0cdbc18:15c0fac:70d63bab"]
                                     [:story/author :story/content :story/id :story/title]}]
            SelectedStory {:target [:current-story]})

  (def state (->> com.example.client/app
                  (:com.fulcrologic.fulcro.application/state-atom)
                  (deref)
                  :current-story))
  state

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

  ; (df/load! app [:person/id 22] Person)

  (df/load! com.example.client/app [:full-story/id "K3Y7GLlRfaBDsUWYD0WuXjH/byGbQnwaMWp+PEBoUZw=_13ef0cdbc18:15c0fac:70d63bab"]
            FullStory {:target [:current-story]})

  (df/load! com.example.client/app :current-story FullStory {:full-story/id "K3Y7GLlRfaBDsUWYD0WuXjH/byGbQnwaMWp+PEBoUZw=_16f4240bb12:f7de18:dcfbed0f"})


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


(report/defsc-report StoriesCustom [this {:ui/keys [current-rows parameters]
                                          :as props}]
  {ro/title            "Stories List"
   ro/source-attribute :story-list/all-stories
   ; this is a link query
   ro/query-inclusions [{[:current-story '_] (comp/get-query FullStory)}]
   ;ro/query-inclusions [{:current-story (comp/get-query FullStory)}]
   ro/row-pk           story-list/id
   ro/columns          [story-list/id story-list/author story-list/title]

   ro/run-on-mount?    true
   ro/route            "stories"}
  (dom/div
    (println current-rows)

    (println "current story: " (:current-story props))
    (println "props: " props)
    (dom/p "Hello 2")
    (map ui-story current-rows)
    (println (keys props))
    (println this)
    (ui-full-story (:current-story props))))



(comment
  (comp/get-query SelectedStory)
  (comp/get-query StoriesCustom)

  ,)



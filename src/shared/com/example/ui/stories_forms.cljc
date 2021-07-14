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
    ;[com.example.model.mutations :as m]
    [com.fulcrologic.fulcro.data-fetch :as df]
    [com.fulcrologic.fulcro.algorithms.denormalize :as fdn]
    [com.fulcrologic.fulcro.algorithms.merge :as merge]
    [com.fulcrologic.fulcro.application :as app]
    [com.fulcrologic.rad.type-support.date-time :as datetime]
    [com.fulcrologic.fulcro.raw.components :as rc]
    [com.fulcrologic.fulcro.mutations :as m]
    #?(:cljs [goog.string :as gstring])
    #?(:cljs [portal.web :as pw])))

#?(:cljs
   (def format goog.string.format))

(declare-mutation bottom-story 'com.example.model.mutations/bottom-story)
(declare-mutation top-story 'com.example.model.mutations/top-story)

(comp/defsc FullStory [_ {:story/keys [id author title content published]
                          :as props}]
  {:query [:story/id :story/author :story/content :story/title :story/published]
   :ident :story/id}
  ;(println "FullStory: props: " props)
  ;(println "FullStory: content: " content)
  (dom/div :.ui.segment
    (dom/h3 "Full Current Story: ")
    ;(println "published: " published)
    (dom/div :.list
      (dom/div :.item (dom/b (str "Author: " author)))
      (dom/div :.item (dom/b (str "Title: " title)))
      (dom/div :.item (dom/b (str "Published: " (if published
                                                  (datetime/inst->html-date (datetime/new-date published))
                                                  "---")))))
    (dom/p)
    ; content has embedded html
    ;    e.g., "<strong> hello! </strong"}})))
    (dom/div {:dangerouslySetInnerHTML
              {:__html content}})))

(def ui-full-story (comp/factory FullStory {:keyfn :story/id}))


(comp/defsc Story [this {:story/keys [id author title published]
                         :ui/keys [number]
                         :as params}
                   ; computed-factory: adds third argument
                   ; otherwise, component will disappear if you don't re-render parent
                   {:keys [on-select selected]}]
  ; change all to :story/id
  {:query [:story/id :story/author :story/title :story/published :ui/number]
   :ident :story/id}
  (dom/div :.item #_{:classes [(when (= id (:story/id selected))
                                 "right triangle icon")]}
    {:id (str "story-" id)}
    ;(println params)
    (dom/a {:href "#!"
            :onClick (fn [_]
                       (when on-select
                         (on-select id)))}
           (let [text (format "%d. %s (%s)" number title author)]
             (if (= id (:story/id selected))
               (dom/strong text)
               text)))))


(def ui-story (comp/computed-factory Story {:keyfn :story/id}))

(comment
  (reset! state)
  (reset! state* (assoc-in @state* [:ui/current-position] 5))

  (fdn/db->tree SelectedStory {:a [:b 1]})
  (fdn/db->tree [:story/id] state state)
  (fdn/db->tree [:story/id :story/author] state state)

  (merge/merge!
    com.example.client/app
    {:current-story {:story/id      "abc"
                     :story/author  "Gene"
                     :story/title   "title"
                     :story/content "XXS"}}
    (comp/get-query FullStory))

  (merge/merge!
    com.example.client/app
    {:current-story [{[:story/id "K3Y7GLlRfaBDsUWYD0WuXjH/byGbQnwaMWp+PEBoUZw=_13ef0cdbc18:15c0fac:70d63bab"]
                      [:story/id :story/author :story]}]}
    (comp/get-query FullStory))

  ; these work!  setting the :story/id triggers all the pathom resolvers!

  (df/load! com.example.client/app [:story/id "K3Y7GLlRfaBDsUWYD0WuXjH/byGbQnwaMWp+PEBoUZw=_13ef0cdbc18:15c0fac:70d63bab"]
            FullStory {:target [:current-story]})
  (df/load! com.example.client/app [:story/id "K3Y7GLlRfaBDsUWYD0WuXjH/byGbQnwaMWp+PEBoUZw=_16f2e23f4d5:155f3ef:69b9f616"]
            FullStory {:target [:current-story]})


  ,)

(comp/defsc CurrentPosition
  [this {:ui/keys [current-position]
         :as params}]
  {:query         [:ui/current-position]
   :initial-state {:ui/current-position 0}}
  (dom/div
    ;(println "CurrentPosition: params: " params)
    (println "CurrentPosition: current-position: " current-position)
    (dom/p "Current story number: " current-position)))

(def ui-current-position (comp/factory CurrentPosition))

#_(report/defsc-report StoriesCustom [this {:ui/keys [current-rows parameters]
                                            :as props}]
    {ro/title            "Stories List"
     ro/source-attribute :story/all-stories
     ; this is a link query
     ro/query-inclusions [
                          ;{[:current-story '_] (comp/get-query FullStory)}
                          ;{[:ui/current-position '_] (comp/get-query CurrentPosition)}
                          :ui/current-position]

     ;ro/query-inclusions [{:current-story (comp/get-query FullStory)}]
     ro/row-pk           story-list/id
     ro/columns          [story-list/id story-list/author story-list/title story-list/pos]

     ro/run-on-mount?    true
     ro/route            "stories"}
    ;(let [state* @(->> com.example.client/app (:com.fulcrologic.fulcro.application/state-atom))])
    (dom/div
      (ui-current-position props)
      ;(dom/div
      ;  (println "StoriesCustom: state*: " state*)
      ;  (dom/p "Current story: " (:ui/current-position state*)))
      (dom/div
        ;(println current-rows)
        ;(println "current story: " (:current-story props))
        (println "StoriesCustom: props: " props))
      ;(println (keys props))

      (dom/div :.ui.grid
               (div :.row
                    (div :.eight.wide.column
                         (dom/ul :.ui.segment
                                 (map ui-story current-rows)))
                    (div :.eight.wide.column
                         (ui-full-story (:current-story props)))))))

(comp/defsc StoryNum
  [this {:ui/keys [all-stories current-story]
         :as params}]
  {:query             [{:ui/all-stories (comp/get-query Story)}
                       {:ui/current-story (comp/get-query FullStory)}]}
   ;:ident (fn [x] [:component/id ::StoryNum])}
  (let [idx     (map-indexed (fn [idx itm] [itm idx]) all-stories)
        ;_       (println idx)
        ; ^^ appends index to end [[id "xxx"] 1..n]
        thisone (->> idx
                  (filter (fn [x]
                            (= (:story/id (first x))
                               (:story/id current-story)))))
        n       (->> thisone first second)]
    ;(println "StoryNum: thisone: " thisone)
    (println "StoryNum: n: " n)
    ;(println "all-stories: " all-stories)
    ;(println "current story:" current-story)
    ;(println "params: " params)
    (dom/p (format "Current story: %d of %d" n (count all-stories)))))


(def ui-story-num (comp/factory StoryNum))


(mutation/defmutation set-mode [mode]
  (action [{:keys [state]}]
    (println "mutation: set-mode: params: " mode)
    ;(do
    ;  (println "bump-number")
    (swap! state assoc-in [:ui/mode] mode)))


(comp/defsc StoriesCustom
  [this {:ui/keys [all-stories current-story]
         :as props}]
  {:query             [{:ui/all-stories (comp/get-query Story)}
                       {:ui/current-story (comp/get-query FullStory)}]
   :ident             (fn [x] [:component/id ::StoriesCustom])
   :initial-state     {:ui/all-stories []}
   :route-segment     ["Stories"]
   :componentDidMount (fn [this]
                        (comp/transact! this [(set-mode {:mode :main})])
                        (df/load! this :story/all-stories Story
                                  {:target [:component/id ::StoriesCustom :ui/all-stories]
                                   :post-mutation 'com.example.model.mutations/top-story}))}
  (dom/div
    (dom/h2 "All Stories")
    (ui-story-num {:ui/all-stories all-stories
                   :ui/current-story current-story})

    (dom/div
      ; (ui-current-position props)
      (dom/div :.ui.grid
        (dom/div :.row
          (dom/div :.five.wide.column
            (dom/div :.ui.selection.list.vertical-scrollbar.segment
              (map-indexed (fn [idx story]
                             (ui-story (merge story {:ui/number idx})
                               {:on-select
                                  (fn [story-id]
                                    ; ; ident: [:story/id story-id]
                                    ; it's what triggers the resolver, it's how you access
                                    ; local client database
                                    (println "on-select: triggered: " story-id)
                                    (df/load! this [:story/id story-id] FullStory
                                        {:target [:component/id ::StoriesCustom :ui/current-story]}))
                                :selected current-story})) all-stories)))
          (dom/div :.eleven.wide.column
            (when current-story
              (ui-full-story current-story))))))))

;(def StoriesSearch StoriesCustom)

(declare-mutation search-stories 'com.example.model.mutations/search-stories)

(comp/defsc Mode
  [this {:ui/keys [mode] :as props}]
  {:query         [:ui/mode]
   :initial-state {:ui/mode :search}})

(defn enter?
  [e]
  (= 13 (.-charCode e)))

(comp/defsc StoriesSearch
  [this {:ui/keys [current-story stories-search-results search-field mode]
         :as props}]
  {:query             [{:ui/stories-search-results (comp/get-query Story)}
                       {:ui/current-story (comp/get-query FullStory)}
                       {:ui/mode (comp/get-query Mode)}
                       :ui/search-field]
   :ident             (fn [x] [:component/id ::StoriesSearch])
   :initial-state     (fn [p]
                        {:ui/stories-search-results []
                         :ui/search-field           "abc"
                         :ui/mode                   (comp/get-initial-state Mode)})
   :route-segment     ["search"]
   :componentDidMount (fn [this]
                        (comp/transact! this [(set-mode {:mode :search})])
                        (df/load! this :search-results/stories
                          (rc/nc [:story/id :story/author :story/content :story/title])
                          {:target [:component/id ::StoriesSearch :ui/stories-search-results]
                           :params {:search/search-query "gene kim"}
                           :post-mutation 'com.example.model.mutations/top-story}))}

  (dom/div
    (println "StoriesSearch: mode: " mode)
    (dom/h2 "Searched Stories")
    (dom/div :.ui.input
      (dom/input {:type     "text"
                  :id       "search-field"
                  :value    search-field
                  :onChange #(m/set-string! this :ui/search-field :event %)
                  :onKeyPress (fn [e]
                                (println "key press" (.-charCode e))
                                (when (enter? e)
                                  (comp/transact! this [(search-stories {:query search-field})])))})
      (dom/div :.ui.button {:onClick (fn [x]
                                       (println "Search button: " search-field)
                                       (comp/transact! this [(search-stories {:query search-field})]))}
        "Search"))

    (ui-story-num {:ui/all-stories stories-search-results
                   :ui/current-story current-story})

    (dom/div :.ui.grid
      (dom/div :.row
        (dom/div :.five.wide.column
          (dom/div :.ui.selection.list.vertical-scrollbar.segment
            (map-indexed (fn [idx story]
                           (ui-story (merge story {:ui/number idx})
                             {:on-select
                                        (fn [story-id]
                                          ; ; ident: [:story/id story-id]
                                          ; it's what triggers the resolver, it's how you access
                                          ; local client database
                                          (println "on-select: triggered: " story-id)
                                          (df/load! this [:story/id story-id] FullStory
                                                    {:target [:component/id ::StoriesSearch :ui/current-story]}))
                              :selected current-story})) stories-search-results)))
        (dom/div :.eleven.wide.column
          (when current-story
            (ui-full-story current-story)))))))


(comment
  (comp/get-query SelectedStory)
  (comp/get-query StoriesCustom)

  ,)

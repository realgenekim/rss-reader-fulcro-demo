(ns com.example.ui.stories-forms
  (:require
    [com.example.model.story-list :as story-list]
    [com.fulcrologic.rad.form-options :as fo]
    [com.fulcrologic.rad.form :as form]
    [com.fulcrologic.rad.report :as report]
    [com.fulcrologic.rad.report-options :as ro]
    #?(:clj  [com.fulcrologic.fulcro.dom-server :as dom :refer [div label input]]
       :cljs [com.fulcrologic.fulcro.dom :as dom :refer [div label input]])
    [com.fulcrologic.fulcro.mutations :as mutation :refer [declare-mutation]]
    [com.fulcrologic.fulcro.components :as comp]
    [com.fulcrologic.fulcro.data-fetch :as df]
    [com.fulcrologic.fulcro.algorithms.denormalize :as fdn]
    [com.fulcrologic.fulcro.algorithms.merge :as merge]
    [com.fulcrologic.fulcro.application :as app]
    [com.fulcrologic.rad.type-support.date-time :as datetime]
    [com.fulcrologic.fulcro.raw.components :as rc]
    [com.fulcrologic.fulcro.mutations :as m]
    [membrane.ui :as ui]
    ;[com.example.ui.button-toys-form :as buttons]

    #?(:cljs [goog.string :as gstring])
    #?(:cljs [portal.web :as pw])))

#?(:cljs
   (def format goog.string.format))

(declare-mutation bottom-story 'com.example.model.mutations/bottom-story)
(declare-mutation top-story 'com.example.model.mutations/top-story)
(declare-mutation set-mode 'com.example.model.mutations/set-mode)

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


(comp/defsc Story [this {:story/keys [id author title]
                         :ui/keys [number]
                         :as params}
                   ; computed-factory: adds third argument
                   ; otherwise, component will disappear if you don't re-render parent
                   {:keys [on-select selected]}]
  ; change all to :story/id
  {:query [:story/id :story/author :story/title :ui/number]
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

(report/defsc-report StoriesRAD [this {:ui/keys [current-rows parameters]
                                       :as props}]
  {ro/title            "Stories RAD Report"
   ;ro/source-attribute :story/all-stories
   ro/source-attribute :story/first-page-stories
   ; this is a link query
   ro/query-inclusions [
                        ;{[:current-story '_] (comp/get-query FullStory)}
                        ;{[:ui/current-position '_] (comp/get-query CurrentPosition)}
                        :ui/current-position]

   ;ro/query-inclusions [{:current-story (comp/get-query FullStory)}]
   ro/row-pk           story-list/id
   ro/columns          [story-list/author story-list/title story-list/pos]

   ro/run-on-mount?    true
   ro/route            "stories-rad"}
  ;(let [state* @(->> com.example.client/app (:com.fulcrologic.fulcro.application/state-atom))])
  #_(dom/div
      (dom/p "hello3"))
  (dom/div
    ;(ui-current-position props)
    ;(dom/div
    ;  (println "StoriesCustom: state*: " state*)
    ;  (dom/p "Current story: " (:ui/current-position state*)))
    (dom/div
      ;(println current-rows)
      ;(println "current story: " (:current-story props))
      (println "StoriesCustom: props: " props))
    ;(println (keys props))

    (div :.eight.wide.column
      (dom/ul :.ui.segment
        (map ui-story current-rows)))

    #_(dom/div :.ui.grid
               (div :.row
                    (div :.eight.wide.column
                         (dom/ul :.ui.segment
                                 (map ui-story current-rows)))
                    (div :.eight.wide.column
                         (ui-full-story (:current-story props)))))))

(report/defsc-report StoriesRADMembrane [this {:ui/keys [current-rows parameters]
                                               :as props}]
  {ro/title            "Stories RAD Report"
   ;ro/source-attribute :story/all-stories
   ro/source-attribute :story/first-page-stories
   ; this is a link query
   ;ro/query-inclusions [
   ;{[:current-story '_] (comp/get-query FullStory)}
   ;{[:ui/current-position '_] (comp/get-query CurrentPosition)}
   ;:ui/current-position

   ;ro/query-inclusions [{:current-story (comp/get-query FullStory)}]
   ro/row-pk           story-list/id
   ro/columns          [story-list/author story-list/title story-list/pos]

   ro/run-on-mount?    true
   ro/route            "stories-rad2"}
  ;(let [state* @(->> com.example.client/app (:com.fulcrologic.fulcro.application/state-atom))])
  (ui/vertical-layout
    (ui/label "hello from stories RAD report")
    (ui/label (str current-rows))
    (ui/label (str "count: " (count current-rows)))))

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
        n       (->> thisone first second)
        nstories (count all-stories)]
    ;(println "StoryNum: thisone: " thisone)
    (println "StoryNum: n: " n)
    ;(println "all-stories: " all-stories)
    ;(println "current story:" current-story)
    ;(println "params: " params)
    (dom/p
      (if-not (= 0 nstories)
       (format "Current story: %d of %d" n nstories)
       "...loading..."))))


(def ui-story-num (comp/factory StoryNum))




(comp/defsc Mode
  [this {:ui/keys [mode show-help?] :as props}]
  {:query         [:ui/mode :ui/show-help?]
   :ident         (fn [] [:component/id ::Mode])
   :initial-state {:ui/mode :main}})
  ;(dom/p "Mode: " (str mode)))

(def ui-mode (comp/factory Mode))


(comp/defsc Help
  [this {:ui/keys [show-help?] :as props}]
  {:query         [:ui/mode :ui/show-help?]
   :ident         (fn [] [:component/id ::Help])
   :initial-state {:ui/show-help? false}}
  (dom/div
    (dom/p "Read 7,120 RSS articles from Planet Clojure — downloaded from Feedly, December 2019.  (This was built upon the Fulcro RAD demo app).")
    (dom/p "\"?\" to get keyboard help: ")
    (if show-help?
      (dom/div
        (dom/h2 "Help")
        (let [help ["j/k: next/prev story"
                    "t/b: top/bottom story"
                    "r: random story"
                    "</>: switch mode"
                    "?: toggle help"]]
          ;(for [h help]
          ;  (dom/p ^{:key h} h)))))))
          (map dom/p help))))
    (dom/p)))





(def ui-help (comp/factory Help))

(comp/defsc StoriesMain
  [this {:ui/keys [all-stories current-story mode next-prev-stories]
         :as props}]
  {:query             [{:ui/all-stories (comp/get-query Story)}
                       {:ui/current-story (comp/get-query FullStory)}
                       {:ui/mode (comp/get-query Mode)}
                       :ui/next-prev-stories]
   :ident             (fn [x] [:component/id ::StoriesMain])
   :initial-state     (fn [x] {:ui/all-stories []
                               :ui/mode        (comp/get-initial-state Mode)})
   :route-segment     ["stories"]
   :componentDidMount (fn [this]
                        (println "StoresMain: mounted!")
                        (comp/transact! this [(set-mode {:ui/mode :main})])
                        (df/load! this :story/first-page-stories Story
                          {:target        [:component/id ::StoriesMain :ui/all-stories]
                           :post-mutation 'com.example.model.mutations/create-prev-story-next-cache})
                        (df/load! this :story/all-stories Story
                          {:target        [:component/id ::StoriesMain :ui/all-stories]
                           :parallel      true
                           :post-mutation 'com.example.model.mutations/create-prev-story-next-cache}))}
  (dom/div
    (dom/h2 "All Stories")
    ;(dom/p "Mode: " (str mode))
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
                                        {:target [:component/id ::StoriesMain :ui/current-story]}))
                                :selected current-story})) all-stories)))
          (dom/div :.eleven.wide.column
            (when current-story
              (ui-full-story current-story))))))))

(declare-mutation search-stories 'com.example.model.mutations/search-stories)



(defn enter?
  [e]
  (= 13 (.-charCode e)))

(def initial-search-query "rich hickey")

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
                         :ui/search-field           initial-search-query
                         :ui/mode                   (comp/get-initial-state Mode)})
   :route-segment     ["search"]
   :componentDidMount (fn [this]
                        (println "StoresSearch: mounted!")
                        (comp/transact! this [(set-mode {:ui/mode :search})])
                        (comp/transact! this [(search-stories {:query initial-search-query})]))}
   ;:keyboard-shortcuts {"j" it's being called globally:
   ;                             [(mutation-next-story {:stories
   ;                     "k"}}

  (dom/div
    (println "StoriesSearch: mode: " mode)
    (dom/h2 "Searched Stories")
    ;(dom/p "Mode: " (str mode))
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

(def ui-stories-main (comp/computed-factory StoriesMain))
(def ui-stories-search (comp/computed-factory StoriesSearch))

(comp/defsc StoriesContainer
  [this {:ui/keys [mode search main show-help?]
         :as      props}]
  {:query             [{:ui/search (comp/get-query StoriesSearch)}
                       {:ui/main (comp/get-query StoriesMain)}
                       {:ui/mode (comp/get-query Mode)}
                       {:ui/show-help? (comp/get-query Help)}]
                       ; uncommenting this will change behavior of routing to ButtonTest1!
                       ;{:ui/buttons (comp/get-query buttons/ButtonTest1)}]
   :ident             (fn [x] [:component/id ::StoriesContainer])
   :initial-state     {:ui/mode    {}
                       :ui/search  {}
                       :ui/main    {}
                       :ui/show-help?    {}}
                       ;:ui/buttons {}}
   :route-segment     ["main"]
   :componentDidMount (fn [this]
                        (println "StoriesContainer: mounted!")
                        (comp/transact! this [(set-mode {:ui/mode :main})]))}
  (dom/div
    (ui-help show-help?)
    ;(dom/h2 "Story Container")
    (div
      (dom/div :.ui.button {:onClick (fn [x] (comp/transact! this [(set-mode {:ui/mode :main})]))}
        "Main")
      (dom/div :.ui.button {:onClick (fn [x] (comp/transact! this [(set-mode {:ui/mode :search})]))}
        "Search"))
    ;(dom/p "mode: " (str mode))
    ; from Tony session: a typo!!!  destructuring would have caught it
    ;  to post: "how much time did we spend chasing down things going wrong in Om Next b/c of this"
    ;(buttons/ui-button-test-1 buttons)
    (case (:ui/mode mode)
      :main (ui-stories-main main)
      :search (ui-stories-search search)
      nil)))

(comp/defsc ModeTest1
  [this {:ui/keys [mode] :as props}]
  {:query [{:ui/mode (comp/get-query Mode)}]
   :ident (fn [_] [:component/id ::ModeTest1])
   :initial-state (fn [_]
                    {:ui/mode (comp/get-initial-state Mode)})
   :route-segment ["mode-test-1"]}
  (dom/div
    (println "ModeTest1: " mode)
    (println "ModeTest1: " props)
    (dom/h2 "Mode Test 1b")
    (dom/p "Render just the mode")
    (dom/p ":ui/mode: " (str mode))
    (ui-mode mode)))


(comment

  (comp/transact! app [(set-mode {:ui/mode :main})])
  (comp/transact! app [(set-mode {:ui/mode :search})])
  (def app (resolve 'com.example.client/app))
  (com.example.model.mutations/get-mode (->> @app :com.fulcrologic.fulcro.application/state-atom))

  (comp/get-query SelectedStory)
  (comp/get-query StoriesMain),
  (let [state (app/current-state app)]
    (com.fulcrologic.fulcro.algorithms.denormalize/db->tree (comp/get-query StoriesContainer)
      state state,))
  (comp/get-query StoriesContainer)
  (app/force-root-render! app)

  ,)

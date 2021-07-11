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
    #?(:cljs [goog.string :as gstring])))

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
    (dom/div :.list
      (dom/div :.item (dom/b (str "Author: " author)))
      (dom/div :.item (dom/b (str "Title: " title)))
      (dom/div :.item (dom/b (str "Published: " (datetime/inst->html-date (datetime/new-date published))))))

    (dom/p)
    ; content has embedded html
    ;    e.g., "<strong> hello! </strong"}})))
    (dom/div {:dangerouslySetInnerHTML
              {:__html content}})))


(def ui-full-story (comp/factory FullStory {:keyfn :story/id}))


(comp/defsc Story [this {:story/keys [id author title published]
                         :as params}
                   ; computed-factory: adds third argument
                   ; otherwise, component will disappear if you don't re-render parent
                   {:keys [on-select selected]}]
  ; change all to :story/id
  {:query [:story/id :story/author :story/title :story/published]
   :ident :story/id}
  (dom/div :.item #_{:classes [(when (= id (:story/id selected))
                                 "right triangle icon")]}
    ;(println params)
    (dom/a {:href "#!"
            :onClick (fn [_]
                       (when on-select
                         (on-select id)))}
           (let [text (format "%s (%s)" title author)]
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

(comp/defsc StoriesCustom
  [this {:ui/keys [all-stories current-story]
         :as props}]
  {:query             [{:ui/all-stories (comp/get-query Story)}
                       {:ui/current-story (comp/get-query FullStory)}]
   :ident             (fn [x] [:component/id ::StoriesCustom])
   :initial-state     {:ui/all-stories []}
   :route-segment     ["Stories"]
   :componentDidMount (fn [this]
                        (df/load! this :story/all-stories Story
                                  {:target [:component/id ::StoriesCustom :ui/all-stories]
                                   :post-mutation 'com.example.model.mutations/top-story}))}
  (dom/div
    (dom/p "Current story number: ")
    (dom/div
      ; (ui-current-position props)
      (dom/div :.ui.grid
        (dom/div :.row
          (dom/div :.five.wide.column
            (dom/div :.ui.selection.list.vertical-scrollbar.segment
              (map (fn [story]
                     (ui-story story
                               {:on-select
                                (fn [story-id]
                                  ; ; ident: [:story/id story-id]
                                  ; it's what triggers the resolver, it's how you access
                                  ; local client database
                                  (println "on-select: triggered: " story-id)
                                  #_(merge/merge-component!
                                      this
                                      StoriesCustom
                                      {:ui/current-story [:story/id story-id]})
                                    ;:append [:teams])
                                  ;(merge/merge!
                                  ;  this
                                  ;  {:ui/current-story [:story/id story-id]}
                                  ;  [:component/id ::StoriesCustom]))
                                  ;(app/schedule-render! this))
                                  ;  (comp/get-query FullStory))
                                  (df/load! this [:story/id story-id] FullStory
                                            {:target [:component/id ::StoriesCustom :ui/current-story]}))
                                :selected current-story})) all-stories)))
          (dom/div :.eleven.wide.column
            (when current-story
              (ui-full-story current-story))))))))




(comment
  (comp/get-query SelectedStory)
  (comp/get-query StoriesCustom)

  ,)

(mutation/defmutation bump-number [ignored]
  (action [{:keys [state]}]
          ;(do
          ;  (println "bump-number")
          (swap! state update :ui/number2 inc)))

(mutation/defmutation bump-number2 [{:keys [field]}]
  ; field: e.g., :ui/number2
  (action [{:keys [ref state]}]
          ; ref is the ident of the component that invoked the mutation
          ; => [:component/id ::Root8]
          (let [path (conj ref field)]
          ;(do
            (println "bump-number2")
            (swap! state update-in path inc))))


(comp/defsc Root7
  [this {number2 :ui/number2
         ;:ui/keys [number]
         ;stories :stories
         ;stories :stories
         ;number :ui/number
         ;current-story :current-story
         :as     props}]
  {:query [
           ;{:stories (comp/get-query Story)}
           ;{:current-story (comp/get-query FullStory)}
           :ui/number2]
           ;[df/marker-table :teams]]
   :ident         (fn [] [:component/id ::LandingPage2])
   :initial-state {:ui/number2 0}
   :route-segment ["root77"]}
  (dom/div
    (println "number: " number2)
    (println "props: " props)
    (dom/button {:onClick #(comp/transact! this [(bump-number {})])}
                "You've clicked this button " number2 " times.")))
    ;(dom/button {:type    "button"
    ;             :onClick (fn [x]
    ;                        (println "df/load! the data from here")
    ;                        (comp/transact! this '[(get-story {:story/id 1})])
    ;                        (df/load! this :current-story Story {:story/id 1}))}
    ;        "Load Mutation...")))
    ;(let [loading? (get props [df/marker-table :teams])]                                  ; scaffolding for TASK 5
    ;  (cond
    ;    loading? (dom/p "Loading...")
    ;    ;; ...
    ;    :else
    ;    (comp/fragment (dom/p ""))))))

    ;(map ui-story stories)))

    ;(ui-full-story current-story)))

(comp/defsc Button1
  [this {:button/keys [value]}]
  {:query [:button/id :button/value]
   :ident :button/id
   :initial-state {:button/id :param/id
                   :button/value :param/value}}
  (dom/button {:onClick #(comp/transact! this [(bump-number2 {:field :button/value})])}
              "You've clicked this button " value " times."))

(def ui-button1 (comp/factory Button1 {:keyfn :button/id}))



#_(comp/defsc Root8
    [this {:ui/keys [number2] :as props}]
    {:query [:ui/number2]
     :initial-state {:ui/number2 3}
     :route-segment ["root8"]
     ;:ident :ui/number2}
     ;:ident         (fn [] [:component/id ::Root8])
     :ident         (fn [] [:component/id ::Root8])}
     ;:ident         (fn [] [:ui/number2])}
    (dom/div
      (dom/h4 "This is an example of modifying a value associated with this component ident: :component/id ::Root8.")
      (println "Root8: props: " props)
      (dom/button {:onClick #(comp/transact! this [(bump-number2 {:field :ui/number2})])}
                  "You've clicked this button " number2 " times.")
      (dom/button {:onClick #(comp/transact! this [(bump-number2 {:field :ui/number2})])}
                  "You've clicked this button " number2 " times.")))

(comp/defsc Root8
  [this {:ui/keys [buttons] :as props}]
  {:query         [{:ui/buttons (comp/get-query Button1)}]
   :initial-state {:ui/buttons [{:id 1 :value 1}
                                {:id 2 :value 2}
                                {:id 2 :value 2}]}
   :route-segment ["root8"]
   ;:ident :ui/number2}
   ;:ident         (fn [] [:component/id ::Root8])
   :ident         (fn [] [:component/id ::Root8])}
  ;:ident         (fn [] [:ui/number2])}
  (dom/div
    (dom/h4 "This is an example of modifying a value associated with this component ident: :component/id ::Root8.")
    (println "ButtonTest1: props: " props)
    (map ui-button1 buttons)))



(comment
  (comp/get-query Root7)
  ,)
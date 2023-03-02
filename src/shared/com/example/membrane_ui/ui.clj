(ns com.example.membrane-ui.ui
  (:require
    [com.fulcrologic.fulcro.application :as app]
    [com.example.membrane-ui.client :as c]
    [com.example.ui.stories-forms :as stories]
    [com.example.model.story-list :as story-list]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.data-fetch :as df]
    [com.fulcrologic.rad.control :as control]
    [com.fulcrologic.fulcro.mutations :refer [defmutation] :as mut]
    [com.fulcrologic.fulcro.algorithms.lookup :as ah]
    [com.fulcrologic.fulcro.algorithms.indexing :as indexing]
    [com.fulcrologic.fulcro.dom :as dom]
    [com.fulcrologic.fulcro.inspect.inspect-client :as inspect]
    [com.fulcrologic.fulcro.algorithms.merge :as merge]
    [com.fulcrologic.fulcro.algorithms.denormalize :as fdn]
    [com.fulcrologic.fulcro.algorithms.tx-processing.synchronous-tx-processing :as stx]
    [com.fulcrologic.rad.report :as report]
    [com.fulcrologic.rad.report-options :as ro]
    [com.fulcrologic.rad.routing :as rroute]
    [com.fulcrologic.fulcro.algorithms.tx-processing-debug :as txd]
    [membrane.ui :as ui]
    [membrane.basic-components :as basic]
    membrane.component
    [membrane.fulcro :as mf
     :refer [uuid
             component->view
             show!
             show-sync!]]
    [membrane.skia :as skia]))

(comp/defsc Story [this {:story/keys [id author title]
                         :ui/keys [number]
                         :as params}
                   ; computed-factory: adds third argument
                   ; otherwise, component will disappear if you don't re-render parent
                   {:keys [on-select selected]}]
  ; change all to :story/id
  {:query [:story/id :story/author :story/title :ui/number]
   :ident :story/id}
  (ui/horizontal-layout
    (let [text (format "%d. %s (%s)" number title author)]
      (if (= id (:story/id selected))
        (ui/label text)))))
  ;(dom/div :.item #_{:classes [(when (= id (:story/id selected))
  ;                               "right triangle icon")]}
  ;  {:id (str "story-" id)}
  ;  (println params)
    ;(dom/a {:href "#!"
    ;        :onClick (fn [_]
    ;                   (when on-select
    ;                     (on-select id)))}
    ;  (let [text (format "%d. %s (%s)" number title author)]
    ;    (if (= id (:story/id selected))
    ;      (dom/strong text)
    ;      text)))))

(def ui-story (comp/computed-factory Story {:keyfn :story/id}))

(defn tx-queue-size
  []
  (-> c/app :com.fulcrologic.fulcro.application/runtime-atom deref :com.fulcrologic.fulcro.algorithms.tx-processing/active-queue
    count))

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
    (do
      (def props props)
      nil)
    (ui/label "hello from stories RAD report 223")
    (ui/label "my props: " (str props))

    (ui/label (str "my component current rows (this should match backdoor count): " (count current-rows)))
    (ui/label (str "my component current rows: " current-rows))
    (ui/label (str "backdoor report db: count: " (-> c/app :com.fulcrologic.fulcro.application/state-atom deref
                                                   :com.fulcrologic.rad.report/id :com.example.membrane-ui.ui/StoriesRADMembrane
                                                   :ui/current-rows count)))


    (ui/label "\n\nother stats:")
    (ui/label (str "tx queue size: " (tx-queue-size)))))

; adrian, I'm looking into database at 5m; backdoor
; - 10m: establishing current state
; - 12m: show! works, but show-sync! hangs
  ;(map ui-story current-rows)))
    ;(map (fn [x]) current-rows)))

#_(dom/div
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
            (ui-full-story (:current-story props))))))

(def ui-report (comp/factory StoriesRADMembrane {:keyfn :story/id}))

;(def todo-item-factory (comp/factory TodoItem))
;(defn ui-todo-item [props]
;  (component->view (todo-item-factory props)))

(comment
  (type StoriesRADMembrane)
  (type ui-report)
  (component->view ui-report)
  (component->view StoriesRADMembrane)
  (report/reload! c/app StoriesRADMembrane)

  (do
    (report/run-report! c/app StoriesRADMembrane)
    (report/start-report! c/app StoriesRADMembrane))

  (com.fulcrologic.fulcro.algorithms.tx-processing/process-queue! c/app)


  (-> c/app :com.fulcrologic.fulcro.application/state-atom deref)
  (tap> (-> c/app :com.fulcrologic.fulcro.application/runtime-atom deref :com.fulcrologic.fulcro.algorithms.tx-processing/active-queue))
  (-> c/app :com.fulcrologic.fulcro.application/runtime-atom deref :com.fulcrologic.fulcro.algorithms.tx-processing/active-queue)
  (-> c/app :com.fulcrologic.fulcro.application/runtime-atom deref :com.fulcrologic.fulcro.algorithms.tx-processing/active-queue
    count)
  (->> c/app :com.fulcrologic.fulcro.application/runtime-atom deref :com.fulcrologic.fulcro.algorithms.tx-processing/active-queue
    first keys)
  (->> c/app :com.fulcrologic.fulcro.application/runtime-atom deref :com.fulcrologic.fulcro.algorithms.tx-processing/active-queue
    (map :com.fulcrologic.fulcro.algorithms.tx-processing/tx))


  (df/load! c/app :story/first-page-stories stories/Story
    {:target        [:component/id ::StoriesRADMembrane :ui/all-stories]})

  (app/schedule-render! c/app {:force-root? true})
  (app/render! c/app)

  (tap> c/app)

  (com.fulcrologic.fulcro.algorithms.tx-processing/activate-submissions! c/app)
  ()
  0)

(comp/defsc FullStory [_ {:story/keys [id author title content published]
                          :as props}]
  {:query [:story/id :story/author :story/content :story/title :story/published]
   :ident :story/id}
  ;(println "FullStory: props: " props)
  (ui/vertical-layout
    (ui/label "props2: " (str props))
    (ui/label "author: " author))
  ;(println "FullStory: content: " content)
  #_(dom/div :.ui.segment
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

(defn dev-view
  " helper: put anything you're working in here in dev
    (for prod app, it'll just be another view, composing all your components "
  []
  (let [states nil]
    (ui/vertical-layout
      (ui/label "hi2")
      (component->view StoriesRADMembrane))))
    ;(selector (-> @*app-state :frame) (count states))
    ;(render-view @*sim-state *app-state)
    ;(mon/show-leaf-counter)))

(comment
  ; Adrian, here's how to get the app bootstrapped.
  (do
    (c/adrian-init)
    (report/run-report! c/app StoriesRADMembrane)
    (report/start-report! c/app StoriesRADMembrane))


  ; end
  c/app

  ;(def app (show! TodoList (comp/get-initial-state TodoList {:todo-list/id (uuid)})))

  (def app2
    (show-sync! FullStory {:story/id "K3Y7GLlRfaBDsUWYD0WuXjH/byGbQnwaMWp+PEBoUZw=_16f28df90a3:1084059:c84ffc39"}))

  (def app (show! StoriesRADMembrane (comp/get-initial-state StoriesRADMembrane {})))
  (def app (show! StoriesRADMembrane (comp/get-initial-state StoriesRADMembrane {})))
  (def app (show-sync! StoriesRADMembrane (comp/get-initial-state StoriesRADMembrane {})))

  (skia/run #'dev-view)



  0)
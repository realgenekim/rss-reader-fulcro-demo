(ns com.example.membrane-ui.ui
  (:require
    [com.fulcrologic.fulcro.application :as app]
    [com.example.membrane-ui.client :as c]
    [com.example.ui.stories-forms :as stories]
    [com.fulcrologic.fulcro.algorithms.denormalize :as fdn]
    [com.fulcrologic.fulcro.algorithms.normalize :as fnorm]
    [com.fulcrologic.fulcro.algorithms.server-render :as ssr]
    [com.fulcrologic.fulcro.dom-server :as sdom]
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
    [com.fulcrologic.fulcro.raw.application :as raw-app]
    [com.example.membrane-ui.http-remote :as http]
    [membrane.ui :as ui]
    [com.fulcrologic.fulcro.ui-state-machines :as uism]
    [membrane.basic-components :as basic]
    membrane.component
    [membrane.fulcro :as mf
     :refer [uuid
             component->view
             show!
             show-sync!]]
    [membrane.skia :as skia]))

(comment
  (raw-app/render! app {:force-root? true})
  0)

(comp/defsc Story [this {:story/keys [id author title]
                         ;:ui/keys [number]
                         :as params}
                   ; computed-factory: adds third argument
                   ; otherwise, component will disappear if you don't re-render parent
                   {:keys [on-select selected number]}]
  ; change all to :story/id
  {:query [:story/id :story/author :story/title :ui/number]
   :ident :story/id}
  (ui/horizontal-layout
    ;(println :Story :number number)
    ;(println :Story :params params)
    (let [text (format "%d. %s (%s)" number title author)]
      ;(if (= id (:story/id selected))
      (ui/label text))))
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
;(def ui-story (comp/factory Story {:keyfn :story/id}))

(defn tx-queue-size
  []
  (-> c/app :com.fulcrologic.fulcro.application/runtime-atom deref :com.fulcrologic.fulcro.algorithms.tx-processing/active-queue
    count))

(declare app)

(defn get-report-state []
  (some-> app :com.fulcrologic.fulcro.application/state-atom deref :com.fulcrologic.fulcro.ui-state-machines/asm-id
    (get [:com.fulcrologic.rad.report/id :com.example.membrane-ui.ui/StoriesRADMembrane])
    :com.fulcrologic.fulcro.ui-state-machines/active-state))

(report/defsc-report StoriesRADMembrane [this {:ui/keys [current-rows loaded-data parameters]
                                               :as props}]
  {ro/title            "Stories RAD Report"
   ;ro/source-attribute :story/all-stories
   ro/source-attribute :story/first-page-stories
   ro/paginate?           true
   ro/page-size 10
   ; this is a link query
   ro/query-inclusions    [:ui/loaded-data :ui/parameters
                           ;:ui/sort-by :ui/show-word-cloud?
                           :ui/cache [:ui/cache :filtered-rows]]
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
    (ui/label "RAD report 2")
    (ui/label (str "Report State: " (get-report-state)))

    ;(ui/label (str "my component current rows (this should match backdoor count): " (count current-rows)))
    ;(ui/label (str "my component current rows: " current-rows))
    ;(ui/label (str "backdoor report db: count: " (-> c/app :com.fulcrologic.fulcro.application/state-atom deref
    ;                                               :com.fulcrologic.rad.report/id :com.example.membrane-ui.ui/StoriesRADMembrane
    ;                                               :ui/current-rows count)

    (ui/label "\n\n")
    ; Adrian: 13m mark: nested component is rendering!!
    ; 17m: got rows rendering
    ; 23m: got row numbers working
    ; 27m: rendering all 7000 stories!
    ;(component->view (ui-story (first current-rows)))

    (println :StoriesRADMembrane :counts (count current-rows) (count loaded-data))


    (apply
      ui/vertical-layout
      (->> current-rows
        (map-indexed (fn [idx itm]
                       (let [m (assoc itm :story/number idx)]
                         (component->view (ui-story m {:number idx})))))))


    (ui/label "\n\nother stats:")
    (ui/label (str "story: " (-> c/app :com.fulcrologic.fulcro.application/state-atom deref
                               :story/id (get-in "K3Y7GLlRfaBDsUWYD0WuXjH/byGbQnwaMWp+PEBoUZw=_16f28df90a3:1084059:c84ffc39"))))
    (ui/label (str "tx queue size: " (tx-queue-size)))))

(comment
  (first current-rows)
  (ui-story (first current-rows))
  0)

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
    (report/run-report! app StoriesRADMembrane)
    (report/start-report! app StoriesRADMembrane)
    nil)

  (com.fulcrologic.fulcro.algorithms.tx-processing/process-queue! c/app)
  (report/reload!)


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
  (app/render! app)
  (app/render! app {:force-root? true})

  (tap> app)

  (com.fulcrologic.fulcro.algorithms.tx-processing/activate-submissions! c/app)
  ()
  0)

(comp/defsc FullStory [_ {:story/keys [id author title content published]
                          :as props}]
  {:query [:story/id :story/author :story/content :story/title :story/published]
   :ident :story/id
   :initial-state {:story/id "K3Y7GLlRfaBDsUWYD0WuXjH/byGbQnwaMWp+PEBoUZw=_16f28df90a3:1084059:c84ffc39"}}
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
;(defn ui-todo-item [props] (component->view (todo-item-factory props)))

(defsc Root [this {:keys [story]}]
  {:query         [{:story (comp/get-query FullStory)}]
   :initial-state (fn [params] {:story (comp/get-initial-state FullStory {})})}
  (ui/vertical-layout
    (ui/label "hello22")
    (component->view (ui-full-story {:story/id "K3Y7GLlRfaBDsUWYD0WuXjH/byGbQnwaMWp+PEBoUZw=_16f28df90a3:1084059:c84ffc39"}))))
  ;(dom/div
  ;  (ui-person-list friends)
  ;  (ui-person-list enemies)})

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
    ;(c/adrian-init)
    (report/run-report! app StoriesRADMembrane)
    (report/start-report! app StoriesRADMembrane))

  (-> c/app :com.fulcrologic.fulcro.application/state-atom deref
    :story/id)
    ;(get-in "K3Y7GLlRfaBDsUWYD0WuXjH/byGbQnwaMWp+PEBoUZw=_16f28df90a3:1084059:c84ffc39"))


  ; end
  c/app
  (tap> app)

  ;(def app (show! TodoList (comp/get-initial-state TodoList {:todo-list/id (uuid)})))

  ; questions
  ; can we verify that component has some state loaded?
  ; how do I get reloading of components working?
  ; how can we get FullStory to "actualize itself" -- load its (initial) state, have it get passed its props, render those props

  (def app2
    (show! FullStory {:story/id "K3Y7GLlRfaBDsUWYD0WuXjH/byGbQnwaMWp+PEBoUZw=_16f28df90a3:1084059:c84ffc39"}))
  (def app2
    (show-sync! Root {}))

  (comp/get-query FullStory)
  (comp/get-query Root)

  (def app (show! StoriesRADMembrane (comp/get-initial-state StoriesRADMembrane {})))
  (def app (show! StoriesRADMembrane (comp/get-initial-state StoriesRADMembrane {})))
  (def app (show-sync! StoriesRADMembrane (comp/get-initial-state StoriesRADMembrane {})))

  (skia/run #'dev-view)

  (def state-map (-> c/app :com.fulcrologic.fulcro.application/state-atom deref))
  (-> app :com.fulcrologic.fulcro.application/state-atom deref :story/id count)
  (comp/get-initial-state Root)
  (comp/get-query Root)

  (def normalized-db
    (let [data-tree       (comp/get-initial-state Root)
          normalized-tree (fnorm/tree->db Root data-tree true)]
      normalized-tree))

  (def root-factory (comp/factory Root))

  (let [db (-> c/app :com.fulcrologic.fulcro.application/state-atom deref)
        ;props                (fdn/db->tree (comp/get-query Root normalized-db) state-map state-map)
        props                (fdn/db->tree (comp/get-query Root normalized-db) state-map state-map)
        root-factory         (comp/factory Root)]
    (binding [comp/*app* YOUR-APP] ; some APIs look for your fulcro-app in this bound var.
      (sdom/render-to-str (root-factory props))))

  (sdom/render-to-str (root-factory props))

  (ui-full-story {:story/id "K3Y7GLlRfaBDsUWYD0WuXjH/byGbQnwaMWp+PEBoUZw=_16f28df90a3:1084059:c84ffc39"
                  :story/author "Adrian"})

  (ui-full-story state-map {:story/id "K3Y7GLlRfaBDsUWYD0WuXjH/byGbQnwaMWp+PEBoUZw=_16f28df90a3:1084059:c84ffc39"
                            :story/author "Adrian"})




  0)


(comment
  (def app (mf/show! StoriesRADMembrane (comp/get-initial-state StoriesRADMembrane {})
             {:remotes {:remote (http/fulcro-http-remote {:url "http://localhost:3000/api"})}}))
  #_(def app (mf/show! StoriesRADMembrane {}
               {:remotes {:remote (http/fulcro-http-remote {:url "http://localhost:3000/api"})}}))
  #_(def app (mf/show-sync! StoriesRADMembrane (comp/get-initial-state StoriesRADMembrane {})
               {:remotes {:remote (http/fulcro-http-remote {:url "http://localhost:3000/api"})}}))

  ; why is report not paginating?  stuck in gathering-parameters

  (-> app :com.fulcrologic.fulcro.application/state-atom deref :com.fulcrologic.fulcro.ui-state-machines/asm-id
    (get [:com.fulcrologic.rad.report/id :com.example.membrane-ui.ui/StoriesRADMembrane])
    :com.fulcrologic.fulcro.ui-state-machines/active-state)

  ;(tap> app)
  ;
  ;(def app (show! StoriesRADMembrane (comp/get-initial-state StoriesRADMembrane {})))
  ;
  ;(comp/get-initial-state FullStory)
  ;
  ;(def app2
  ;  (show! FullStory {:story/id "K3Y7GLlRfaBDsUWYD0WuXjH/byGbQnwaMWp+PEBoUZw=_16f28df90a3:1084059:c84ffc39"}
  ;    c/app)



  (do
    ;(c/adrian-init)
    (report/run-report! app StoriesRADMembrane)
    (report/start-report! app StoriesRADMembrane))

  (uism/trigger! app (comp/get-ident StoriesRADMembrane {}) :event/run)
  (uism/trigger! app (comp/get-ident StoriesRADMembrane {}) :event/goto-page {:page 3})

  ;(rroute/route-to! this StoriesRADMembrane {})
  0)

(defonce app nil)

(defn refresh []
  ;; hot code reload of installed controls
  ;(log/info "Reinstalling controls")
  ;(setup-RAD app)
  ;(comp/refresh-dynamic-queries! app)
  (if app
    (mf/reload! app StoriesRADMembrane)))
  ;(app/mount! app Root "app"))

(refresh)


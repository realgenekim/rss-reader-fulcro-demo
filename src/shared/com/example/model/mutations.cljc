(ns com.example.model.mutations
  (:require
    [com.wsscode.pathom.connect :as pc :refer [defresolver]]
    [com.fulcrologic.fulcro.raw.components :as rc]
    [com.fulcrologic.fulcro.data-fetch :as df]
    #?@(:clj
        [[com.wsscode.pathom.connect :as pc :refer [defmutation]]
         ;[com.example.components.parser :as p]]
         [com.example.components.database-queries :as db]]
        :cljs
        [[com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
         [com.fulcrologic.fulcro.components :as comp]])
    ;[com.example.ui.stories-forms :as s]])
    [clojure.pprint :as pp]
    [com.example.ui.stories-forms :as stories]
    [com.fulcrologic.fulcro.algorithms.merge :as merge]
    [taoensso.timbre :as log]
    [com.fulcrologic.guardrails.core :refer [>defn =>]]
    [com.fulcrologic.fulcro.mutations :as mutation]))







#?(:cljs
   (do

     ;(mutation/declare-mutation top-story top-story)

     ; alternative: subroutes
     ; mode will go away
     ; replace with search field always being on main screen
     ; if search-results (that come from server) is not empty, render search results
     (defmutation set-mode [{:ui/keys [mode] :as params}]
       (action [{:keys [state]}]
         (println "mutation: set-mode: params: " mode)
         ;(do
         ;  (println "bump-number")
         (swap! state assoc-in [:component/id :com.example.ui.stories-forms/Mode :ui/mode] mode)))

     (defn get-mode
       [state]
       (let [mode (-> (get-in @state [:component/id :com.example.ui.stories-forms/Mode :ui/mode]))]
         (println "get-mode: " mode)
         mode))

     (defmutation switch-mode [_]
       (action [{:keys [app state]}]
         (let [mode    (get-mode state)
               _       (println "mutation: switch-mode: current mode: " mode)
               newmode (case (:ui/mode mode)
                         :search :main
                         :main :search)]
           (comp/transact! app [(set-mode {:ui/mode newmode})]))))

     (comment
       (def app (resolve 'com.example.client/app))
       (comp/transact! app [(set-mode {:ui/mode :search})])
       (com.example.model.mutations/get-mode (->> @app :com.fulcrologic.fulcro.application/state-atom))
       ,)

     (defmutation toggle-help [_]
       (action [{:keys [state]}]
         (let [ident [:component/id :com.example.ui.stories-forms/Help :ui/show-help?]
               b (-> (get-in @state ident))]
           ;(do
           ;  (println "bump-number")
           (println "mutation: toggle-help: current mode: " b)
           (swap! state assoc-in ident (not b)))))

     ; https://stackoverflow.com/questions/123999/how-can-i-tell-if-a-dom-element-is-visible-in-the-current-viewport
     (defn scroll-into-view
       [story-id]
       (let [id (str "story-" story-id)
             el (-> js/document (.getElementById id))]
         ; https://developer.mozilla.org/en-US/docs/Web/API/Element/scrollIntoView
         (.scrollIntoView el {:behavior "smooth"
                              :block "nearest"})))

     (defmutation scroll-to-element
       [params]
       (action [{:keys [app state]}]
         (let [ident [:component/id :com.example.ui.stories-forms/StoriesMain]
               props (get-in @state ident)
               {:ui/keys [all-stories current-story]} props
               story-id (second current-story)]
           (println "mutation: scroll-to-element: story-id: " story-id)
           (scroll-into-view story-id))))



     (defn get-state-and-stories
       " given mode, return ident (where current value will live) and stories (which reside in its scope) "
       [state mode] [map? keyword? => map?]
       (println "get-state-and-stories: mode; " mode)
       (let [ident          (case mode
                              :search [:component/id :com.example.ui.stories-forms/StoriesSearch]
                              :main [:component/id :com.example.ui.stories-forms/StoriesMain]
                              nil)
             props          (get-in state ident)
             {:ui/keys [all-stories stories-search-results current-story]} props
             ; if you want the denormalized props, use db->tree to get maps of maps,
             ; like in UI
             source-stories (case mode
                              :search stories-search-results
                              :main all-stories
                              nil)]
         {:source-ident ident
          :source-stories source-stories}))


     (defn get-next-story-ident-from-action
       " given ident and source-stories and action, return the next-story-ident which can be
         loaded via df/load! "
       [ident-and-stories current-story action]
       (let [{:keys [source-ident source-stories]} ident-and-stories
             story-pairs      (partition 2 1 source-stories)
             f                (case action
                                :up first
                                :down second)
             pair-of-interest (time
                                (first
                                  (filter (fn [x]
                                            ; either first (up) or second (down)
                                            (= (f x)
                                              current-story))
                                    story-pairs)))]
         pair-of-interest))

     (defn create-story-prev-next-lookup-cache
       " input: sequence of items
         output: {0 {:prev nil, :curr 0, :next 1}]
                  1 {:prev 0, :curr 1, :next 2}]
                  2 {:prev 1, :curr 2, :next 3}}
                  2 {:prev 2, :curr 3, :next nil}}"
       [stories]
       ; put nils as first and last, to create entries w/no :prev, and w/no :next
       (let [story-triples (partition 3 1 (concat [nil] stories [nil]))
             labelled-map  (->> story-triples
                             (map #(zipmap [:prev :curr :next] %))
                             (map (juxt :curr identity))
                             (into {}))]
         labelled-map))



     (comment
       (create-story-prev-next-lookup-cache (range 5)))


     (defmutation next-story
       [params]
       (action [{:keys [ref app state]}]
         ; ref is the ident of the component that invoked the mutation
         ; => [:component/id ::Root8]
         (println "mutation: next-story: mode: " (get-mode state))
         (time
           (let [ident-and-stories (get-state-and-stories @state (get-mode state))
                 {:keys [source-ident source-stories]} ident-and-stories
                 props             (get-in @state source-ident)
                 {:ui/keys [current-story next-prev-stories]} props
                 _ (println current-story)
                 entry (get next-prev-stories current-story)]
             (let [next-story-ident (get entry :next)]
               (println "next story: story-id: " next-story-ident)
               (df/load! app next-story-ident
                 (rc/nc [:story/id :story/author :story/content :story/title])
                 {:target (conj source-ident :ui/current-story)}))))))


                 ;_                 (println "source-stories: " source-stories)
             ;    pair-of-interest  (get-next-story-ident-from-action ident-and-stories current-story :up)]
             ;;pair-of-interest  [(rand-nth source-stories) (rand-nth source-stories)]]
             ;(when-let [next-story-ident (second pair-of-interest)]
             ;  (let [new-story-id (second next-story-ident)]
             ;    (println "next story: story-id: " new-story-id)
             ;    (df/load! app next-story-ident
             ;      (rc/nc [:story/id :story/author :story/content :story/title])
             ;      {:target (conj source-ident :ui/current-story)})))))))
               ; (scroll-into-view new-story-id))))))

     (defmutation random-story
       [params]
       (action [{:keys [ref app state]}]
         ; ref is the ident of the component that invoked the mutation
         ; => [:component/id ::Root8]
         (println "mutation: random-story: mode: " (get-mode state))
         (time
           (let [ident-and-stories (get-state-and-stories @state (get-mode state))
                 {:keys [source-ident source-stories]} ident-and-stories
                 pair-of-interest  [(rand-nth source-stories) (rand-nth source-stories)]]
             (when-let [next-story-ident (second pair-of-interest)]
               (let [new-story-id (second next-story-ident)]
                 (println "random story: story-id: " new-story-id)
                 (df/load! app next-story-ident
                   (rc/nc [:story/id :story/author :story/content :story/title])
                   {:target (conj source-ident :ui/current-story)})))))))


     (defmutation previous-story
       [params]
       (action [{:keys [app state]}]
         (println "mutation: prev-story: mode: " (get-mode state))
         (time
           (let [ident-and-stories (get-state-and-stories @state (get-mode state))
                 {:keys [source-ident source-stories]} ident-and-stories
                 props             (get-in @state source-ident)
                 {:ui/keys [current-story next-prev-stories]} props
                 _ (println current-story)
                 entry (get next-prev-stories current-story)]
             (let [next-story-ident (get entry :prev)]
               (println "previous story: story-id: " next-story-ident)
               (df/load! app next-story-ident
                 (rc/nc [:story/id :story/author :story/content :story/title])
                 {:target (conj source-ident :ui/current-story)}))))))

     (defmutation top-story
       [params]
       (action [{:keys [app state]}]
               (let [ident-and-stories (get-state-and-stories @state (get-mode state))
                     {:keys [source-ident source-stories]} ident-and-stories
                     target-ident (first source-stories)]
                 (println "top-story: " target-ident)
                 (when target-ident
                   (df/load! app target-ident
                             (rc/nc [:story/id :story/author :story/content :story/title])
                             {:target (conj source-ident :ui/current-story)})))))

     (defmutation create-prev-story-next-cache
       ; tony: store this in :ui/cache {:keyboard {"a" ... }
       ; [search/main] (or unify the two components)
       ; and thus the need to figure out which component is active
       [params]
       (action [{:keys [ref app state]}]
         ; ref is the ident of the component that invoked the mutation
         ; => [:component/id ::Root8]
         (println "mutation: create-prev-next-cache: ")
         (time
           (let [ident-and-stories (get-state-and-stories @state (get-mode state))
                 {:keys [source-ident source-stories]} ident-and-stories
                 _ (println "create-prev-story-next-cache: source-ident: " source-ident)
                 _ (println "create-prev-story-next-cache: ref" ref)
                 lookup-table (create-story-prev-next-lookup-cache source-stories)]
             (swap! state assoc-in (conj source-ident :ui/next-prev-stories) lookup-table)
             (comp/transact! app [(top-story {})])))))

     (defmutation bottom-story
       [params]
       (action [{:keys [app state]}]
               (let [ident-and-stories (get-state-and-stories @state (get-mode state))
                     {:keys [source-ident source-stories]} ident-and-stories
                     target-ident (last source-stories)]
                 (println "bottom-story: " target-ident)
                 (when target-ident
                   (df/load! app target-ident
                             (rc/nc [:story/id :story/author :story/content :story/title])
                             {:target (conj source-ident :ui/current-story)})))))

     (defmutation search-stories
       [params]
       (action [{:keys [app state]}]
         (println "mutation: search-stories: params: " params)
         (df/load! app :search-results/stories
           (rc/nc [:story/id :story/author :story/content :story/title])
           {:target [:component/id :com.example.ui.stories-forms/StoriesSearch :ui/stories-search-results]
            :params {:search/search-query (:query params)}
            :post-mutation 'com.example.model.mutations/create-prev-story-next-cache})))))





(def resolvers [])
(def mutations [])
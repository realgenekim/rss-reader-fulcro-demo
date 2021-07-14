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
    [com.fulcrologic.guardrails.core :refer [>defn =>]]))

#?(:clj
   (defn read-file []
     ; "ccsp2.txt"
     ; "clojure.txt"
     (-> ;(slurp (str "/Users/genekim/src.local/feedly" "/" "clojure.txt"))
       (slurp "resources/stories.txt")
       (read-string))))

(defresolver my-very-awesome-teams [_ _] ; a global resolver
  {::pc/input  #{}
   ::pc/output [{:teams [:team/id :team/name :team/players]}]}
  {:teams [#:team{:name "Hikers" :id :hikers
                  :players [#:player{:id 1}
                            #:player{:id 2}
                            #:player{:id 3}]}]})

(defresolver get-stories [_ _] ; a global resolver)
  {::pc/input  #{}
   ::pc/output [{:stories [:story/id :story/title :team/author]}]}
  (do
    (println "get-stories! resolver")
    {:stories [#:story{:id 1, :title "abc", :author "Gene"}
               #:story{:id 2, :title "def", :author "Jez"}]}))

#?(:cljs
   (do

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
         (let [ident [:component/id :com.example.ui.stories-forms/StoriesCustom]
               props (get-in @state ident)
               {:ui/keys [all-stories current-story]} props
               story-id (second current-story)]
           (println "mutation: scroll-to-element: story-id: " story-id)
           (scroll-into-view story-id))))

     (comment
       (get-in @state [:ui/mode])
       (swap! state assoc-in [:ui/mode] :search)
       ,)

     (defn get-mode
       [state]
       (->> (get-in @state [:ui/mode]) :mode))

     (>defn get-state-and-stories
       " given mode, return ident (where current value will live) and stories (which reside in its scope) "
       [state mode] [map? keyword? => map?]
       (let [ident (case mode
                     :search [:component/id :com.example.ui.stories-forms/StoriesSearch]
                     :main   [:component/id :com.example.ui.stories-forms/StoriesCustom]
                     nil)
             props (get-in state ident)
             {:ui/keys [all-stories stories-search-results current-story]} props
             ; if you want the denormalized props, use db->tree to get maps of maps,
             ; like in UI
             source-stories (case mode
                              :search stories-search-results
                              :main all-stories)]
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

     (defmutation next-story
       [params]
       (action [{:keys [ref app state]}]
         ; ref is the ident of the component that invoked the mutation
         ; => [:component/id ::Root8]
         (println "mutation: next-story: mode: " (get-mode state))
         (let [ident-and-stories (get-state-and-stories @state (get-mode state))
               {:keys [source-ident source-stories]} ident-and-stories
               props             (get-in @state source-ident)
               {:ui/keys [current-story]} props
               ;_                 (println "source-stories: " source-stories)
               pair-of-interest  (get-next-story-ident-from-action ident-and-stories current-story :up)]
           (when-let [next-story-ident (second pair-of-interest)]
             (let [new-story-id (second next-story-ident)]
               (println "next story: story-id: " new-story-id)
               (df/load! app next-story-ident
                         (rc/nc [:story/id :story/author :story/content :story/title])
                         {:target (conj source-ident :ui/current-story)}))))))
               ; (scroll-into-view new-story-id))))))



     (defmutation previous-story
       [params]
       (action [{:keys [app state]}]
               (let [ident-and-stories (get-state-and-stories @state (get-mode state))
                     {:keys [source-ident source-stories]} ident-and-stories
                     props             (get-in @state source-ident)
                     {:ui/keys [current-story]} props
                     ;_                 (println "source-stories: " source-stories)
                     pair-of-interest  (get-next-story-ident-from-action ident-and-stories current-story :down)]
                 (when-let [prev-story-ident (first pair-of-interest)]
                   (df/load! app prev-story-ident
                             (rc/nc [:story/id :story/author :story/content :story/title])
                             {:target (conj source-ident :ui/current-story)})))))

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
            :params {:search/search-query (:query params)}})))))





(def resolvers [])
(def mutations [])
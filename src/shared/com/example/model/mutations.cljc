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
    [taoensso.timbre :as log]))

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

     (defmutation next-story
       [params]
       (action [{:keys [app state]}]
         (let [ident [:component/id :com.example.ui.stories-forms/StoriesCustom]
               props (get-in @state ident)
               ; if you want the denormalized props, use db->tree to get maps of maps,
               ; like in UI
               {:ui/keys [all-stories current-story]} props
               story-pairs (partition 2 1 all-stories)
               pair-of-interest (time (first (filter (fn [x]
                                                       (= (first x)
                                                          current-story))
                                                     story-pairs)))]
           (when-let [next-story-ident (second pair-of-interest)]
             (let [new-story-id (second next-story-ident)]
              ; [:story/id [:story/id "K3Y7GLlRfaBDsUWYD0Wu … 70d2:ae3a36:ad5391a1" ]]
              ; (log/spy :warn pair-of-interest)
              ; (log/spy :warn (->> pair-of-interest second))
               (println "next story: story-id: " new-story-id)
               #_(merge/merge-component!
                   app
                   stories/StoriesCustom
                   {:ui/current-story [:story/id new-story-id]})
               (df/load! app next-story-ident
                         (rc/nc [:story/id :story/author :story/content :story/title])
                         {:target (conj ident :ui/current-story)}))))))
               ; (scroll-into-view new-story-id))))))



     (defmutation previous-story
       [params]
       (action [{:keys [app state]}]
               (let [ident [:component/id :com.example.ui.stories-forms/StoriesCustom]
                     props (get-in @state ident)
                     ; if you want the denormalized props, use db->tree to get maps of maps,
                     ; like in UI
                     {:ui/keys [all-stories current-story]} props
                     story-pairs (partition 2 1 all-stories)
                     pair-of-interest (time (first (filter (fn [x]
                                                             (= (second x)
                                                                current-story))
                                                           story-pairs)))]
                 (when-let [prev-story-ident (first pair-of-interest)]
                   (df/load! app prev-story-ident
                             (rc/nc [:story/id :story/author :story/content :story/title])
                             {:target (conj ident :ui/current-story)})))))

     (defmutation top-story
       [params]
       (action [{:keys [app state]}]
               (let [ident       [:component/id :com.example.ui.stories-forms/StoriesCustom]
                     props       (get-in @state ident)
                     {:ui/keys [all-stories current-story]} props
                     target-ident (first all-stories)]
                 (println "top-story: " target-ident)
                 (when target-ident
                   (df/load! app target-ident
                             (rc/nc [:story/id :story/author :story/content :story/title])
                             {:target (conj ident :ui/current-story)})))))

     (defmutation bottom-story
       [params]
       (action [{:keys [app state]}]
               (let [ident [:component/id :com.example.ui.stories-forms/StoriesCustom]
                     props (get-in @state ident)
                     {:ui/keys [all-stories current-story]} props
                     target-ident (last all-stories)]
                 (println "bottom-story: " target-ident)
                 (when target-ident
                   (df/load! app target-ident
                             (rc/nc [:story/id :story/author :story/content :story/title])
                             {:target (conj ident :ui/current-story)})))))))




#?(:cljs
   (defmutation get-story
     [params]
     (action [{:keys [app state]}]
             (do
               (println "mutation: get-story: params"
                        (with-out-str (pp/pprint params)))
               (println "mutation: get-story: state"
                        (with-out-str (pp/pprint @state)))
               (let [retval #:story{:id (:story/id params)
                                    :title (:story/title params)
                                    :author (:story/author params)
                                    :content "AAAA"}]
                 (println "mutation: new current-story: state"
                          (with-out-str (pp/pprint retval)))
                 (reset! state (assoc @state :current-story retval)))))
     (remote [env] true)))



(def resolvers [])
(def mutations [])
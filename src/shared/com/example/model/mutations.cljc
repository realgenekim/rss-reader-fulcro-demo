(ns com.example.model.mutations
  (:require
    [com.wsscode.pathom.connect :as pc :refer [defresolver]]
    #?@(:clj
        [[com.wsscode.pathom.connect :as pc :refer [defmutation]]
         ;[com.example.components.parser :as p]]
         [com.example.components.database-queries :as db]]
        :cljs
        [[com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
         [com.fulcrologic.fulcro.components :as comp]])
         ;[com.example.ui.stories-forms :as s]])
    [clojure.pprint :as pp]))

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
   (defmutation get-story
     [params]
     (action [{:keys [app state]}]
             (do
               (println "mutation: get-story: params"
                        (with-out-str (pp/pprint params)))
               (println "mutation: get-story: state"
                        (with-out-str (pp/pprint @state)))
               (let [retval #:story{:id (:story-list/id params)
                                    :title (:story-list/title params)
                                    :author (:story-list/author params)
                                    :content "AAAA"}]
                 (println "mutation: new current-story: state"
                          (with-out-str (pp/pprint retval)))
                 (reset! state (assoc @state :current-story retval)))))
     (remote [env] true)))
;(remote [env] true) ; see client/app definitions for remotes
;(remote [env] (m/returning env :fulcro.fulcro-exercises/Story))))
;(let [retval (m/returning env SaveYouTubePlaylistComponent)]
;  (println "mutation: retval: " retval)
;  retval)) ; see client/app definitioons for remotes
;#_ (my-custom-remote [env] (do-whatever)))




;
; set-story
;

#?(:clj
   (pc/defmutation set-story
     [env params]
     {::pc/input [:story-list/id]}
     {::pc/output [:story/id :story/title :story/author :story/content]}
     (do
       (println "set-story: params: " params)
       (let [ret (db/get-story-by-id env (:story-list/id params))]
         (println "ret: " (assoc ret :story/content "xxx"))
         ret))))

     ;  (let [retval (com.example.components.parser/parser com.example.components.config/config
     ;                                                     [{[:story-list/id "K3Y7GLlRfaBDsUWYD0WuXjH/byGbQnwaMWp+PEBoUZw=_13ef0cdbc18:15c0fac:70d63bab"]
     ;                                                       [:story/author :story/content :story/id :story/title]}])]
     ;    (println retval)
     ;    #:story{:id 1 :title "abc" :author "Gene" :content "AAAA"}))))

#?(:cljs
    (comp/defsc FullStory [_ params]
                {:query [:story-list/id :story/id :story/content :story/title :story/author]
                 :ident :story-list/id}))
                ;(println "FullStory: current-story: " current-story)
                ;(println "FullStory: params: " params)
                ;(dom/div (dom/h2 "Full Current Story: " (:story/content params)
                ;                           (dom/p (str (:story/id params)
                ;                                       (:story/author params)
                ;                                       (:story/title params)
                ;                                       (:story/content params)))
                ;                           (dom/div {:dangerouslySetInnerHTML {:__html "<strong> hello! </strong"}})))))
#?(:cljs
    (defmutation set-story
      [params]
      (action [{:keys [app state]}]
              (do
                (println "mutation: set-story: params"
                         (with-out-str (pp/pprint params)))))
                ;(println "mutation: set-story: state"
                ;         (with-out-str (pp/pprint @state)))
                ;(reset! state (assoc @state :current-story
                ;                            #:story{:id (:story-list/id params)
                ;                                    :title (:story-list/title params)
                ;                                    :author (:story-list/author params)
                ;                                    :content "AAAA"}))))
      (remote [env]
              (do
                ;(println "set-story: remote:" env)
                true))))
              ;(do
              ;  (println "ENV: " env)
              ;  (-> env
              ;      (m/with-target [:current-story])
              ;      (m/returning FullStory (assoc params :story/content "XXXXXXX")))))))

;#?(:cljs
;   (pc/defmutation get-stories [{:keys [orgnr]}]
;               (action [{:keys [app state ref]}
;                        (df/set-load-marker! app :simulate-bill-run :loading)])
;               (remote [env] (m/returning env (doto (comp/registry-key->class :minbedrift.ui.kostnadsdeling.ui/SimulateBillRun) (assert))))
;               (refresh [_] [:tem-organization/organization-number orgnr])
;               (ok-action [{:keys [app state]}] (df/remove-load-marker! app :simulate-bill-run))
;               (error-action [{:keys [app state]}](df/set-load-marker! app :simulate-bill-run :failed)))
;   :clj
;   (pc/defmutation simulate-bill-run [env {:keys [orgnr]}]
;     {;::pc/params [:orgnr]
;      ::pc/output [:tem-organization/organization-number :bill-run/logs]}
;     (assert orgnr "orgnr is required")
;     (let [logs
;           (:logs (kostnadsdeling-data/simulate-bill-run
;                    (doto (get-in env [:com.fulcrologic.rad.database-adapters.sql/connection-pools :minbedrift]) (assert "Missing MB DB"))
;                    orgnr))]
;       {:tem-organization/organization-number orgnr
;        :bill-run/logs (vec logs)})))


(def resolvers [])
(def mutations [set-story])
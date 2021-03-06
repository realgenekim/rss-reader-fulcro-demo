(ns com.example.client
  (:require
    [com.example.ui :as ui :refer [Root]]
    [com.example.ui.login-dialog :refer [LoginForm]]
    [com.fulcrologic.fulcro.application :as app]
    [com.fulcrologic.fulcro.components :as comp]
    [com.fulcrologic.fulcro.mutations :as m]
    [com.fulcrologic.rad.application :as rad-app]
    [com.fulcrologic.rad.report :as report]
    [com.fulcrologic.rad.authorization :as auth]
    [com.fulcrologic.rad.rendering.semantic-ui.semantic-ui-controls :as sui]
    [com.fulcrologic.fulcro.algorithms.timbre-support :refer [console-appender prefix-output-fn]]
    [taoensso.timbre :as log]
    [taoensso.tufte :as tufte :refer [profile]]
    [com.fulcrologic.rad.type-support.date-time :as datetime]
    [com.fulcrologic.fulcro.algorithms.tx-processing.synchronous-tx-processing :as stx]
    [com.fulcrologic.rad.routing.html5-history :as hist5 :refer [html5-history]]
    [com.fulcrologic.rad.routing.history :as history]
    [com.fulcrologic.rad.routing :as routing]
    [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
    [com.fulcrologic.fulcro.mutations :as mutation]
    [com.fulcrologic.fulcro.components :as comp]
    [com.example.ui.stories-forms :as stories]
    [com.example.model.mutations]
    ["mousetrap" :as mousetrap]))

(defonce stats-accumulator
  (tufte/add-accumulating-handler! {:ns-pattern "*"}))

(m/defmutation fix-route
  "Mutation. Called after auth startup. Looks at the session. If the user is not logged in, it triggers authentication"
  [_]
  (action [{:keys [app]}]
    (let [logged-in (auth/verified-authorities app)]
      (if (empty? logged-in)
        (routing/route-to! app ui/LandingPage {})
        (hist5/restore-route! app ui/LandingPage {})))))

(defn setup-RAD [app]
  (rad-app/install-ui-controls! app sui/all-controls)
  (report/install-formatter! app :boolean :affirmation (fn [_ value] (if value "yes" "no"))))

(defonce app (rad-app/fulcro-rad-app {}))
; (defonce app (rad-app/fulcro-rad-app {:optimized-render! com.fulcrologic.fulcro.rendering.keyframe-render2/render!}))

(defn refresh []
  ;; hot code reload of installed controls
  (log/info "Reinstalling controls")
  (setup-RAD app)
  (comp/refresh-dynamic-queries! app)
  (app/mount! app Root "app"))


(comment
  (let [state (app/current-state app)]
    (com.fulcrologic.fulcro.algorithms.denormalize/db->tree
      (comp/get-query Root) state state))
  ,)

(m/declare-mutation next-story 'com.example.model.mutations/next-story)
(m/declare-mutation previous-story 'com.example.model.mutations/previous-story)
(m/declare-mutation top-story 'com.example.model.mutations/top-story)
(m/declare-mutation bottom-story 'com.example.model.mutations/bottom-story)
(m/declare-mutation scroll-to-element 'com.example.model.mutations/scroll-to-element)
(m/declare-mutation switch-mode 'com.example.model.mutations/switch-mode)
(m/declare-mutation random-story 'com.example.model.mutations/random-story)
(m/declare-mutation toggle-help 'com.example.model.mutations/toggle-help)

(defn init-keyboard-bindings []
  (.bind js/Mousetrap "h" #(js/alert "keyboard shortcut!"))
  ; (comp/transact! this '[(get-story {:story/id 1})])
  (.bind js/Mousetrap "j" #(comp/transact! app [(next-story {})]))
  (.bind js/Mousetrap "k" #(comp/transact! app [(previous-story {})]))
  (.bind js/Mousetrap "t" #(comp/transact! app [(top-story {})]))
  (.bind js/Mousetrap "b" #(comp/transact! app [(bottom-story {})]))
  (.bind js/Mousetrap "$" #(comp/transact! app [(bottom-story {})]))
  (.bind js/Mousetrap "^" #(comp/transact! app [(scroll-to-element {})]))
  (.bind js/Mousetrap "<" #(comp/transact! app [(switch-mode {})]))
  (.bind js/Mousetrap ">" #(comp/transact! app [(switch-mode {})]))
  (.bind js/Mousetrap "r" #(comp/transact! app [(random-story {})]))
  (.bind js/Mousetrap "?" #(comp/transact! app [(toggle-help {})])))

(defn init []
  (log/merge-config! {:output-fn prefix-output-fn
                      :appenders {:console (console-appender)}})
  (log/info "Starting App")
  ;; default time zone (should be changed at login for given user)
  (datetime/set-timezone! "America/Los_Angeles")
  ;; Avoid startup async timing issues by pre-initializing things before mount
  (app/set-root! app Root {:initialize-state? true})
  (dr/initialize! app)
  (setup-RAD app)
  ;(dr/change-route! app ["landing-page"])
  (history/install-route-history! app (html5-history))
  ;(auth/start! app [LoginForm] {:after-session-check `fix-route})
  (init-keyboard-bindings)
  (app/mount! app Root "app" {:initialize-state? false})
  ; when there's no auth, just call this to route to what URL bar says: (normally called in fix-route)
  (hist5/restore-route! app stories/StoriesContainer {}))

(comment)



(defonce performance-stats (tufte/add-accumulating-handler! {}))

(defn pperf
  "Dump the currently-collected performance stats"
  []
  (let [stats (not-empty @performance-stats)]
    (println (tufte/format-grouped-pstats stats))))




(ns com.example.ui.button-toys-form
  (:require
    #?(:clj  [com.fulcrologic.fulcro.dom-server :as dom :refer [div label input]]
       :cljs [com.fulcrologic.fulcro.dom :as dom :refer [div label input]])
    [com.fulcrologic.fulcro.mutations :as mutation :refer [declare-mutation]]
    [com.fulcrologic.fulcro.components :as comp]
    #?(:cljs [goog.string :as gstring])
    #?(:cljs [portal.web :as pw])))


;
; simple button test
;

(mutation/defmutation bump-number [ignored]
  (action [{:keys [ref state]}]
    (println "bump-number")
    (swap! state update-in [:component/id ::ButtonTest1 :ui/number2] inc)))

(comp/defsc ButtonTest1
  [this {number2 :ui/number2
         :as     props}]
  {:query [:ui/number2]
   :ident         (fn [] [:component/id ::ButtonTest1])
   :initial-state {:ui/number2 0}
   :route-segment ["button-test-1"]}
  (dom/div
    (dom/h2 "Button Test 1:")
    (dom/p "This demo shows updating state that resides in this component:")
    (println "number: " number2)
    (println "props: " props)
    (dom/button {:onClick #(comp/transact! this [(bump-number {})])}
      "You've clicked this button " number2 " times.")))


(comp/defsc ButtonTest1b
  [this {:ui/keys [number2] :as props}]
  {:query [{:ui/number2 (comp/get-query ButtonTest1)}]
   :ident (fn [_] [:component/id ::ButtonTest1b])
   :initial-state (fn [_]
                    {:ui/number2 (comp/get-initial-state ButtonTest1)})
   :route-segment ["button-test-1b"]}
  (dom/div
    (dom/h2 "Button Test 1b")
    (dom/p "Embed button from Button Test 1")
    (dom/p ":ui/number2: " (str number2))))



(def ui-button-test-1 (comp/factory ButtonTest1))

;
; support multiple buttons per page, all pointing to the same ident
;

(mutation/defmutation bump-number2 [{:keys [field]}]
  ; field: e.g., :ui/number2
  (action [{:keys [ref state]}]
    ; ref is the ident of the component that invoked the mutation
    ; => [:component/id ::Root8]
    (let [path (conj ref field)]
      ;(do
      (println "bump-number2")
      (swap! state update-in path inc))))

(comp/defsc ButtonTest2
  [this {:ui/keys [number2] :as props}]
  {:query [:ui/number2]
   :initial-state {:ui/number2 3}
   :route-segment ["root8"]
   ;:ident :ui/number2}
   ;:ident         (fn [] [:component/id ::Root8])
   :ident         (fn [] [:component/id ::Root8])}
  ;:ident         (fn [] [:ui/number2])}
  (dom/div
    (dom/h2 "Button Test 2")
    (dom/p "This is an example of two buttons that modify a single stagte: they modify a value associated with this component ident: :component/id ::Root8.")
    (println "Root8: props: " props)
    (dom/button {:onClick #(comp/transact! this [(bump-number2 {:field :ui/number2})])}
      "You've clicked this button " number2 " times.")
    (dom/button {:onClick #(comp/transact! this [(bump-number2 {:field :ui/number2})])}
      "You've clicked this button " number2 " times.")))


(comment
  (def state (->> com.example.client/app (:com.fulcrologic.fulcro.application/state-atom)))
  (get-in @state [:ui/number2])
  (swap! state update :ui/number2 inc)

  (get-in @state [:ui/mode])
  (swap! state assoc-in [:ui/mode] :search)
  ,)

;
; button test 3: multiple button states on same page
;

(comp/defsc Button1
  [this {:button/keys [value]}]
  {:query [:button/id :button/value]
   :ident :button/id
   :initial-state {:button/id :param/id
                   :button/value :param/value}}
  (dom/button {:onClick #(comp/transact! this [(bump-number2 {:field :button/value})])}
    "You've clicked this button " value " times."))

(def ui-button (comp/factory Button1 {:keyfn :button/id}))

(comp/defsc ButtonTest3
  [this {:ui/keys [buttons] :as props}]
  {:query         [{:ui/buttons (comp/get-query Button1)}]
   :initial-state {:ui/buttons [{:id 1 :value 1}
                                {:id 2 :value 2}
                                {:id 2 :value 2}]}
   :route-segment ["buttontest3"]
   :ident         (fn [] [:component/id ::ButtonTest3])}
  (dom/div
    (dom/h2 "Button Test3")
    (dom/p "This is an example of modifying a value associated with this component ident: :component/id ::buttontest3.")
    (println "ButtonTest1: props: " props)
    (map ui-button buttons)))



(comment
  (comp/get-query ButtonTest1)
  ,)
(ns com.example.components.database-queries
  (:require
    ;[com.fulcrologic.rad.database-adapters.datomic :as datomic]
    ;[datomic.api :as d]
    [taoensso.timbre :as log]
    [taoensso.encore :as enc]
    [feedly :as f]))

(defn get-all-accounts
  [env query-params]
  (if-let [db (some-> (get-in env [:datomic/databases :production]) deref)]
    (let [ids (if (:show-inactive? query-params)
                nil
                nil)]
                ;(d/q [:find '[?uuid ...]
                ;      :where
                ;      ['?dbid :account/id '?uuid]] db)
                ;(d/q [:find '[?uuid ...]
                ;      :where
                ;      ['?dbid :account/active? true]
                ;      ['?dbid :account/id '?uuid]] db))]
      (mapv (fn [id] {:account/id id}) ids))
    (log/error "No database atom for production schema!")))

(comment
  (get-all-accounts)
  {:account/role #:db{:id 17592186045421},
   :password/salt "��u`sY���l�EDq",
   :account/id #uuid"ffffffff-ffff-ffff-ffff-000000000100",
   :time-zone/zone-id #:db{:id 17592186045972},
   :password/iterations 100,
   :account/active? true,
   :password/hashed-value "ZozfrISsrVeNl8Tiknfvlg4u6RmgSJ3+goV5FFpGGulbBMVSrytlIzAvq4XjOD4Cb06jdAVtevyd/kqQ+0iiyg==",
   :account/email "tony@example.com",
   :account/addresses [{:db/id 17592186046078,
                        :address/id #uuid"ffffffff-ffff-ffff-ffff-000000000001",
                        :address/street "111 Main St.",
                        :address/city "Sacramento",
                        :address/state #:db{:id 17592186045426},
                        :address/zip "99999"}],
   :db/id 17592186046079,
   :account/primary-address {:db/id 17592186046080,
                             :address/id #uuid"ffffffff-ffff-ffff-ffff-000000000300",
                             :address/street "222 Other",
                             :address/city "Sacramento",
                             :address/state #:db{:id 17592186045426},
                             :address/zip "99999"},
   :account/name "Tony"}
  ,)


; https://stackoverflow.com/questions/43722091/clojure-programmatically-namespace-map-keys
(defn map->nsmap
  "Apply the string n to the supplied structure m as a namespace."
  [m n]
  (clojure.walk/postwalk
    (fn [x]
      (if (keyword? x)
        (keyword n (name x))
        x))
    m))


(def stories (f/read-file))

(defn get-all-stories
  [env query-params]
  (log/warn "*** get-all-stories!")
  (->> stories
       ;(take 2000)
       (map #(select-keys % [:id]))
       (map (fn [m]
              (map->nsmap m "story")))))


(comment
  (->> stories
       (filter #(= (:id %)
                   "K3Y7GLlRfaBDsUWYD0WuXjH/byGbQnwaMWp+PEBoUZw=_13ef0cdbc18:15c0fac:70d63bab")))

  ,)


(defn get-full-story-by-id
  [env story-id]
  ;(log/warn "*** get-full-story! " story-id)
  (let [retval (->> stories
                    (filter #(= (:id %) story-id))
                    (map #(select-keys % [:id :author :title :published :content]))
                    (map (fn [m]
                           (assoc m :content
                                    (-> m :content :content))))
                    (map (fn [m]
                           (map->nsmap m "story"))))]
    ;(println "*** " retval)
    (first retval)))

(defn get-story-by-id-shaped
  " wrapper around get-full-story-by-id, but only retain the specified keys "
  [env story-id output-keys]
  (let [retval  (get-full-story-by-id env story-id)
        trimmed (select-keys retval output-keys)]
    ;(log/warn trimmed)
    trimmed))

(comment
  (get-story-by-id nil "K3Y7GLlRfaBDsUWYD0WuXjH/byGbQnwaMWp+PEBoUZw=_13ef0cdbc18:15c0fac:70d63bab")
  (get-story-by-id-shaped nil "K3Y7GLlRfaBDsUWYD0WuXjH/byGbQnwaMWp+PEBoUZw=_13ef0cdbc18:15c0fac:70d63bab"
                          [:story/id :story/author])
  ,)


;(if-let [db (some-> (get-in env [::datomic/databases :production]) deref)]
;  (d/q '[:find ?account-uuid .
;         :in $ ?invoice-uuid
;         :where
;         [?i :invoice/id ?invoice-uuid]
;         [?i :invoice/customer ?c]
;         [?c :account/id ?account-uuid]] db story-id)
;  (log/error "No database atom for production schema!")))




(comment
  (get-all-stories)

  (->> (take 3 stories)
       (map #(select-keys % [:id :author :title :published :content]))
       (map (fn [m]
              (assoc m :content
                       (-> m :content :content))))
       (map (fn [m]
              (map->nsmap m "story-list"))))


  (clojure.set/rename-keys m {:id        :story/id
                              :author    :story/author
                              :title     :story/author
                              :published :story/published})

  (merge
    (map (fn [x]
           {(keyword "story-list" id) ;(name (first x)))
            (second x)})
         ;{(keyword "story-list" (first x))
         ; (second x))
         m))

  (defattr all-stories :story/all-stories :ref
           {ao/target     :story/id
            ao/pc-output  [{:story/all-stories [:story/id]}]
            ao/pc-resolve (fn [{:keys [query-params] :as env} _]
                            (:clj
                             {:story/all-stories (queries/get-all-stories env query-params)}))})

  (def k1 :id)
  (->> k1
       (name)
       ((fn [k] (keyword "story-list" k))))
  (def k2 *1)
  (def k3 :story/id)
  (= k2 k3)

  ,)

(defn get-all-items
  [env {:category/keys [id]}]
  (if-let [db (some-> (get-in env [:datomic/databases :production]) deref)]
    (let [ids (if id
                nil
                nil)]
                ;(d/q '[:find [?uuid ...]
                ;       :in $ ?catid
                ;       :where
                ;       [?c :category/id ?catid]
                ;       [?i :item/category ?c]
                ;       [?i :item/id ?uuid]] db id)
                ;(d/q '[:find [?uuid ...]
                ;       :where
                ;       [_ :item/id ?uuid]] db))]
      (mapv (fn [id] {:item/id id}) ids))
    (log/error "No database atom for production schema!")))

(defn get-customer-invoices [env {:account/keys [id]}]
  (if-let [db (some-> (get-in env [:datomic/databases :production]) deref)]
    (let [ids nil]
          ;(d/q '[:find [?uuid ...]
          ;       :in $ ?cid
          ;       :where
          ;       [?dbid :invoice/id ?uuid]
          ;       [?dbid :invoice/customer ?c]
          ;       [?c :account/id ?cid]] db id)]
      (mapv (fn [id] {:invoice/id id}) ids))
    (log/error "No database atom for production schema!")))

(defn get-all-invoices
  [env query-params]
  (if-let [db (some-> (get-in env [:datomic/databases :production]) deref)]
    (let [ids nil]
          ;(d/q [:find '[?uuid ...]
          ;      :where
          ;      ['?dbid :invoice/id '?uuid]] db)]
      (mapv (fn [id] {:invoice/id id}) ids))
    (log/error "No database atom for production schema!")))

(defn get-invoice-customer-id
  [env invoice-id]
  (if-let [db (some-> (get-in env [:datomic/databases :production]) deref)]
    nil
    ;(d/q '[:find ?account-uuid .
    ;       :in $ ?invoice-uuid
    ;       :where
    ;       [?i :invoice/id ?invoice-uuid]
    ;       [?i :invoice/customer ?c]
    ;       [?c :account/id ?account-uuid]] db invoice-id)
    (log/error "No database atom for production schema!")))

(defn get-all-categories
  [env query-params]
  (if-let [db (some-> (get-in env [:datomic/databases :production]) deref)]
    (let [ids nil]
          ;(d/q '[:find [?id ...]
          ;       :where
          ;       [?e :category/label]
          ;       [?e :category/id ?id]] db)]
      (mapv (fn [id] {:category/id id}) ids))
    (log/error "No database atom for production schema!")))

(defn get-line-item-category [env line-item-id]
  (if-let [db (some-> (get-in env [:datomic/databases :production]) deref)]
    (let [id nil]
          ;(d/q '[:find ?cid .
          ;       :in $ ?line-item-id
          ;       :where
          ;       [?e :line-item/id ?line-item-id]
          ;       [?e :line-item/item ?item]
          ;       [?item :item/category ?c]
          ;       [?c :category/id ?cid]] db line-item-id)]
      id)
    (log/error "No database atom for production schema!")))

(defn get-login-info
  "Get the account name, time zone, and password info via a username (email)."
  [{:datomic/keys [databases] :as env} username]
  (enc/if-let [db @(:production databases)]
    nil))
    ;(d/pull db [:account/name
    ;            {:time-zone/zone-id [:db/ident]}
    ;            :password/hashed-value
    ;            :password/salt
    ;            :password/iterations]
    ;  [:account/email username])))

(ns status-im.anon-metrics.core
  (:require [taoensso.timbre :as log]
            [re-frame.core :as re-frame]
            [re-frame.interceptor :refer [->interceptor]]
            [status-im.async-storage.core :as async-storage]
            [status-im.multiaccounts.update.core :as multiaccounts.update]
            [status-im.utils.async :refer [async-periodic-exec async-periodic-stop!]]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.utils.platform :as platform]
            [status-im.utils.build :as build]
            [status-im.utils.fx :as fx]
            [status-im.anon-metrics.transformers :as txf]))

(defonce events-foyer (atom []))
(defonce periodic-tasks-chan (atom nil))

(defn onboard-events
  "Check if there are any events in the foyer,
  flush them to the backend and clear foyer on-success."
  []
  (let [outstanding-events @events-foyer]
    (when (seq outstanding-events)
      (reset! events-foyer [])
      (json-rpc/call
       {:method     "appmetrics_saveAppMetrics"
        :params     [outstanding-events]
        :on-success #()
        :on-error   (fn [err]
                      (log/error {:error  err
                                  :events outstanding-events})
                      (log/warn "The logged events will be rejected"))}))))

(re-frame/reg-fx
 ::transfer-data
 (fn [transfer?]
   (if (and transfer?
            ;; double run safety
            (not @periodic-tasks-chan))
     (do
       (log/info "[anon-metrics] Start collection service")
       (reset! periodic-tasks-chan
               ;; interval = 4000 ms (run every `interval` ms)
               ;; timeout = 5000 ms (exit if the fn doesn't exit within `timeout` ms)
               (async-periodic-exec onboard-events 4000 5000)))
     (do
       (log/info "[anon-metrics] Stop collection service")
       (when @periodic-tasks-chan
         (async-periodic-stop! @periodic-tasks-chan)
         (onboard-events) ; final onboard, will save and clear any pending events
         (reset! periodic-tasks-chan nil))))))

(fx/defn start-transferring
  [_]
  {::transfer-data true})

(fx/defn stop-transferring
  [_]
  {::transfer-data false})

(defn transform-and-log [context]
  (when-let [transformed-payload (txf/transform context)]
    (swap!
     events-foyer
     conj
     {:event       (-> context :coeffects :event first)
      :value       transformed-payload
      :app_version build/version
      :os          platform/os})))

(re-frame/reg-fx ::transform-and-log transform-and-log)

(defn catch-events-before [context]
  (transform-and-log context)
  context)

(def interceptor
  (->interceptor
   :id     :catch-events
   :before catch-events-before))

(fx/defn hoax-capture-event
  "Due to usage of fx/defn with fx/merge, it might not be able to
  intercept some events (like navigate-to-cofx). In cases like that,
  this hoax capture event can be used in conjunction with `fx/merge`"
  {:events [::hoax-capture-event]}
  [_ {:keys [og-event]}]
  ;; re-shape event to look like a context object
  {::transform-and-log {:coeffects {:event og-event}}})

(fx/defn fetch-local-metrics-success
  {:events [::fetch-local-metrics-success]}
  [{:keys [db]} {:keys [metrics clear-existing?]}]
  {:db (-> db
           (as-> db
                 (if clear-existing?
                   (assoc db :anon-metric-events metrics)
                   (update db :anon-metric-events concat metrics)))
           (dissoc :anon-metrics-fetching?)
           (assoc :anon-metrics-all-fetched? (-> metrics
                                                 seq
                                                 boolean
                                                 not)))})

(fx/defn fetch-local-metrics
  {:events [::fetch-local-metrics]}
  [{:keys [db]} {:keys [limit offset clear-existing?]}]
  {::json-rpc/call [{:method     "appmetrics_getAppMetrics"
                     :params     [(or limit 3) (or offset 0)]
                     :on-success #(re-frame/dispatch
                                   [::fetch-local-metrics-success
                                    {:metrics (or % [])
                                     :clear-existing? clear-existing?}])}]
   :db (assoc db :anon-metrics-fetching? true)})

(re-frame/reg-sub
 ::events
 (fn [db]
   (get db :anon-metric-events [])))

(re-frame/reg-sub
 ::fetching?
 (fn [db]
   (get db :anon-metrics-fetching?)))

(re-frame/reg-sub
 ::all-fetched?
 (fn [db]
   (get db :anon-metrics-all-fetched?)))

(fx/defn set-opt-in-screen-displayed-flag
  [_]
  {::async-storage/set! {:anon-metrics/opt-in-screen-displayed? true}})

(fx/defn opt-in
  {:events [::opt-in]}
  [cofx enabled?]
  (fx/merge cofx
            (set-opt-in-screen-displayed-flag)
            (if enabled?
              (start-transferring)
              (stop-transferring))
            (multiaccounts.update/multiaccount-update
             :anon-metrics/should-send? enabled?
             {:on-success #(re-frame/dispatch
                            [:navigate-reset
                             {:index  0
                              :routes [{:name :tabs}]}])})))

(fx/defn fetch-opt-in-screen-displayed?
  {:events [::fetch-opt-in-screen-displayed?]}
  [cofx]
  {::async-storage/get
   {:keys [:anon-metrics/opt-in-screen-displayed?]
    ;; the first arg to callback is a map where keys are the requested keys
    ;; and vals are their values from async storage
    ;; the thread-last macro is used  to generate
    ;; a vector like [:set :key :value]
    :cb #(re-frame/dispatch (->> %
                                 seq
                                 first
                                 (into [:set])))}})

(re-frame/reg-sub
 ::opt-in-screen-displayed?
 (fn [db]
   (get db :anon-metrics/opt-in-screen-displayed?)))

(comment
  ;; read the database
  (def events-in-db (atom nil))
  (->> events-in-db
       deref
       (take-last 5))
  (json-rpc/call {:method     "appmetrics_getAppMetrics"
                  :params     [1000 0] ; limit, offset
                  :on-success #(reset! events-in-db %)}))


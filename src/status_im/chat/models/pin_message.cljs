(ns status-im.chat.models.pin-message
  (:require [status-im.chat.models.message-list :as message-list]
            [status-im.constants :as constants]
            [status-im.data-store.messages :as data-store.messages]
            [status-im.utils.fx :as fx]
            [taoensso.timbre :as log]
            [re-frame.core :as re-frame]
            [status-im.chat.models.loading :as loading]))

(fx/defn handle-failed-loading-pin-messages
  {:events [::failed-loading-pin-messages]}
  [{:keys [db]} current-chat-id _ err]
  (log/error "failed loading pin messages" current-chat-id err)
  (when current-chat-id
    {:db (assoc-in db [:pagination-info current-chat-id :loading-pin-messages?] false)}))

(fx/defn pin-messages-loaded
  {:events [::pin-messages-loaded]}
  [{db :db} chat-id session-id {:keys [cursor messages]}]
  (when-not (and (get-in db [:pagination-info chat-id :pin-messages-initialized?])
                 (not= session-id
                       (get-in db [:pagination-info chat-id :pin-messages-initialized?])))
    (let [pin-messages (map #(assoc-in % [:pinned?] true)
                            messages)
          already-loaded-pin-messages (get-in db [:pin-messages chat-id] {})
          {:keys [all-messages new-messages]} (reduce (fn [{:keys [all-messages] :as acc}
                                                           {:keys [message-id alias from]
                                                            :as   message}]
                                                        (cond-> acc
                                                          (nil? (get all-messages message-id))
                                                          (update :new-messages conj message)

                                                          :always
                                                          (update :all-messages assoc message-id message)))
                                                      {:all-messages already-loaded-pin-messages
                                                       :new-messages []}
                                                      pin-messages)
          messages-id-list (map :message-id pin-messages)
          current-clock-value (get-in db [:pagination-info chat-id :pin-cursor-clock-value])
          clock-value (when cursor (loading/cursor->clock-value cursor))]
      {:db (-> db
               ((fn [db]
                  (reduce (fn [acc message-id]
                            (if (get-in acc [:messages chat-id message-id])
                              (assoc-in acc [:messages chat-id message-id :pinned?]
                                        true)
                              acc))
                          db
                          messages-id-list)))
               (update-in [:pagination-info chat-id :pin-cursor-clock-value]
                          #(if (and (seq cursor) (or (not %) (< clock-value %)))
                             clock-value
                             %))
               (update-in [:pagination-info chat-id :pin-cursor]
                          #(if (or (empty? cursor) (not current-clock-value) (< clock-value current-clock-value))
                             cursor
                             %))
               (assoc-in [:pagination-info chat-id :loading-pin-messages?] false)
               (assoc-in [:pin-messages chat-id] all-messages)
               (update-in [:pin-message-lists chat-id] message-list/add-many new-messages)
               (assoc-in [:pagination-info chat-id :all-pin-loaded?]
                         (empty? cursor)))})))

(fx/defn load-more-pin-messages
  {:events [:load-more-pin-messages]}
  [{:keys [db]} chat-id first-request]
  (when-let [session-id (get-in db [:pagination-info chat-id :pin-messages-initialized?])]
    (when (and
           (not (get-in db [:pagination-info chat-id :all-pin-loaded?]))
           (not (get-in db [:pagination-info chat-id :loading-pin-messages?])))
      (let [cursor (get-in db [:pagination-info chat-id :pin-cursor])]
        (when (or first-request cursor)
          (merge
           {:db (assoc-in db [:pagination-info chat-id :loading-pin-messages?] true)}
           (data-store.messages/pinned-message-by-chat-id-rpc
            chat-id
            cursor
            constants/default-number-of-pin-messages
            #(re-frame/dispatch [::pin-messages-loaded chat-id session-id %])
            #(re-frame/dispatch [::failed-loading-pin-messages chat-id session-id %]))))))))

(fx/defn send-pin-message
  "Pin message, rebuild pinned messages list"
  {:events [::send-pin-message]}
  [{:keys [db] :as cofx} {:keys [chat-id message-id pinned?] :as pin-message}]
  (fx/merge cofx
            {:db (as-> db $
                   (assoc-in $ [:messages chat-id message-id :pinned?] pinned?)
                   (if pinned?
                     (-> $
                         (update-in [:pin-message-lists chat-id] message-list/add pin-message)
                         (assoc-in [:pin-messages chat-id message-id] pin-message))
                     (-> $
                         (update-in [:pin-message-lists chat-id] message-list/remove-message pin-message)
                         (update-in [:pin-messages chat-id] dissoc message-id))))}
            (data-store.messages/send-pin-message {:chat-id (pin-message :chat-id)
                                                   :message-id (pin-message :message-id)
                                                   :pinned (pin-message :pinned?)})))

(fx/defn load-more-messages-for-current-chat
  {:events [:chat.ui/load-more-pin-messages-for-current-chat]}
  [{:keys [db] :as cofx}]
  (load-more-pin-messages cofx (:current-chat-id db) false))

(fx/defn load-pin-messages
  {:events [::load-pin-messages]}
  [{:keys [db now] :as cofx} chat-id]
  (when-not (get-in db [:pagination-info chat-id :pin-messages-initialized?])
    (fx/merge cofx
              {:db (assoc-in db [:pagination-info chat-id :pin-messages-initialized?] now)}
              (load-more-pin-messages chat-id true))))

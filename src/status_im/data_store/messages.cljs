(ns status-im.data-store.messages
  (:require [clojure.set :as clojure.set]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.utils.fx :as fx]
            [taoensso.timbre :as log]))

(defn ->rpc [{:keys [content] :as message}]
  (cond-> message
    content
    (assoc :text (:text content)
           :sticker (:sticker content))
    :always
    (clojure.set/rename-keys {:chat-id :chat_id
                              :message-id        :messageId
                              :whisper-timestamp :whisperTimestamp
                              :community-id :communityId
                              :clock-value :clock
                              :pinned? :pinned})))

(defn <-rpc [message]
  (-> message
      (clojure.set/rename-keys {:id :message-id
                                :whisperTimestamp :whisper-timestamp
                                :commandParameters :command-parameters
                                :messageType :message-type
                                :localChatId :chat-id
                                :communityId :community-id
                                :contentType  :content-type
                                :clock  :clock-value
                                :quotedMessage :quoted-message
                                :outgoingStatus :outgoing-status
                                :audioDurationMs :audio-duration-ms
                                :new :new?})

      (update :quoted-message clojure.set/rename-keys {:parsedText :parsed-text :communityId :community-id})
      (update :outgoing-status keyword)
      (update :command-parameters clojure.set/rename-keys {:transactionHash :transaction-hash
                                                           :commandState :command-state})
      (assoc :content {:chat-id (:chatId message)
                       :text (:text message)
                       :image (:image message)
                       :sticker (:sticker message)
                       :ens-name (:ensName message)
                       :line-count (:lineCount message)
                       :parsed-text (:parsedText message)
                       :links (:links message)
                       :rtl? (:rtl message)
                       :response-to (:responseTo message)}
             :outgoing (boolean (:outgoingStatus message)))
      (dissoc :ensName :chatId :text :rtl :responseTo :image :sticker :lineCount :parsedText :links)))

(defn update-outgoing-status-rpc [message-id status]
  {::json-rpc/call [{:method (json-rpc/call-ext-method "updateMessageOutgoingStatus")
                     :params [message-id status]
                     :on-success #(log/debug "updated message outgoing stauts" message-id status)
                     :on-failure #(log/error "failed to update message outgoing status" message-id status %)}]})

(defn messages-by-chat-id-rpc [chat-id
                               cursor
                               limit
                               on-success
                               on-failure]
  {::json-rpc/call [{:method     (json-rpc/call-ext-method "chatMessages")
                     :params     [chat-id cursor limit]
                     :on-success (fn [result]
                                   (on-success (update result :messages #(map <-rpc %))))
                     :on-failure on-failure}]})

(defn mark-seen-rpc [chat-id ids on-success]
  {::json-rpc/call [{:method (json-rpc/call-ext-method "markMessagesSeen")
                     :params [chat-id ids]
                     :on-success #(do
                                    (log/debug "successfully marked as seen" %)
                                    (when on-success (on-success chat-id ids %)))
                     :on-failure #(log/error "failed to get messages" %)}]})

(defn delete-message-rpc [id]
  {::json-rpc/call [{:method (json-rpc/call-ext-method "deleteMessage")
                     :params [id]
                     :on-success #(log/debug "successfully deleted message" id)
                     :on-failure #(log/error "failed to delete message" % id)}]})

(defn delete-messages-from-rpc [author]
  {::json-rpc/call [{:method (json-rpc/call-ext-method "deleteMessagesFrom")
                     :params [author]
                     :on-success #(log/debug "successfully deleted messages from" author)
                     :on-failure #(log/error "failed to delete messages from" % author)}]})

(defn delete-messages-by-chat-id-rpc [chat-id]
  {::json-rpc/call [{:method (json-rpc/call-ext-method "deleteMessagesByChatID")
                     :params [chat-id]
                     :on-success #(log/debug "successfully deleted messages by chat-id" chat-id)
                     :on-failure #(log/error "failed to delete messages by chat-id" % chat-id)}]})

(defn send-pin-message-rpc [pin-message]
  {::json-rpc/call [{:method (json-rpc/call-ext-method "sendPinMessage")
                     :params [(->rpc pin-message)]
                     :on-success #(log/debug "successfully pinned message" pin-message)
                     :on-failure #(log/error "failed to pin message" % pin-message)}]})

(defn pinned-message-by-chat-id-rpc [chat-id
                                     cursor
                                     limit
                                     on-success
                                     on-failure]
  {::json-rpc/call [{:method     (json-rpc/call-ext-method "chatPinnedMessages")
                     :params     [chat-id cursor limit]
                     :on-success (fn [result]
                                   (on-success (update result :messages #(map <-rpc %))))
                     :on-failure on-failure}]})

(fx/defn delete-message [cofx id]
  (delete-message-rpc id))

(fx/defn delete-messages-from [cofx author]
  (delete-messages-from-rpc author))

(fx/defn mark-messages-seen [cofx chat-id ids on-success]
  (mark-seen-rpc chat-id ids on-success))

(fx/defn update-outgoing-status [cofx message-id status]
  (update-outgoing-status-rpc message-id status))

(fx/defn delete-messages-by-chat-id [cofx chat-id]
  (delete-messages-by-chat-id-rpc chat-id))

(fx/defn send-pin-message [cofx pin-message]
  (send-pin-message-rpc pin-message))

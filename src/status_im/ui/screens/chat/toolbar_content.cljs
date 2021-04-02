(ns status-im.ui.screens.chat.toolbar-content
  (:require [status-im.i18n.i18n :as i18n]
            [status-im.constants :as constants]
            [status-im.ui.components.chat-icon.screen :as chat-icon.screen]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.chat.styles.main :as st]
            [re-frame.core :as re-frame]))

(defn- group-last-activity [{:keys [contacts public?]}]
  [react/view {:flex-direction :row}
   [react/text {:style st/toolbar-subtitle}
    (if public?
      (i18n/label :t/public-group-status)
      (let [cnt (count contacts)]
        (if (zero? cnt)
          (i18n/label :members-active-none)
          (i18n/label-pluralize cnt :t/members-active))))]])

(defn one-to-one-name [from]
  (let [[first-name _] @(re-frame.core/subscribe [:contacts/contact-two-names-by-identity from])]
    [react/text {:style               st/chat-name-text
                 :number-of-lines     1
                 :accessibility-label :chat-name-text}
     first-name]))

(defn contact-indicator [contact-id]
  (let [added? @(re-frame/subscribe [:contacts/contact-added? contact-id])]
    [react/view {:flex-direction :row}
     [react/text {:style st/toolbar-subtitle}
      (if added?
        (i18n/label :chat-is-a-contact)
        (i18n/label :chat-is-not-a-contact))]]))

(defn toolbar-content-view-inner [chat-info]
  (let [{:keys [group-chat invitation-admin color chat-id contacts chat-type chat-name public?]}
        chat-info]
    [react/view {:style st/toolbar-container}
     [react/view {:margin-right 10}
      [react/touchable-highlight {:on-press #(when-not group-chat (re-frame/dispatch [:chat.ui/show-profile chat-id]))}
       [chat-icon.screen/chat-icon-view-toolbar chat-id group-chat chat-name color]]]
     [react/view {:style st/chat-name-view}
      (if group-chat
        [react/text {:style               st/chat-name-text
                     :number-of-lines     1
                     :accessibility-label :chat-name-text}
         chat-name]
        [one-to-one-name chat-id])
      (when-not group-chat
        [contact-indicator chat-id])
      (when (and group-chat (not invitation-admin) (not= chat-type constants/community-chat-type))
        [group-last-activity {:contacts   contacts
                              :public?    public?}])]]))

(defn toolbar-content-view []
  [toolbar-content-view-inner @(re-frame/subscribe [:chats/current-chat])])

(defn toolbar-pin-content-view []
  (let [{:keys [group-chat chat-id chat-name]}
        @(re-frame/subscribe [:chats/current-chat])
        pinned-messages @(re-frame/subscribe [:chats/pinned chat-id])]
    [react/view {:style st/toolbar-container}
     [react/view {:style st/pins-name-view}
      (if group-chat
        [react/text {:style               st/chat-name-text
                     :number-of-lines     1
                     :accessibility-label :chat-name-text}
         chat-name]
        [one-to-one-name chat-id])
      [react/text {:style st/toolbar-subtitle}
       (if (= (count pinned-messages) 0)
         (i18n/label :t/no-pinned-messages)
         (i18n/label-pluralize (count pinned-messages) :t/pinned-messages-count))]]]))

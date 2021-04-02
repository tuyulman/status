(ns status-im.ui.screens.chat.views
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.i18n.i18n :as i18n]
            [status-im.ui.components.chat-icon.screen :as chat-icon.screen]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.connectivity.view :as connectivity]
            [status-im.ui.components.icons.icons :as icons]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.chat.sheets :as sheets]
            [quo.animated :as animated]
            [quo.react-native :as rn]
            [status-im.ui.screens.chat.audio-message.views :as audio-message]
            [quo.react :as quo.react]
            [status-im.ui.screens.chat.message.message :as message]
            [status-im.ui.screens.chat.stickers.views :as stickers]
            [status-im.ui.screens.chat.styles.main :as style]
            [status-im.ui.screens.chat.toolbar-content :as toolbar-content]
            [status-im.ui.screens.chat.image.views :as image]
            [status-im.ui.screens.chat.state :as state]
            [status-im.ui.screens.chat.extensions.views :as extensions]
            [status-im.ui.screens.chat.group :as chat.group]
            [status-im.ui.screens.chat.message.gap :as gap]
            [status-im.ui.components.invite.chat :as invite.chat]
            [status-im.ui.screens.chat.components.accessory :as accessory]
            [status-im.ui.screens.chat.components.input :as components]
            [status-im.ui.screens.chat.message.datemark :as message-datemark]
            [status-im.ui.components.toolbar :as toolbar]
            [quo.core :as quo]
            [clojure.string :as string]
            [status-im.constants :as constants]
            [status-im.utils.platform :as platform]
            [status-im.utils.utils :as utils]
            [quo.design-system.colors :as quo.colors]))

(defn topbar []
  ;;we don't use topbar component, because we want chat view as simple (fast) as possible
  [react/view {:height 56 :border-bottom-width 1 :border-bottom-color (:ui-01 @quo.colors/theme)}
   [react/touchable-highlight {:on-press-in #(re-frame/dispatch [:navigate-back])
                               :accessibility-label :back-button
                               :style {:height 56 :width 40 :align-items :center :justify-content :center
                                       :padding-left 16}}
    [icons/icon :main-icons/arrow-left]]
   [react/view {:flex 1 :left 52 :right 52 :top 0 :bottom 0 :position :absolute}
    [toolbar-content/toolbar-content-view]]
   [react/touchable-highlight {:on-press-in #(re-frame/dispatch [:bottom-sheet/show-sheet
                                                                 {:content (fn []
                                                                             [sheets/current-chat-actions])
                                                                  :height  256}])
                               :accessibility-label :chat-menu-button
                               :style {:right 0 :top 0 :bottom 0 :position :absolute
                                       :height 56 :width 40 :align-items :center :justify-content :center
                                       :padding-right 16}}
    [icons/icon :main-icons/more]]])

(defn pins-topbar []
  ;;we don't use topbar component, because we want chat view as simple (fast) as possible
  [react/view {:height 56 :border-bottom-width 1 :border-bottom-color (:ui-01 @quo.colors/theme)}
   [react/touchable-highlight {:on-press-in #(re-frame/dispatch [:navigate-back])
                               :accessibility-label :back-button
                               :style {:height 56 :width 40 :align-items :center :justify-content :center
                                       :padding-left 16}}
    [icons/icon :main-icons/arrow-left]]
   [react/view {:flex 1 :left 52 :right 52 :top 0 :bottom 0 :position :absolute}
    [toolbar-content/toolbar-pin-content-view]]])

(defn invitation-requests [chat-id admins]
  (let [current-pk @(re-frame/subscribe [:multiaccount/public-key])
        admin? (get admins current-pk)]
    (when admin?
      (let [invitations @(re-frame/subscribe [:group-chat/pending-invitations-by-chat-id chat-id])]
        (when (seq invitations)
          [react/touchable-highlight
           {:on-press            #(re-frame/dispatch [:navigate-to :group-chat-invite])
            :accessibility-label :invitation-requests-button}
           [react/view {:style (style/add-contact)}
            [react/text {:style style/add-contact-text}
             (i18n/label :t/group-membership-request)]]])))))

(defn add-contact-bar [public-key]
  (when-not @(re-frame/subscribe [:contacts/contact-added? public-key])
    [react/touchable-highlight
     {:on-press
      #(re-frame/dispatch [:contact.ui/add-to-contact-pressed public-key])
      :accessibility-label :add-to-contacts-button}
     [react/view {:style (style/add-contact)}
      [icons/icon :main-icons/add
       {:color colors/blue}]
      [react/i18n-text {:style style/add-contact-text :key :add-to-contacts}]]]))

(defn chat-intro [{:keys [chat-id
                          chat-name
                          chat-type
                          group-chat
                          invitation-admin
                          contact-name
                          color
                          loading-messages?
                          no-messages?]}]
  [react/view (style/intro-header-container loading-messages? no-messages?)
   ;; Icon section
   [react/view {:style {:margin-top    42
                        :margin-bottom 24}}
    [chat-icon.screen/chat-intro-icon-view
     chat-name chat-id group-chat
     {:default-chat-icon      (style/intro-header-icon 120 color)
      :default-chat-icon-text style/intro-header-icon-text
      :size                   120}]]
   ;; Chat title section
   [react/text {:style (style/intro-header-chat-name)}
    (if group-chat chat-name contact-name)]
   ;; Description section
   (if group-chat
     [chat.group/group-chat-description-container {:chat-id chat-id
                                                   :invitation-admin invitation-admin
                                                   :loading-messages? loading-messages?
                                                   :chat-name chat-name
                                                   :chat-type chat-type
                                                   :no-messages? no-messages?}]
     [react/text {:style (assoc style/intro-header-description
                                :margin-bottom 32)}

      (str
       (i18n/label :t/empty-chat-description-one-to-one)
       contact-name)])])

(defn chat-intro-one-to-one [{:keys [chat-id] :as opts}]
  (let [contact-names @(re-frame/subscribe
                        [:contacts/contact-two-names-by-identity chat-id])]
    (chat-intro (assoc opts :contact-name (first contact-names)))))

(defn chat-intro-header-container
  [{:keys [group-chat invitation-admin
           chat-type
           color chat-id chat-name
           public?]}
   no-messages]
  [react/touchable-without-feedback
   {:style    {:flex        1
               :align-items :flex-start}
    :on-press (fn [_]
                (react/dismiss-keyboard!))}
   (let [opts
         {:chat-id chat-id
          :group-chat group-chat
          :invitation-admin invitation-admin
          :chat-type chat-type
          :chat-name chat-name
          :public? public?
          :color color
          :loading-messages? @(re-frame/subscribe [:chats/might-have-join-time-messages? chat-id])
          :no-messages? no-messages}]
     (if group-chat
       [chat-intro opts]
       [chat-intro-one-to-one opts]))])

(defn pinned-messages-empty []
  [react/view {:style {:flex 1
                       :align-items :center
                       :justify-content :center}}
   [react/text {:style style/intro-header-description}
    (i18n/label :t/pinned-messages-empty)]])

(defonce messages-list-ref (atom nil))

(defn on-viewable-items-changed [^js e]
  (when @messages-list-ref
    (reset! state/first-not-visible-item
            (when-let [^js last-visible-element (aget (.-viewableItems e) (dec (.-length ^js (.-viewableItems e))))]
              (let [index (.-index last-visible-element)
                    ;; Get first not visible element, if it's a datemark/gap
                    ;; we might unnecessarely add messages on receiving as
                    ;; they do not have a clock value, but most of the times
                    ;; it will be a message
                    first-not-visible (aget (.-data ^js (.-props ^js @messages-list-ref)) (inc index))]
                (when (and first-not-visible
                           (= :message (:type first-not-visible)))
                  first-not-visible))))))

(defn bottom-sheet [input-bottom-sheet]
  (case input-bottom-sheet
    :stickers
    [stickers/stickers-view]
    :extensions
    [extensions/extensions-view]
    :images
    [image/image-view]
    :audio
    [audio-message/audio-message-view]
    nil))

(defn invitation-bar [chat-id]
  (let [{:keys [state chat-id] :as invitation}
        (first @(re-frame/subscribe [:group-chat/invitations-by-chat-id chat-id]))
        {:keys [retry? message]} @(re-frame/subscribe [:chats/current-chat-membership])]
    [react/view {:margin-horizontal 16 :margin-top 10}
     (cond
       (and invitation (= constants/invitation-state-requested state) (not retry?))
       [toolbar/toolbar {:show-border? true
                         :center
                         [quo/button
                          {:type     :secondary
                           :disabled true}
                          (i18n/label :t/request-pending)]}]

       (and invitation (= constants/invitation-state-rejected state) (not retry?))
       [toolbar/toolbar {:show-border? true
                         :right
                         [quo/button
                          {:type     :secondary
                           :accessibility-label :retry-button
                           :on-press #(re-frame/dispatch [:group-chats.ui/membership-retry])}
                          (i18n/label :t/mailserver-retry)]
                         :left
                         [quo/button
                          {:type     :secondary
                           :accessibility-label :remove-group-button
                           :on-press #(re-frame/dispatch [:group-chats.ui/remove-chat-confirmed chat-id])}
                          (i18n/label :t/remove-group)]}]
       :else
       [toolbar/toolbar {:show-border? true
                         :center
                         [quo/button
                          {:type                :secondary
                           :accessibility-label :introduce-yourself-button
                           :disabled            (string/blank? message)
                           :on-press            #(re-frame/dispatch [:send-group-chat-membership-request])}
                          (i18n/label :t/request-membership)]}])]))

(defn get-space-keeper-ios [bottom-space panel-space active-panel text-input-ref]
  (fn [state]
    ;; NOTE: Only iOs now because we use soft input resize screen on android
    (when platform/ios?
      (cond
        (and state
             (< @bottom-space @panel-space)
             (not @active-panel))
        (reset! bottom-space @panel-space)

        (and (not state)
             (< @panel-space @bottom-space))
        (do
          (some-> ^js (quo.react/current-ref text-input-ref) .focus)
          (reset! panel-space @bottom-space)
          (reset! bottom-space 0))))))

(defn get-set-active-panel [active-panel]
  (fn [panel]
    (rn/configure-next
     (:ease-opacity-200 rn/custom-animations))
    (reset! active-panel panel)
    (reagent/flush)
    (when panel
      (js/setTimeout #(react/dismiss-keyboard!) 100))))

(defn list-footer [{:keys [chat-id chat-type] :as chat}]
  (let [loading-messages? @(re-frame/subscribe [:chats/loading-messages? chat-id])
        no-messages? @(re-frame/subscribe [:chats/chat-no-messages? chat-id])
        all-loaded? @(re-frame/subscribe [:chats/all-loaded? chat-id])]
    [react/view {:style (when platform/android? {:scaleY -1})}
     (if (or loading-messages? (not chat-id) (not all-loaded?))
       [react/view {:height 324 :align-items :center :justify-content :center}
        [react/activity-indicator {:animating true}]]
       [chat-intro-header-container chat no-messages?])
     (when (= chat-type constants/one-to-one-chat-type)
       [invite.chat/reward-messages])]))

(defn list-header [{:keys [chat-id chat-type invitation-admin]}]
  (when (= chat-type constants/private-group-chat-type)
    [react/view {:style (when platform/android? {:scaleY -1})}
     [chat.group/group-chat-footer chat-id invitation-admin]]))

(defn render-fn [{:keys [outgoing type] :as message}
                 idx
                 _
                 {:keys [group-chat public? current-public-key space-keeper chat-id show-input?]}]
  [react/view {:style (when platform/android? {:scaleY -1})}
   (if (= type :datemark)
     [message-datemark/chat-datemark (:value message)]
     (if (= type :gap)
       [gap/gap message idx messages-list-ref false chat-id]
       ; message content
       [message/chat-message
        (assoc message
               :incoming-group (and group-chat (not outgoing))
               :group-chat group-chat
               :public? public?
               :current-public-key current-public-key
               :show-input? show-input?)
        space-keeper]))])

(def list-key-fn #(or (:message-id %) (:value %)))
(def list-ref #(reset! messages-list-ref %))

;;TODO this is not really working in pair with inserting new messages because we stop inserting new messages
;;if they outside the viewarea, but we load more here because end is reached,so its slowdown UI because we
;;load and render 20 messages more, but we can't prevent this , because otherwise :on-end-reached will work wrong
(defn list-on-end-reached []
  (if @state/scrolling
    (re-frame/dispatch [:chat.ui/load-more-messages-for-current-chat])
    (utils/set-timeout #(re-frame/dispatch [:chat.ui/load-more-messages-for-current-chat])
                       (if platform/low-device? 700 200))))

(defn pin-list-on-end-reached []
  (if @state/scrolling
    (re-frame/dispatch [:chat.ui/load-more-pin-messages-for-current-chat])
    (utils/set-timeout #(re-frame/dispatch [:chat.ui/load-more-pin-messages-for-current-chat])
                       (if platform/low-device? 700 200))))

(defn messages-view [{:keys [chat bottom-space pan-responder space-keeper show-input?]}]
  (let [{:keys [group-chat chat-id public?]} chat
        messages @(re-frame/subscribe [:chats/chat-messages-stream chat-id])
        current-public-key @(re-frame/subscribe [:multiaccount/public-key])]
    ;;do not use anonymous functions for handlers
    [list/flat-list
     (merge
      pan-responder
      {:key-fn                       list-key-fn
       :ref                          list-ref
       :header                       [list-header chat]
       :footer                       [list-footer chat]
       :data                         messages
       :render-data                  {:group-chat         group-chat
                                      :public?            public?
                                      :current-public-key current-public-key
                                      :space-keeper       space-keeper
                                      :chat-id            chat-id
                                      :show-input?        show-input?}
       :render-fn                    render-fn
       :on-viewable-items-changed    on-viewable-items-changed
       :on-end-reached               list-on-end-reached
       :on-scroll-to-index-failed    identity              ;;don't remove this
       :content-container-style      {:padding-top (+ bottom-space 16)
                                      :padding-bottom 16}
       :scroll-indicator-insets      {:top bottom-space}    ;;ios only
       :keyboard-dismiss-mode        :interactive
       :keyboard-should-persist-taps :handled
       :onMomentumScrollBegin        state/start-scrolling
       :onMomentumScrollEnd          state/stop-scrolling
       ;;TODO https://github.com/facebook/react-native/issues/30034
       :inverted                     (when platform/ios? true)
       :style                        (when platform/android? {:scaleY -1})})]))

(defn pinned-messages-view [{:keys [chat bottom-space pan-responder space-keeper show-input?]}]
  (let [{:keys [group-chat chat-id public?]} chat
        pinned-messages @(re-frame/subscribe [:chats/pinned-messages-stream chat-id])
        current-public-key @(re-frame/subscribe [:multiaccount/public-key])]
    ;;do not use anonymous functions for handlers
    (if (= (count pinned-messages) 0)
      [pinned-messages-empty]
      [list/flat-list
       (merge
        pan-responder
        {:key-fn                       list-key-fn
         :ref                          list-ref
         :header                       [list-header chat]
         :data                         (reverse pinned-messages)
         :render-data                  {:group-chat         group-chat
                                        :public?            public?
                                        :current-public-key current-public-key
                                        :space-keeper       space-keeper
                                        :chat-id            chat-id
                                        :show-input?        show-input?}
         :render-fn                    render-fn
         :on-viewable-items-changed    on-viewable-items-changed
         :on-end-reached               pin-list-on-end-reached
         :on-scroll-to-index-failed    identity              ;;don't remove this
         :content-container-style      {:padding-top 16
                                        :padding-bottom 16}
         :scroll-indicator-insets      {:top bottom-space}    ;;ios only
         :keyboard-dismiss-mode        :interactive
         :keyboard-should-persist-taps :handled
         :onMomentumScrollBegin        state/start-scrolling
         :onMomentumScrollEnd          state/stop-scrolling})])))

(defn pinned-messages []
  (let [bottom-space (reagent/atom 0)
        panel-space (reagent/atom 52)
        active-panel (reagent/atom nil)
        position-y (animated/value 0)
        pan-state (animated/value 0)
        text-input-ref (quo.react/create-ref)
        pan-responder (accessory/create-pan-responder position-y pan-state)
        space-keeper (get-space-keeper-ios bottom-space panel-space active-panel text-input-ref)]
    (fn []
      (let [chat
            ;;we want to react only on these fields, do not use full chat map here
            @(re-frame/subscribe [:chats/current-chat-chat-view])
            max-bottom-space (max @bottom-space @panel-space)]
        [:<>
         [pins-topbar]
         [connectivity/loading-indicator]
         ;;MESSAGES LIST
         [pinned-messages-view {:chat          chat
                                :bottom-space  max-bottom-space
                                :pan-responder pan-responder
                                :space-keeper  space-keeper}]]))))

(defn chat []
  (let [bottom-space (reagent/atom 0)
        panel-space (reagent/atom 52)
        active-panel (reagent/atom nil)
        position-y (animated/value 0)
        pan-state (animated/value 0)
        text-input-ref (quo.react/create-ref)
        on-update #(when-not (zero? %) (reset! panel-space %))
        pan-responder (accessory/create-pan-responder position-y pan-state)
        space-keeper (get-space-keeper-ios bottom-space panel-space active-panel text-input-ref)
        set-active-panel (get-set-active-panel active-panel)
        on-close #(set-active-panel nil)]
    (reagent/create-class
     {:component-will-unmount #(re-frame/dispatch-sync [:close-chat])
      :reagent-render
      (fn []
        (let [{:keys [chat-id show-input? group-chat admins invitation-admin] :as chat}
              ;;we want to react only on these fields, do not use full chat map here
              @(re-frame/subscribe [:chats/current-chat-chat-view])
              max-bottom-space (max @bottom-space @panel-space)]
          [:<>
           [topbar]
           [connectivity/loading-indicator]
           (when chat-id
             (if group-chat
               [invitation-requests chat-id admins]
               [add-contact-bar chat-id]))
           ;;MESSAGES LIST
           [messages-view {:chat          chat
                           :bottom-space  max-bottom-space
                           :pan-responder pan-responder
                           :space-keeper  space-keeper
                           :show-input?   show-input?}]
           (when (and group-chat invitation-admin)
             [accessory/view {:y               position-y
                              :on-update-inset on-update}
              [invitation-bar chat-id]])
           [components/autocomplete-mentions text-input-ref max-bottom-space]
           (when show-input?
             [accessory/view {:y               position-y
                              :pan-state       pan-state
                              :has-panel       (boolean @active-panel)
                              :on-close        on-close
                              :on-update-inset on-update}
              [components/chat-toolbar
               {:chat-id          chat-id
                :active-panel     @active-panel
                :set-active-panel set-active-panel
                :text-input-ref   text-input-ref}]
              [bottom-sheet @active-panel]])]))})))

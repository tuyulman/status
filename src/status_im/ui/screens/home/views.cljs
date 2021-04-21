(ns status-im.ui.screens.home.views
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.i18n.i18n :as i18n]
            [status-im.react-native.resources :as resources]
            [status-im.anon-metrics.core :as anon-metrics]
            [status-im.communities.core :as communities]
            [status-im.ui.components.connectivity.view :as connectivity]
            [status-im.ui.components.icons.icons :as icons]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.home.styles :as styles]
            [status-im.ui.screens.communities.views :as communities.views]
            [status-im.ui.screens.home.views.inner-item :as inner-item]
            [status-im.ui.screens.referrals.home-item :as referral-item]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.screens.add-new.new-public-chat.view :as new-public-chat]
            [quo.core :as quo]
            [status-im.add-new.core :as new-chat]
            [status-im.ui.components.search-input.view :as search-input]
            [status-im.add-new.db :as db]
            [status-im.utils.debounce :as debounce]
            [status-im.utils.utils :as utils]
            [status-im.utils.config :as config]
            [cljs-bean.core :as bean]
            [status-im.multiaccounts.login.core :as multiaccounts.login]
            [status-im.ui.components.invite.views :as invite]
            [status-im.ui.components.topbar :as topbar]
            [status-im.ui.components.plus-button :as components.plus-button]
            [status-im.ui.components.tabbar.styles :as tabs.styles]
            [status-im.ui.screens.chat.sheets :as sheets])
  (:require-macros [status-im.utils.views :as views]))

(defn welcome-image-wrapper []
  (let [dimensions (reagent/atom {})]
    (fn []
      [react/view {:on-layout (fn [^js e]
                                (reset! dimensions (bean/->clj (-> e .-nativeEvent .-layout))))
                   :style     {:align-items     :center
                               :justify-content :center
                               :flex            1}}
       (let [padding    0
             image-size (- (min (:width @dimensions) (:height @dimensions)) padding)]
         [react/image {:source      (resources/get-theme-image :welcome)
                       :resize-mode :contain
                       :style       {:width image-size :height image-size}}])])))

(defn welcome []
  [react/view {:style styles/welcome-view}
   [welcome-image-wrapper]
   [react/i18n-text {:style styles/welcome-text :key :welcome-to-status}]
   [react/view
    [react/i18n-text {:style styles/welcome-text-description
                      :key   :welcome-to-status-description}]]
   [react/view {:align-items :center :margin-bottom 50}
    [quo/button {:on-press            #(re-frame/dispatch [::multiaccounts.login/welcome-lets-go])
                 :accessibility-label :lets-go-button}
     (i18n/label :t/lets-go)]]])

(defn home-tooltip-view []
  [react/view (styles/chat-tooltip)
   [react/view {:style {:flex-direction :row}}
    [react/view {:flex 1}
     [react/view {:style styles/empty-chats-header-container}
      [react/view {:style {:width       66 :position :absolute :top -6 :background-color colors/white
                           :align-items :center}}
       [react/image {:source (resources/get-image :empty-chats-header)
                     :style  {:width 50 :height 50}}]]]
     [react/touchable-highlight
      {:style               {:position :absolute :right  0  :top         0
                             :width    44        :height 44 :align-items :center :justify-content :center}
       :on-press            #(re-frame/dispatch [:multiaccounts.ui/hide-home-tooltip])
       :accessibility-label :hide-home-button}
      [icons/icon :main-icons/close-circle {:color colors/gray}]]]]
   [react/view
    [react/i18n-text {:style styles/no-chats-text :key :chat-and-transact}]]
   [invite/button]
   [react/view {:align-items :center}
    [react/view {:style (styles/hr-wrapper)}]
    [react/i18n-text {:style (styles/or-text) :key :or}]]
   [react/view {:margin-top 16}
    [react/i18n-text {:style {:margin-horizontal 16
                              :text-align        :center}
                      :key   :follow-your-interests}]
    [react/view {:style styles/tags-wrapper}
     [react/view {:flex-direction :row :flex-wrap :wrap :justify-content :center}
      (for [chat (new-public-chat/featured-public-chats)]
        (new-public-chat/render-topic chat))]]
    (when config/communities-enabled?
      [react/view
       [react/i18n-text {:style {:margin-horizontal 16
                                 :text-align        :center}
                         :key   :join-a-community}]
       [react/view {:style styles/tags-wrapper}
        [react/view {:flex-direction :row :flex-wrap :wrap :justify-content :center}
         (for [community communities/featured]
           (communities.views/render-featured-community community))]]])]])

(defn welcome-blank-page []
  [react/view {:style {:flex 1 :flex-direction :row :align-items :center :justify-content :center}}
   [react/i18n-text {:style styles/welcome-blank-text :key :welcome-blank-message}]])

(defonce search-active? (reagent/atom false))

(defn search-input-wrapper [search-filter chats-empty]
  [react/view {:padding-horizontal 16
               :padding-vertical   10}
   [search-input/search-input
    {:search-active? search-active?
     :search-filter  search-filter
     :on-cancel      #(re-frame/dispatch [:search/home-filter-changed nil])
     :on-blur        (fn []
                       (when chats-empty
                         (re-frame/dispatch [:search/home-filter-changed nil]))
                       (re-frame/dispatch [::new-chat/clear-new-identity]))
     :on-focus       (fn [search-filter]
                       (when-not search-filter
                         (re-frame/dispatch [:search/home-filter-changed ""])
                         (re-frame/dispatch [::new-chat/clear-new-identity])))
     :on-change      (fn [text]
                       (re-frame/dispatch [:search/home-filter-changed text])
                       (re-frame/dispatch [:set-in [:contacts/new-identity :state] :searching])
                       (debounce/debounce-and-dispatch [:new-chat/set-new-identity text] 300))}]])

(defn start-suggestion [search-value]
  (let [{:keys [state ens-name public-key]}
        @(re-frame/subscribe [:contacts/new-identity])
        valid-private? (= state :valid)
        valid-public?  (db/valid-topic? search-value)]
    (when (or valid-public? valid-private?)
      [react/view
       [quo/list-header (i18n/label :t/search-no-chat-found)]
       (when valid-private?
         [quo/list-item {:theme    :accent
                         :icon     :main-icons/private-chat
                         :title    (or ens-name (utils/get-shortened-address public-key))
                         :subtitle (i18n/label :t/join-new-private-chat)
                         :on-press (fn []
                                     (debounce/dispatch-and-chill [:contact.ui/contact-code-submitted false] 3000)
                                     (re-frame/dispatch [:search/home-filter-changed nil]))}])
       (when valid-public?
         [quo/list-item {:theme    :accent
                         :icon     :main-icons/public-chat
                         :title    (str "#" search-value)
                         :subtitle (i18n/label :t/join-new-public-chat)
                         :on-press (fn []
                                     (re-frame/dispatch [:chat.ui/start-public-chat search-value])
                                     (re-frame/dispatch [:set :public-group-topic nil])
                                     (re-frame/dispatch [:search/home-filter-changed nil]))}])])))

(defn render-fn [{:keys [chat-id] :as home-item}]
  ;; We use `chat-id` to distinguish communities from chats
  (if chat-id
    [inner-item/home-list-item
     home-item
     {:on-press      (fn []
                       (re-frame/dispatch [:dismiss-keyboard])
                       (re-frame/dispatch [:chat.ui/navigate-to-chat chat-id])
                       (re-frame/dispatch [:search/home-filter-changed nil]))
      :on-long-press #(re-frame/dispatch [:bottom-sheet/show-sheet
                                          {:content (fn []
                                                      [sheets/actions home-item])}])}]
    [communities.views/community-home-list-item home-item]))

(defn chat-list-key-fn [item]
  (or (:chat-id item) (:id item)))

(views/defview communities-and-chats []
  (views/letsubs [{:keys [items search-filter]} [:home-items]
                  hide-home-tooltip? [:hide-home-tooltip?]]
    (if (and (empty? items)
             (empty? search-filter)
             hide-home-tooltip?
             (not @search-active?))
      [welcome-blank-page]
      [list/flat-list
       {:key-fn                       chat-list-key-fn
        :initialNumToRender           5
        :keyboard-should-persist-taps :always
        :data                         items
        :render-fn                    render-fn
        :header                       [:<>
                                       (when (or (seq items) @search-active? (seq search-filter))
                                         [search-input-wrapper search-filter (empty? items)])
                                       [referral-item/list-item]
                                       (when (and (empty? items)
                                                  (or @search-active? (seq search-filter)))
                                         [start-suggestion search-filter])]
        :footer                       (if (and (not hide-home-tooltip?) (not @search-active?))
                                        [home-tooltip-view]
                                        [react/view {:height 68}])}])))

(views/defview chats-list []
  (views/letsubs [loading? [:chats/loading?]]
    [:<>
     [connectivity/loading-indicator]
     (if loading?
       [react/view {:flex 1 :align-items :center :justify-content :center}
        [react/activity-indicator {:animating true}]]
       [communities-and-chats])]))

(views/defview plus-button []
  (views/letsubs [logging-in? [:multiaccounts/login]]
    [components.plus-button/plus-button
     {:on-press (when-not logging-in?
                  #(re-frame/dispatch [:bottom-sheet/show-sheet :add-new {}]))
      :loading logging-in?
      :accessibility-label :new-chat-button}]))

(views/defview notifications-button []
  (views/letsubs [notif-count [:activity.center/notifications-count]]
    [react/view
     [quo/button {:type     :icon
                  :style {:margin-left 10}
                  :accessibility-label "notifications-button"
                  :on-press #(do
                               (re-frame/dispatch [:mark-all-activity-center-notifications-as-read])
                               (re-frame/dispatch [:navigate-to :notifications-center]))
                  :theme    :icon}
      :main-icons/notification]
     (when (pos? notif-count)
       [react/view {:style (merge (tabs.styles/counter-public-container) {:top 5 :right 5})
                    :pointer-events :none}
        [react/view {:style               tabs.styles/counter-public
                     :accessibility-label :notifications-unread-badge}]])]))

(views/defview redirect-to-metrics-opt-in-screen []
  {:component-did-mount #(re-frame/dispatch [::anon-metrics/fetch-opt-in-screen-displayed?])}
  (views/letsubs [metrics-opt-in-displayed? [::anon-metrics/opt-in-screen-displayed?]]
    (when-not metrics-opt-in-displayed?
      (re-frame/dispatch [:navigate-to :anon-metrics-opt-in]))
    []))

(defn home []
  [react/keyboard-avoiding-view {:style {:flex 1}}
   [topbar/topbar {:title           (i18n/label :t/chat)
                   :navigation      :none
                   :right-component [react/view {:flex-direction :row :margin-right 16}
                                     [connectivity/connectivity-button]
                                     [notifications-button]]}]
   ;; [redirect-to-metrics-opt-in-screen]
   [chats-list]
   [plus-button]])

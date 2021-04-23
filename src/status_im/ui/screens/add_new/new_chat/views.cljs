(ns status-im.ui.screens.add-new.new-chat.views
  (:require [re-frame.core :as re-frame]
            [status-im.i18n.i18n :as i18n]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im.ui.components.chat-icon.screen :as chat-icon]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.icons.icons :as icons]
            [quo.core :as quo]
            [status-im.utils.gfycat.core :as gfycat]
            [status-im.qr-scanner.core :as qr-scanner]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.topbar :as topbar]
            [status-im.utils.debounce :as debounce]
            [status-im.utils.utils :as utils]
            [reagent.core :as reagent]
            [quo.react-native :as rn]
            [clojure.string :as string]
            [status-im.ui.components.invite.views :as invite]
            [status-im.ethereum.ens :as ens]
            [quo.platform :as platform]
            [status-im.transport.filters.core :as filters]
            [status-im.utils.identicon :as identicon]
            [status-im.react-native.resources :as resources])
  (:require-macros [status-im.utils.views :as views]))

(defn- render-row [row]
  (let [first-name (first (multiaccounts/contact-two-names row false))]
    [quo/list-item
     {:title    first-name
      :icon     [chat-icon/contact-icon-contacts-tab
                 (multiaccounts/displayed-photo row)]
      :on-press #(re-frame/dispatch [:chat.ui/start-chat
                                     (:public-key row)])}]))

(defn- icon-wrapper [color icon]
  [react/view
   {:style {:width            32
            :height           32
            :border-radius    25
            :align-items      :center
            :justify-content  :center
            :background-color color}}
   icon])

(defn- input-icon
  [state new-contact? entered-nickname]
  (let [icon (if new-contact? :main-icons/add :main-icons/arrow-right)]
    (case state
      :searching
      [icon-wrapper colors/gray
       [react/activity-indicator {:color colors/white-persist}]]

      :valid
      [react/touchable-highlight
       {:on-press #(debounce/dispatch-and-chill [:contact.ui/contact-code-submitted new-contact? entered-nickname] 3000)}
       [icon-wrapper colors/blue
        [icons/icon icon {:color colors/white-persist}]]]

      [icon-wrapper colors/gray
       [icons/icon icon {:color colors/white-persist}]])))

(defn get-validation-label [value]
  (case value
    :invalid
    (i18n/label :t/profile-not-found)
    :yourself
    (i18n/label :t/can-not-add-yourself)))

(defn search-contacts [filter-text {:keys [name alias nickname]}]
  (or
   (string/includes? (string/lower-case (str name)) filter-text)
   (string/includes? (string/lower-case (str alias)) filter-text)
   (when nickname
     (string/includes? (string/lower-case (str nickname)) filter-text))))

(defn filter-contacts [filter-text contacts]
  (let [lower-filter-text (string/lower-case filter-text)]
    (if filter-text
      (filter (partial search-contacts lower-filter-text) contacts)
      contacts)))

(defn is-valid-username? [username]
  (let [is-chat-key? (and (filters/is-public-key? username)
                          (= (count username) 132))
        is-ens? (ens/valid-eth-name-prefix? username)]
    (or is-chat-key? is-ens?)))

(views/defview new-chat []
  (views/letsubs [contacts      [:contacts/active]
                  {:keys [state ens-name public-key error]} [:contacts/new-identity]
                  search-value (reagent/atom "")
                  {current-public-key :public-key preferred-name :preferred-name} @(re-frame/subscribe [:multiaccount])
                  on-share        #(re-frame/dispatch [:show-popover
                                                       {:view     :share-chat-key
                                                        :address  current-public-key
                                                        :ens-name preferred-name}])]
    [react/view {:style {:flex 1}}
     [topbar/topbar
      {:title  (i18n/label :t/new-chat)
       :modal? true
       :right-accessories
       [{:icon                :qr
         :accessibility-label :scan-contact-code-button
         :on-press            #(re-frame/dispatch [::qr-scanner/scan-code
                                                   {:title   (i18n/label :t/new-chat)
                                                    :handler :contact/qr-code-scanned}])}]}]
     [react/view {:flex-direction :row
                  :padding        16}
      [react/view {:flex          1}
       [quo/text-input
        {:on-change-text
         #(do
            (reset! search-value %)
            (re-frame/dispatch [:set-in [:contacts/new-identity :state] :searching])
            (debounce/debounce-and-dispatch [:new-chat/set-new-identity %] 600))
         :on-submit-editing
         #(when (= state :valid)
            (debounce/dispatch-and-chill [:contact.ui/contact-code-submitted false nil] 3000))
         :placeholder         (i18n/label :t/enter-contact-code)
         :show-cancel         false
         :accessibility-label :enter-contact-code-input
         :auto-capitalize     :none
         :return-key-type     :go
         :monospace           true}]]]
     [react/view (if (and
                      (= (count contacts) 0)
                      (= @search-value ""))
                   {:flex 1}
                   {:justify-content :flex-end})
      (if (and
           (= (count contacts) 0)
           (= @search-value ""))
        [react/view {:flex 1
                     :align-items :center
                     :padding-horizontal 58
                     :padding-top 160}
         [quo/text {:size  :base
                    :align :center
                    :color :secondary}
          (i18n/label :t/you-dont-have-contacts-invite-friends)]
         [invite/button]]
        [list/flat-list {:data                      (filter-contacts @search-value contacts)
                         :key-fn                    :address
                         :render-fn                 render-row
                         :enableEmptySections       true
                         :keyboardShouldPersistTaps :always}])]
     (when-not (= @search-value "")
       [react/view
        [quo/text {:style {:margin-horizontal 16
                           :margin-vertical 14}
                   :size  :base
                   :align :left
                   :color :secondary}
         (i18n/label :t/non-contacts)]
        (when (and (= state :searching)
                   (is-valid-username? @search-value))
          [rn/activity-indicator {:color colors/gray
                                  :size  (if platform/android? :large :small)}])
        (if (= state :valid)
          [quo/list-item
           (merge
            {:title    (or ens-name (gfycat/generate-gfy public-key))
             :subtitle (if ens-name (gfycat/generate-gfy public-key) (utils/get-shortened-address public-key))
             :icon     [chat-icon/contact-icon-contacts-tab
                        (identicon/identicon public-key)]
             :on-press #(re-frame/dispatch [:chat.ui/start-chat public-key])}
            (when ens-name {:subtitle-secondary public-key}))]
          [quo/text {:style {:margin-horizontal 16}
                     :size  :base
                     :align :center
                     :color :secondary}
           (if (is-valid-username? @search-value)
             (when (= state :error)
               (get-validation-label error))
             (i18n/label :t/invalid-username-or-key))])])
     [react/touchable-opacity {:style {:padding-horizontal 2
                                       :height 36
                                       :width 124
                                       :background-color colors/blue
                                       :border-radius 18
                                       :position :absolute
                                       :bottom 42
                                       :align-self :center
                                       :elevation 8
                                       :shadow-offset {:width 0 :height 4}
                                       :shadow-color "rgba(0, 34, 51, 0.16)"
                                       :shadow-radius 4
                                       :shadow-opacity 1}
                               :on-press on-share}
      [react/view {:style {:flex 1
                           :flex-direction :row
                           :align-items :center}}
       [react/image {:source (:contact resources/ui)
                     :style  {:width 32 :height 32 :margin-right 6}}]
       [quo/text {:size  :base
                  :weight :medium
                  :color :inverse}
        (i18n/label :t/my-profile)]]]]))

(defn- nickname-input [entered-nickname]
  [quo/text-input
   {:on-change-text      #(reset! entered-nickname %)
    :auto-capitalize     :none
    :max-length          32
    :auto-focus          false
    :accessibility-label :nickname-input
    :placeholder         (i18n/label :t/add-nickname)
    :return-key-type     :done
    :auto-correct        false}])

(views/defview new-contact []
  (views/letsubs [{:keys [state ens-name public-key error]} [:contacts/new-identity]
                  entered-nickname (reagent/atom "")]
    [react/view {:style {:flex 1}}
     [topbar/topbar
      {:title  (i18n/label :t/new-contact)
       :modal? true
       :right-accessories
       [{:icon                :qr
         :accessibility-label :scan-contact-code-button
         :on-press            #(re-frame/dispatch [::qr-scanner/scan-code
                                                   {:title        (i18n/label :t/new-contact)
                                                    :handler      :contact/qr-code-scanned
                                                    :new-contact? true}])}]}]
     [react/view {:flex-direction :row
                  :padding        16}
      [react/view {:flex          1
                   :padding-right 16}
       [quo/text-input
        {:on-change-text
         #(do
            (re-frame/dispatch [:set-in [:contacts/new-identity :state] :searching])
            (debounce/debounce-and-dispatch [:new-chat/set-new-identity %] 600))
         :on-submit-editing
         #(when (= state :valid)
            (debounce/dispatch-and-chill [:contact.ui/contact-code-submitted true @entered-nickname] 3000))
         :placeholder         (i18n/label :t/enter-contact-code)
         :show-cancel         false
         :accessibility-label :enter-contact-code-input
         :auto-capitalize     :none
         :return-key-type     :go}]]
      [react/view {:justify-content :center
                   :align-items     :center}
       [input-icon state true @entered-nickname]]]
     [react/view {:min-height 30 :justify-content :flex-end :margin-bottom 16}
      [quo/text {:style {:margin-horizontal 16}
                 :size  :small
                 :align :center
                 :color :secondary}
       (cond (= state :error)
             (get-validation-label error)
             (= state :valid)
             (str (when ens-name (str ens-name " • "))
                  (utils/get-shortened-address public-key))
             :else "")]]
     [react/text {:style {:margin-horizontal 16 :color colors/gray}}
      (i18n/label :t/nickname-description)]
     [react/view {:padding 16}

      [nickname-input entered-nickname]
      [react/text {:style {:align-self :flex-end :margin-top 16
                           :color      colors/gray}}
       (str (count @entered-nickname) " / 32")]]]))
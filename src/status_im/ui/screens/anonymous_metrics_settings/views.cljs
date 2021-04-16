(ns status-im.ui.screens.anonymous-metrics-settings.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :as re-frame]
            [status-im.react-native.resources :as resources]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.colors :as colors]
            [status-im.i18n.i18n :as i18n]
            [quo.design-system.spacing :as spacing]
            [status-im.ui.components.icons.icons :as icons]
            [status-im.ui.components.topbar :as topbar]
            [quo.core :as quo]))

(defn graphic-and-desc []
  [:<>
   [react/view {:align-items     :center
                :margin-vertical 25}
    [react/image {:source (resources/get-image :graph)}]]

   [react/text {:style {:color             colors/gray
                        :text-align        :center
                        :margin-horizontal 24}}
    (i18n/label :t/anonymous-usage-data-subtitle)]

   [react/touchable-highlight
    {:on-press #(re-frame/dispatch [:keycard.onboarding.recovery-phrase.ui/learn-more-pressed])}
    [react/text {:style {:color         colors/blue
                         :margin-bottom 24
                         :text-align    :center}}
     (i18n/label :t/learn-more)]]])

(defn setting-switch [enabled? on-press]
  [quo/list-item {:title              (i18n/label :t/share-anonymous-usage-data)
                  :active             enabled?
                  :accessory          :switch
                  :subtitle-max-lines 2
                  :on-press           on-press}])

(defn what-is-shared []
  [:<>
   [quo/list-header (i18n/label :t/what-is-shared)]
   (for [label [(i18n/label :t/anon-metrics-your-interactions)
                (i18n/label :t/anon-metrics-bg-activity)
                (i18n/label :t/anon-metrics-settings-and-prefs)]]
     [quo/list-item {:title label
                     :size  :small
                     :icon  :main-icons/info}])])

(defview settings []
  (views/letsubs []
    [react/view {:flex 1}
     [topbar/topbar {:title (i18n/label :t/anonymous-usage-data)}]
     [graphic-and-desc]
     [setting-switch true #()]
     [quo/separator]
     [what-is-shared]
]))

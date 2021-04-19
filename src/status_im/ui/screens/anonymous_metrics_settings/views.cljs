(ns status-im.ui.screens.anonymous-metrics-settings.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :as re-frame]
            [status-im.react-native.resources :as resources]
            [status-im.anon-metrics.core :as anon-metrics]
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
   [react/touchable-highlight
    {:on-press
     #(re-frame/dispatch [:bottom-sheet/show-sheet :anon-metrics/learn-more])}
    [react/nested-text
     {:style (merge {:color      colors/gray
                     :text-align :center}
                    (:base spacing/padding-horizontal))}
     (i18n/label :t/anonymous-usage-data-subtitle)
     [{:style {:color colors/blue}}
      (str " " (i18n/label :t/learn-more))]]]])

(defn setting-switch [enabled? on-press]
  [quo/list-item {:title              (i18n/label :t/share-anonymous-usage-data)
                  :active             enabled?
                  :accessory          :switch
                  :subtitle-max-lines 2
                  :on-press           on-press}])

(defn icon-list-item
  ([icon label]
   (icon-list-item icon {} label))
  ([icon icon-opts label]
   [react/view {:flex-direction :row
                :margin-horizontal (:base spacing/spacing)
                :margin-vertical (:tiny spacing/spacing)}
    [icons/icon icon (into icon-opts
                           {:container-style {:margin-right (:tiny spacing/spacing)}})]
    [react/view {:style {:padding-right (:base spacing/spacing)}}
     (if (string? label)
       [react/text label]
       label)]]))

(defn what-is-shared []
  [:<>
   [quo/list-header (i18n/label :t/what-is-shared)]
   (for [label [(i18n/label :t/anon-metrics-your-interactions)
                (i18n/label :t/anon-metrics-bg-activity)
                (i18n/label :t/anon-metrics-settings-and-prefs)]]
     ^{:key label}
     [icon-list-item :main-icons/info {:color colors/blue} label])])


(defn event-item [event]
  [react/text "kuch aya"])

(defview view-data-bottom-sheet []
  (letsubs [events [::anon-metrics/events]]
    {:component-did-mount #(re-frame/dispatch [::anon-metrics/fetch-local-metrics])}
    [:<>
     [quo/header {:title         (i18n/label :t/data-collected)
                  :border-bottom false}]
     [react/touchable-highlight
      {:on-press
       #(re-frame/dispatch [:bottom-sheet/show-sheet :anon-metrics/learn-more])}
      [react/nested-text
       {:style (merge {:text-align :center}
                      (:base spacing/padding-horizontal))}
       (i18n/label :t/data-collected-subtitle)
       [{:style {:color colors/blue}}
        (str " " (i18n/label :t/view-rules))]]]
     [react/view {:style (merge
                          (:base spacing/padding-vertical)
                          (:base spacing/padding-horizontal))}
      (for [event events]
        ^{:key (:created_at event)}
        [event-item event])]]))

(defn view-data-button [events]
  [react/view {:flex 1
               :align-items :center
               :style {:margin-top (:x-large spacing/spacing)}}
   [quo/button {:type :primary
                :theme :main
                :on-press #(re-frame/dispatch
                            [:bottom-sheet/show-sheet :anon-metrics/view-data])}
    (i18n/label :t/view-data)]])

(defn desc-point-with-link [desc link-text]
  [:<>
   [react/text desc]
   [react/view {:flex-direction :row}
    [react/text {:style {:text-align :center
                         :color      colors/blue}}
     link-text]
    [icons/tiny-icon :tiny-icons/tiny-external
     {:color           colors/blue
      :container-style {:margin-left 4
                        :margin-top  4}}]]])

(defn learn-more-bottom-sheet []
  [:<>
   [quo/header {:title (i18n/label :t/about-sharing-data)
                :border-bottom false}]
   [react/text {:style (merge
                        (:base spacing/padding-horizontal)
                        (:base spacing/padding-vertical))}
    (i18n/label :t/about-sharing-data-subtitle)]
   [what-is-shared]
   [view-data-button]
   [quo/separator {:style {:margin-vertical (:base spacing/spacing)}}]
   [quo/list-header (i18n/label :t/how-it-works)]
   (for [label [[desc-point-with-link
                 (i18n/label :t/sharing-data-desc-1)
                 (i18n/label :t/view-rules)]
                (i18n/label :t/sharing-data-desc-2)
                (i18n/label :t/sharing-data-desc-3)
                (i18n/label :t/sharing-data-desc-4)
                [desc-point-with-link
                 (i18n/label :t/sharing-data-desc-5)
                 (i18n/label :t/view-public-dashboard)]
                (i18n/label :t/sharing-data-desc-6)]]
     ^{:key label}
     [icon-list-item :main-icons/arrow-right label])])

(defview settings []
  (letsubs [{:keys [:anon-metrics/should-send?]} [:multiaccount]]
    [react/view {:flex 1}
     [topbar/topbar {:title (i18n/label :t/anonymous-usage-data)}]
     [graphic-and-desc]
     [setting-switch
      should-send?
      #(re-frame/dispatch [:multiaccounts.ui/share-anonymous-usage-data-switched (not should-send?)])]
     [quo/separator]
     [what-is-shared]
     [view-data-button]]))

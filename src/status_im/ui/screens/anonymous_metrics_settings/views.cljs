(ns status-im.ui.screens.anonymous-metrics-settings.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :as re-frame]
            [status-im.react-native.resources :as resources]
            [status-im.anon-metrics.core :as anon-metrics]
            [status-im.ui.components.accordion :as accordion]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.colors :as colors]
            [status-im.i18n.i18n :as i18n]
            [quo.design-system.spacing :as spacing]
            [status-im.ui.components.icons.icons :as icons]
            [status-im.ui.components.topbar :as topbar]
            [quo.core :as quo]))

(defn graphic-and-desc [{:keys [show-title?]}]
  [:<>
   [react/view {:align-items     :center
                :margin-vertical 25}
    [react/image {:source (resources/get-image :graph)}]]
   (when show-title?
     [quo/header {:title         (i18n/label :t/help-improve-status)
                  :border-bottom false}])
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
  [accordion/section
   {:title [react/view {:flex           1
                        :flex-direction :row
                        :margin-bottom  (:base spacing/spacing)}
            [react/view {:style {:padding-right (:base spacing/spacing)}}
             (for [label ["event" "time" "os"]]
               ^{:key label}
               [react/text {:style {:color colors/gray}}
                label])]

            [react/view
             [react/text (:event event)]
             [react/text (:created_at event)]
             [react/text (:os event)]]]
    :content [react/view {:style {:background-color colors/gray-lighter
                                  :padding          (:small spacing/spacing)
                                  :border-radius    14}}
              [react/text (:value event)]]}])

(defn data-sheet-header-and-desc []
  [:<>
   [quo/header {:title         (i18n/label :t/data-collected)
                :border-bottom false}]
   [react/touchable-highlight
    {:on-press
     #(re-frame/dispatch [:bottom-sheet/show-sheet :anon-metrics/learn-more])}
    [react/nested-text
     {:style (:base spacing/padding-horizontal)}
     (i18n/label :t/data-collected-subtitle)
     [{:style {:color colors/blue}}
      (str " " (i18n/label :t/view-rules))]]]])

(defview view-data-bottom-sheet []
  (letsubs [events [::anon-metrics/events]
            all-fetched? [::anon-metrics/all-fetched?]
            fetching? [::anon-metrics/fetching?]]
    {:component-did-mount #(re-frame/dispatch [::anon-metrics/fetch-local-metrics
                                               {:clear-existing? true
                                                :limit           2
                                                :offset          0}])}
    [:<>
     [data-sheet-header-and-desc]
     [react/view
      {:style (merge
               {:border-width  1
                :border-radius 8
                :border-color  colors/gray-lighter
                :margin        16}
               (:base spacing/padding-vertical)
               (:base spacing/padding-horizontal))}
      (doall
       (map-indexed
        (fn [index event]
          ^{:key index}
          [event-item event])
        events))
      (if fetching?
        [react/activity-indicator {:size      :large
                                   :animating true}]
        [quo/button {:type     :primary
                     :disabled all-fetched?
                     :on-press #(re-frame/dispatch [::anon-metrics/fetch-local-metrics
                                                    {:clear-existing? false
                                                     :limit           2
                                                     :offset          (count events)}])}
         (i18n/label :t/show-more)])]]))

(defn view-data-button []
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

(defn allow-or-not-actions []
  [react/view {:flex        1
               :align-items :center
               :style       {:margin-top (:x-large spacing/spacing)}}
   [quo/button {:type     :primary
                :theme    :main
                :on-press #(re-frame/dispatch [::anon-metrics/opt-in true])}
    (i18n/label :t/allow-and-send)]
   [react/view {:style {:margin-top (:base spacing/spacing)}}]
   [quo/button {:type     :primary
                :theme    :main
                :on-press #(re-frame/dispatch [::anon-metrics/opt-in false])}
    (i18n/label :t/no-thanks)]])

(defn new-account-opt-in []
  [react/view {:flex 1}
   [graphic-and-desc {:show-title? true}]
   [react/view {:style (:base spacing/padding-vertical)}
    [quo/separator]]
   [what-is-shared]
   [react/view {:style (:base spacing/padding-vertical)}
    [quo/separator]]
   [allow-or-not-actions]])

(comment
  (re-frame/dispatch [:navigate-to :anon-metrics-opt-in])
  )

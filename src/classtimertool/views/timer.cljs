(ns classtimertool.views.timer
  (:require
   [re-frame.core :as re-frame]
   [classtimertool.toolsview :as vt]
   [classtimertool.toolsreframe :as h]
   [cljs.pprint :as pp]
   [classtimertool.stylesgarden :as gstyle]
   [reagent.core  :as reagent]
   [classtimertool.db :as db]
   ))
;; CURRENT TIME
(defn dispatch-timer-event
  []
  (let [now (h/now)]
    (re-frame/dispatch [:now now])))

(defonce do-timer (js/setInterval dispatch-timer-event 1000))

(defn timer [now {:keys [id name start end]}]
  (let [length (h/duration start end)
        time-used (h/time-used now start)
        time-left (h/time-left now end)
        time-used-str (h/duration->string time-used)
        length-str (h/duration->minutes-seconds-string length)
        time-left-str (h/duration->string time-left)
        progress-bar-length-str (h/duration->seconds->string length)
        progress-bar-time-used-str (h/duration->seconds->string time-used)
        start-str (h/twelve-hour-time start)
        end-str (h/twelve-hour-time end)]

    (if (h/running? now start end)
      ;; RUNNING state — blue left border, white card
      [:div.p-6.m-4.rounded-xl.shadow-md.bg-white.border-l-4.border-blue-500
       [:div.flex.justify-between.items-center.mb-3
        [:h2.text-lg.font-bold name]
        [:button.w-7.h-7.rounded-full.bg-red-100.hover:bg-red-500.text-red-600.hover:text-white.text-sm.font-bold.flex.items-center.justify-center.transition
         {:on-click #(re-frame/dispatch [:kill-timer id])}
         "✕"]]
       [:div.text-center.mb-3
        [:div.text-3xl.font-bold.text-blue-600 time-left-str]
        [:div.text-sm.text-gray-500 "remaining"]]
       [:progress.w-full.h-3.mb-3.accent-blue-600
        {:value progress-bar-time-used-str :max progress-bar-length-str}]
       [:div.flex.justify-between.text-xs.text-gray-500
        [:span start-str " → " end-str]
        [:span length-str]]]

      ;; NOT RUNNING — split into ended vs not-yet-started
      (if (h/timer-ended? now end)
        ;; Ended — gray, muted
        [:div.p-6.m-4.rounded-xl.shadow-sm.bg-gray-50.border-l-4.border-gray-400
         [:div.flex.justify-between.items-center
          [:h2.text-base.font-semibold.text-gray-500 name]
          [:button.w-7.h-7.rounded-full.text-gray-400.hover:bg-red-100.hover:text-red-500.text-sm.font-bold.flex.items-center.justify-center.transition
           {:on-click #(re-frame/dispatch [:kill-timer id])}
           "✕"]]
         [:p.text-xs.text-gray-400.mt-1 length-str " · " start-str " – " end-str]
         [:p.font-semibold.text-sm.text-gray-500.mt-2 "Ended " time-left-str " ago"]]

        ;; Not started yet — amber, waiting
        [:div.p-6.m-4.rounded-xl.shadow-sm.bg-yellow-50.border-l-4.border-yellow-400
         [:div.flex.justify-between.items-center
          [:h2.text-base.font-semibold.text-yellow-700 name]
          [:button.w-7.h-7.rounded-full.text-yellow-500.hover:bg-red-100.hover:text-red-500.text-sm.font-bold.flex.items-center.justify-center.transition
           {:on-click #(re-frame/dispatch [:kill-timer id])}
           "✕"]]
         [:p.text-xs.text-yellow-600.mt-1 length-str " · " start-str " – " end-str]
         [:p.font-semibold.text-sm.text-yellow-700.mt-2 "Starts in " time-used-str]]))))

;;================================================================================

(defn timers []
  (let [running @(re-frame/subscribe [:running])
        now @(re-frame/subscribe [:now])]
    [:div
     (for [t running]
       ^{:key (:id t)} [timer now t])]))


(defn add-class []
  (let [open-dialog? (reagent/atom false)
        title  (reagent/atom nil)
        length  (reagent/atom nil)]
    (fn []
      (if @open-dialog?
        [:div.fixed.top-0.left-0.w-full.h-full.bg-black.bg-opacity-50.flex.items-center.justify-center
         [:dialog.bg-white.p-6.rounded-xl.shadow-xl.w-full.max-w-sm
          {:open @open-dialog?}

          ;; Header
          [:div.flex.justify-between.items-center.mb-5
           [:h2.font-bold.text-lg "Add Timer"]
           [:button.w-7.h-7.rounded-full.bg-gray-100.hover:bg-gray-200.text-gray-500.text-sm.font-bold.flex.items-center.justify-center.transition
            {:on-click #(reset! open-dialog? false)}
            "✕"]]

          ;; Timer name
          [:div.mb-4
           [:label.block.text-sm.font-medium.text-gray-700.mb-1 "Timer name"]
           [:input.block.w-full.px-3.py-2.border.border-gray-300.rounded-lg.text-sm.shadow-sm.placeholder-gray-400.focus:outline-none.focus:border-blue-500.focus:ring-1.focus:ring-blue-500
            {:type "text"
             :placeholder "e.g. Maths lesson"
             :on-change #(reset! title (-> % .-target .-value))}]]

          ;; Length
          [:div.mb-6
           [:label.block.text-sm.font-medium.text-gray-700.mb-1 "Length (minutes)"]
           [:input.block.w-full.px-3.py-2.border.border-gray-300.rounded-lg.text-sm.shadow-sm.placeholder-gray-400.focus:outline-none.focus:border-blue-500.focus:ring-1.focus:ring-blue-500
            {:type "number"
             :min "1"
             :value @length
             :placeholder "e.g. 45"
             :on-change #(reset! length (-> % .-target .-value))
             :autocomplete "off"}]]

          ;; Submit
          [:button.w-full.rounded-lg.bg-blue-600.py-2.hover:bg-blue-700.text-white.font-medium.transition
           {:on-click #(do
                         (reset! open-dialog? false)
                         (re-frame/dispatch [:add-timer [@title @length]]))}
           "Start Timer"]]]

        ;; Floating action buttons
        [:div.grid.grid-cols-2.gap-2
         {:style {:position "fixed" :bottom "5%" :right "5%"}}
         [:button.rounded-full.border-2.border-gray-300.bg-white.hover:bg-red-50.hover:border-red-400.text-gray-600.hover:text-red-600.p-4.shadow-md.transition.text-sm.font-medium
          {:on-click #(re-frame/dispatch [:delete-all-running])}
          "Delete All"]
         [:button.bg-blue-600.rounded-full.p-4.hover:bg-blue-700.text-white.shadow-md.transition.font-medium
          {:on-click #(reset! open-dialog? true)}
          "Add Timer"]]))))

(defn quick-timers []
  (let [quick-timers-display (reagent/atom false)]
    (fn []
      (if @quick-timers-display
        [:div.p-6.m-4.rounded-xl.shadow-md.bg-white.grid.gap-4
         {:class (gstyle/grid-auto-fit)}
         [:div.col-span-full.flex.justify-between.items-center
          [:button.font-bold.text-blue-500.text-lg
           {:on-click #(reset! quick-timers-display false)}
           "Quick Timers"]
          [:span.text-xs.text-gray-400 "tap to start"]]

         (for [length-seconds [15 30 60 90 120 180 240 300 600 900 1200 1500 1800 2400]]
           (let [length (h/seconds->duration length-seconds)]
             [:button.bg-white.shadow-md.rounded-full.flex.items-center.justify-center.h-24.w-24.hover:bg-blue-50.hover:shadow-lg.active:bg-blue-600.active:text-white.transition
              {:on-click #(re-frame/dispatch [:running-quick-timer length])
               :key (str length-seconds)}
              [:span.text-lg.font-bold (h/duration->minutes-seconds-string length)]]))]

        [:div.p-6.m-4.rounded-xl.shadow-md.bg-white.grid.gap-4
         {:class (gstyle/grid-auto-fit)}
         [:div.col-span-full
          [:button.font-bold.text-blue-500.text-lg
           {:on-click #(reset! quick-timers-display true)}
           "Quick Timers"]]]))))

;;================================================================================
(defn brain-breaks []
  (let [brain-breaks-display (reagent/atom false)]
    (fn []
      (let [brainbreak @(re-frame/subscribe [:brainbreak])
            now @(re-frame/subscribe [:now])
            breaking (:breaking brainbreak)
            last-str (h/duration->string
                      (h/time-used now (:last brainbreak)))
            time-used (h/duration->seconds (h/time-used now (:last brainbreak)))]

        (if @brain-breaks-display
          [:div.p-6.m-4.rounded-xl.shadow-md.bg-white
           ;; Header row
           [:div.flex.justify-between.items-center.mb-4
            [:button.font-bold.text-blue-500.text-lg
             {:on-click #(reset! brain-breaks-display false)}
             "Brain Breaks"]
            [:button.rounded-lg.bg-gray-100.hover:bg-gray-200.px-3.py-1.text-sm.text-gray-600.transition
             {:on-click #(js/alert "feature to be added")}
             "Examples"]]

           ;; Content
           (if (h/started? now (:last brainbreak))
             [:div.flex.items-center.justify-between
              [:div.text-sm
               (if breaking
                 [:span "Breaking for " [:span.font-bold.text-blue-600 last-str]]
                 [:span.text-gray-600 "Last break " [:span.font-bold last-str] " ago"])]
              [:button.rounded-lg.px-5.py-2.text-white.font-medium.text-sm.transition
               {:style {:background-color (if breaking "#2563eb" (h/calculate-color time-used))}
                :on-click #(re-frame/dispatch [:toggle-brainbreak now])}
               (if breaking "Stop Break" "Start Break")]]
             [:p.text-gray-500.text-sm "Class has not started yet"])]

          ;; Collapsed state
          [:div.p-6.m-4.rounded-xl.shadow-md.bg-white
           [:button.font-bold.text-blue-500.text-lg
            {:on-click #(reset! brain-breaks-display true)}
            "Brain Breaks"]])))))

(defn main []
  [:<>
   [timers]
   [brain-breaks]
   [quick-timers]
   [add-class]])

;; ROUTING
(def toolbar-items
  [["Timers" :routes/#frontpage]
   ["Classes" :routes/#classes]])

(defn route-info [route]
  [:div.m-4
   [:p "Routeinfo"]
   [:pre.border-solid.border-2.rounded
    (with-out-str (pp/pprint route))]])

(defn show-panel [route]
  (when-let [route-data (:data route)]
    (let [view (:view route-data)]
      [:<>
       [view]])))

(defn main-panel []
  (let [active-route (re-frame/subscribe [:routes/current-route])]
    [:div
     [:nav.bg-white.shadow-sm.p-4
      [:div.container.mx-auto.flex.justify-between.items-center
       [:div.flex.items-center.gap-3
        [:img {:src "ln-logo.svg" :alt "Learning Now" :class "h-8"}]
        [:span.text-gray-900.font-bold.text-lg "Class Timer"]]
       [vt/navigation toolbar-items]]]
     [show-panel @active-route]]))

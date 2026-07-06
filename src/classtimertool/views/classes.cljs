(ns classtimertool.views.classes
  (:require
   [reagent.core  :as reagent]
   [re-frame.core :as re-frame]
   [classtimertool.toolsreframe :as h]
   [classtimertool.toolsview :as vt]
   [cljs.pprint :as pp]
   [classtimertool.stylesgarden :as gstyle]
   [classtimertool.db :as db]
   [reitit.frontend.easy :as rtfe]
   ))

;; (defn class-layout_grid [{:keys [id name start end]}]
;;   [:div.grid.grid-cols-5.gap-1 ;;({:class gstyle/classes-layout)}
;;     [:div.col-start-1.col-span-2.whitespace-normal
;;     [:a.col-span-full {:href (rtfe/href :routes/#frontpage)}
;;      [:button.text-blue-500.underline.hover:text-blue-700
;;       {
;;        :id id
;;        :href (rtfe/href :routes/#frontpage)
;;        :on-click #(re-frame/dispatch [:run-class id])
;;        }
;;       name]]]
;;    [:div.col-start-3 (str (h/twelve-hour-time start) " - " (h/twelve-hour-time end))]
;;    [:div.col-start-4 (str (h/duration->minutes->string (h/duration start end)) " mins")]
;;    [:div.col-start-5.flex.items-center.justify-end [:button.rounded.bg-blue-600.text-white.px-4.shadow-md.border {:on-click #(re-frame/dispatch [:delete-class id])} "x"]]])

;; (defn class-list_grid []
;; (let [classes @(re-frame/subscribe [:classes])]
;; (if (seq classes)
;;  [:div.grid.rounded-xl.shadow-lg.items-center.p-6.m-4.grid-cols-5
;;   [:div.col-start-1.col-span-4 [:h2.font-bold "Classes"]]
;;   [:div.col-start-5.text-right [:button.rounded.bg-red-600.px-4
;;                                 {:on-click #(re-frame/dispatch [:sort])}
;;                                 "sort"]]
;;   [:div.col-start-1.col-span-2 "Name"]
;;   [:div.col-start-3 "Time"]
;;   [:div.col-start-4 "Length"]
;;   [:div.col-start-5 ""]

;;   [:div.col-span-full
;;    (for [class  classes]
;;      ^{:key (:id class)} [class-layout_grid class])
;;    ]])))

(defn class-layout [{:keys [id name start end]}]
    [:tr
     [:td [:a.col-span-full {:href (rtfe/href :routes/#frontpage)}
       [:button.text-blue-500.underline.hover:text-blue-700
        {
         :id id
         :href (rtfe/href :routes/#frontpage)
         :on-click #(re-frame/dispatch [:run-class id])
         }
        name]]]
   [:td (str (h/twelve-hour-time start) " - " (h/twelve-hour-time end))]
   [:td (str (h/duration->minutes->string (h/duration start end)) " mins")]
   [:td [:button.rounded.bg-blue-600.text-white.px-4.shadow-md.border.justify-self-end {:on-click #(re-frame/dispatch [:delete-class id])} "x"]]])

(defn class-list []
(let [classes @(re-frame/subscribe [:classes])]
(if (seq classes)

  [:div.grid.rounded-xl.shadow-lg.items-center.p-6.m-4

   [:div.grid.grid-cols-2
    [:div.col-start-1 [:h2.font-bold "Classes"]]
    [:div.col-start-2.justify-self-end [:button.rounded.bg-red-600.px-4
           {:on-click #(re-frame/dispatch [:sort])}
           "sort"]]]
   [:br]

   [:table.table-fixed
    [:tr
     [:th "Name"]
     [:th "Time"]
     [:th "Length"]
     [:th ""]
     ]
    (for [class  classes]
      ^{:key (:id class)} [class-layout class])
    ]
   ]

   ;; [:div.col-start-1.col-span-4 [:h2.font-bold "Classes"]]
   ;; [:div.col-start-5.text-right [:button.rounded.bg-red-600.px-4
   ;;                               {:on-click #(re-frame/dispatch [:sort])}
   ;;                               "sort"]]
   ;; [:div.col-start-1.col-span-2 "Name"]
   ;; [:div.col-start-3 "Time"]
   ;; [:div.col-start-4 "Length"]
   ;; [:div.col-start-5 ""]

   ;; [:div.col-span-full
   ;;  (for [class  classes]
   ;;    ^{:key (:id class)} [class-layout class])
)))

;;=========================================================================

(defn add-class []
  (let [open-dialog? (reagent/atom false)
        title  (reagent/atom nil)
        start  (reagent/atom nil)
        end    (reagent/atom nil)]
    (fn []
      (if @open-dialog?
        [:div.fixed.top-0.left-0.w-full.h-full.bg-black.bg-opacity-50.flex.items-center.justify-center
         [:dialog.bg-white.p-6.rounded-xl.shadow-xl.w-full.max-w-sm
          {:open @open-dialog?}

          ;; Header
          [:div.flex.justify-between.items-center.mb-5
           [:h2.font-bold.text-lg "Create a class"]
           [:button.w-7.h-7.rounded-full.bg-gray-100.hover:bg-gray-200.text-gray-500.text-sm.font-bold.flex.items-center.justify-center.transition
            {:on-click #(reset! open-dialog? false)}
            "✕"]]

          ;; Class name
          [:div.mb-4
           [:label.block.text-sm.font-medium.text-gray-700.mb-1 "Class name"]
           [:input.block.w-full.px-3.py-2.border.border-gray-300.rounded-lg.text-sm.shadow-sm.placeholder-gray-400.focus:outline-none.focus:border-blue-500.focus:ring-1.focus:ring-blue-500
            {:type "text"
             :placeholder "e.g. Maths"
             :on-change #(reset! title (-> % .-target .-value))}]]

          ;; Start / End side by side
          [:div.grid.grid-cols-2.gap-3.mb-6
           [:div
            [:label.block.text-sm.font-medium.text-gray-700.mb-1 "Start time"]
            [:input.block.w-full.px-3.py-2.border.border-gray-300.rounded-lg.text-sm.shadow-sm.focus:outline-none.focus:border-blue-500.focus:ring-1.focus:ring-blue-500
             {:type "time"
              :value @start
              :on-change #(reset! start (-> % .-target .-value))
              :autocomplete "off"}]]
           [:div
            [:label.block.text-sm.font-medium.text-gray-700.mb-1 "End time"]
            [:input.block.w-full.px-3.py-2.border.border-gray-300.rounded-lg.text-sm.shadow-sm.focus:outline-none.focus:border-blue-500.focus:ring-1.focus:ring-blue-500
             {:type "time"
              :value @end
              :on-change #(reset! end (-> % .-target .-value))}]]]

          ;; Submit
          [:button.w-full.rounded-lg.bg-blue-600.py-2.hover:bg-blue-700.text-white.font-medium.transition
           {:on-click #(do
                         (reset! open-dialog? false)
                         (re-frame/dispatch [:add-class [@title @start @end]]))}
           "Add Class"]]]

        [:div
         [:button.bg-blue-600.rounded-full.p-6.hover:bg-blue-700.text-white.font-medium.shadow-md.transition
          {:on-click #(reset! open-dialog? true)
           :style {:position "fixed" :bottom "5%" :right "5%"}}
          "Create Class"]]))))

(defn main []
;; (let [app-db @(re-frame/subscribe [:app-db])]
  [:div
   ;; [:h2.text-4xl "Classes"]
   [class-list]
   [add-class]
   ;; [:div[:p (str app-db)]]
   ]
  ;; )
  )

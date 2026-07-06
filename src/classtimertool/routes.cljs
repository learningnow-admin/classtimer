(ns classtimertool.routes
  (:require
    [re-frame.core :as rf]
    [reitit.frontend :as rtf]
    [reitit.frontend.easy :as rtfe]
    [reitit.coercion.schema :as rsc]
    [classtimertool.views.timer :as timer]
    ;; [classtimertool.views.compo :as compo]
    [classtimertool.views.classes :as classes]
    [classtimertool.toolsreframe :refer [sdb gdb]]))

;;https://clojure.org/guides/weird_characters#__code_code_var_quote
(def routes
    (rtf/router
      ["/"
       [""
        {:name :routes/#frontpage
         :view #'timer/main}]
       ;; ["about"
       ;;  {:name :routes/#about
       ;;   :view #'compo/main}]

       ["classes"
        {:name :routes/#classes
         :view #'classes/main}]
       ]

      {:data {:coercion rsc/coercion}}))

(defn on-navigate [new-match]
  (when new-match
    (rf/dispatch [:routes/navigated new-match])))

(defn app-routes []
  (rtfe/start! routes
               on-navigate
               {:use-fragment true}))

(rf/reg-sub
 :routes/current-route
 (gdb [:current-route]))

;;; Events
(rf/reg-event-db
 :routes/navigated
 (sdb [:current-route]))

(rf/reg-event-fx
 :routes/navigate
 (fn [_cofx [_ & route]]
   {:routes/navigate! route}))

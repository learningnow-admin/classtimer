(ns ^:figwheel-hooks classtimertool.core
  (:require [classtimertool.config :as config]
            [classtimertool.routes :as routes]
            [classtimertool.styles :as styl]
            ;; [classtimertool.use-cases.core-cases :as ccases]
            [classtimertool.views.timer :as views]
            [classtimertool.events]
            [goog.dom :as gdom]
            [react :as react]
            [re-frame.core :as re-frame]
            [reagent.dom.client :as rdc]))

(defn dev-setup []
  (when config/debug?
    (enable-console-print!)
    (println "dev mode")))

(defonce root (rdc/create-root (gdom/getElement "app")))

(defn mount-root []
  (println "mount")
  (re-frame/clear-subscription-cache!)
  (styl/inject-trace-styles js/document)
  (rdc/render root [:> react/StrictMode {} [#'views/main-panel]]))

(defn ^:after-load re-render []
  (mount-root))

(defn ^:export init []
  (println "init again..")
  ;; (re-frame/dispatch-sync [::ccases/initialize-db])
  (re-frame/dispatch-sync [:initialize-db])

  (dev-setup)
  (routes/app-routes)

  (mount-root))

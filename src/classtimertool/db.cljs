(ns classtimertool.db
  (:require
   [re-frame.core :as re-frame]
   [tick.core :as t]
   [clojure.edn :as c]
   [cljs.reader :as reader]
   [classtimertool.toolsreframe :as h]))

(def default-db
  {
   :now (h/now)
   :class-timers {:brainbreak {:last (h/now) :breaking false}
                  :running-id 1
                  :running []
                  :class-id 1
                  :classes []}
   })


;; -- Local Storage  ----------------------------------------------------------
(def ls-key "ls-classes")                         ;; localstore key

(defn classtimertool->local-store
  "Puts todos into localStorage"
  [classes]
  ;; (js/console.log (str "Hello" classes))
  (.setItem js/localStorage ls-key (str classes)))     ;; sorted-map written as an EDN map

;; -- cofx Registrations  -----------------------------------------------------
;; (re-frame/reg-cofx
;;  :local-store-classes
;;  (fn [cofx _]
;;       ;; put the localstore todos into the coeffect under :local-store-todos
;;    (assoc cofx :local-store-classes
;;              ;; read in todos from localstore, and process into a sorted map
;;           (into (sorted-map)
;;                 (some->> (.getItem js/localStorage ls-key)
;;                          (cljs.reader/read-string)    ;; EDN map -> map
;;                          )))))

(re-frame/reg-cofx
 :local-store-classes
 (fn [cofx _]
   (let [
         ;; custom-tag-map {'time/time (fn [x] (t/time x)),
         ;;                 'time/date-time (fn [x] (t/date-time x))}
         data-from-storage (.getItem js/localStorage ls-key)]
        (reader/register-tag-parser! 'time/time (fn [x] (t/time x)))
        (reader/register-tag-parser! 'time/date-time (fn [x] (t/date-time x)))
     (if-let [parsed-data (when data-from-storage
                            (try
                              (cljs.reader/read-string data-from-storage)
                              ;; (c/read-string {:readers custom-tag-map} data-from-storage)
                              (catch js/Error e
                                (js/console.error "Error parsing data from local storage:" e)
                                nil)))]
       (assoc cofx :local-store-classes (into (sorted-map) parsed-data))
       cofx))))

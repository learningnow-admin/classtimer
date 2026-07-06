(ns classtimertool.events
  (:require
   [re-frame.core :as re-frame]
   [classtimertool.db :as db]
   [classtimertool.toolsreframe :refer [sdb gdb]]
   [classtimertool.toolsreframe :as h]
   ;; [day8.re-frame.tracing :refer-macros [fn-traced]]
   ;; [re-frame.alpha :as reframe]
   ;; [re-frame.alpha :refer [reg-sub reg-event-db reg-event-fx reg-flow inject-cofx path after sub]]

   [tick.core :as t]))
;;===============================================================================
;; -- Interceptors --------------------------------------------------------------
;; ==============================================================================
;;Puts classes into local store
(def ->local-store (re-frame/after db/classtimertool->local-store))

(def interceptors [
                   (re-frame/path :class-timers)
                   ->local-store])

;; LOCAL STORE
(re-frame/reg-event-fx
 :initialize-db
 [(re-frame/inject-cofx :local-store-classes)]
 (fn [{:keys [db local-store-classes]} _]
   (if (empty? local-store-classes)
     {:db db/default-db} 
     {:db (assoc db/default-db :class-timers local-store-classes)}))
)

;;===============================================================================
;;SUBSCRIPTIONS
;;===============================================================================

;; Get map located in class-timers
(defn class-timers
  [db _]
  (:class-timers db))

(re-frame/reg-sub
 :class-timers
 class-timers)

;;Get the whole list of classes
(re-frame/reg-sub
 :classes
 :<-[:class-timers]
 (fn [db _]
   (:classes db)))

(re-frame/reg-sub
 :running
 :<-[:class-timers]
 (fn [db _ ]
   (:running db)
   ))


(re-frame/reg-sub
 :brainbreak
 :<-[:class-timers]
 (fn [db _]
   (:brainbreak db)))


(re-frame/reg-sub :now (gdb [:now]))



(re-frame/reg-sub
 :app-db
 (fn [db _]
   db))



;;===============================================================================
;;EVENTS
;;===============================================================================

(re-frame/reg-event-db
 :add-class
 interceptors
 (fn [db [_ class]]
   (let [
         id (:class-id db)
         name (first class)
         start (h/input->time (second class)) ;;(h/string->time (second class))
         end (h/input->time (last class)) ;;(h/string->time (last class))
         ;; length (h/duration start end) ;;(h/time-left start end)
         ]
     (-> db
         (update :class-id inc)
         (assoc  :classes (conj (:classes db)
                                {:id id :name name :start start :end end ;; :length length
                                 }
                                ))))))
;;Add class as a timer
(re-frame/reg-event-db
 :run-class
 interceptors
 (fn [db [_ class-id]]
   (let [id (:running-id db)
         class (h/find-map-by-id class-id (:classes db))
         {:keys [name start end]} class
         length (h/duration start end)
         start-time-date (h/time->date-time start)
         end-time-date (h/end-time start-time-date length)]
     (-> db
         (assoc :brainbreak {:last start-time-date :breaking false})
         (update :running-id inc)
         (assoc  :running (conj (:running db)
                                {:id id :name name :start start-time-date :end end-time-date}
                                ))))))

(re-frame/reg-event-db
 :toggle-brainbreak
 interceptors
 (fn [db [_ now]]
     (-> db
         (assoc :brainbreak {:last now :breaking (not (:breaking (:brainbreak db)))}))))

(re-frame/reg-event-db
 :sort
 interceptors
 (fn [db _]
   (let [
         classes (:classes db)
         new-classes (h/sort-classes classes)
         new-class-id (count new-classes)
         ]
     (-> db
         (assoc :class-id  new-class-id)
         (assoc-in [:classes] new-classes)
         )
     )))

(re-frame/reg-event-db :now (sdb [:now]))

(re-frame/reg-event-db
 :running-quick-timer
 interceptors
 (fn [db [_ length]]
   (let [
         id (:running-id db)
         name "Quick Timer"
         start (h/now)
         end (h/end-time start length) ;;(h/end-time start length)
         ]
     ;; (js/console.log (str "ID: "id))
     (-> db
         (update :running-id inc) ;;(inc (:running-id db))
         (assoc  :running (conj (:running db)
                                {:id id :name name :start start :end end}
                                ))))))

(re-frame/reg-event-db
 :add-timer
 interceptors
 (fn [db [_ [title length]]]
   (let [
         id (:running-id db)
         name title
         start (h/now)
         end (h/end-time start (h/minutes->duration (js/parseInt length)))
         ]
     (-> db
         (update :running-id inc) ;;(inc (:running-id db))
         (assoc  :running (conj (:running db)
                                {:id id :name name :start start :end end}
                                ))))))


(re-frame/reg-event-db
 :delete-all-running
 interceptors
 (fn [db _]
   ;; (js/alert "hello")
     (-> db
         (assoc :running-id 1)
         (assoc  :running []))))


;; (def fake-map {:running-id 12
;;                :running [{:id 1 :name "hello"}
;;                          {:id 2 :name "bye"}]}
;;   )
;; (assoc fake-map :running-id 1)


(defn remove-map-by-id [id data]
  (into [] (filter #(not= (:id %) id) data)))

(re-frame/reg-event-db
 :kill-timer
 interceptors
 (fn [db [_ id]]
   (assoc db :running (remove-map-by-id id (:running db)))))

(re-frame/reg-event-db
 :delete-class
 interceptors
 (fn [db [_ id]]
   (assoc db :classes (remove-map-by-id id (:classes db)))))

(ns classtimertool.toolsreframe
  (:require
   [tick.core :as t]
   [clojure.string :as str]
   [tick.alpha.interval :as t.i]
   [goog.string :as gstring]
   ))

(defn sdb [path]
  (fn [db [_ v]]
    (assoc-in db path v)))

(defn gdb
  [path]
  (fn [db _] (get-in db path)))

;;===============================================
;; STRING CONVERSIONS
;;===============================================

;; https://juxt.github.io/tick/#_miscellaneous
(defn twelve-hour-time
  "Takes a time and gives the 12 hour display"
  [t]
  (let [minute (t/minute t)
        hour (t/hour t)
        ;; seconds (t/seconds t)
        ]
    (cond
      (= (t/noon) t)
      "12:00 NOON"

      (>= hour 13)
      (gstring/format "%02d:%02d PM" (- hour 12) minute)

      (>= hour 12)
      (gstring/format "%02d:%02d PM" hour minute)

      (>= hour 1)
      (gstring/format "%02d:%02d AM" hour minute)

      (= hour 0)
      (gstring/format "12:%02d AM" minute))))

;; ;; test
;; (twelve-hour-time (t/time))

;; https://github.com/dvingo/tick-util/blob/8eb28d04df664d616adab7a5779db48c1b924ead/src/main/dv/tick_util.cljc#L1421
(defn duration->string
  [duration]
  (let [hours   (t/hours duration)

        minutes (t/minutes (t/- duration (t/new-duration hours :hours)))
        seconds (t/seconds (t/- duration
                                (t/new-duration minutes :minutes)
                                (t/new-duration hours :hours)))

        hours-abs (js/Math.abs hours)
        minutes-abs (js/Math.abs minutes)
        seconds-abs (js/Math.abs seconds)
        has-hours (pos? hours-abs)
        has-minutes (or has-hours (pos? minutes-abs))]
    ;; (gstring/format "%02d:%02d:%02d" hours-abs minutes-abs seconds-abs)
    (cond
      (and has-hours has-minutes)
      (gstring/format "%2dh:%02dm:%02ds" hours-abs minutes-abs seconds-abs)

      has-minutes
      (gstring/format "%dm:%02ds" minutes-abs seconds-abs)

      (pos? seconds-abs)
      (gstring/format "%ds" seconds-abs))
    )
  )
(defn duration->minutes-seconds-string
  [duration]
  (let [minutes (t/minutes duration )
        seconds (t/seconds (t/- duration
                                (t/new-duration minutes :minutes)
                                ))
        has-minutes (pos? minutes)]
    (cond
      (and has-minutes (pos? seconds))
      (gstring/format "%dm %02ds" minutes seconds)
      has-minutes
      (gstring/format "%dm" minutes)
      (pos? seconds)
      (gstring/format "%ds" seconds))
    ))
;; (duration->string (t/new-duration 23332 :seconds))

(defn duration->minutes->string
  [duration]
  (str (t/minutes duration)))

(defn duration->seconds->string
[duration]
  (str (t/seconds duration)))
;; (duration->minutes->string (t/new-duration 323232 :seconds))

(defn string->time_t
  "Used to convert time string to time"
  [time-str]
  (t/time time-str))
;;(string->time_t "12:00")

;;===============================================
;; CALCULATIONS
;;===============================================

(def s_ex (-> (t/time "12:00:12")
               (t/on (t/date))))
(def n_ex (-> (t/time "12:00:12")
              (t/on (t/date))))
(def e_ex (-> (t/time "13:30")
     (t/on (t/date))))

(defn time->date-time
  [time]
  (-> time (t/on (t/today))))

;; (defn now [] (t/date-time))

(defn now
  "Used to get the current time"
  []
  (let [now (js/Date.)
        hours (.getHours now)
        minutes (.getMinutes now)
        seconds (.getSeconds now)
        time (t/time (gstring/format "%02d:%02d:%02d" hours minutes seconds))]
    (time->date-time time)))

(defn time-used
  "Used to calculate the time used given the current time and start time"
  [now start]
  (t/between start now))

(defn time-left
  "Used to calculate the time left given the current time and end time"
  [now end]
  (t/between now end))

(defn timer-ended?
  "Used to determine that a timer has finished"
  [time end]
  (t/> time end))


(defn timer-ended-for-hour?
  "Used to determine that a timer has finished for one hour"
  [time end]
  (t/< (t/between time end) (t/new-duration
                             -10 :seconds
                             ;; -60 :minutes
                             ))
  )

;; (def n_ex (-> (t/time "13:40")
;;      (t/on (t/date))))

;; (def e_ex (-> (t/time "13:30")
;;      (t/on (t/date))))

;; (t/< (t/between n_ex e_ex) (t/new-duration -60 :minutes))


(timer-ended-for-hour? n_ex e_ex)

(defn running?
  [now start end]
  (and (t/>= now start) (t/< now end)))

;; (running? n_ex s_ex e_ex)

(defn end-time
  "Used to calculate the end time given the start time and length"
  [start length]
  (t/>> start length))

;; (end-time s_ex (t/new-duration 2 :hours))


(defn started?
  [now start]
  (t/>= now start))


;;================================================

(def example_class (t.i/new-interval
                    (t/time "12:00")
                    (t/time "12:10")))

(:tick/beginning example_class)

(t.i/relation example_class example_class)
;; => :equals

 (-> (t/time "13:30")
     (t/on (t/today)))

(-> (t/time "13:30")
    (t/on (t/tomorrow)))

(def example_interval (t.i/new-interval(-> (t/time "13:30")
                       (t/on (t/today)))

                   (-> (t/time "13:30")
                       (t/on (t/tomorrow)))))

;; (t.i/relation (t/date-time) example_interval )



;;===============================================
;; SETTING UP CLASSES
;;===============================================
(defn input->time
  [input]
   (-> (t/time input)))

(defn duration
[start end]
  (let [d (t/between start end)]
    (if (t/>= d (t/new-duration 0 :seconds))
      d
      (t/- (t/new-duration 24 :hours) (t/between end start)))))

(duration s_ex e_ex)

(defn seconds->duration
[seconds]
  (t/new-duration seconds :seconds))

(defn duration->seconds
  [duration]
  (t/seconds duration))


(defn minutes->duration
  [minutes]
  (t/new-duration minutes :minutes) )
;; (duration s_ex e_ex)

(defn calculate-color [seconds]
  (let [max-seconds 1200
        percent (float (/ seconds max-seconds))
        red (int (* percent 255))
        green (- 255 red)]
    ;; (js/console.log
    ;; (str "rgb(" red ", " green ", 0)"))
    (str "rgb(" red ", " green ", 0)")))

;; (calculate-color 0)
;; => "rgb(0, 255, 0)"

;; (calculate-color 1200)
;; => "rgb(255, 0, 0)"

;;===============================================
;; MANAGING CLASSES
;;===============================================

(defn find-map-by-id [id data]
  (into {} (filter #(= (:id %) id) data)))



(defn rename-id [index class-map]
  (assoc class-map :id (inc index)))

(defn sort-classes [classes]
;; (js/alert
;;   (into [] (map-indexed rename-id (sort-by :start classes))))
  (into [] (map-indexed rename-id (sort-by :start classes)))
  )



;; (def classes [{:id 3, :name "test1", :start (t/time "18:00"), :end (t/time "19:34")} {:id 4, :name "test2", :start (t/time "19:00"), :end (t/time "23:34")} {:id 5, :name "test3", :start (t/time "14:00"), :end (t/time "23:34")} {:id 6, :name "test4", :start (t/time "15:00"), :end (t/time "23:34")}])

;; ;; (def foo [{:id 10} {:id 40} {:id 5} {:id 6}])
;; (sort-by :start classes)
;; ;; => ({:id 5, :name "test3", :start #time/time "14:00", :end #time/time "23:34"} {:id 6, :name "test4", :start #time/time "15:00", :end #time/time "23:34"} {:id 3, :name "test1", :start #time/time "18:00", :end #time/time "19:34"} {:id 4, :name "test2", :start #time/time "19:00", :end #time/time "23:34"})



;; (def renamed-classes
;;   (map-indexed rename-id classes))

;; (println renamed-classes)






;;===============================================
;; OLD FUNCTIONS
;;===============================================

;; (defn now
;;   "Used to get the current time"
;;   []
;;   (let [now (js/Date.)
;;         hours (.getHours now)
;;         minutes (.getMinutes now)
;;         seconds (.getSeconds now)]
;;     {:hours hours :minutes minutes :seconds seconds}
;;     ))

(defn string->time
  "Used to convert input of type time into map for app-db {:minutes 120 :seconds 30}"
  [time-str]
  (let [[hours minutes seconds] (mapv #(js/parseInt %) (str/split time-str #":"))]
    {:hours hours :minutes minutes :seconds (if seconds seconds 0)}))

;; (defn time-left
;;   "Used to calculate the time left given the current time and end time"
;;   [now end]
;;   (let [now-seconds (+ (* (:hours now) 3600) (* (:minutes now) 60) (:seconds now))
;;         end-seconds (+ (* (:hours end) 3600) (* (:minutes end) 60) (:seconds end))
;;         total-seconds-left (- end-seconds now-seconds)
;;         hours-left (quot total-seconds-left 3600)
;;         remaining-seconds (mod total-seconds-left 3600)
;;         minutes-left (quot remaining-seconds 60)
;;         seconds-left (mod remaining-seconds 60)]
;;     {:hours hours-left :minutes minutes-left :seconds seconds-left}))

;; (defn timer-end
;;   "Used to determine that a timer has finished"
;;   [time]
;;   (let [hours (:hours time)
;;         minutes (:minutes time)
;;         seconds (:seconds time)]
;;   (and (= hours 0) (= minutes 0) (= seconds 0)
;;       )))

;; (defn time-used
;;   "Used to calculate the time used given the current time and start time"
;;   [now start]
;;   (let [now-seconds (+ (* (:hours now) 3600) (* (:minutes now) 60) (:seconds now))
;;         start-seconds (+ (* (:hours start) 3600) (* (:minutes start) 60) (:seconds start))
;;         total-seconds-used (- now-seconds start-seconds)
;;         hours-used (quot total-seconds-used 3600)
;;         remaining-seconds (mod total-seconds-used 3600)
;;         minutes-used (quot remaining-seconds 60)
;;         seconds-used (mod remaining-seconds 60)]
;;     {:hours hours-used :minutes minutes-used :seconds seconds-used}))

;; (defn end-time
;;  "Used to calculate the end time given the start time and length"
;;   [start length]
;;   (let [total-seconds-start (+ (* (:hours start) 3600) (* (:minutes start) 60) (:seconds start))
;;         total-seconds-length (+ (* (:hours length) 3600) (* (:minutes length) 60) (:seconds length))
;;         total-seconds-end (+ total-seconds-start total-seconds-length)
;;         hours-end (quot total-seconds-end 3600)
;;         minutes-end (quot (rem total-seconds-end 3600) 60)
;;         seconds-end (rem (rem total-seconds-end 3600) 60)]
;;     {:hours hours-end :minutes minutes-end :seconds seconds-end}))

(defmulti time-string
  "Used to convert time to a string for display"
  (fn [t time] t))

(defmethod time-string :minutes [t time]
  (let [{:keys [hours minutes seconds]} time]
    (str (+ (* 60 hours) minutes (/ seconds 60)))
    ))

(defmethod time-string :minutes-seconds [t time]
  (let [{:keys [hours minutes seconds]} time
        padded-minutes (if (< minutes 10) (str "0" minutes) (str minutes))
        padded-seconds (if (< seconds 10) (str "0" seconds) (str seconds))]
    (str padded-minutes ":" padded-seconds)
    ))

(defmethod time-string :hours-minutes [t time]
  (let [{:keys [hours minutes seconds]} time
        padded-hours (if (< hours 10) (str "0" hours) (str hours))
        padded-minutes (if (< minutes 10) (str "0" minutes) (str minutes))]
    (str padded-hours ":" padded-minutes)))

(defmethod time-string :hours-minutes-seconds [t time]
  (let [{:keys [hours minutes seconds]} time
        padded-hours (if (< hours 10) (str "0" hours) (str hours))
        padded-minutes (if (< minutes 10) (str "0" minutes) (str minutes))
        padded-seconds (if (< seconds 10) (str "0" seconds) (str seconds))
        ]
    (str padded-hours ":" padded-minutes ":" padded-seconds)
    ))

;; (defn running? [now start end]
;;   (let [now-seconds (+ (* (:hours now) 3600)
;;                        (* (:minutes now) 60)
;;                        (:seconds now))
;;         start-seconds (+ (* (:hours start) 3600)
;;                          (* (:minutes start) 60)
;;                          (:seconds start))
;;         end-seconds (+ (* (:hours end) 3600)
;;                        (* (:minutes end) 60)
;;                        (:seconds end))]
;;     (and (<= start-seconds now-seconds)
;;          (<= now-seconds end-seconds))))


(defn ended? [now end]
  (let [now-seconds (+ (* (:hours now) 3600)
                       (* (:minutes now) 60)
                       (:seconds now))
        end-seconds (+ (* (:hours end) 3600)
                       (* (:minutes end) 60)
                       (:seconds end))]
    (< end-seconds now-seconds)
    ))

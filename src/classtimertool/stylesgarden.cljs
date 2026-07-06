(ns classtimertool.stylesgarden
  (:require-macros
    [garden.def :refer [defcssfn]])
  (:require
    [spade.core   :refer [defglobal defclass]]
    [garden.units :refer [deg px]]
    [garden.color :refer [rgba]]
    [garden.core :refer [css]]
    ))


;; GLOBAL PROPERTIES

;;=================================================

(defglobal defaults
  [[:body {:background-color "#f1f5f9";; "#cbd5e1"
           ;; :font-family "Arial, sans-serif"
           :margin "0"
           :padding "0"}]
   ;; [:p {:color :red}]

[:td :th {
          :border "1px solid #dddddd"     ;
          :text-align "left"              ;
          :padding "8px"                  ;
          }
]
;; [:tr:nth-child(even)
;;  {:background-color "#dddddd"}
;;  ]
   ]
  )


(symbol (str (name "default") "-factory$"))

;; (symbol "garden.stylesheet" (name :red))
(vec (concat ["style-name-var" "params-var"] nil))

(defonce ^:dynamic *css-compile-flags*
  {:pretty-print? goog.DEBUG})

(defn compile-css [elements]
  (css *css-compile-flags* elements))

(compile-css (vec [[:body {:color "red"} ] [:p {:color "blue"}]]))


;; (defglobal defaults-2
;;   [:p
;;    {
;;     :color "blue"
;;     }
;;    ])


;;=================================
;; LAYOUT SYSTEM
(defclass classes-layout []
  {
   :display "grid"
   :grid-template (str "1fr 1fr 1fr 1fr 5px");;repeat(" rows ", 1fr
   :grid-gap "2px"
  })

(defclass grid-layout [columns rows]
  {
   :display "grid"
   :grid-template (str "auto / repeat(" columns ", 1fr)") ;;repeat(" rows ", 1fr)
   :grid-gap "2px"
   ;; :grid-auto-rows "auto"
   ;; :height "100vh"
   ;; :overflow-y :auto
   }
  ;; [:.item {:border "1px solid #000"
  ;;          :padding "10px"}]
  )

(defclass grid-cell
  [x y x-length y-length]
  {
   :grid-area (str y " / " x "/ span " y-length " / span " x-length)
   ;; :border "1px solid #000"
   :background-color "#FFDAB9"

   ;; :display "inline-grid"
   })


;; COMPONENTS
(defclass level1
  []
  {:color :blue})

(defclass button [default hover active]
  []
  {
   ;; :display "inline-block"
   :background-color default
   :border "none"
   :color :white
   :padding "0.25rem"  ; Equivalent to 1.5rem top/bottom and 2rem left/right of the button width
   :text-decoration "none"
   :font-size "1em"
   :width "90%"            ; Button width set to 50% of its container width
   :margin "0.5rem"     ; Equivalent to 1rem top/bottom and 1rem left/right of the button width
   :cursor "pointer"
   :border-radius "0.5em"
   }
  [:&:hover {:background-color hover}]
  [:&:active {:background-color active}]
  )


(defclass date-picker
  []
  {:padding "10px"
   :border "1px solid #ccc"
   :border-radius "4px"
   :font-size "16px"
   :cursor "pointer"})

(defclass time-picker []
  {:padding "10px"
   :border "1px solid #ccc"
   :border-radius "4px"
   :font-size "16px"
   :cursor "pointer"})


(defclass circle-button []
  {:border-radius "50%"
   :width "50px"
   :height "50px"
   :padding "10px"
   :text-align "center"
   :background-color "#007bff"
   :color "#fff"
   :border "none"
   :cursor "pointer"
   :outline "none"
   :box-shadow "0 2px 4px rgba(0,0,0,0.2)"}
  )


(defclass paragraph []
  {
   :padding "10px"
   :text-align "center"
                           })

(defclass random []
  {:background-color "#ADD8E6" :text-align :center})


(defclass grid-auto-fit []
  {:grid-template-columns "repeat(auto-fit, minmax(90px, 1fr))"}
  )

(defclass grid-classes []
  {:grid-template-columns "1fr 1fr 1fr 10%"}
  )

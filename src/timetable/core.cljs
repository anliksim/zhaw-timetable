(ns timetable.core
  (:require-macros [secretary.core :refer [defroute]])
  (:import goog.History)
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]
            [secretary.core :as sec]
            [goog.events :as gevents]
            [goog.history.EventType :as EventType]
            [day8.re-frame.http-fx]
            [clojure.string :as str]
            [ajax.core :as ajax]))

;; load uri from compiler settings
(goog-define api-uri "none")

;;;; Routing ------------------------

(defonce history
  (doto (History.)
        (gevents/listen
         EventType/NAVIGATE
         (fn [event]
           (sec/dispatch! (.-token event))))
        (.setEnabled true)))

(defn routes
  []
  (let [id nil]
    (sec/set-config! :prefix "#")
    (defroute "/" [] (sec/dispatch! "/anliksim"))
    (defroute "/:id" [id] (rf/dispatch [:load-json id]))))


;;;; Event Handlers ------------------------

(rf/reg-event-db
 ;; sets up initial application state
 ;; usage:  (dispatch [:initialize])
 :initialize
 (fn [_ _]
   {:api-result nil
    :error      "Loading data..."}))

(rf/reg-event-db
 ;; dispatched when data gets updated
 ;; usage:  (dispatch [:result-change new-result])
 :result-change
 (fn [db [_ new-result]]
   (assoc db :api-result new-result)))

(rf/reg-event-db
 ;; dispatched when an error occured
 ;; usage:  (dispatch [:error-change new-error])
 :error-change
 (fn [db [_ new-error]]
   (assoc db :error new-error)))

(rf/reg-event-fx
 ;; usage:  (dispatch [:load-json])
 :load-json
 (fn [_ [_ id]]
   {:http-xhrio {:method          :get
                 :uri             (str api-uri id)
                 :timeout         8000
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [:good-http-result]
                 :on-failure      [:bad-http-result]}}))

(rf/reg-event-fx
 :good-http-result
 (fn [db [_ result]]
   (rf/dispatch [:result-change result])))

(rf/reg-event-fx
 :bad-http-result
 (fn [db [_ result]]
   (rf/dispatch [:error-change (result :last-error)])))

;;;; Subs ------------------------

(rf/reg-sub
 :error
 (fn [db _]
   (:error db)))

(rf/reg-sub
 :api-result
 (fn [db _]
   (:api-result db)))

;;;; Views ------------------------

;; ^{:key index} in combination with map-indexed is used for performance reasons

(defn toTimeRangeString
  [startTime endTime]
  (let [start (js/Date. startTime)
        end   (js/Date. endTime)]
    (str (.toDateString start) " "
         (.toLocaleTimeString start) " -> "
         (.toLocaleTimeString end))))

(defn slot-view
  [index {:keys [startTime endTime]}]
  ^{:key index}
  [:div
   [:span (toTimeRangeString startTime endTime)]])

(defn lecturers-view
  [index {:keys [firstName lastName]}]
  ^{:key index}
  [:div
   [:span (str firstName " " lastName)]])

(defn event-relization-view
  [index {:keys [lecturers room]}]
  ^{:key index}
  [:div
   (map-indexed lecturers-view lecturers)
   [:div (:name room)]])

(defn event-view
  [index {:keys [name, slots, eventRealizations]}]
  ^{:key index}
  [:div.pad25
   [:div name]
   (map-indexed slot-view slots)
   (map-indexed event-relization-view eventRealizations)])

(defn days-view
  [index {:keys [events]}]
  ^{:key index}
  [:div
   (map-indexed event-view events)])

(defn timetable
  [{:keys [days]}]
  [:div
   (map-indexed days-view days)])

(defn footer
  []
  [:div
   (str "---")
   [:div
    [:span
     [:a {:href "https://anlikers.ch/"} "Simon Anliker"]
     " | GitHub: "
     [:a {:href "https://github.com/anliksim/zhaw-timetable"} "zhaw-timetable"]]]])

(defn container
  []
  (let [api-result @(rf/subscribe [:api-result])
        error      @(rf/subscribe [:error])]
    [:div
     (if (empty? api-result)
       [:span error]
       [:div [timetable api-result]])
     [footer]]))

;;;; Main ------------------------

(defn ^:export run
  []
  ;; mount routes
  (routes)
  ;; init app state
  (rf/dispatch-sync [:initialize])
  ;  (rf/dispatch [:load-json "anliksim"])
  (sec/dispatch! "/")
  ;; render ui into app div
  (reagent/render [container]
                  (js/document.getElementById "app")))



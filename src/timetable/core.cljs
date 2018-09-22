(ns timetable.core
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]
            [day8.re-frame.http-fx]
            [clojure.string :as str]
            [ajax.core :as ajax]))

;; load uri from compiler settings
(goog-define api-uri "none")

;;;; Event Handlers ------------------------

(rf/reg-event-db
 ;; sets up initial application state
 ;; usage:  (dispatch [:initialize])
 :initialize
 (fn [_ _]
   {:api-result nil
    :error      "No data loaded."}))

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
 (fn [{:keys [db]} _]
   {:http-xhrio {:method          :get
                 :uri             api-uri
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

(defn slot-view
  [index {:keys [startTime endTime]}]
  ^{:key index}
  [:div
   [:div startTime]
   [:div endTime]])

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
  [:div
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

(defn container
  []
  (let [api-result @(rf/subscribe [:api-result])
        error      @(rf/subscribe [:error])]
    [:div
     (if (empty? api-result)
       [:span error]
       [:div [timetable api-result]])]))

;;;; Main ------------------------

(defn ^:export run
  []
  ;; init app state
  (rf/dispatch-sync [:initialize])
  ;; request data from rest endpoint
  (rf/dispatch [:load-json])
  ;; render ui into app div
  (reagent/render [container]
                  (js/document.getElementById "app")))

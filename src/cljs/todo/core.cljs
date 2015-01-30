(ns todo.core
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [om-tools.core :refer-macros [defcomponentk]]
            [om-tools.dom :include-macros true]
            [cljs.core.async :refer [put! chan <!]]
            [sablono.core :as html :refer-macros [html]]
            [todo.state :refer [app-state process-action]]))

(defcomponentk list-item-view
  "Single todo list item display"
  [data]
  (display-name [this] "List item")
  (render-state [this {:keys [action-chan]}]
    (html [:a
           {:class "list-group-item"
            :style {:text-decoration
                    (if (data :done) "line-through" "none")}
            :href "#"
            :onClick (fn [e]
                       (put! action-chan [:toggle-done @data]))}
           (data :title)
           [:button {:class ["btn" "btn-xs" "pull-right"]
                     :onClick (fn [e]
                                (put! action-chan [:remove-item @data])
                                (.stopPropagation e))}
            [:span {:class ["glyphicon" "glyphicon-remove"]}]]])))

(defcomponentk add-list-item-view
  "Input control for adding a new list item"
  [state]
  (display-name [this] "Add list item")
  (render-state [this {:keys [action-chan new-item-title]}]
    (html
     [:form
      {:onSubmit (fn [e]
                   (put! action-chan [:add-item new-item-title])
                   (swap! state assoc :new-item-title "")
                   (.preventDefault e))}
      [:input
       {:ref "new-list-item"
        :class "list-group-item"
        :type "text"
        :value new-item-title
        :onChange (fn [e]
                    (let [new-value (.. e -target -value)]
                      (swap! state assoc :new-item-title new-value)))
        :style {:width "100%"}
        :placeholder "Add new item..."}]])))

(defcomponentk list-view
  "Top-level todo list component"
  [data owner]
  (display-name [this] "List")
  (init-state [_]
    {:action-chan (chan)
     :new-item-title ""})
  (will-mount [_]
    (let [action-chan (om/get-state owner :action-chan)]
      (go-loop []
               (process-action (<! action-chan))
               (recur))))
  (render-state [this {:keys [action-chan new-item-title]}]
    (html [:div {:class "well-lg"}
           [:h3 "My awesome tasks"]
           [:div
            [:div
             {:class "list-group"}
             (om/build-all list-item-view (data :items)
                           {:init-state {:action-chan action-chan}
                            :key :id})
             (om/build add-list-item-view data
                       {:init-state {:action-chan action-chan
                                     :new-item-title new-item-title}})]]])))

(defn main []
  (om/root list-view app-state
           {:target (. js/document (getElementById "app"))}))

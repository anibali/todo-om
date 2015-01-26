(ns todo.core
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [om-tools.core :refer-macros [defcomponentk]]
            [om-tools.dom :include-macros true]
            [cljs.core.async :refer [put! chan <!]]
            [sablono.core :as html :refer-macros [html]]
            [todo.validation :as v]))

(defonce app-state (atom {
  :next-id 4
  :items [{:id 1, :title "Bake a cake", :done true}
          {:id 2, :title "Cake a clown", :done false}
          {:id 3, :title "Bake a cake", :done false}]}))

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
            :onClick (fn [e] (put! action-chan [:toggle-done @data]))}
           (data :title)
           [:button {:class ["btn" "btn-xs" "pull-right"]
                     :onClick (fn [e]
                                (put! action-chan [:remove-item @data])
                                false)}
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
                   false)}
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
         (let [[action-name, action-data] (<! action-chan)]
           (case action-name
             :toggle-done
             (let [item action-data
                   new-item (update-in item [:done] not)]
               (om/transact! data :items
                             (fn [items]
                               (vec (replace {item new-item} items)))))
             :remove-item
             (let [item action-data
                   new-item (update-in item [:done] not)]
               (om/transact! data :items
                             (fn [items]
                               (vec (remove #(= % item) items)))))
             :add-item
             (let [title action-data
                   id (@data :next-id)
                   new-item {:id id, :title title, :done false}]
               (if (v/valid-list-item? new-item)
                 (do
                   (om/transact! data :next-id inc)
                   (om/transact! data :items (fn [items] (conj items new-item)))))))
           (recur)))))
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

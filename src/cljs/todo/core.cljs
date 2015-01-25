(ns todo.core
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [om-tools.core :refer-macros [defcomponentk]]
            [om-tools.dom :include-macros true]
            [cljs.core.async :refer [put! chan <!]]
            [sablono.core :as html :refer-macros [html]]))

(defonce app-state (atom {
  :next-id 4
  :items [{:id 1, :title "Bake a cake", :done true}
          {:id 2, :title "Cake a clown", :done false}
          {:id 3, :title "Bake a cake", :done false}]}))

(defn valid-list-item? [item]
  (not (clojure.string/blank? (item :title))))

(defcomponentk list-item-view [data]
  (display-name [this] "List item")
  (render-state [this {:keys [toggle-done remove-old]}]
    (html [:a
           {:class "list-group-item"
            :style {:text-decoration
                    (if (data :done) "line-through" "none")}
            :href "#"
            :onClick (fn [e] (put! toggle-done @data))}
           (data :title)
           [:button {:class ["btn" "btn-xs" "pull-right"]
                     :onClick (fn [e]
                                (put! remove-old @data)
                                false)}
            [:span {:class ["glyphicon" "glyphicon-remove"]}]]])))

(defcomponentk add-list-item-view [state]
  (display-name [this] "Add list item")
  (render-state [this {:keys [add-new new-item-title]}]
    (html
     [:form
      {:onSubmit (fn [e]
                   (put! add-new new-item-title)
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

(defcomponentk list-view [data owner]
  (display-name [this] "List")
  (init-state [_]
    {:toggle-done (chan)
     :remove-old (chan)
     :add-new (chan)
     :new-item-title ""})
  (will-mount [_]
    (let [toggle-done (om/get-state owner :toggle-done)
          remove-old (om/get-state owner :remove-old)
          add-new (om/get-state owner :add-new)]
      (go-loop []
               (let [item (<! toggle-done)
                     new-item (update-in item [:done] not)]
                 (om/transact! data :items
                               (fn [items]
                                 (vec (replace {item new-item} items))))
                 (recur)))
      (go-loop []
               (let [item (<! remove-old)
                     new-item (update-in item [:done] not)]
                 (om/transact! data :items
                               (fn [items]
                                 (vec (remove #(= % item) items))))
                 (recur)))
      (go-loop []
               (let [title (<! add-new)
                     id (@data :next-id)
                     new-item {:id id, :title title, :done false}]
                 (if (valid-list-item? new-item)
                   (do
                     (om/transact! data :next-id inc)
                     (om/transact! data :items (fn [items] (conj items new-item)))))
                 (recur)))))
  (render-state [this {:keys [toggle-done add-new new-item-title remove-old]}]
    (html [:div {:class "well-lg"}
           [:h3 "My awesome tasks"]
           [:div
            [:div
             {:class "list-group"}
             (om/build-all list-item-view (data :items)
                           {:init-state {:toggle-done toggle-done
                                         :remove-old remove-old}
                            :key :id})
             (om/build add-list-item-view data
                       {:init-state {:add-new add-new
                                     :new-item-title new-item-title}})]]])))

(defn main []
  (om/root list-view app-state
           {:target (. js/document (getElementById "app"))}))

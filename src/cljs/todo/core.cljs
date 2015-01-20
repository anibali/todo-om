(ns todo.core
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer [put! chan <!]]
            [sablono.core :as html :refer-macros [html]]))

(defonce app-state (atom {
  :next-id 4
  :items [{:id 1, :title "Bake a cake", :done true}
          {:id 2, :title "Cake a clown", :done false}
          {:id 3, :title "Bake a cake", :done false}]}))

(defn list-item-view [item]
  (reify
    om/IDisplayName
    (display-name [this] "List item")
    om/IRenderState
    (render-state [this {:keys [toggle-done]}]
            (html [:a
                   {:class "list-group-item"
                    :style {:text-decoration
                            (if (item :done) "line-through" "none")}
                    :href "#"
                    :onClick (fn [e] (put! toggle-done @item))}
                   (item :title)]))))

(defn add-list-item-view [app owner]
  (reify
    om/IDisplayName
    (display-name [this] "Add list item")
    om/IRenderState
    (render-state [this {:keys [add-new]}]
            (html [:input
                   {:ref "new-list-item"
                    :class "list-group-item"
                    :type "text"
                    :style {:width "100%"}
                    :placeholder "Add new item..."
                    :onClick (fn [e] ; TODO: Trigger on <Enter> instead
                               (let [input-field (om/get-node owner "new-list-item")
                                     new-item-title (.-value input-field)]
                               (put! add-new new-item-title)))}]))))

(defn valid-list-item? [item]
  (not (clojure.string/blank? (item :title))))

(defn list-view [app owner]
  (reify
    om/IDisplayName
    (display-name [this] "List")
    om/IInitState
    (init-state [_]
                {:toggle-done (chan), :add-new (chan)})
    om/IWillMount
    (will-mount [_]
                (let [toggle-done (om/get-state owner :toggle-done)
                      add-new (om/get-state owner :add-new)]
                  (go-loop []
                           (let [item (<! toggle-done)
                                 new-item (update-in item [:done] not)]
                             (om/transact! app :items
                                           (fn [items]
                                             (vec (replace {item new-item} items))))
                             (recur)))
                  (go-loop []
                           (let [title (<! add-new)
                                 id (@app :next-id)
                                 new-item {:id id, :title title, :done false}]
                             (if (valid-list-item? new-item)
                               (do
                                 (om/transact! app :next-id inc)
                                 (om/transact! app :items (fn [items] (conj items new-item)))))
                             (recur)))))
    om/IRenderState
    (render-state [this {:keys [toggle-done add-new]}]
                  (html [:div {:class "well-lg"}
                         [:h3 "My awesome tasks"]
                         [:div
                          [:div
                           {:class "list-group"}
                           (om/build-all list-item-view (app :items)
                                         {:init-state {:toggle-done toggle-done}
                                          :key :id})
                           (om/build add-list-item-view app
                                     {:init-state {:add-new add-new}})]]]))))

(defn main []
  (om/root list-view app-state
           {:target (. js/document (getElementById "app"))}))

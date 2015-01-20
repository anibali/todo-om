(ns todo.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer [put! chan <!]]
            [sablono.core :as html :refer-macros [html]]))

(defonce app-state (atom {
  :items [{:id 1, :title "Bake a cake", :done true}
          {:id 2, :title "Cake a clown", :done false}
          {:id 3, :title "Bake a cake", :done false}]}))

(defn list-item-view [item]
  (reify
    om/IRenderState
    (render-state [this {:keys [toggle-done]}]
            (html [:a
                   {:class "list-group-item"
                    :style {:text-decoration
                            (if (item :done) "line-through" "none")}
                    :href "#"
                    :onClick (fn [e] (put! toggle-done @item))}
                   (item :title)]))))

(defn list-view [app owner]
  (reify
    om/IInitState
    (init-state [_]
                {:toggle-done (chan)})
    om/IWillMount
    (will-mount [_]
                (let [toggle-done (om/get-state owner :toggle-done)]
                  (go (loop []
                        (let [item (<! toggle-done)
                              new-item (update-in item [:done] not)]
                          (om/transact! app :items
                                        (fn [items]
                                          (vec (replace {item new-item} items))))
                          (recur))))))
    om/IRenderState
    (render-state [this {:keys [toggle-done]}]
                  (html [:div {:class "well-lg"}
                         [:h3 "My awesome tasks"]
                         [:div
                          [:div
                           {:class "list-group"}
                           (om/build-all list-item-view (app :items)
                                         {:init-state {:toggle-done toggle-done}
                                          :key :id})
                           [:input
                            {:class "list-group-item"
                             :type "text"
                             :style {:width "100%"}
                             :placeholder "Add new item..."}]]]]))))

(defn main []
  (om/root list-view app-state
           {:target (. js/document (getElementById "app"))}))

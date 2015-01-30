(ns todo.state
  (:require [todo.validation :as v]))

;; The initial app state

(defonce app-state (atom {
  :next-id 0
  :items []}))

;; "Actions" manipulate the app state

(defn remove-item
  "Removes an item from the list"
  [state item]
  (let [items (state :items)]
    (assoc state :items
      (vec (remove #(= % item) items)))))

(defn add-item
  "Adds a new item to the list"
  [state title]
  (let [id (state :next-id)
        new-item {:id id, :title title, :done false}]
    (if (v/valid-list-item? new-item)
      (let [next-id (state :next-id)
            items (state :items)]
        (assoc state
          :next-id (inc next-id)
          :items (conj items new-item)))
      state)))

(defn toggle-done
  "Toggles an item's completion status"
  [state item]
  (let [new-item (update-in item [:done] not)
        items (state :items)]
    (assoc state :items
      (vec (replace {item new-item} items)))))

(defn process-action
  "Processes an action by executing the appropriate function for
  the action name with the action data as an argument"
  [[action-name action-data]]
  (let [actions {:toggle-done toggle-done
                 :remove-item remove-item
                 :add-item add-item}]
    (swap! app-state (actions action-name) action-data)))

;; Populate list with some initial data

(defonce _init_
  (do
    (swap! app-state add-item "Bake a cake")
    (swap! app-state add-item "Cake a clown")))

(ns todo.validation)

(defn valid-list-item?
  "Check whether a list item is valid or not.

  A list item cannot have a blank title."
  [item]
  (not (clojure.string/blank? (item :title))))

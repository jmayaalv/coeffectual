(ns coeffectual.coeffect)

(defn resolve-coeffects!
  [context coeffects]
  (->> coeffects
       (reduce-kv (fn [context id handler]
                    (assoc-in context [:coeffects id] (handler context)))
                  context)
       :coeffects))


(defn combine
  "Combines multiple coeffect-producing functions into one.
   Each function should return a map of coeffect handlers."
  [& cofx-fns]
  (fn [context]
    (reduce (fn [coeffects cofx-fn]
              (into coeffects (cofx-fn context)))
            {}
            cofx-fns)))
(ns coeffectual.coeffect)

(defn resolve-coeffects!
  [context coeffects]
  (->> coeffects
       (reduce-kv (fn [context id handler]
                    (assoc-in context [:coeffects id] (handler context)))
                  context)
       :coeffects))

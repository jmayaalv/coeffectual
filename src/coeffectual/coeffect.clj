(ns coeffectual.coeffect)

(defmulti coeffect!
  (fn [_coeffects coeffect]
    (:coeffect/type coeffect)))


(defn resolve-coeffects!
  [context requirements]
  (reduce-kv (fn [context id [type args]]
               (assoc context
                      id
                      (coeffect! context (-> args
                                             (assoc :coeffect/id id)
                                             (assoc :coeffect/type type)))))
          context
          requirements))

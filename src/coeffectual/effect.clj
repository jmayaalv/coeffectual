(ns coeffectual.effect)

(defonce !effect->handler (atom {}))

(defn register-effect!
  [effect f]
  (swap! !effect->handler assoc effect f))

(defn- handler
  [id]
  (when-let [handler (get @!effect->handler id)]
    (cond
      (var? handler)
      (deref handler)

      (fn? handler)
      handler

      :else
      (throw (ex-info "Effect handler not registered." {:id id})))))


(defn- execute-effect! [context effect]
  (if-let [handler! (handler (:effect/type (meta effect)))]
    (handler! context effect)
    (throw (ex-info (str "No effect handler found for " (:effect/type (meta effect))) effect))))

(defn execute-effects! [context effects]
  #dbg (->> effects
       (map (fn [[k effect]]
              (with-meta effect {:effect/type k})))
       (run! (partial execute-effect! context))))

(ns coeffectual.middleware)

(defn wrap-coeffect
  [handler id coeffect-fn]
  (fn [context]
    (handler (assoc context id (coeffect-fn context)))))

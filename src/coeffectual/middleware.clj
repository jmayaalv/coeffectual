(ns coeffectual.middleware)

(defn execute-handler
  [context handler]
  (if (var? handler)
    ((deref handler) context)
    (handler context)))

(defn wrap-coeffect
  [handler id coeffect-fn]
  (fn [context]
    (execute-handler (assoc context id (coeffect-fn context))
                     handler)))

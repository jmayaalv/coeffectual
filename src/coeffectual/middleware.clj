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


(defn wrap-with-handler
  [handler]
  (fn [context]
    (execute-handler (assoc (:coeffects context)
                            :command
                            (:command context))
                     handler)))

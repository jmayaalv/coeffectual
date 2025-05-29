(ns coeffectual.middleware)

(defn- get-fn
  [fn-or-var]
  (if (var? fn-or-var)
    (deref fn-or-var)
    fn-or-var))

(defn wrap-coeffect
  [handler id coeffect-fn]
  (fn [context]
    (let [handler-fn  (get-fn handler)
          coeffect-fn (get-fn coeffect-fn)]
      (handler-fn (assoc-in context
                            [:coeffects id]
                            (coeffect-fn context))))))


(defn wrap-handler
  [handler]
  (fn [context]
    (let [handler-fn (get-fn handler)]
     (handler-fn (assoc (:coeffects context)
                        :command
                        (:command context))))))

(ns coeffectual.effect)

(defonce !effect->handler (atom {}))

(defn register-effect!
  [effect f]
  (swap! !effect->handler assoc effect f))



(def default-order {:http         1
                    :db           10
                    :mq           20
                    :csv          30
                    :file         35
                    :pdf          37
                    :notification 40
                    :email        50
                    :slack        60
                    :event        70})

(defn- io-comparator [order]
  (fn [effect1 effect2]
     (compare (get order  (:effect/type effect1) 0)
             (get order (:effect/type effect2) 0))))

(defn- execute-effect! [context effect]
  (if-let [handler! (get @!effect->handler (:effect/type effect))]
    (handler! context effect)
    (throw (ex-info (str "No effect handler found for " (:effect/type effect)) effect))))

(defn execute-effects! [context effects]
  (->> effects
       (map (fn [[k effect]]
              (assoc effect :effect/type k)))
       (into (sorted-set-by (io-comparator default-order)))
       (run! (partial execute-effect! context))))

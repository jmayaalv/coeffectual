(ns coeffectual.effect)

(defmulti effect! (fn [_context effect]
                    (:effect/type effect)))


(def default-order {:http   1
                    :mq     10
                    :csv    20
                    :csvs   20
                    :event  30
                    :events 30})

(defn io-comparator [order]
  (fn [effect1 effect2]
    (compare (get order  (:effect/type effect1) 100)
             (get order (:effect/type effect2) 100))))

(defn execute-effects! [context effects]
  (->> effects
       (map (fn [[type effect]]
              (assoc effect :effect/type type)))
       (into (sorted-map-by io-comparator))
       (run! (partial effect! context))))

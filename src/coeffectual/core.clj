(ns coeffectual.core
  (:require [clojure.spec.alpha :as s]
            [coeffectual.coeffect :as coeffect]
            [coeffectual.effect :as effect]))

(s/def :coeffect/type keyword?)
(s/def :coeffect/id keyword?)

(defn execute!
  [context f args]
  (let [requirements (f args)
        coeffects    (coeffect/resolve-coeffects! context requirements)
        {:keys [state effects]} (f coeffects args)]
    (when (seq effects)
      (effect/execute-effects! context effects))
    state))

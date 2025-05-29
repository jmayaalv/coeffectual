(ns coeffectual.core
  (:require [coeffectual.coeffect :as cofx]
            [coeffectual.effect :as effect]))


(def resolve-cofx! cofx/resolve-coeffects!)

(def combine-cofx cofx/combine)

(def execute-fx! effect/execute-effects!)

(def register-fx! effect/register-effect!)

(defn execute! [context args f]
  (let [coeffects (cofx/resolve-coeffects! context (f args))
        effects   (f coeffects args)]
    (if (map? effects)
      (do (execute-fx! context (:effects effects))
          (:state effects))
      (do (execute-fx! context (second effects))
          (first effects)))))

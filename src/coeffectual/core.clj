(ns coeffectual.core
  (:require [coeffectual.coeffect :as cofx]
            [coeffectual.effect :as effect]))

(def resolve-cofx! cofx/resolve-coeffects!)

(def register-fx! effect/register-effect!)

(ns coeffectual.core
  (:require [clojure.spec.alpha :as s]
            [clojure.tools.logging :as log]))


(defn execute
  ;;coeffects
  ([args])

  ;;effects
  ([coffects args]))


(defmulti resolve-coeffect!
  (fn [_context coeffect]
    (:coeffect/type coeffect)))

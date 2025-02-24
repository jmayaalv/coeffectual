(ns coeffectual.core
  (:require [clojure.spec.alpha :as s]))

(s/def ::map-destructure-optionals
  (s/tuple #{:or} (s/map-of simple-symbol? any?)))

(s/def ::simple-keys-binding
  (s/tuple #{:keys} (s/coll-of ident? :kind vector?)))

(s/def ::qualified-keys-binding
  (s/tuple
   (s/and qualified-keyword? #(= (name %) "keys"))
   (s/coll-of simple-symbol? :kind vector?)))

(s/def ::as-binding
  (s/tuple #{:as} simple-symbol?))

(s/def ::map-destructure
  (s/every
   (s/or :simple-keys-binding ::simple-keys-binding
         :qualified-keys-bindings ::qualified-keys-binding
         :named-extract (s/tuple ::fn-argument keyword?)
         :as ::as-binding
         :or ::map-destructure-optionals)
   :kind map?))

(s/def ::fn-argument
  (s/or :sym symbol?
        :map ::map-destructure))

(s/def ::fn-args
  (s/coll-of ::fn-argument :kind vector?))


(s/def ::defc-args
  (s/cat :name simple-symbol?
         :docstring (s/? string?)
         :arglist ::fn-args
         :coeffects (s/? map?)
         :body (s/+ any?)))

(s/def ::defcoeffectual-args
  (s/cat :name simple-symbol?
         :dispatch-val any?
         :docstring (s/? string?)
         :arglist ::fn-args
         :coeffects (s/? map?)
         :body (s/+ any?)))


(s/fdef defc
  :args ::defc-args
  :ret any?)

(s/fdef defcoeffectual
  :args ::defcoeffectual-args
  :ret any?)

(defn params->coeffects [{:keys [arglist coeffects body docstring]}]
  (let [[input-type input-arg] (last arglist)
        last-expr              (last body)]
    coeffects))


(defn resolve-coeffects [args context coeffects]
  (reduce (fn [ctx [k v]]
            (merge { k (apply v (into [ctx] args))} ctx))
          context
          coeffects))


(defmacro defc
  {:arglists '([name docstring? arglist coeffects? & body])}
  [& args]
  #dbg (let [{:keys [name docstring arglist coeffects body] :as params}
             (s/conform ::defc-args args)
             arglist'  (s/unform ::fn-args arglist)
             defdoc    (cond-> [] docstring (conj docstring))
             coeffects (params->coeffects params)]
    `(defn ~name ~@defdoc ~arglist'
       (let [~(second (first arglist)) (resolve-coeffects ~(vec (rest arglist'))
                                                          ~(second (first arglist))
                                                          ~coeffects)]
         ~@body))))

(defmacro defcoeffectual
  {:arglists '([name dispatch-val docstring? arglist coeffects? & body])}
  [& args]
  #dbg (let [{:keys [name dispatch-val docstring arglist coeffects body] :as params}
             (s/conform ::defcoeffectual-args args)

             arglist'  (s/unform ::fn-args arglist)
             defdoc    (cond-> [] docstring (conj docstring))
             coeffects (params->coeffects params)]
         `(defmethod ~name ~dispatch-val ~@defdoc ~arglist'
            (let [~(second (first arglist)) (resolve-coeffects ~(vec (rest arglist'))
                                                               ~(second (first arglist))
                                                               ~coeffects)]
              ~@body))))

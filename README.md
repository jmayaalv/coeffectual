# coeffectual

Coeffectual is a Clojure library that enhances function and multimethod definitions by introducing "coeffects." Coeffects are side-input computations that enrich the context passed to a function, providing a structured and declarative way to manage context enrichment.

## Usage 

### defc Macro (Functions with Coeffects)

The defc macro defines regular functions with coeffects.

Syntax:

```Clojure

(defc function-name
  [context-arg arg1 arg2 ...]
  {coeffect-key1 (fn [context-arg arg1 arg2 ...] ...)
   coeffect-key2 (fn [context-arg arg1 arg2 ...] ...)
   ...}
  body...)
```
Example:

```Clojure

(ns example
  (:require [coeffectual :refer [defc]]))

(defc my-func
  [ctx x]
  {:y (fn [ctx x] (* x 2))
   :z (fn [ctx x] (+ (:y ctx) 3))}
  (+ (:y ctx) (:z ctx) x))

(println (my-func {:initial-val 10} 5)) ; Output: 23
```
In this example:

my-func takes a context map (ctx) and an argument x.
The coeffects map defines two coeffects: :y and :z.
The coeffect functions calculate values that are merged into the ctx map.
The function body uses the enriched ctx map.

### defcoeffectual Macro (Multimethods with Coeffects)
The defcoeffectual macro defines multimethods with coeffects.

Syntax:

```Clojure

(defcoeffectual multimethod-name dispatch-val
  [context-arg arg1 arg2 ...]
  {coeffect-key1 (fn [context-arg arg1 arg2 ...] ...)
   coeffect-key2 (fn [context-arg arg1 arg2 ...] ...)
   ...}
  body...)
```
Example:

```Clojure

(ns example
  (:require [coeffectual :refer [defcoeffectual]])
  (:require [clojure.pprint :as pprint]))

(defmulti my-multimethod (fn [ctx m]) (:type m)

(defcoeffectual my-multimethod :foo
  [ctx x]
  {:y (constantly 10)
   :z (fn [ctx x] (+ (:y ctx) x))}
  (pprint/pprint (+ (:y ctx) (:z ctx) x)))

(my-multimethod {:type :foo} 5) ; Output: 25
```
In this example:

my-multimethod is a multimethod dispatched on the :type key.
defcoeffectual defines a method for the :foo dispatch value.
The coeffects and function body work similarly to the defc macro.

## Key Features
Context Enrichment: Coeffects allow you to pre-process data or perform computations before the main function body is executed.
Purity: Coeffect functions can be pure, ensuring predictable behavior.
Declarative Syntax: Provides a clear and concise way to define functions and multimethods with coeffects.

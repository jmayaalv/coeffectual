(ns coeffectual.test.core-test
  (:require [clojure.test :refer [deftest testing is]]
            [coeffectual.core :as core]
            [coeffectual.coeffect :as cofx]
            [coeffectual.effect :as effect]))

(deftest execute!-test
  (testing "execute! with map return value as array"

    (effect/register-effect! :test-effect (fn [_ {:keys [value]}]
                                            (is (= value "effect-executed"))))

    ;; Test function that requests coeffects and returns effects
    (let [test-fn (fn ([_]
                       {:test-cofx (fn [_context]
                                     :ok)})

                    ([coeffects args]
                     [(:test-cofx coeffects) {:test-effect args}]))
          result (core/execute! {} {:value "effect-executed"} test-fn)]

      (is (= :ok result))))

  (testing "execute! with map return value as map"

    (effect/register-effect! :test-effect (fn [_ {:keys [value]}]
                                            (is (= value "effect-executed"))))

    ;; Test function that requests coeffects and returns effects
    (let [test-fn (fn ([_]
                       {:test-cofx (fn [_context]
                                     :ok)})

                    ([coeffects args]
                     {:state   (:test-cofx coeffects)
                      :effects {:test-effect args}}))
          result (core/execute! {} {:value "effect-executed"} test-fn)]
      (is (= :ok result))))



 )

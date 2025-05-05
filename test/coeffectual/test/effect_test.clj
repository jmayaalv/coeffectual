(ns coeffectual.test.effect-test
  (:require [clojure.test :refer [is deftest  testing ] :as t]
            [coeffectual.effect :as effect]))

(deftest test-execute-effects!
  (testing "effects are executed in the correct order"
    (let [executed-effects (atom [])
          test-context     {:test-data "context"}

          ;; Register test effect handlers
          _ (effect/register-effect! :http (fn [_ctx effect]
                                             (swap! executed-effects conj [:http effect])))
          _ (effect/register-effect! :db (fn [_ctx effect]
                                           (swap! executed-effects conj [:db effect])))
          _ (effect/register-effect! :email (fn [_ctx effect]
                                              (swap! executed-effects conj [:email effect])))
          _ (effect/register-effect! :custom (fn [_ctx effect]
                                               (swap! executed-effects conj [:custom effect])))

          ;; Define test effects (intentionally out of order)
          test-effects {:email  {:data "Send email"}
                        :http   {:url "https://example.com"}
                        :db     ["insert into ...?" 1]
                        :custom {:action "do-something"}}]

      ;; Execute the effects
      (effect/execute-effects! test-context test-effects)

      ;; Verify effects were executed in the correct order
      (is (= '(:custom :http :db :email)
             (map first @executed-effects)))

      ;; Verify the effect data was passed correctly
      (is (= {:url "https://example.com"}
             (:http (into {} (map (fn [[type effect]] [type effect]) @executed-effects)))))

      ;; Verify context was passed to han dlers
      (is (= 4 (count @executed-effects)))))

  (testing "throws exception for unregistered effect types"
    (let [test-context {:test-data "context"}
          test-effects {:unknown-effect {:data "test"}}]

      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"No effect handler found for :unknown-effect"
                            (effect/execute-effects! test-context test-effects)))))

  (testing "empty effects collection doesn't cause errors"
    (let [test-context {:test-data "context"}]
      (is (nil? (effect/execute-effects! test-context {})))))

  (testing "effects are sorted according to default order"
    (let [executed-effects (atom [])

          ;; Register handlers for effects with different priorities
          _ (effect/register-effect! :notification (fn [_ _] (swap! executed-effects conj :notification)))
          _ (effect/register-effect! :http (fn [_ _] (swap! executed-effects conj :http)))
          _ (effect/register-effect! :email (fn [_ _] (swap! executed-effects conj :email)))

          ;; Define effects in random order
          test-effects {:email        {}
                        :http         {}
                        :notification {}}]

      ;; Execute effects
      (effect/execute-effects! {} test-effects)

      ;; Verify they were executed in priority order
      (is (= [:http :notification :email] @executed-effects)))))

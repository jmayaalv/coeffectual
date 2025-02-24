(ns coeffectual.test.core-test
  (:require [clojure.test :refer [deftest is testing]]
            [coeffectual.core :refer [defc defcoeffectual] :as c]))

(defc f1
  [ctx x]
  {:y (constantly 10)}
  (+ x (:y ctx)))

(defc f2 [{:c/keys [b]} x]
  {:c/a (fn [ctx x] (* x 2))
   :c/b (fn [ctx x] (+ (:c/a ctx) 3))}
  b)

(defc f3
  [ctx x y]
  {:z (constantly 10)}
  (+ x y (:z ctx)))

(defc f3
  [ctx x y]
  {:z (constantly 10)}
  (+ x y (:z ctx)))

(defc with-error
  [ctx x y]
  {:z                 (constantly (throw (ex-info "error" {})))
   :coeffectual/error (fn [_error]
                       :error)}
  (+ x y (:z ctx)))


(deftest test-defc
  (testing "basic"
    (is (= (f1 {} 5) 15)))
  (testing "Ensures existing ctx keys are preserved"
    (is (= 25 (f1 {:y 20} 5))))
  (testing "Coeffects execution order"
    (is (= 13 (f2 {} 5))))
  (testing "many args "
    (is (= 22 (f3 {} 5 7))))
  (testing "with error"
    (is (= :error (with-error {} 5 7)))))

(defmulti m1 (fn [_context m]
               (:type m)))

(defmulti m2 (fn [_context m x]
               (:type m)))

(defcoeffectual m1 :test
  [ctx {:keys [x]}]
  {:y (constantly 10)}
  (+ x (:y ctx)))

(defcoeffectual m2 :test
  [{:keys [y]} {:keys [x]} z]
  {:y (constantly 10)}
  (+ x z y))

(defcoeffectual m1 :test2
  [{:keys [y]} {:keys [x]}]
  {:y (constantly 10)}
  (+ x y))

(defcoeffectual m1 :test2
  [{:keys [y]} {:keys [x]}]
  {:y (constantly 10)}
  (+ x y))

(defcoeffectual m1 :error
  [{:keys [y]} {:keys [x]}]
  {:y                 (constantly (throw (ex-info "an error" {})))
   :coeffectual/error (fn [_] :error)}
  (+ x y))


(deftest test-defcoeffectual
  (testing "basic"
    (is (= 15 (m1 {} {:type :test :x 5}))))
  (testing "preserve context"
    (is (= 10 (m1 {:y 5} {:type :test :x 5}))))
  (testing "many args "
    (is (= 22 (m2 {} {:type :test :x 5} 7))))
  (testing "error  "
    (is (= :error (m1 {} {:type :error :x 5})))))

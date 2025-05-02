(ns coeffectual.test.coeffect-test
  (:require [clojure.test :refer [deftest is testing]]
            [coeffectual.coeffect :as coeffect]))

;; Define test implementations of the coeffect! multimethod
(defmethod coeffect/coeffect! :db
  [context _coeffect]
  #dbg (if (:first context)
    {:users {2 {:id 2 :username "user2"}}}
    {:users {1 {:id 1 :username "user"}}}))

(defmethod coeffect/coeffect! :test-time
  [_context _]
  (java.time.LocalDate/now))

(deftest resolve-coeffects!-test
  (testing "resolves empty requirements"
    (let [context      {:existing "value"}
          requirements {}
          result       (coeffect/resolve-coeffects! context requirements)]
      (is (= context result))))

  (testing "resolves single requirement"
    (let [context      {:existing "value"}
          requirements {:test-db [:db {:connection "string"}]}
          result       (coeffect/resolve-coeffects! context requirements)]
      (is (= {:existing "value" :test-db {:users {1 {:id 1 :username "user"}}}}
             result))))

  (testing "resolves multiple requirements"
    (let [context      {:existing "value"}
          requirements {:db   [:db {:connection "string"}]
                        :time [:test-time nil]}
          result       (coeffect/resolve-coeffects! context requirements)]
      (is (= {:db       {:users {1 {:id 1, :username "user"}}},
              :existing "value",
              :time     (java.time.LocalDate/now)}
             result))))

  (testing "overwrites existing keys in context"
    (let [context      {:existing "value" :db "old-value"}
          requirements {:db [:db {:connection "string"}]}
          result       (coeffect/resolve-coeffects! context requirements)]
      (is (= {:db {:users {1 {:id 1, :username "user"}}}, :existing "value"}
             result))))

  (testing "context is passed to each coeffect handler"
    (let [context      {:existing "value"}
          requirements {:first  [:db {:order 1}]
                        :second [:db {:order 2}]}
          result       (coeffect/resolve-coeffects! context requirements)]
      (is (= {:existing "value",
              :first    {:users {1 {:id 1, :username "user"}}},
              :second   {:users {2 {:id 2, :username "user2"}}}}
             result))
      )))

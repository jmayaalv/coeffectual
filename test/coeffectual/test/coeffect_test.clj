(ns coeffectual.test.coeffect-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [coeffectual.coeffect :as cofx]))

(deftest test-resolve-coeffects!
  (testing "resolves coeffects correctly"
    (reset! cofx/!coeffects {})
    (cofx/register-cofx! :db
                         (fn [_]
                           {:users [{:id 1 :name "Alice"} {:id 2 :name "Bob"}]}))
    (cofx/register-cofx! :http
                         (fn [_] {:status 200 :body "Success"}))
    (cofx/register-cofx! :config
                         (fn [_] {:api-key "test-key" :env "test"}))
    (cofx/register-cofx! :with-param
                         (fn [_ param]
                           {:param param}))

    (is (= {:config     {:api-key "test-key" :env "test"}
            :db         {:users [{:id 1 :name "Alice"} {:id 2 :name "Bob"}]}
            :http       {:body "Success" :status 200}
            :with-param {:param "test-value"}}
           (:coeffects (cofx/execute! {} [(cofx/inject-cofx :db)
                                         (cofx/inject-cofx :http)
                                         (cofx/inject-cofx :config)
                                         (cofx/inject-cofx :with-param "test-value")])))))

  (testing "throws exception for unregistered coeffect"
    (reset! cofx/!coeffects {})
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"Coeffect handler not found: :unknown-coeffect"
                          (cofx/execute! nil [(cofx/inject-cofx :unknown-coeffect)]))))

  (testing "multiple coeffects are composed correctly"
    (reset! cofx/!coeffects {})
    (let [execution-order (atom [])
          _               (cofx/register-cofx! :first
                                               (fn [_]
                                                 (swap! execution-order conj :first)
                                                 :first-result))
          _               (cofx/register-cofx! :second
                                               (fn [_]
                                                 (swap! execution-order conj :second)
                                                 :second-result))
          _               (cofx/register-cofx! :third
                                               (fn [_]
                                                 (swap! execution-order conj :third)
                                                 :third-result))]

      (is (= {:first :first-result, :second :second-result, :third :third-result}
             (:coeffects (cofx/execute! {} [(cofx/inject-cofx :first)
                                           (cofx/inject-cofx :second)
                                           (cofx/inject-cofx :third)])))))))

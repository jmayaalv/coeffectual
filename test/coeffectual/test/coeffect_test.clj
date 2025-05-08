(ns coeffectual.test.coeffect-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [coeffectual.coeffect :as cofx]))

(deftest test-resolve-coeffects!
  (testing "resolves empty coeffects map"
    (let [context {:coeffects {}}
          result (cofx/resolve-coeffects! context {})]
      (is (= {} result))))
  
  (testing "resolves single coeffect"
    (let [context {:coeffects {}}
          coeffects {:db (fn [_] "db-value")}
          result (cofx/resolve-coeffects! context coeffects)]
      (is (= {:db "db-value"} result))))
  
  (testing "resolves multiple coeffects"
    (let [context {:coeffects {}}
          coeffects {:db (fn [_] "db-value")
                     :local-storage (fn [_] "storage-value")
                     :time (fn [_] "time-value")}
          result (cofx/resolve-coeffects! context coeffects)]
      (is (= {:db "db-value"
              :local-storage "storage-value"
              :time "time-value"} 
             result))))
  
  (testing "handlers have access to context"
    (let [context {:coeffects {} :request-id "123"}
          coeffects {:query-id (fn [ctx] (:request-id ctx))}
          result (cofx/resolve-coeffects! context coeffects)]
      (is (= {:query-id "123"} result))))
  
  (testing "preserves existing coeffects"
    (let [context {:coeffects {:existing "value"}}
          coeffects {:new (fn [_] "new-value")}
          result (cofx/resolve-coeffects! context coeffects)]
      (is (= {:existing "value" :new "new-value"} result))))
  
  (testing "handler can access other coeffects"
    (let [context {:coeffects {}}
          coeffects {:first (fn [_] "first-value")
                     :second (fn [ctx] (str (get-in ctx [:coeffects :first]) "-modified"))}
          result (cofx/resolve-coeffects! context coeffects)]
      (is (= {:first "first-value"
              :second "first-value-modified"} 
             result)))))

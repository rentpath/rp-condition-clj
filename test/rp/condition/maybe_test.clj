(ns rp.condition.maybe-test
  (:require [clojure.test :refer :all]
            [rp.condition.maybe :refer :all]))

(deftest test-handle
  (is (= 21
         (handle (fn [n] (/ n 2))
                 (fn [e] (ex-data e))
                 42)))
  (is (= 21
         (handle (fn [{:keys [answer]}] (/ answer 2))
                 (fn [e] (ex-data e))
                 {:answer 42}))
      "Should support destructuring")
  (is (= :bears
         (handle (fn [n] "We could have been great together.")
                 (fn [e] (:type (ex-data e)))
                 (ex-info "Bad news" {:type :bears}))))
  (is (= :null-and-void
         (handle (fn [n] (if-not n
                           :null-and-void
                           (/ n 2)))
                 (fn [e] (ex-data e))
                 nil))
      "A nil value is ok; only ExceptionInfo objects are considered errors."))

(deftest test-pipeline
  (let [init 5]
    (is (= init (pipeline [] init)))
    (is (= init (pipeline nil init)))

    (let [inc-step (fn [n] (inc n))
          double-step (fn [n] (* 2 n))
          ex (ex-info "Oh no" {:foo "bar"})
          err-step (fn [n] ex)]
      (is (= 13
             (pipeline [inc-step double-step inc-step] init)))
      (is (= ex
             (pipeline [inc-step double-step err-step inc-step] init))))))

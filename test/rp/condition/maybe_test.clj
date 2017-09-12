(ns rp.condition.maybe-test
  (:require [clojure.test :refer :all]
            [rp.condition.maybe :refer :all]))

(deftest test-handle
  (is (= 21
         (handle (fn [n] (/ n 2))
                 (fn [e] (error-data e))
                 42)))
  (is (= 21
         (handle (fn [{:keys [answer]}] (/ answer 2))
                 (fn [e] (error-data e))
                 {:answer 42}))
      "Should support destructuring")
  (is (= :bears
         (handle (fn [n] "We could have been great together.")
                 (fn [e] (:type (error-data e)))
                 (make-error {:type :bears}))))
  (is (= :null-and-void
         (handle (fn [n] (if-not n
                           :null-and-void
                           (/ n 2)))
                 (fn [e] (error-data e))
                 nil))
      "A nil value is ok; only objects satisfying IError are considered errors."))

(deftest test-pipeline
  (let [init 5]
    (is (= init (pipeline [] init)))
    (is (= init (pipeline nil init)))

    (let [inc-step (fn [n] (inc n))
          double-step (fn [n] (* 2 n))
          err (make-error "Boom!")
          err-step (fn [n] err)]
      (is (= 13
             (pipeline [inc-step double-step inc-step] init)))
      (is (= err
             (pipeline [inc-step double-step err-step inc-step] init))))))

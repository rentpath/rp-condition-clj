(ns rp.condition-test
  (:require [clojure.test :refer :all]
            [rp.condition :refer :all])
  (:import clojure.lang.ExceptionInfo))

(deftest test-throw-it
  (is (thrown-with-msg? ExceptionInfo #"Uncaught exception\."
                        (throw-it {:some :data})))
  (is (thrown-with-msg? ExceptionInfo #"My message"
                        (throw-it "My message")))
  (is (thrown-with-msg? ExceptionInfo #"Test message"
                        (throw-it "Test message" {:some :data}))))

(deftest test-unbound-conditions
  (is (thrown-with-msg? ExceptionInfo #"Uncaught exception\."
                        (*not-found* {:some :data})))
  (is (thrown-with-msg? ExceptionInfo #"My message"
                        (*not-found* "My message")))
  (is (thrown-with-msg? ExceptionInfo #"Test message"
                        (*not-found* "Test message" {:some :data}))))

(deftest test-bound-conditions
  (binding [*not-found* (constantly 42)]
    (is (= 42 (*not-found* "Message"))))
  (binding [*not-found* (fn [x] (* x 2))]
    (is (= 84 (*not-found* 42))))
  (binding [*not-found* (fn [x y] (/ x y))]
    (is (= 21 (*not-found* 42 2))))
  (binding [*not-found* (fn [x y z] (+ x y z))]
    (is (= 6 (*not-found* 1 2 3)))))

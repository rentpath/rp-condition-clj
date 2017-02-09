(ns rp.condition.result-test
  (:require [clojure.test :refer :all]
            [rp.condition.result :refer :all]))

(deftest test-with-result
  (is (= 21
         (with-result (result 42)
           [:ok data] (/ data 2)
           [:error e] (ex-data e))))
  (is (= 21
         (with-result (result {:answer 42})
           [:ok {:keys [answer]}] (/ answer 2)
           [:error e]             (ex-data e)))
      "Should support destructuring")
  (is (= :bears
         (with-result (result (ex-info "Bad news" {:type :bears}))
           [:ok data] "We could have been great together."
           [:error e] (:type (ex-data e)))))
  (is (= :null-and-void
         (with-result (result nil)
           [:ok data] (if-not data
                        :null-and-void
                        (/ data 2))
           [:error e] (ex-data e)))
      "A result is 'ok' as long as its :error is not set. The :ok may be falsey."))

(deftest test-with-result-evaluates-forms-once
  (let [main-runs (atom 0)
        ok-runs (atom 0)
        error-runs (atom 0)]
    (letfn [(inc-main [] (swap! main-runs inc))
            (inc-ok [] (swap! ok-runs inc))
            (inc-error [] (swap! error-runs inc))
            (reset-all []
              (reset! main-runs 0)
              (reset! ok-runs 0)
              (reset! error-runs 0))]
      (let [ok-res (with-result (result (do (inc-main) 42))
                     [:ok n] (do (inc-ok) (+ n 8))
                     [:error e] (do (inc-error) (ex-data e)))]
        (is (= 50 ok-res))
        (is (= 1 @main-runs))
        (is (= 1 @ok-runs))
        (is (= 0 @error-runs))
        ;; DON'T FORGET
        (reset-all))
      (let [error-res (with-result (result (do (inc-main) (ex-info "bad" {:news :bears})))
                        [:ok n] (do (inc-ok) (+ n 8))
                        [:error e] (do (inc-error) (assoc (ex-data e) :actor :matthau)))]
        (is (= {:news :bears :actor :matthau} error-res))
        (is (= 1 @main-runs))
        (is (= 0 @ok-runs))
        (is (= 1 @error-runs))
        ;; DON'T FORGET
        (reset-all))
      (let [ok-nil (with-result (result (do (inc-main) nil))
                     [:ok n] (do (inc-ok) "nada")
                     [:error e] (do (inc-error) (ex-data e)))]
        (is (= "nada" ok-nil))
        (is (= 1 @main-runs))
        (is (= 1 @ok-runs))
        (is (= 0 @error-runs))
        ;; DON'T FORGET
        (reset-all))
      (let [ok-false (with-result (result (do (inc-main) false))
                       [:ok n] (do (inc-ok) "falsch")
                       [:error e] (do (inc-error) (ex-data e)))]
        (is (= "falsch" ok-false))
        (is (= 1 @main-runs))
        (is (= 1 @ok-runs))
        (is (= 0 @error-runs))
        ;; DON'T FORGET
        (reset-all)))))

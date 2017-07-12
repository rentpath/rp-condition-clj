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

(deftest test-handle-result
  (is (= 21
         (handle-result (fn [n] (/ n 2))
                        (fn [e] e)
                        (result 42))))
  (is (= 21
         (handle-result (fn [{:keys [answer]}] (/ answer 2))
                        (fn [e] e)
                        (result {:answer 42})))
      "Should support destructuring")
  (is (= :bears
         (handle-result (fn [n] "We could have been great together.")
                        (fn [e] (:type e))
                        (error {:msg "Bad news" :type :bears}))))
  (is (= :null-and-void
         (handle-result (fn [n] (if-not n
                                  :null-and-void
                                  (/ n 2)))
                        (fn [e] e)
                        (ok nil)))
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

(deftest test-handle-result-evaluates-forms-once
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
      (let [ok-res (handle-result (fn [n] (do (inc-ok) (+ n 8)))
                                  (fn [e] (do (inc-error) (ex-data e)))
                                  (result (do (inc-main) 42)))]
        (is (= 50 ok-res))
        (is (= 1 @main-runs))
        (is (= 1 @ok-runs))
        (is (= 0 @error-runs))
        ;; DON'T FORGET
        (reset-all))
      (let [error-res (handle-result (fn [n] (do (inc-ok) (+ n 8)))
                                     (fn [e] (do (inc-error) (assoc (ex-data e) :actor :matthau)))
                                     (result (do (inc-main) (ex-info "bad" {:news :bears}))))]
        (is (= {:news :bears :actor :matthau} error-res))
        (is (= 1 @main-runs))
        (is (= 0 @ok-runs))
        (is (= 1 @error-runs))
        ;; DON'T FORGET
        (reset-all))
      (let [ok-nil (handle-result (fn [n] (do (inc-ok) "nada"))
                                  (fn [e] (do (inc-error) (ex-data e)))
                                  (result (do (inc-main) nil)))]
        (is (= "nada" ok-nil))
        (is (= 1 @main-runs))
        (is (= 1 @ok-runs))
        (is (= 0 @error-runs))
        ;; DON'T FORGET
        (reset-all))
      (let [ok-false (handle-result (fn [n] (do (inc-ok) "falsch"))
                                    (fn [e] (do (inc-error) (ex-data e)))
                                    (result (do (inc-main) false)))]
        (is (= "falsch" ok-false))
        (is (= 1 @main-runs))
        (is (= 1 @ok-runs))
        (is (= 0 @error-runs))
        ;; DON'T FORGET
        (reset-all)))))

(deftest test-pipeline
  (let [init 5]
    (is (= (ok init) (pipeline [] init)))
    (is (= (ok init) (pipeline nil init)))

    (let [inc-step (fn [n] (ok (inc n)))
          double-step (fn [n] (ok (* 2 n)))
          bad-step (fn [n] :not-a-result)
          err "Oh no"
          err-step (fn [n] (error err))]
      (testing "pipeline runs successfully to completion"
        (is (= (ok 13)
               (pipeline [inc-step double-step inc-step] init))))
      (testing "pipeline stops when it encounters an error"
        (is (= (error err)
               (pipeline [inc-step double-step err-step inc-step] init))))
      (testing "pipeline throws an exception when a step doesn't return a Result"
        (is (thrown? AssertionError
               (pipeline [inc-step double-step bad-step inc-step] init)))))))

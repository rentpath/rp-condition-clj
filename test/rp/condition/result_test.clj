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


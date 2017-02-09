(ns rp.condition.result
  "A result 'type' à la Result in Rust, the Error monad, etc."
  (:import clojure.lang.ExceptionInfo))

(defrecord Result [ok error])

(defn result
  "A result is in an 'error' state if its `error` entry has an instance of `ExceptionInfo` as its value. Otherwise it's assumed to be `ok`."
  [x]
  (if (instance? ExceptionInfo x)
    (->Result nil x)
    (->Result x nil)))

(defn unwrap
  "Blindly ask for the result, throwing the `(:error result)` exception if the result has an error instead."
  [result]
  (if-let [error (:error result)]
    (throw error)
    (:ok result)))

(defn validate-match-args
  [conditions condition-1 condition-2]
  (when-not (and (conditions condition-1)
                 (conditions condition-2))
    (throw (IllegalArgumentException.
            (str "The only legal conditions are "
                 (pr-str conditions)
                 " but you supplied "
                 (pr-str condition-1)
                 " and "
                 (pr-str condition-2))))))

(defmacro with-result
  "A 'pattern match' for the :ok and :error cases of the Result type.

  This macro provides a single point of enforcement that both 'good' and 'bad'
  result scenarios are handled explicitly. This can be used to thread
  result values through a call stack rather than using dynamic scope or
  exceptions for control flow.

  This could certainly be built upon to do something more dynamic by dispatching
  on the :ok and/or :error payloads.

  Examples:

  (with-result (result 42)
    [:ok data] (/ data 2)
    [:error e] (ex-data e)) ;=> 21

  (with-result (result (ex-info \"Bad news\" {:type :bears}))
    [:ok data] data
    [:error e] (:type (ex-data e))) ;=> :bears

  ;; Destructuring is supported, just like in `let`
  (with-result (result {:answer 42})
    [:ok {:keys [answer]}] (/ answer 2)
    [:error e]             (ex-data e)) ;=> 21

  ;; Order of the :ok/:error clauses doesn't matter
  (with-result (result (ex-info \"Bad news\" {:type :bears}))
    [:error e] (:type (ex-data e))
    [:ok data] data) ;=> :bears"
  [result
   condition-binding-a branch-a
   condition-binding-b branch-b]
  (let [conditions #{:ok :error} ;; these access the Result record
        ;; Allow :ok and :error branches in either order.
        ;; Bindings...
        [ok-condition-binding error-condition-binding] (if (= (first condition-binding-a) :ok)
                                                         [condition-binding-a condition-binding-b]
                                                         [condition-binding-b condition-binding-a])
        ;; ...and Branches
        [ok-branch error-branch] (if (= condition-binding-a ok-condition-binding)
                                   [branch-a branch-b]
                                   [branch-b branch-a])
        [ok ok-binding] ok-condition-binding
        [error error-binding] error-condition-binding]
    (validate-match-args conditions ok error)
    `(let [result# ~result ;; Evaluate form that returns Result only once
           ok-result# (:ok result#)
           error-result# (:error result#)]
       (if error-result#
         (let [~error-binding error-result#]
           ~error-branch)
         (let [~ok-binding ok-result#]
           ~ok-branch)))))

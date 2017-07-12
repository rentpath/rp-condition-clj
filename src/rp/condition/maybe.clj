(ns rp.condition.maybe
  (:import clojure.lang.ExceptionInfo))

(defn ex-info?
  [x]
  (instance? ExceptionInfo x))

(defn handle
  [ok-fn error-fn x]
  (if (ex-info? x)
    (error-fn x)
    (ok-fn x)))

(defn pipeline
  "Pass `init` data value into a pipeline of functions.
   As soon as an ex-info is encountered, the pipeline will return it.
   Otherwise the return value from each step function will be passed as the input into the
   next function until all functions have been called, and the final result will be returned.

   When there are no functions, simply returns `init`"
  [[f & more-fs] init]
  (if f
    (handle (fn [x] (pipeline more-fs x))
            (fn [e] e)
            (f init))
    init))

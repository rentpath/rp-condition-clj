(ns rp.condition.maybe)

(defprotocol IError
  (error-data [this]))

(defrecord ErrorImpl [data]
  IError
  (error-data [this] data))

(defn make-error
  "Make an IError from anything.
   `data` can be whatever type you like (map, keyword, whatever).
   Choose your own conventions for error payloads."
  [data]
  (->ErrorImpl data))

(defn error?
  [x]
  (satisfies? IError x))

(defn handle
  [ok-fn error-fn x]
  (if (error? x)
    (error-fn x)
    (ok-fn x)))

(defn pipeline
  "Pass `init` data value into a pipeline of functions.
   As soon as an error is encountered, the pipeline will return it.
   Otherwise the return value from each step function will be passed as the input into the
   next function until all functions have been called, and the final result will be returned.

   When there are no functions, simply returns `init`"
  [[f & more-fs] init]
  (if f
    (handle (fn [x] (pipeline more-fs x))
            (fn [e] e)
            (f init))
    init))

(ns rp.condition)

;;;;;;;;;;;;
;; Errors ;;
;;;;;;;;;;;;

(defn throw-it
  "Throw an `clojure.lang.ExceptionInfo` exception. This is a helper function which acts as the default implementation for conditions in this library."
  ([msg-or-data]
   (if (string? msg-or-data)
     (throw-it msg-or-data {})
     (throw-it "Uncaught exception." msg-or-data)))
  ([msg data]
   (throw (ex-info msg data))))

(def ^:dynamic *not-found* throw-it)

(def ^:dynamic *missing-data* throw-it)

(def ^:dynamic *timeout* throw-it)

;;;;;;;;;;;;;;
;; Restarts ;;
;;;;;;;;;;;;;;

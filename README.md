# rp.condition

This library provides a simple condition system for Clojure, inspired by Chris Houser's [_Condition Systems in an Exceptional Language_](https://www.youtube.com/watch?v=zp0OEDcAro0).

See `rp.condition` for the public conditions provided. Unless otherwise rebound, conditions in this library will throw a `clojure.lang.ExceptionInfo` exception.

## Usage

It is not uncommon for timeouts to occur when communicating over a network. This library includes a `rp.condition/*timeout*` condition that represents this scenario.

If you use `*timeout*` in isolation, it will throw an exception:

```clj
(defn make-networked-call [args]
  (let [fut (future (network-call args))
        result (deref fut 1000 ::timeout)]
    (if (= result ::timeout)
      (*timeout* "Request to foo timed out." {:args args})
      result)))
```

By using the dynamically-scoped nature of Clojure's dynamic var's, you can rebind `*timeout*` at a higher level in your call stack to change what it returns when called. In this case, we return an empty result vector instead of throwing an exception, assumably because in this part of our code base, a failure to complete `make-networked-call` is not a crucial operation:

```clj
;; Higher up the call stack:
(binding [*timeout* (constantly [])]
  (make-networked-call [:a :b :c]))
;; Returns response from the network call if
;; it doesn't timeout, else []
```

## License

Copyright © 2016 RentPath, LLC.

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

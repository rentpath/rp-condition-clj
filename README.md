# rp.condition

[![Build Status](https://travis-ci.org/rentpath/rp-condition-clj.svg?branch=master)](https://travis-ci.org/rentpath/rp-condition-clj) [![Clojars Project](https://img.shields.io/clojars/v/com.rentpath/rp-condition-clj.svg)](https://clojars.org/com.rentpath/rp-condition-clj)


This library provides abstractions for dealing with errors and "exceptional" conditions with tools outside of Java exceptions.

The `rp.condition` namespace provides a simple condition system for Clojure, inspired by Chris Houser's [_Condition Systems in an Exceptional Language_](https://www.youtube.com/watch?v=zp0OEDcAro0). It's primary purpose is to provide a set of public, shared conditions to be used across libraries and applications within an organization. See `rp.condition` for the public conditions provided. Unless otherwise rebound, conditions in this library will throw a `clojure.lang.ExceptionInfo` exception.

The `rp.condition.result` namespace provides a simple result type in the spirit of Rust's [Result](https://doc.rust-lang.org/std/result/). The `result` function builds a result, and the `unwrap` and `with-result` forms provide a means to handle the "ok" and "error" scenarios of a given result value.

## Usage

It is not uncommon for timeouts to occur when communicating over a network. This library includes a `rp.condition/*timeout*` condition that represents this "exceptional" condition.

If you use `*timeout*` in isolation, it will throw an exception:

```clj
(require '[rp.condition :refer [*timeout*]])

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

### Errors and Restarts

An error condition is one that, when rebound, provides the ability of code higher in the call stack to inject default values into lower levels of the code. This can be a static default value, as shown in the timeout example above, or a value computed based on runtime values.

Restarts in a condition system support choosing one or more code branches to follow depending on the nature of the exceptional situation at runtime. This library does not currently include the machinery for restarts, but it can be added when the need arises.

## License

Copyright © 2016 RentPath, LLC.

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

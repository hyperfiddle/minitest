# RCF – turn your Rich Comment Forms into tests

RCF is a REPL-friendly Clojure/Script test macro and notation for describing what code does, or should do. We find it especially good for brainstorming. A well-formed idea, presented for consideration, comes in the form of an RCF.

```Clojure
{:deps {hyperfiddle/rcf {:git/url "https://github.com/hyperfiddle/rcf.git" :sha ...}}}
```

[![JVM](https://github.com/hyperfiddle/rcf/actions/workflows/tests_clj.yml/badge.svg?branch=master)](https://github.com/hyperfiddle/rcf/actions/workflows/tests_clj.yml)
[![NodeJS](https://github.com/hyperfiddle/rcf/actions/workflows/tests_node.yml/badge.svg?branch=master)](https://github.com/hyperfiddle/rcf/actions/workflows/tests_node.yml)
[![Browser](https://github.com/hyperfiddle/rcf/actions/workflows/tests_browser.yml/badge.svg?branch=master)](https://github.com/hyperfiddle/rcf/actions/workflows/tests_browser.yml)

# Usage

Tests are run when you send a file or form to your Clojure/Script REPL. In Cursive, that's cmd-shift-L to re-run the file.

```clojure
(ns example
  (:require [hyperfiddle.rcf :refer [tests ! %]]))

(tests
  "equality"
  (inc 1) := 2

  "wildcards"
  {:a :b, :b [2 :b]} := {:a _, _ [2 _]}

  "unification"
  {:a :b, :b [2 :b]} := {:a ?b, ?b [2 ?b]}

  "unification on reference types"
  (def x (atom nil))
  {:a x, :b x} := {:a ?x, :b ?x}

  "the usual REPL bindings"
  :foo
  :bar
  :baz
  *3 := :foo
  *2 := :bar
  *1 := :baz

  (tests
    "nested tests for convenience"
    1 := 1))
```
```text
Loading src/example.cljc...
✅✅✅✅✅✅✅✅Loaded
```

# Async tests (experimental)

Coming soon

```Clojure
(ns example
  (:require [clojure.core.async :refer [chan >! go go-loop <! timeout close!]]
            [hyperfiddle.rcf :as rcf :refer [tests ! %]]
            [missionary.core :as m]))

(rcf/set-timeout! 100)

(tests
  "async tests"
  #?(:clj  (tests
             (future
               (rcf/! 1) (Thread/sleep 10)
               (rcf/! 2) (Thread/sleep 200)
               (rcf/! 3))
             % := 1
             % := 2
             % := ::rcf/timeout)
     :cljs (tests
             (defn setTimeout [f ms] (js/setTimeout ms f))
             (rcf/! 1) (setTimeout 10 (fn []
             (rcf/! 2) (setTimeout 200 (fn []
             (rcf/! 3)))))
             % := 1
             % := 2
             % := ::rcf/timeout))

  "core.async"
  (def c (chan))
  (go-loop [x (<! c)]
    (when x
      (<! (timeout 10))
      (! x)
      (recur (<! c))))
  (go (>! c :hello) (>! c :world))
  % := :hello
  % := :world
  (close! c)

  "missionary"
  (def !x (atom 0))
  (def dispose ((m/reactor (m/stream! (m/ap (! (inc (m/?< (m/watch !x)))))))
                (fn [_] #_(prn ::done)) #(prn ::crash %)))
  % := 1
  (swap! !x inc)
  (swap! !x inc)
  % := 2
  % := 3
  (dispose))
```

# Configuration

`(tests)` blocks erase by default (macroexpanding to nothing). They will only run and assert under a flag:

```Clojure
(ns dev-entrypoint
  (:require [example] ; transitive inline tests will erase
            [hyperfiddle.rcf :refer [tests]]))

; wait to enable tests until after app namespaces are loaded (intended for subsequent REPL interactions) 
#?(:clj  (alter-var-root #'hyperfiddle.rcf/*enabled* (constantly true))
   :cljs (set! hyperfiddle.rcf/*enabled* true))

; subsequent REPL interactions will run tests

; prevent test execution during cljs hot code reload
#?(:cljs (defn ^:dev/before-load stop [] (set! hyperfiddle.rcf/*enabled* false)))
#?(:cljs (defn ^:dev/after-load start [] (set! hyperfiddle.rcf/*enabled* true)))
```

# CI

To run in CI, generate clojure.test deftests with a JVM flag and then run them with clojure.test. [Github actions examples](https://github.com/hyperfiddle/rcf/tree/master/.github/workflows).

```Clojure
; deps.edn
{:aliases {:test {:jvm-opts ["-Dhyperfiddle.rcf.generate-tests=true"]}}}
```
```bash
% clj -M:test -e "(require 'example)(clojure.test/run-tests 'example)"

Testing example
✅✅✅✅✅✅✅✅
Ran 1 tests containing 8 assertions.
0 failures, 0 errors.
{:test 1, :pass 8, :fail 0, :error 0, :type :summary}
```

# FAQ

*One of my tests threw an exception, but the stack trace is empty?* — you want `{:jvm-opts ["-XX:-OmitStackTraceInFastThrow"]}` [explanation](https://web.archive.org/web/20190416091616/http://yellerapp.com/posts/2015-05-11-clojure-no-stacktrace.html) (this may be JVM specific)

# Contributing

Sure – you can reach us on clojurians.net in #hyperfiddle or ping @dustingetz.

# Acknowledgments

Thank you to https://github.com/tristefigure for discovery, first implementations, and especially exploring various ClojureScript compiler monkey patches that we considered. RCF was not easy to make.

![Scroll Of Truth meme saying "you do not really understand something until you can explain it as a passing test".](./doc/meme.png)

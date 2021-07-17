(ns example
  (:require [hyperfiddle.rcf :as rcf :refer [tests ! %]]
            [clojure.core.async :refer [chan >! go go-loop <! timeout close!]]
            [missionary.core :as m]))

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
    "nested tests are sometimes convenient"
    1 := 1)

  "inequality"
  1 :<> 2

(tests {:timeout 100}
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
             % := ::rcf/timeout)))

(tests
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

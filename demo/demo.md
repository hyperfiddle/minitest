# How to use RCF

## With `clj`

```shell
$ pwd
/Users/geoffreygaillard/Documents/hyperfiddle/rcf

$ clj
Clojure 1.10.0
user=>

user=> (require '[hyperfiddle.rcf :refer [tests]])
nil

user=> (tests 1 := 1)
nil

user=> (alter-var-root #'hyperfiddle.rcf/*enabled* (constantly true))
true

user=> (tests 1 := 1)
✅nil

user=> (tests 1 := 2)

❌ user:1 
 in 1

1
:≠ 
2

nil

```

## With an editor

https://user-images.githubusercontent.com/3972968/126034516-36214002-bdba-468e-9192-fcfa47394ecf.mp4


## Use case

## Advanced usage

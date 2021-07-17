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

<video controls>
    <source src="./videos/getting_started.mp4" type="video/mp4">
    <p>Your browser doesn’t support HTML5 Videos, but you can <a href="./videos/getting_started.mp4">download the video</a></p>
</video>

## Use case

## Advanced usage

(ns minitest.with-bindings
  (:require  [net.cgrand.macrovich       :as macros]
    #?(:cljs [applied-science.js-interop :as j])
    #?(:cljs [clojure.string             :as str])))

(macros/case :cljs
  (do (defn var-name [var]
        (symbol (-> var meta :ns) (-> var meta :name)))

      (defn js-name [var]
        (munge (str (-> var meta :ns) "." (-> var meta :name))))

      (def bindings-jsa
        "Joint security area to convoy data between Cljs and runtime evaled Js
        without relying on the Cljs compiler"
        (js-obj))

      (def ^:private ^:export bindings-jsa-name
        (-> `bindings-jsa str (str/replace "/" ".") munge))

      (defn ^:private var-set [var val]
        (let [id      (str (gensym))
              js-name (js-name var)]
          (j/assoc! bindings-jsa id val)
          (js/eval (str js-name " = " bindings-jsa-name \[ \" id \" \]))
          (j/assoc! bindings-jsa id nil)))

      (defn with-bindings* [bindings-map f & args]
        (let [orig  (->> (keys bindings-map)
                         (map (juxt identity deref))
                         (into {}))]
          (doseq [[var val] bindings-map]
            (var-set var val))
          (try (apply f args)
            (finally
              (doseq [[var orig-val] orig]
                (var-set var orig-val))))))))
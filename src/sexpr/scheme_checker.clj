(ns sexpr.scheme-checker
  (:require [clojure.set :as set]))


(defn check-by-scheme
  [data scheme]
  (let [global-ident-restr (first data)
        global-mods-restr (second data)
        scheme (last data)
        base-present-idents (set (first global-ident-restr))
        base-possible-idents (set (second global-ident-restr))
        base-nonpresent-idents (set (last global-ident-restr))
        base-present-mods (set (first global-mods-restr))
        base-possible-mods (set (second global-mods-restr))
        base-nonpresent-mods (set (last global-mods-restr))]
    (loop [data data
           scheme scheme]
      (let [all-mod-restr (second (first scheme))
            all-mod-restr (second (first scheme))
            present-idents nil
            possible-idents nil
            nonpresent-idents nil
            present-mods (set/union base-present-mods (first all-mod-restr))
            possible-mods (set/union base-possible-mods (second all-mod-restr))
            nonpresent-mods (set/union base-nonpresent-mods (last all-mod-restr))]
        ()))))

(println (set/union #{:a :b} '(:b :c)))
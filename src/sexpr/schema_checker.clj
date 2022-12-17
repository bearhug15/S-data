(ns sexpr.schema-checker
  (:require [clojure.set :as set]))

(defn check-idents-restrictions
  [gir]
  (if (and (seq? gir) (= 3 (count gir)))
    (reduce
      (fn [res restrictions]
        (and
          res
          (if (seq? restrictions)
            (reduce
              (fn [res restriction]
                (and
                  res
                  (string? restriction)))
              true
              restrictions)
            false)))
      true
      gir)
    false))
(defn check-mods-restrictions
  [restrictions]
  (if (and (seq? restrictions) (= 3 (count restrictions)))
    (reduce
      (fn [res restriction]
        (and
          res
          (reduce
            (fn [res mod]
              (and
                res
                (string? mod)))
            true
            restriction)))
      true
      restrictions)
    false))
(defn check-global-mods-restrictions
  [gmr]
  (if (seq? gmr)
    (reduce
      (fn [res ident-mods]
        (if (and (seq? ident-mods) (= 4 (count ident-mods)))
          (let [ident (first ident-mods)
                mods (rest ident-mods)]
            (and
              res
              (string? ident)
              (check-mods-restrictions mods)))
          false))
      true
      gmr)
    false))

(defn check-root
  [root]
  (cond
    (and (seq? root) (= 1 (count root)))
      (and (string? (first root)) (= (first root) "ROOT"))
    (and (seq? root) (= 4 (count root)))
      (and
        (string? (first root))
        (= (first root) "ROOT")
        (check-mods-restrictions (rest root)))
    :else false))

(defn check-schema-ident
  [schema-ident]
  (let [sch-ident (first schema-ident)
        mods-restr (rest schema-ident)]
    (and
      (or
        (string? sch-ident)
        (check-idents-restrictions sch-ident))
      (if (empty? mods-restr)
        true
        (check-mods-restrictions mods-restr)))))

(defn schema-value-is-schema
  [schema-value]
  (cond
    (= 2 (count schema-value))
      true
    (or (= 1 (count schema-value)) (= 4 (count schema-value)))
      false
    :else (throw (Exception. "Bad schema value"))))

(defn check-schema
  [schema]
  (if (and (seq? schema) (= 2 (count schema)))
    (let [schema-ident (first schema)
          schema-values (second schema)]
      (and
        (check-schema-ident schema-ident)
        (reduce
          (fn [res schema-value]
            (and
              res
              (if (schema-value-is-schema schema-value)
                (check-schema schema-value)
                (check-schema-ident schema-value))))
          true
          schema-values)))
    false))


(defn check-schema-correctness
  [schema]
  (if (and (seq? schema) (= 3 (count schema)))
    (and
      (if (empty? (first schema))
        true
        (check-idents-restrictions (first schema)))
      (check-global-mods-restrictions (second schema))
      (let [root-schema (last schema)]
        (cond
          (= 1 (count root-schema))
            (check-root (first root-schema))
          (= 2 (count root-schema))
            (and
              (check-root (first root-schema))
              (check-schema (second root-schema)))
          :else false)
        ))
    false))

(defn check-by-schema
  [data schema]
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
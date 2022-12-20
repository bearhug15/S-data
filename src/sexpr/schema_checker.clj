(ns sexpr.schema-checker
  (:require [clojure.set :as set]
            [clojure.core.matrix :as matrix]
            [clojure.core.matrix.linear :as linear])
  )
;(matrix/set-current-implementation :vectorz)
(def matrix-example
  (matrix/emap-indexed
    (fn [[idx idy] val]
      (if (and (= 0 idx) (= 0 idy))
        1
        (+ idx idy)))
    (matrix/fill (matrix/new-matrix 3 3) 1)))
(println (matrix/matrix '([1 2 3] [1 2 3])))
(println matrix-example)
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

(defn get-restrictions
  [restrictions]
  ([(set (first restrictions)) (set (second restrictions)) (set (last restrictions))]))

(defn get-ident-mods-restrictions
  [restrictions]
  (reduce
    (fn [hmap restriction]
      (assoc hmap (first restriction) (get-restrictions (rest restriction))))
    (hash-map)
    restrictions))

(defn schema-ident-is-ident
  [schema-ident]
  (string? (first schema-ident)))

;;
(defn check-mods-by-restrictions
  [mods mods-restrictions]
  (let [[present-mods possible-mods nonpresent-mods] mods-restrictions
        [upm, _, res]
        (reduce
          (fn [[present-mods possible-mods res] mod]
            (let [mod-name (first mod)]

              (if (contains? present-mods mod-name)
                [(disj present-mods mod-name) (conj possible-mods mod-name) (and res true)]
                (if (empty? possible-mods)
                  (if (contains? nonpresent-mods mod-name)
                    [present-mods possible-mods false]
                    [present-mods possible-mods (and res true)])
                  (if (contains? possible-mods mod-name)
                    [present-mods possible-mods (and res true)]
                    [present-mods possible-mods false])))))
          [present-mods possible-mods true]
          mods)]
    (if res
      (empty? upm)
      false))
  )

;; returns [result, possibly updated ident-restrictions]
(defn check-ident-by-restrictions
  [ident restrictions]
  (if (seq? restrictions)
    (let [[present-ident possible-ident nonpresent-ident] restrictions]
      (if (contains? present-ident ident)
        [true [(disj present-ident ident) (conj possible-ident ident) nonpresent-ident]]
        (if (empty? possible-ident)
          (if (contains? nonpresent-ident ident)
            [false restrictions]
            [true restrictions])
          (if (contains? possible-ident ident)
            [true restrictions]
            [false restrictions]))))
    (throw (new Exception "wrong ident restriction format"))))

;; returns [result, possibly updated basic-ident-restrictions]
(defn check-element-by-restrictions
  [element base-ident-restrictions loc-ident-restrictions mods-restrictions]
  (let [ident (first element)
        mods (butlast (rest element))
        [b-pr-ident b-po-ident b-non-ident] base-ident-restrictions]
    (if (string? loc-ident-restrictions)
      (let [new-b-po-ident (if (contains? b-pr-ident ident) (conj b-po-ident ident) b-po-ident)] [(and (= ident loc-ident-restrictions) (check-mods-by-restrictions mods mods-restrictions)), [(disj b-pr-ident ident) new-b-po-ident b-non-ident]])
      (let [[loc-pr-restr loc-po-restr loc-non-restr] loc-ident-restrictions
            new-loc-pr-restr loc-pr-restr
            new-loc-po-restr (set/union (set/difference b-po-ident loc-non-restr) loc-po-restr)
            new-loc-non-restr (set/union (set/difference b-non-ident loc-pr-restr loc-po-restr) loc-non-restr)
            [ident-res, _] (check-ident-by-restrictions ident [new-loc-pr-restr new-loc-po-restr new-loc-non-restr])
            mods-res (check-mods-by-restrictions mods mods-restrictions)
            new-b-po-ident (if (contains? b-pr-ident ident) (conj b-po-ident ident) b-po-ident)]
        [(and ident-res mods-res) [(disj b-pr-ident ident) new-b-po-ident b-non-ident]]))
    ))

(defn upd-mods-restr-by-ident
  [ident mods-restr basic-ident-mods-restr]
  (if (contains? basic-ident-mods-restr ident)
    (let [[pr-restr po-restr non-restr] mods-restr
          [new-pr-restr new-po-restr new-non-restr] (get basic-ident-mods-restr ident)]
      [(set/union pr-restr new-pr-restr) (set/union po-restr new-po-restr) (set/union non-restr new-non-restr)])
    mods-restr))

(declare check-by-schema)

;; delete non object elements
(defn clear-elements
  [elements]
  (filter
    (fn [element]
      (seq? element))
    elements))

(declare check-elements-by-schema-values)
;;returns [result, possibly updated basic-ident-restrictions]
(defn check-element-by-schema-value
  [element value bir bimr]
  (if (= 2 (count value))
    (check-by-schema element value bir bimr)
    (let [schema-ident-restr (first value)
          schema-mods-restr (second value)]
      (check-element-by-restrictions
        element
        bir
        schema-ident-restr
        (upd-mods-restr-by-ident (first element) schema-mods-restr bimr)))))

(defn check-rows
  [m]
  (reduce
    (fn [res row]
      (and
        res
        (reduce
          (fn [sub-res value]
            (or sub-res value))
          false
          row)))
    true
    (matrix/rows m)))
(defn check-columns
  [m]
  (reduce
    (fn [res row]
      (and
        res
        (reduce
          (fn [sub-res value]
            (or sub-res value))
          false
          row)))
    true
    (matrix/columns m)))

(defn vswap
  [v i j]
  (-> v (assoc i (v j)) (assoc j (v i))))
(defn mat-swap
  [m indexes i j]
  [(matrix/swap-rows m i j), (vswap indexes i j)])
;;return false or [matrix, indexes]
(defn next-permutation
  [m indexes]
  (let [n (count indexes)
        j (some
            (fn [val]
              (if (or (= -1 val)
                      (< (nth indexes val)
                         (nth indexes (+ 1 val))))
                val
                false))
            (iterate dec (- n 2)))]
    (if (= -1 j)
      false
      (let [k (some
                (fn [val]
                  (if (< (nth indexes j)
                         (nth indexes val))
                    val
                    false))
                (iterate dec (- n 1)))
            [m indexes] (mat-swap m indexes j k)
            l (+ j 1)
            r (- n 1)]
        (some
          (fn [[l r m indexes]]
            (if (>= l r)
              [m indexes]
              false))
          (iterate
            (fn [[l r m indexes]]
              (let [[m indexes] (mat-swap m indexes l r)]
                [(+ l 1) (- r 1) m indexes]))
            [l r m indexes]))
        ))
    ))


(defn check-matrix-diag
  [m]
  (let [[x y] (matrix/shape m)
        columns (matrix/columns m)
        diag (map-indexed
               (fn [id val]
                 (nth val id))
               columns)]
    (reduce
      (fn [res val]
        (and res (= val 1)))
      true
      diag)))


(defn check-matrix
  [m]
  (and
    (check-rows m)
    (check-columns m)
    (let [[x y] (matrix/shape m)
          indexes (vec (take x (iterate inc 0)))
          result
          (some
            (fn [val]
              (if val
                (let [[m] indexes]
                  (if (check-matrix-diag m)
                    m
                    false))
                true))
            (iterate
              (fn [[m indexes]]
                (next-permutation m indexes))
              [m indexes]))]
      (if (boolean? result) false true)
      )))
;; returns [result, possibly updated basic-ident-restrictions]
(defn check-elements-by-schema-values
  [elements schema-values bir bimr]
  (let [cleared-elements (clear-elements elements)
        e-size (count cleared-elements)
        s-size (count schema-values)]
    (if (or (< e-size s-size) (and (empty? schema-values) (not (empty? clear-elements))))
      [false bir]
      (let [res-matrix (matrix/fill (matrix/new-matrix e-size s-size) 0)
            ures-matrix
            (matrix/emap-indexed
              (fn [[idx idy] val]
                (let [el (nth cleared-elements idx)
                      sv (nth schema-values idy)
                      [res bir] (check-element-by-schema-value el sv bir bimr)]
                  (if res 1 0)
                  ))
              res-matrix)]
        [(check-matrix ures-matrix), bir])))
  )

(defn check-by-schema
  [data schema base-ident-restrictions base-ident-mods-restrictions]
  (if (= (empty? data) (empty? schema))
    (let [schema-ident (first schema)
          schema-values (second schema)
          schema-ident-restr (first schema-ident)
          schema-mods-restr (rest schema-ident)
          [res upd-bir] (check-element-by-restrictions
                          data
                          base-ident-restrictions
                          schema-ident-restr
                          (upd-mods-restr-by-ident (first data) schema-mods-restr base-ident-mods-restrictions))]
      (if res
        (check-elements-by-schema-values (last data) schema-values upd-bir base-ident-mods-restrictions)
        [false base-ident-restrictions]))
    [false base-ident-restrictions]))

(defn check-by-full-schema
  [data schema]
  (let [idents-restrictions (get-restrictions (first schema))
        base-ident-mods-restr (get-ident-mods-restrictions (second schema))
        root-schema (last schema)
        root-restr (first root-schema)
        root-res (check-element-by-restrictions
                   data
                   idents-restrictions
                   "ROOT"
                   (rest root-restr))
        elements (clear-elements (last data))]
    (if root-res
      (let [schemas (first (rest root-schema))]
        (if (nil? schemas)
          true
          (check-elements-by-schema-values
            elements
            schemas
            idents-restrictions
            base-ident-mods-restr)))
      (false))
    ))

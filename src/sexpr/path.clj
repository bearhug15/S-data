(ns sexpr.path)

(defn get-modifiers
  [path-part modifiers-list]
  (let [modifiers (rest path-part)]
    (reduce
      (fn [coll modifier]
        (concat
          coll
          (let [res (some
                      (fn [mod-rule]
                        (if ((first mod-rule) (first modifier))
                          (partial
                            (second mod-rule)
                            (if (= 1 (count modifier))
                              nil
                              (second modifier)))
                          false))
                      modifiers-list)]
            (if (nil? res) (list) (list res)))))
      (list)
      modifiers)))

(def pre-modifiers
  (list
    [(fn [mod-name] (= mod-name "modifier1"))
     (fn [mod-value is-single element current-result] current-result)]
    [(fn [mod-name] (= mod-name "modifier2"))
     (fn [mod-value is-single element current-result] current-result)]))
(def post-modifiers
  (list
    [(fn [mod-name] (= mod-name "№"))
     (fn [mod-value is-single element current-result]
       (if is-single
         current-result
         (if (or (not (number? mod-value)) (< mod-value 0) (>= mod-value (count current-result)))
           (list)
           (list (nth current-result mod-value)))))]
    [(fn [mod-name] (= mod-name "modifier4"))
     (fn [mod-value is-single element current-result] current-result)]))

(def global-pre-modifiers
  (list
    [(fn [mod-name] (= mod-name "global-modifier1"))
     (fn [mod-value is-single element elements] element)]

    ))

(def global-post-modifiers
  (list
    [(fn [mod-name] (= mod-name "clear-tails"))
     (fn [mod-value is-single element elements]
       (if is-single
         (concat (butlast elements) '(()))
         elements))]
    [(fn [mod-name] (= mod-name "get-tails"))
     (fn [mod-value is-single element elements]
       (if is-single
         elements
         (reduce
           (fn [coll element]
             (concat coll (last element)))
           (list)
           elements)))]
    ))

(defn check-mod-conditions
  [element mod-conditions]
  (if (empty? mod-conditions)
    true
    (let [modifiers (butlast (rest element))]
      (if (empty? modifiers)
        false
        (reduce
          (fn [result mod-condition]
            (and
              result
              (reduce
                (fn [sub-result modifier]
                  (or
                    sub-result
                    (let [modifier-ident (first modifier)
                          mod-condition-ident (first mod-condition)]
                      (if (and (= (type modifier-ident) (type mod-condition-ident)) (= modifier-ident mod-condition-ident))
                        (let [modifier-size (count modifier)
                              mod-condition-size (count mod-condition)]
                          (cond
                            (and (= 1 mod-condition-size))
                            (= modifier-ident mod-condition-ident)
                            (and (= 2 mod-condition-size) (= 2 modifier-size))
                            (= (second modifier) (second mod-condition))
                            (and (= 3 mod-condition-size) (= 2 modifier-size))
                            (if (number? (second modifier))
                              (and
                                (>= (second modifier) (first (rest mod-condition)))
                                (<= (second modifier) (second (rest mod-condition))))
                              false)
                            :else false))
                        false))))
                false
                modifiers)))
          true
          mod-conditions)))))
(defn check-ident-conditions
  [element ident-conditions]
  (if (empty? ident-conditions)
    true
    (reduce
      (fn [result condition]
        (or
          result
          (= (first element) condition)))
      false
      ident-conditions)))

(defn check-search-condition
  [element search-condition]
  (if (check-ident-conditions element (first search-condition))
    (if (empty? (rest search-condition))
      true
      (check-mod-conditions element (rest search-condition)))
    false))

(defn check-joined-condition
  [element joined-conditions]
  (loop [joined-conditions joined-conditions]
    (if (empty? joined-conditions)
      false
      (if (check-search-condition element (first joined-conditions))
        true
        (recur (rest joined-conditions))))))

(defn objects-to-sub-objects
  [objects]
  (mapcat
    (fn [elem]
      (let [elem-coll (last elem)]
        (filter (fn [val] (seq? val)) elem-coll)))
    objects))

;get list of elements which matches condition
(defn get-elems-by-condition
  [element condition]
  (let [[start-depth end-depth] (first condition)
        start-value (if (empty? start-depth)
                      0
                      (if (< (first start-depth) 0)
                        0
                        (first start-depth)))
        end-value (if (empty? end-depth) Integer/MAX_VALUE (first end-depth))]
    (if (and (< end-value start-value) (>= end-value 0))
      (throw (AssertionError. "Wrong start end values combination."))
      (loop [depth -1
             elements-to-check (list element)
             result-elements (list)]
        (if (or (empty? elements-to-check) (and (> depth end-value) (> end-value -1)))
          result-elements
          (if (< depth start-value)
            (recur
              (+ depth 1)
              (objects-to-sub-objects elements-to-check)
              result-elements)
            (let [[good bad] ((juxt filter remove)
                              (fn [val] (check-joined-condition
                                          val
                                          (rest condition)))
                              elements-to-check)]
              (recur
                (+ depth 1)
                (objects-to-sub-objects bad)
                (concat result-elements good))))))))
  )

(defn get-elems-by-ident
  [element ident]
  (mapcat
    (fn [value]
      (if (seq? value)
        (if (= (first value) ident)
          (list value)
          (list))
        (list)))
    (last element)))

(defn apply-modifiers
  [element elements modifiers-list]
  (reduce
    (fn [coll modifier]
      (let [updated-elems (map
                            (fn [elem]
                              (modifier true element elem))
                            coll)]
        (modifier false element updated-elems)))
    elements
    modifiers-list))

(defn go-by-path
  ([data path global-pre-mods global-post-mods]
   (let [pre-modified (if (empty? global-pre-mods) data (apply-modifiers data (list data) global-pre-mods))
         result
         (loop [sub-result (list pre-modified)
                path path]
           (if (empty? path)
             sub-result
             (let [path-part (first path)
                   search-condition (first path-part)
                   pre-mods (get-modifiers path-part pre-modifiers)
                   post-mods (get-modifiers path-part post-modifiers)
                   modified-elems (apply-modifiers data sub-result pre-mods)
                   get-elems-func (if (seq? search-condition) get-elems-by-condition get-elems-by-ident)]
               (recur
                 (apply-modifiers
                   data
                   (mapcat
                     (fn [element]
                       (get-elems-func element search-condition))
                     modified-elems)
                   post-mods)
                 (rest path)))))
         post-modified (apply-modifiers (list) result global-post-mods)]
     post-modified)))

(defn path
  [data path]
  (let [first-path-part (first path)
        part-name (first first-path-part)]
    (if (= part-name "ROOT")
      (let [global-pre-mods (get-modifiers first-path-part global-pre-modifiers)
            global-post-mods (get-modifiers first-path-part global-post-modifiers)]
        (go-by-path data (rest path) global-pre-mods global-post-mods))
      (go-by-path data path (list) (list)))))


(def insert-pre-modifiers
  (list
    [(fn [mod-name] (= mod-name "modifier1"))
     (fn [mod-value element] element)]
    [(fn [mod-name] (= mod-name "modifier2"))
     (fn [mod-value element] element)]))
(def insert-post-modifiers
  (list
    [(fn [mod-name] (= mod-name "modifier4"))
     (fn [mod-value element] element)]))
(def global-insert-pre-modifiers
  (list
    [(fn [mod-name] (= mod-name "global-modifier1"))
     (fn [mod-value element] element)]
    ))
(def global-insert-post-modifiers
  (list
    [(fn [mod-name] (= mod-name "global-modifier1"))
     (fn [mod-value element] element)]
    ))

(defn apply-insert-modifiers
  [element modifiers-list]
  (reduce
    (fn [modifier]
      (modifier element))
    element
    modifiers-list))


(declare insert-elements-go-by-path)

(defn insert-elems-by-ident
  [data ident path elements]
  (if (seq? data)
    (let [data-ident (first data)
          data-coll (last data)]
      (if (= data-ident ident)
        (if (empty? path)
          (concat (butlast data) (list (concat data-coll elements)))
          (concat
            (butlast data)
            (list
              (map
                (fn [element]
                  (insert-elements-go-by-path element path elements))
                data-coll))))
        data))
    data))

(defn insert-elems-by-condition
  ([data search-condition path elements]
   (if (seq? data)
     (let [[start-depth end-depth] (first search-condition)
           start-value (if (empty? start-depth)
                         0
                         (if (< (first start-depth) 0)
                           0
                           (first start-depth)))
           end-value (if (empty? end-depth) Integer/MAX_VALUE (max 0 (first end-depth)))
           res (insert-elems-by-condition data 0 start-value end-value search-condition path elements)]
       ;(println res)
       res
       )
     data)
   )
  ([data depth start end search-condition path elements]
   ;(println data)
   ;(println depth start end)
   ;(println path)
   ;(println elements)
   (if (seq? data)
     (let [condition (rest search-condition)]
       (cond
         (< depth start)
         (let [data-coll (last data)]
           (concat
             (butlast data)
             (list
               (map
                 (fn [element]
                   (insert-elems-by-condition element (+ depth 1) start end search-condition path elements))
                 data-coll))))
         (and (>= depth start) (<= depth end))
         (let [data-coll (last data)]
           ;(println (check-joined-condition data condition))
           (if (check-joined-condition data condition)
             (concat
               (butlast data)
               (list
                 (if (empty? path)
                   (concat data-coll elements)
                   (map
                     (fn [element]
                       (insert-elements-go-by-path element path elements))
                     data-coll))))
             (concat
               (butlast data)
               (list
                 (map
                   (fn [element]
                     (insert-elems-by-condition element (+ depth 1) start end search-condition path elements))
                   data-coll)))))
         (> depth end)
         data))
     data)))

(defn insert-elements-go-by-path
  [data path elements]

  (if (seq? data)
    (let [path-part (first path)
          search-condition (first path-part)
          pre-mods (get-modifiers path-part insert-pre-modifiers)
          post-mods (get-modifiers path-part insert-post-modifiers)
          pre-data (apply-insert-modifiers data pre-mods)
          insert-elems-func (if (seq? search-condition) insert-elems-by-condition insert-elems-by-ident)
          res (apply-insert-modifiers (insert-elems-func pre-data search-condition (rest path) elements) post-mods)]
      res)
    data))

(defn insert-elements-by-path
  [data path elements]
  (if (empty? path)
    (concat (butlast data) (list (concat (last data) elements)))
    (let [first-path-part (first path)
          part-name (first first-path-part)]
      (if (= part-name "ROOT")
        (let [global-pre-mods (get-modifiers first-path-part global-insert-pre-modifiers)
              global-post-mods (get-modifiers first-path-part global-insert-post-modifiers)
              premodified-data (apply-insert-modifiers data global-pre-mods)
              in-data (concat
                        (butlast premodified-data)
                        (list (map
                                (fn [element]
                                  (insert-elements-go-by-path element (rest path) elements))
                                (last premodified-data))))]
          (apply-insert-modifiers in-data global-post-mods))
        (concat
          (butlast data)
          (list (map
                  (fn [element]
                    (insert-elements-go-by-path element path elements))
                  (last data)))))))
  )
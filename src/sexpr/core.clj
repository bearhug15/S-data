(ns sexpr.core
  (:import [java.io PushbackReader])
  (:require [clojure.java.io :as io]
             [clojure.edn :as edn]) )

(defn check-mods-correctness
  [modificators]
  (reduce
    (fn [res mod]
      (and
        res
        (if (and (seq? mod) (string? (first mod)))
          (case (count mod)
            1 true
            2 (or (string? (second mod)) (number? (second mod)))
            false)
          false)))
    true
    modificators))

(declare is-value)

(defn check-correctness
  [data]
  (if (seq? data)
    (if (string? (first data))
      (if (check-mods-correctness (butlast (rest data)))
        (if (seq? (last data))
          (reduce
            (fn [res value]
              (and
                res
                (is-value value)))
            true
            (last data))
          false)
        false)
      false)
    false))

(defn is-value
  [data]
  (if (or (string? data) (number? data))
    true
    (check-correctness data)))

(defn read-forms [file]
  (let [rdr (-> file io/file io/reader PushbackReader.)
        sentinel (Object.)]
    (loop [forms []]
      (let [form (edn/read {:eof sentinel} rdr)]
        (if (= sentinel form)
          forms
          (recur (conj forms form)))))))

(defn read-sdata
  [path]
  (let [data-seq (try
                   (read-forms path)
                   (catch Exception e (list))
                   (finally))]
    (filter
      (fn [data]
        (check-correctness data))
      data-seq)))

(defn -main []
  (let [path-to-file1 "test/test-data/data1.txt"
        path-to-file2 "test/test-data/data2.txt"
        path-to-file3 "test/test-data/data3.txt"
        path-to-file4 "test/test-data/data4.txt"
        path-to-file5 "test/test-data/data5.txt"
        path-to-file6 "test/test-data/data6.txt"
        path-to-file7 "test/test-data/data7.txt"
        path-to-file8 "test/test-data/data8.txt"
        path-to-file9 "test/test-data/data9.txt"]
    (println (read-sdata path-to-file1))
    (println (read-sdata path-to-file2))
    (println (read-sdata path-to-file3))
    (println (read-sdata path-to-file4))
    (println (read-sdata path-to-file5))
    (println (read-sdata path-to-file6))
    (println (read-sdata path-to-file7))
    (println (read-sdata path-to-file8))
    (println (read-sdata path-to-file9)))
  )


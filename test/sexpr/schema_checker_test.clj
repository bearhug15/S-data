(ns sexpr.schema-checker-test
  (:require [clojure.test :refer :all]
            [sexpr.schema-checker :refer :all])
  (:use [sexpr.core :only [read-sdata]]))

(def global-ident-restrictions1 '())
(def global-ident-restrictions2 '(() () ()))
(def global-ident-restrictions3 '(("ident1") ("ident2") ("ident3")))
(def global-ident-restrictions4 '(("ident1" "ident2") () ()))
(def global-ident-restrictions5 '(("ident1" 2) () ()))

(deftest check-global-ident-restrictions-test
  (testing "global-ident-restrictions1"
    (is (= (check-idents-restrictions global-ident-restrictions1)
           false)))
  (testing "global-ident-restrictions2"
    (is (= (check-idents-restrictions global-ident-restrictions2)
           true)))
  (testing "global-ident-restrictions3"
    (is (= (check-idents-restrictions global-ident-restrictions3)
           true)))
  (testing "global-ident-restrictions4"
    (is (= (check-idents-restrictions global-ident-restrictions4)
           true)))
  (testing "global-ident-restrictions5"
    (is (= (check-idents-restrictions global-ident-restrictions5)
           false)))
  )

(def global-mods-restrictions1  '())
(def global-mods-restrictions2  '(("ident1")))
(def global-mods-restrictions3  '(("ident1" () () ())))
(def global-mods-restrictions4  '((2 () () ())))
(def global-mods-restrictions5  '(("ident1" ("mod1" "mod2") ("mod2") ("mod3"))))
(def global-mods-restrictions6  '(("ident1" ("mod1" 2) ("mod2") ("mod3"))))
(def global-mods-restrictions7 '(("ident1" ("mod1") ("mod2") ("mod3")) ("ident1" ("mod1" 2) ("mod2") ("mod3"))))
(def global-mods-restrictions8  '(("ident1" ("mod1") ("mod2") ("mod3")) ("ident1" ("mod1" "mod2") ("mod2") ("mod3"))))

(deftest check-global-mods-restrictions-test
  (testing "global-mods-restrictions1"
    (is (= (check-global-mods-restrictions global-mods-restrictions1)
           true)))
  (testing "global-mods-restrictions2"
    (is (= (check-global-mods-restrictions global-mods-restrictions2)
           false)))
  (testing "global-mods-restrictions3"
    (is (= (check-global-mods-restrictions global-mods-restrictions3)
           true)))
  (testing "global-mods-restrictions4"
    (is (= (check-global-mods-restrictions global-mods-restrictions4)
           false)))
  (testing "global-mods-restrictions5"
    (is (= (check-global-mods-restrictions global-mods-restrictions5)
           true)))
  (testing "global-mods-restrictions6"
    (is (= (check-global-mods-restrictions global-mods-restrictions6)
           false)))
  (testing "global-mods-restrictions7"
    (is (= (check-global-mods-restrictions global-mods-restrictions7)
           false)))
  (testing "global-mods-restrictions8"
    (is (= (check-global-mods-restrictions global-mods-restrictions8)
           true)))
  )

(def schema1 '(() () (("ROOT") )))
(def schema2 '((() () ()) () (("ROOT") )))
(def schema3 '())
(def schema4 '((("ident1") ("ident2") ("ident3")) () (("ROOT") )))
(def schema5 '((("ident1") ("ident2") ("ident3")) (("ident2" ("mod1") ("mod2") ("mod3"))) (("ROOT") )))
(def schema6 '(() () (("ROOT") (("ident1") ()))))
(def schema7 '(() () (("ROOT") (("ident1" ("mod1") ("mod2") ()) ()))))
(def schema8 '(() () (("ROOT") (((("ident1") ("ident2") ()) ("mod1") ("mod2") ()) ()))))

(deftest check-schema-correctness-test
  (testing "schema1"
    (is (= (check-schema-correctness schema1)
           true)))
  (testing "schema2"
    (is (= (check-schema-correctness schema2)
           true)))
  (testing "schema3"
    (is (= (check-schema-correctness schema3)
           false)))
  (testing "schema4"
    (is (= (check-schema-correctness schema4)
           true)))
  (testing "schema5"
    (is (= (check-schema-correctness schema5)
           true)))
  (testing "schema6"
    (is (= (check-schema-correctness schema6)
           true)))
  (testing "schema7"
    (is (= (check-schema-correctness schema7)
           true)))
  (testing "schema8"
    (is (= (check-schema-correctness schema8)
           true)))
  )

(def mods1 '())
(def mods2 '(("mod1")))
(def mods3 '(("mod2" "mod1") ("mod3" 1)))

(def mods-restr1 [(set '()) (set '()) (set '())])
(def mods-restr2 [(set '("mod1")) (set '()) (set '())])
(def mods-restr3 [(set '()) (set '("mod1")) (set '())])
(def mods-restr4 [(set '()) (set '()) (set '("mod1"))])
(def mods-restr5 [(set '("mod2")) (set '()) (set '("mod3"))])

(deftest check-mods-by-restrictions-test
  (testing "mods1 mods-restr1"
    (is (= (check-mods-by-restrictions mods1 mods-restr1)
           true)))
  (testing "mods2 mods-restr1"
    (is (= (check-mods-by-restrictions mods2 mods-restr1)
           true)))
  (testing "mods1 mods-restr2"
    (is (= (check-mods-by-restrictions mods1 mods-restr2)
           false)))
  (testing "mods2 mods-restr2"
    (is (= (check-mods-by-restrictions mods2 mods-restr2)
           true)))
  (testing "mods3 mods-restr2"
    (is (= (check-mods-by-restrictions mods3 mods-restr2)
           false)))
  (testing "mods1 mods-restr3"
    (is (= (check-mods-by-restrictions mods1 mods-restr3)
           true)))
  (testing "mods2 mods-restr3"
    (is (= (check-mods-by-restrictions mods2 mods-restr3)
           true)))
  (testing "mods3 mods-restr3"
    (is (= (check-mods-by-restrictions mods3 mods-restr3)
           false)))
  (testing "mods1 mods-restr4"
    (is (= (check-mods-by-restrictions mods1 mods-restr4)
           true)))
  (testing "mods2 mods-restr4"
    (is (= (check-mods-by-restrictions mods2 mods-restr4)
           false)))
  (testing "mods3 mods-restr4"
    (is (= (check-mods-by-restrictions mods3 mods-restr4)
           true)))
  (testing "mods3 mods-restr5"
    (is (= (check-mods-by-restrictions mods3 mods-restr5)
           false)))
  )

(def test-data1 (first (read-sdata "test/test-data/data1.txt")))
(def test-data2 (first (read-sdata "test/test-data/data2.txt")))
(def test-data3 (first (read-sdata "test/test-data/data3.txt")))
(def test-data4 (first (read-sdata "test/test-data/data4.txt")))
(def test-data5 (first (read-sdata "test/test-data/data5.txt")))
(def test-data6 (first (read-sdata "test/test-data/data6.txt")))
(def test-data7 (first (read-sdata "test/test-data/data7.txt")))
(def test-data8 (first (read-sdata "test/test-data/data8.txt")))
(def test-data9 (first (read-sdata "test/test-data/data9.txt")))
(def test-data10 (first (read-sdata "test/test-data/data10.txt")))
(def test-data11 (first (read-sdata "test/test-data/data11.txt")))

(def test-schemas1 (read-schemas "test/test-schema/schema1.txt"))
(def test-schema1_1 (first test-schemas1))
(def test-schema2 (first (read-schemas "test/test-schema/schema2.txt")))

(def test-schemas3 (read-schemas "test/test-schema/schema3.txt"))
(def test-schema3_0 (nth test-schemas3 0))
(def test-schema3_1 (nth test-schemas3 1))
(def test-schema3_2 (nth test-schemas3 2))
(def test-schema3_3 (nth test-schemas3 3))
(def test-schema3_4 (nth test-schemas3 4))

(def test-schemas4 (read-schemas "test/test-schema/schema4.txt"))
;(println test-schemas4)
(def test-schema4_0 (nth test-schemas4 0))
(def test-schema4_1 (nth test-schemas4 1))

(def test-schemas5 (read-schemas "test/test-schema/schema5.txt"))
(def test-schema5_0 (nth test-schemas5 0))

(def test-schemas6 (read-schemas "test/test-schema/schema6.txt"))
(def test-schema6_0 (nth test-schemas6 0))
(def test-schema6_1 (nth test-schemas6 1))

(def test-schemas7 (read-schemas "test/test-schema/schema7.txt"))
(def test-schema7_0 (nth test-schemas7 0))
(def test-schema7_1 (nth test-schemas7 1))
(def test-schema7_2 (nth test-schemas7 2))

(deftest check-by-full-schema-testing
  (testing "1"
    (is (= (check-by-full-schema test-data1 test-schema1_1)
           true)))
  (testing "2"
    (is (= (check-by-full-schema test-data2 test-schema3_0)
           true)))
  (testing "3"
    (is (= (check-by-full-schema test-data2 test-schema3_1)
           false)))
  (testing "4"
    (is (= (check-by-full-schema test-data2 test-schema3_2)
           false)))
  (testing "5"
    (is (= (check-by-full-schema test-data2 test-schema3_3)
           true)))
  (testing "6"
    (is (= (check-by-full-schema test-data2 test-schema3_4)
           true)))
  (testing "7"
    (is (= (check-by-full-schema test-data2 test-schema4_0)
           false)))
  (testing "8"
    (is (= (check-by-full-schema test-data4 test-schema4_0)
           true)))
  (testing "9"
    (is (= (check-by-full-schema test-data4 test-schema4_1)
           false)))
  (testing "10"
    (is (= (check-by-full-schema test-data5 test-schema4_0)
           true)))
  (testing "11"
    (is (= (check-by-full-schema test-data5 test-schema5_0)
           true)))
  (testing "12"
    (is (= (check-by-full-schema test-data4 test-schema5_0)
           false)))
  (testing "13"
    (is (= (check-by-full-schema test-data6 test-schema6_0)
           true)))
  (testing "14"
    (is (= (check-by-full-schema test-data6 test-schema6_1)
           false)))
  (testing "15"
    (is (= (check-by-full-schema test-data9 test-schema6_1)
           false)))
  (testing "16"
    (is (= (check-by-full-schema test-data10 test-schema7_0)
           true)))
  (testing "17"
    (is (= (check-by-full-schema test-data10 test-schema7_1)
           false)))
  (testing "18"
    (is (= (check-by-full-schema test-data11 test-schema7_2)
           false)))
  )
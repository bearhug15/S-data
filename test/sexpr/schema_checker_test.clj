(ns sexpr.schema-checker-test
  (:require [clojure.test :refer :all]
            [sexpr.schema-checker :refer :all]))

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
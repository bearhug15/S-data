(ns sexpr.path-test
  (:require [clojure.test :refer :all]
            [sexpr.path :refer :all]
            [sexpr.core :refer :all]))

(def empty-element '("empty-element" ()))
(def empty-element-simple-mod '("empty-element" ("mod1") ()))
(def empty-element-ident-mod '("empty-element" ("mod1" "ident1") ()))
(def empty-element-number-mod '("empty-element" ("mod1" 1) ()))
(def empty-element-two-simple-mod '("empty-element" ("mod1") ("mod2") ()))

(def ident-condition1 '())
(def ident-condition2 '("empty-element"))
(def ident-condition3 '("ident"))
(def ident-condition4 '("ident" "empty-element"))
(def ident-condition5 '("ident" "nident"))

(deftest iden-conditions-check-test
  (testing "empty ident conditions testing"
    (is (= true (check-ident-conditions empty-element ident-condition1))))
  (testing "right ident conditions testing"
    (is (= true (check-ident-conditions empty-element ident-condition2))))
  (testing "wrong ident conditions testing"
    (is (= false (check-ident-conditions empty-element ident-condition3))))
  (testing "several with right ident conditions testing"
    (is (= true (check-ident-conditions empty-element ident-condition4))))
  (testing "several without right ident conditions testing"
    (is (= false (check-ident-conditions empty-element ident-condition5))))
  )

(def mod-condition1 '())
(def mod-condition2 '(("mod1")))
(def mod-condition3 '(("mod2")))
(def mod-condition4 '(("mod1" "ident1")))
(def mod-condition5 '(("mod1" "ident2")))
(def mod-condition6 '(("mod1" 1)))
(def mod-condition7 '(("mod1" 2)))
(def mod-condition8 '(("mod1" 1 2)))
(def mod-condition9 '(("mod1" 0 0)))
(def mod-condition10 '(("mod1") ("mod2")))

(deftest mod-condition-check-test
  (testing "empty mod empty element conditions testing"
    (is (= true (check-mod-conditions empty-element mod-condition1))))
  (testing "empty mod not empty element conditions testing"
    (is (= true (check-mod-conditions empty-element-simple-mod mod-condition1))))
  (testing "mod not empty element conditions testing"
    (is (= true (check-mod-conditions empty-element-simple-mod mod-condition2))))
  (testing "wrong mod not empty element conditions testing"
    (is (= false (check-mod-conditions empty-element-simple-mod mod-condition3))))
  (testing "wrong ident mod not empty element conditions testing"
    (is (= false (check-mod-conditions empty-element-simple-mod mod-condition4))))
  (testing "ident mod not empty element conditions testing"
    (is (= true (check-mod-conditions empty-element-ident-mod mod-condition4))))
  (testing "wrong ident mod not empty element conditions testing"
    (is (= false (check-mod-conditions empty-element-ident-mod mod-condition5))))
  (testing "number mod not empty element conditions testing"
    (is (= true (check-mod-conditions empty-element-number-mod mod-condition6))))
  (testing "wrong number mod not empty element conditions testing"
    (is (= false (check-mod-conditions empty-element-number-mod mod-condition7))))
  (testing "interval mod not empty element conditions testing"
    (is (= true (check-mod-conditions empty-element-number-mod mod-condition8))))
  (testing "wrong interval mod not empty element conditions testing"
    (is (= false (check-mod-conditions empty-element-number-mod mod-condition9))))
  (testing "two mod not empty element conditions testing"
    (is (= true (check-mod-conditions empty-element-two-simple-mod mod-condition10))))
  )

(def no-mod-empty-ident-condition (list ident-condition1))
(def empty-mod-empty-ident-condition (list ident-condition1 mod-condition1))
(def mod-empty-ident-condition (list ident-condition1 mod-condition2) )
(def empty-mod-ident-condition  (list ident-condition2 mod-condition1))
(def empty-mod-wrong-ident-condition  (list ident-condition3 mod-condition1))

(deftest search-conditions-check
  (testing "no mod empty ident empty-element-simple-mod testing"
    (is (= (check-search-condition empty-element-simple-mod no-mod-empty-ident-condition)
           true)))
  (testing "empty mod empty ident empty-element-simple-mod testing"
    (is (= (check-search-condition empty-element-simple-mod empty-mod-empty-ident-condition)
           true)))
  (testing "not empty mod empty ident empty-element-simple-mod testing"
    (is (= (check-search-condition empty-element-simple-mod mod-empty-ident-condition)
           true)))
  (testing "not empty mod empty ident empty-element testing"
    (is (= (check-search-condition empty-element mod-empty-ident-condition)
           false)))
  (testing "not empty mod ident empty-element testing"
    (is (= (check-search-condition empty-element empty-mod-ident-condition)
           true)))
  )

(def joined-condition1 (list no-mod-empty-ident-condition))
(def joined-condition2 (list no-mod-empty-ident-condition empty-mod-empty-ident-condition))
(def joined-condition3 (list mod-empty-ident-condition empty-mod-ident-condition))
(def joined-condition4 (list mod-empty-ident-condition empty-mod-wrong-ident-condition))

(deftest joined-conditions-test
  (testing "joined-condition1 empty-element-simple-mod testing"
    (is (= (check-joined-condition empty-element-simple-mod joined-condition1)
           true)))
  (testing "joined-condition2 empty-element-simple-mod testing"
    (is (= (check-joined-condition empty-element-simple-mod joined-condition2)
           true)))
  (testing "joined-condition3 empty-element testing"
    (is (= (check-joined-condition empty-element joined-condition3)
           true)))
  (testing "joined-condition4 empty-element testing"
    (is (= (check-joined-condition empty-element joined-condition4)
           false)))
  (testing "joined-condition4 empty-element-simple-mod testing"
    (is (= (check-joined-condition empty-element-simple-mod joined-condition4)
           true)))
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

(deftest path-testing
  (testing "1"
    (is (= (path test-data1 '())
           (list test-data1))))
  (testing "2"
    (is (= (path test-data4 '(("ident1") ))
           (list '("ident1" ("mod2" 3) ())))))
  (testing "3"
    (is (= (path test-data5 '(("ident1")))
           (list '("ident1" ("mod2" 3) ())
                 '("ident1" ("mod3" 4) ())))))
  (testing "4"
    (is (= (path test-data5 '(
                              (((() ()) (("ident1") )))
                              ))
           (list '("ident1" ("mod2" 3) ())
                 '("ident1" ("mod3" 4) ())))))
  (testing "5"
    (is (= (path test-data5 '(
                              (((() ()) (("ident1") ("mod2"))))
                              ))
           (list '("ident1" ("mod2" 3) ())))))
  (testing "6"
    (is (= (path test-data5 '(
                              (((() ()) (("ident1") ("mod2" 1 3))))
                              ))
           (list '("ident1" ("mod2" 3) ())))))
  (testing "7"
    (is (= (path test-data5 '(
                              (((() ()) (("ident1") ("mod2" 4 5))))
                              ))
           (list))))
  (testing "8"
    (is (= (path test-data5 '(
                              ((((1) ()) (("ident1"))))
                              ))
           (list))))
  (testing "9"
    (is (= (path test-data5 '(
                              ((((0) (1)) (("ident1"))))
                              ))
           (list '("ident1" ("mod2" 3) ())
                 '("ident1" ("mod3" 4) ())))))
  (testing "10"
    (is (= (path test-data9 '(
                              (((() ()) (("ident1"))))
                              ))
           (list '("ident1" (
                            ("ident2" ("mod1") ())
                            ("ident3" ("mod2" 3) ())))
                 '("ident1" (
                            ("ident4" ())))
                 '("ident1" ("mod3") ())))))
  (testing "11"
    (is (= (path test-data9 '(
                              (((() (1)) (("ident1"))))
                              ))
           (list '("ident1" (
                             ("ident2" ("mod1") ())
                             ("ident3" ("mod2" 3) ())))
                 '("ident1" (
                             ("ident4" ())))))))
  (testing "12"
    (is (= (path test-data9 '(
                              (((() ()) (("ident1"))) ("№" 1))
                              ))
           (list '("ident1" (("ident4" ())))))))
  (testing "13"
    (is (= (path test-data9 '( ("ROOT" ("clear-tails"))
                              (((() ()) (("ident1"))))
                              ))
           (list '("ident1" ())
                 '("ident1" ())
                 '("ident1" ("mod3") ())))))
  (testing "13"
    (is (= (path test-data9 '(
                              (((() ()) (("ident1" "ident2"))))
                              ))
           (list '("ident1" (
                            ("ident2" ("mod1") ())
                            ("ident3" ("mod2" 3) ())))
                 '("ident1" (
                            ("ident4" ())))
                 '("ident2" (
                            ("ident5" (
                                       ("ident1" ("mod3") ())))))))))
  (testing "14"
    (is (= (path test-data9 '(
                              ("ident1")
                              ("ident3")
                               ))
           (list '("ident3" ("mod2" 3) ())))))
  (testing "15"
    (is (= (path test-data9 '(
                              ((((1) (1)) (())))
                              ))
           (list '("ident2" ("mod1") ())
                 '("ident3" ("mod2" 3) ())
                 '("ident4" ())
                 '("ident5" (("ident1" ("mod3") ())))))))
  (testing "16"
    (is (= (path test-data9 '(("ROOT" ("get-tails"))
                              ((((1) (1)) (())))
                              ))
           (list '("ident1" ("mod3") ())))))
  (testing "17"
    (is (= (path test-data9 '(
                              (((() ()) (("ident1"))))
                              ("ident3")
                               ))
           (list '("ident3" ("mod2" 3) ())))))
  )

(deftest insert-testing
  (testing "1"
    (is (= (insert-elements-by-path test-data4 '(("ident1")) '(("ident2" ())))
           '("ROOT" ("mod1" "val") (
                                    ("ident1" ("mod2" 3) (("ident2" ())))
                                    "val1")))))
  (testing "2"
    (is (= (insert-elements-by-path test-data4 '() '(("ident2" ())))
           '("ROOT" ("mod1" "val") (
                                    ("ident1" ("mod2" 3) ())
                                    "val1"
                                    ("ident2" ()))))))
  (testing "3"
    (is (= (insert-elements-by-path test-data5 '(
                                                 ((((0) (0)) (())))
                                                 ) '(("ident2" ())))
           '("ROOT" ("mod2" "val") (
                                    ("ident1" ("mod2" 3) (("ident2" ())))
                                    ("ident1" ("mod3" 4) (("ident2" ()))))))))
  (testing "4"
    (is (= (insert-elements-by-path test-data8 '(
                                                 ((((0) ()) (("ident2"))))
                                                 ) '(("ident6" ())))
           '("ROOT" (
                     ("ident1" (
                                ("ident2" (("ident6" ())))
                                ("ident3" ())))
                     ("ident1" (
                                ("ident4" ())))
                     ("ident2" (
                                ("ident5" ())
                                ("ident6" ()))))))))
  )

(comment
  )
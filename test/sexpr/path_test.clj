(ns sexpr.path-test
  (:require [clojure.test :refer :all]
            [sexpr.path :refer :all]))

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
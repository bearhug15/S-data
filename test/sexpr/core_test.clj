(ns sexpr.core-test
  (:require [clojure.test :refer :all]
            [sexpr.core :refer :all]))

(def mods1 '())
(def mods2 '("a"))
(def mods3 '(2 "b"))
(def mods4 '(()))
(def mods5 '(("a")))
(def mods6 '(("a") "b"))
(def mods7 '(("a") ("b")))
(def mods8 '(("a" "a")))
(def mods9 '(("a" 1) ))
(def mods10 '((1 "a")))

(deftest mods-check-test
  (testing "1"
    (is (= (check-mods-correctness mods1)
           true) ))
  (testing "2"
    (is (= (check-mods-correctness mods2)
           false) ))
  (testing "3"
    (is (= (check-mods-correctness mods3)
           false) ))
  (testing "4"
    (is (= (check-mods-correctness mods4)
           false) ))
  (testing "5"
    (is (= (check-mods-correctness mods5)
           true) ))
  (testing "6"
    (is (= (check-mods-correctness mods6)
           false) ))
  (testing "7"
    (is (= (check-mods-correctness mods7)
           true) ))
  (testing "8"
    (is (= (check-mods-correctness mods8)
           true) ))
  (testing "9"
    (is (= (check-mods-correctness mods9)
           true) ))
  (testing "10"
    (is (= (check-mods-correctness mods10)
           false) ))
  )

(deftest check-correctness-test
  (testing "1"
    (is (= (check-correctness '("ROOT" ())) true))))
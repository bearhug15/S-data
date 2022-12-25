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

(def test-data1 (read-sdata "test/test-data/data1.txt"))
(def test-data2 (read-sdata "test/test-data/data2.txt"))
(def test-data3 (read-sdata "test/test-data/data3.txt"))
(def test-data4 (read-sdata "test/test-data/data4.txt"))
(def test-data5 (read-sdata "test/test-data/data5.txt"))
(def test-data6 (read-sdata "test/test-data/data6.txt"))
(def test-data7 (read-sdata "test/test-data/data7.txt"))
(def test-data8 (read-sdata "test/test-data/data8.txt"))
(def test-data9 (read-sdata "test/test-data/data9.txt"))

(deftest check-correctness-test
  (testing "1"
    (is (= (count test-data1) 1)))
  (testing "2"
    (is (= (count test-data2) 1)))
  (testing "3"
    (is (= (count test-data3) 3)))
  (testing "4"
    (is (= (count test-data4) 1)))
  (testing "5"
    (is (= (count test-data5) 1)))
  (testing "6"
    (is (= (count test-data6) 1)))
  (testing "7"
    (is (= (count test-data7) 1)))
  (testing "8"
    (is (= (count test-data8) 1)))
  (testing "9"
    (is (= (count test-data9) 1))))
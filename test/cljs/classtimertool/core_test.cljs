(ns classtimertool.core-test
  (:require  [cljs.test :refer [deftest is run-tests]]
             [classtimertool.helper :as h]))

(deftest foo
  (is (= 4 4)))

(deftest moo
  (is (= 1 1)))

(deftest too
  (is (= 1 1)))

(deftest poo
  (is (= (h/example 2) 2)))

(run-tests 'classtimertool.core-test)

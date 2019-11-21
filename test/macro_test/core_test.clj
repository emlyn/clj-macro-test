(ns macro-test.core-test
  (:require [clojure.test :refer :all]
            [macro-test.core :refer :all]))

(deftest infix-test
  (testing "This check will fail: the macro doesn't get expanded"
    (is (= '(+ 1 1)
           (macroexpand '(infix (1 + 1))))))

  (testing "It works if we use the fully qualified name"
    (is (= '(+ 1 1)
           (macroexpand '(macro-test.core/infix (1 + 1))))))

  (testing "Or we can use a syntax-quote, but then other symbols (like +) are also qualified"
    (is (= '(clojure.core/+ 1 1)
           (macroexpand `(infix (1 + 1)))))))

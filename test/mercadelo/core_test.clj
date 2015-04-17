(ns mercadelo.core-test
  (:require [clojure.test :refer :all]
            [mercadelo.core :refer :all]))

(def simple-ledger-no-loops
  {"joam" {:id "joam"
           :owes {}
           :has {"maria" 10}
           :accepts {}
           :accepted-by {}}
   "maria" {:id "maria"
            :owes {"joam" 10}
            :has {"clara" 5 "lois" 20}
            :accepts {}
            :accepted-by {}}
   "clara" {:id "clara"
            :owes {"lois" 5 "maria" 5}
            :has {}
            :accepts {}
            :accepted-by {}}
   "lois" {:id "lois"
           :owes {"maria" 20}
           :has {"clara" 5}
           :accepts {} :accepted-by {}}})

(def simple-ledger-with-loops
  {"joam" {:id "joam"
           :owes {"lois" 5}
           :has {"maria" 10 "clara" 5}
           :accepts {}
           :accepted-by {}}
   "maria" {:id "maria"
            :owes {"joam" 10 "lois" 40}
            :has {"clara" 5}
            :accepts {}
            :accepted-by {}}
   "clara" {:id "clara"
            :owes {"joam" 5 "maria" 5}
            :has {"lois" 3}
            :accepts {}
            :accepted-by {}}
   "lois" {:id "lois"
           :owes {"clara" 3}
           :has {"joam" 5 "maria" 40}
           :accepts {}
           :accepted-by {}}})

(def direct-payment-ledger
  {"joam" {:id "joam"
           :owes {}
           :has {"maria" 10}
           :accepts {"maria" 5}
           :accepted-by {}}
   "maria" {:id "maria"
            :owes {"joam" 10}
            :has {"clara" 5 "lois" 20}
            :accepts {}
            :accepted-by {"joam" 5}}
   "clara" {:id "clara"
            :owes {"lois" 5 "maria" 5}
            :has {}
            :accepts {}
            :accepted-by {}}
   "lois" {:id "lois"
           :owes {"maria" 20}
           :has {"clara" 5}
           :accepts {}
           :accepted-by {}}})

(def ledger-after-direct-payment
  {"joam" {:id "joam"
           :owes {}
           :has {"maria" 5}
           :accepts {"maria" 5}
           :accepted-by {}}
   "maria" {:id "maria"
            :owes {"joam" 5}
            :has {"clara" 5 "lois" 20}
            :accepts {}
            :accepted-by {"joam" 5}}
   "clara" {:id "clara"
            :owes {"lois" 5 "maria" 5}
            :has {}
            :accepts {}
            :accepted-by {}}
   "lois" {:id "lois"
           :owes {"maria" 20}
           :has {"clara" 5}
           :accepts {}
           :accepted-by {}}})

(deftest ledger-sanity-checks
  (testing "Ledger sanity checks"
    (is (ledger-valid? {}) "Empty ledger is valid, FWIW"))
  (testing "All test ledgers valid"
    (is
     (every? ledger-valid? [simple-ledger-no-loops
                            simple-ledger-with-loops
                            direct-payment-ledger
                            ledger-after-direct-payment]))))
(deftest test-loops
  (testing "Loops"
    (is (not (seq (loops simple-ledger-no-loops ["joam" "maria"] 10)))
        "No loops expected")
    (is (= (loops simple-ledger-with-loops ["joam" "maria"] 10)
           [{:amount 3 :path ["joam" "maria" "clara" "lois"]}])
        "One loop expected")))

(deftest payments
  (testing "Payments"
    (is (= (find-payments direct-payment-ledger "joam" "maria" 5)
           {:payments [{:route [{:giver "joam", :taker "maria", :currency "maria"}],
                        :amount 5}],
            :ledger ledger-after-direct-payment})
        "One payment expected")))

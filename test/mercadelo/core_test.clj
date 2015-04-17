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
           :accepts {}
           :accepted-by {}}})

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

(def payment-ledger
  {"joam" {:id "joam"
           :owes {}
           :has {"maria" 10}
           :accepts {"maria" 5}
           :accepted-by {"lois" 10}}
   "maria" {:id "maria"
            :owes {"joam" 10}
            :has {"clara" 5 "lois" 20}
            :accepts {"clara" 10}
            :accepted-by {"joam" 5}}
   "clara" {:id "clara"
            :owes {"lois" 5 "maria" 5}
            :has {}
            :accepts {}
            :accepted-by {"maria" 10}}
   "lois" {:id "lois"
           :owes {"maria" 20}
           :has {"clara" 5}
           :accepts {"joam" 10}
           :accepted-by {}}})

(def ledger-after-direct-payment
  {"joam" {:id "joam"
           :owes {}
           :has {"maria" 5}
           :accepts {"maria" 5}
           :accepted-by {"lois" 10}}
   "maria" {:id "maria"
            :owes {"joam" 5}
            :has {"clara" 5 "lois" 20}
            :accepts {"clara" 10}
            :accepted-by {"joam" 5}}
   "clara" {:id "clara"
            :owes {"lois" 5 "maria" 5}
            :has {}
            :accepts {}
            :accepted-by {"maria" 10}}
   "lois" {:id "lois"
           :owes {"maria" 20}
           :has {"clara" 5}
           :accepts {"joam" 10}
           :accepted-by {}}})

(def ledger-after-two-payments
  {"joam" {:id "joam"
           :owes {"lois" 5}
           :has {}
           :accepts {"maria" 5}
           :accepted-by {"lois" 10}}
   "maria" {:id "maria"
            :owes {}
            :has {"clara" 10 "lois" 20}
            :accepts {"clara" 10}
            :accepted-by {"joam" 5}}
   "clara" {:id "clara"
            :owes {"maria" 10}
            :has {}
            :accepts {}
            :accepted-by {"maria" 10}}
   "lois" {:id "lois"
           :owes {"maria" 20}
           :has {"joam" 5}
           :accepts {"joam" 10}
           :accepted-by {}}})

(deftest ledger-sanity-checks
  (testing "Ledger sanity checks"
    (is (ledger-valid? {}) "Empty ledger is valid, FWIW"))
  (testing "All test ledgers valid"
    (is
     (every? ledger-valid? [simple-ledger-no-loops
                            simple-ledger-with-loops
                            payment-ledger
                            ledger-after-direct-payment
                            ledger-after-two-payments]))))
(deftest test-loops
  (testing "Loops"
    (is (not (seq (loops simple-ledger-no-loops ["joam" "maria"] 10)))
        "No loops expected")
    (is (= (loops simple-ledger-with-loops ["joam" "maria"] 10)
           [{:amount 3 :path ["joam" "maria" "clara" "lois"]}])
        "One loop expected")))

(deftest payments
  (testing "Payments"
    (is (= (find-payments payment-ledger "maria" "joam" 5)
           {:payments [] :ledger payment-ledger})
        "No payment found")
    (is (= (find-payments payment-ledger "joam" "maria" 5)
           {:payments [{:route [{:giver "joam", :taker "maria", :currency "maria"}],
                        :amount 5}],
            :ledger ledger-after-direct-payment})
        "One-step payment")
    (is (= (find-payments payment-ledger "joam" "maria" 20)
           {:payments [{:route [{:giver "joam", :taker "maria", :currency "maria"}]
                        :amount 10}
                       {:route [{:giver "joam", :taker "lois", :currency "joam"}
                                {:giver "lois", :taker "maria", :currency "clara"}]
                        :amount 5}]
            :ledger ledger-after-two-payments})
        "Two payments, one direct and other with 2 steps, one short-cutting")))

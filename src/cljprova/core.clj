(ns cljprova.core
  (:gen-class))

(def simple-accounts-no-loops
  {"joam" {:id "joam"
           :owes 0
           :has {"maria" 10}
           :accepts {}
           :accepted-by {}}
   "maria" {:id "maria"
            :owes 10
            :has {"clara" 5 "lois" 20}
            :accepts {}
            :accepted-by {}}
   "clara" {:id "clara"
            :owes 10
            :has {}
            :accepts {}
            :accepted-by {}}
   "lois" {:id "lois"
           :owes 20
           :has {"clara" 5}
           :accepts {} :accepted-by {}}})

(def simple-accounts-with-loops
  {"joam" {:id "joam"
           :owes 5
           :has {"maria" 10 "clara" 5}
           :accepts {}
           :accepted-by {}}
   "maria" {:id "maria"
            :owes 70
            :has {"clara" 5 "lois" 20}
            :accepts {}
            :accepted-by {}}
   "clara" {:id "clara"
            :owes 10
            :has {}
            :accepts {}
            :accepted-by {}}
   "lois" {:id "lois"
           :owes 20
           :has {"joam" 5 "maria" 60}
           :accepts {}
           :accepted-by {}}})


(defn can-take [accounts giver taker currency]
  "The maximum amount of currency taker will take from giver."
  (let [giver-map (accounts giver)
        taker-map (accounts taker)]
    ((if (= giver currency)
       identity
       (partial min (get giver-map currency 0)))
     (max 0 (- (get (:accepts taker-map) currency 0)
               (get (:has taker-map) currency 0))))))

(defn find-direct-payment [accounts giver taker amount]
  "Vector of payments, or nil if none found.

   Payments in currencies other than taker's own will be preferred.
   Other than that, if several possible currency choices are valid,
   an arbitrary combination is chosen."
  (let [giver-map (accounts giver)
        taker-map (accounts taker)]
    (loop [amount-left amount
           [currency & rest-currencies] (conj (shuffle (keys (:has giver-map)))
                                              giver)
           payments []]
      (let [curr-amt (can-take accounts giver taker currency)]
        (if (>= curr-amt amount-left)
          (conj payments {:giver giver
                          :taker taker
                          :currency currency
                          :amount amount-left})
          (recur (- amount-left curr-amt)
                 rest-currencies
                 (conj payments {:giver giver
                                 :taker taker
                                 :currency currency
                                 :amount curr-amt})))))))

(defn find-indirect-payment [accounts giver taker amount]
  ; TODO
  nil)

; POC
(defn find-payment [accounts giver taker amount]
  (if-let [direct-payment (find-direct-payment accounts giver taker amount)]
    direct-payment
    (find-indirect-payment accounts giver taker amount)))

;; input:
;;    accounts: mapping of accounts, same format as in the examples
;;              above, but excluding entries with keys in `path'.
;;    path: non-empty vector of ids for the users in the debt chain.
;;    amount: maximum amount that is owed along all of the path
;;            above (that is, the minimum of the amounts owed along
;;            the path).
;; return:
;;    sequence of paths, where each path has the form
;;    {:amount amount :path ['joam' 'clara' 'lois']}
(defn loops
  [accounts path amount]
  (let [beginning (first path)]
    (mapcat (fn [[debtor amt]]
              (let [min-amount (if amount
                                 (min amt amount)
                                 amt)]
                (if (= debtor beginning)
                  [{:amount min-amount :path path}]
                  (loops (dissoc accounts (peek path))
                         (conj path debtor)
                         min-amount))))
            (:has (accounts (peek path))))))

(defn ids-match-keys? [accounts]
  (every? (fn [[k v]]
            (= k (:id v)))
          accounts))

(defn all-debtors-exist? [accounts]
  (every? accounts
          (mapcat (comp keys :has) (vals accounts))))

(defn no-self-debts? [accounts]
  (not (some #(get (:has %) (:id %))
             (vals accounts))))

(defn zero-sum-balances? [accounts]
  (every?
   (fn [acc]
     (= (:owes acc)
        (reduce +
                (map
                 (fn [other]
                   (get (:has other)
                        (:id acc)
                        0))
                 (vals accounts)))))
   (vals accounts)))

(defn account-sanity-check? [accounts]
  (and
   (ids-match-keys? accounts)
   (all-debtors-exist? accounts)
   (no-self-debts? accounts)
   (zero-sum-balances? accounts)))

(defn sanity-check? []
  (and
   (account-sanity-check? accounts-no-loops)
   (account-sanity-check? accounts-with-loops)
   (not (seq (loops accounts-no-loops ["joam" "maria"] 10)))
   (= (loops accounts-with-loops ["joam" "maria"] 10)
      [{:amount 5 :path ["joam" "maria" "lois"]}])))

(defn -main
  "Quick test"
  [& args]
  (str "Sanity checks " (if (sanity-check?)
                          "pass."
                          "fail.")))

(-main)

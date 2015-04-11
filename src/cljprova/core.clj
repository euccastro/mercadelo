(ns cljprova.core
  (:gen-class))

(def simple-accounts-no-loops
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

(def simple-accounts-with-loops
  {"joam" {:id "joam"
           :owes {"lois" 5}
           :has {"maria" 10 "clara" 5}
           :accepts {}
           :accepted-by {}}
   "maria" {:id "maria"
            :owes {"joam" 10 "lois" 60}
            :has {"clara" 5 "lois" 20}
            :accepts {}
            :accepted-by {}}
   "clara" {:id "clara"
            :owes {"joam" 5 "maria" 5}
            :has {}
            :accepts {}
            :accepted-by {}}
   "lois" {:id "lois"
           :owes {"maria" 20}
           :has {"joam" 5 "maria" 60}
           :accepts {}
           :accepted-by {}}})

(def direct-payment-accounts
  {"joam" {:id "joam"
           :owes {}
           :has {"maria" 10}
           :accepts {"maria" 5}
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


(defn can-take
  "The maximum amount of currency taker will take from giver."
  [accounts giver taker currency maximum]
  (min
   maximum
   ((if (= giver currency)
      identity
      (partial min (get-in accounts [giver :has currency] 0)))
    (if (= taker currency)
      maximum
      (max 0 (- (get-in accounts [taker :accepts currency] 0)
                (get-in accounts [taker :has currency] 0)))))))

(defn find-direct-payment
  "Vector of payments, or nil if none found.

  Payments in the currency of the taker will be preferred.
  Payments in currencies other than giver's will be preferred.
  Other than that, if several possible currency choices are valid,
  an arbitrary combination is chosen."
  [accounts giver taker amount]
  (loop [amount-left amount
         [currency & rest-currencies]
         (cons taker
               (conj (shuffle (keys (get-in accounts [giver :has])))
                     giver))
         payments []]
    (let [currency-amount (can-take accounts giver taker currency amount-left)]
      (if (= currency-amount amount-left)
        (conj payments {:giver giver
                        :taker taker
                        :currency currency
                        :amount amount-left})
        (recur (- amount-left currency-amount)
               rest-currencies
               (conj payments {:giver giver
                               :taker taker
                               :currency currency
                               :amount currency-amount}))))))

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

(defn all-keys-exist? [accounts]
  (every? accounts
          (mapcat keys
                  (mapcat (juxt :owes :has :accepts :accepted-by)
                          (vals accounts)))))

(defn all-subvals-positive? [accounts]
  (every? #(and (number? %) (>= % 0))
          (mapcat vals
                  (mapcat (juxt :owes :has :accepts :accepted-by)
                          (vals accounts)))))

(defn no-self-debts? [accounts]
  (not (some #(get (:has %) (:id %))
             (vals accounts))))

(defn complementary-maps? [accounts key1 key2]
  (every?
   (fn [[id acc]]
     (every?
      (fn [[other-id amount]]
        (= amount (get-in accounts [other-id key2 id])))
      (key1 acc)))
   accounts))

(defn account-sanity-check? [accounts]
  (and
   (ids-match-keys? accounts)
   (all-keys-exist? accounts)
   (all-subvals-positive? accounts)
   (no-self-debts? accounts)
   (complementary-maps? accounts :has :owes)
   (complementary-maps? accounts :accepts :accepted-by)))

(defn sanity-check? []
  (and
   (account-sanity-check? simple-accounts-no-loops)
   (account-sanity-check? simple-accounts-with-loops)
   (not (seq (loops simple-accounts-no-loops ["joam" "maria"] 10)))
   (= (loops simple-accounts-with-loops ["joam" "maria"] 10)
      [{:amount 5 :path ["joam" "maria" "lois"]}])
   (= (find-payment direct-payment-accounts "joam" "maria" 5)
      [{:giver "joam", :taker "maria", :currency "maria", :amount 5}])))

(defn -main
  "Quick test"
  [& args]
  (str "Sanity checks " (if (sanity-check?)
                          "pass."
                          "fail.")))

(-main)

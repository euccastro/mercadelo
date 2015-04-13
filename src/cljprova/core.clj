(ns cljprova.core
  (:gen-class))

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


(defn can-take
  "The maximum amount of currency taker will take from giver."
  [ledger giver taker currency maximum]
  (min
   maximum
   ((if (= giver currency)
      identity
      (partial min (get-in ledger [giver :has currency] 0)))
    (if (= taker currency)
      maximum
      (max 0 (- (get-in ledger [taker :accepts currency] 0)
                (get-in ledger [taker :has currency] 0)))))))

(defn find-direct-payment
  "Vector of payments, or nil if none found.

  Payments in the currency of the taker will be preferred.
  Payments in currencies other than giver's will be preferred.
  Other than that, if several possible currency choices are valid,
  an arbitrary combination is chosen."
  [ledger giver taker amount]
  (loop [amount-left amount
         [currency
          & rest-currencies] (cons taker
                                   (conj (shuffle
                                          (keys
                                           (get-in ledger [giver :has])))
                                         giver))
         payments []]
    (let [currency-amount (can-take ledger giver taker currency amount-left)]
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

(defn expand [ledger giver]
  "-> ((taker currency amount) ...)"
  (concat (map
           (fn [[taker amount]]
             [taker taker amount])
           (get-in ledger [giver :has]))
          (map
           (fn [[taker amount]]
             [taker giver amount])
           (get-in ledger [giver :accepted-by]))))

;; acumular pagamentos é ũa loucura.  Melhor repetir o pagamento até atingir o
;; amount-left ou até nom poder mais.
;; fazer funçom find-one-payment (-> amount, route), execute-one-payment -> ledger
(defn find-indirect-payments
  [ledger giver taker amount]
  (loop [ledger ledger
         amount amount
         payments []]
    (if (= amount 0)
      {:payments payments :ledger ledger}
      (let [result (find-one-payment ledger giver taker)]
        (if (nil? result)
          {:payments payments :ledger ledger}
          (let [payment-amount (min amount (:amount result))]
            (recur
             (execute-payment-route (:route result) payment-amount)
             (- amount payment-amount)
             (conj payments (assoc result :amount payment-amount)))))))))


(defn find-indirect-payment
  ;; iterative deepening search for a collection of (possibly transitive) payments
  ;; that will have the net result of taking up to amount from giver to taker
  ;; each part will only get paid in their own currency or in a currency they accept.
  ([ledger giver taker amount]
   (first
    (filter #(not (= % :cutoff))
            (map #(find-indirect-payment ledger giver taker amount amount [] %)
                 (iterate inc 1)))))
  ([ledger ; the ledger as in this state of the search
    giver ; the (possibly intermediate giver) considered at this point of the search
    taker ; the ultimate receiver of the payment
    max-amount ; maximum amount that can be delivered through this path of the search
    amount-left ; amount left for which to find a payment
    payments ; all partial payment routes discovered so far
    limit ; how far deep to search from here
    ]
   (cond (= giver taker)
         [{:amount amount :payments payments :ledger ledger}]
         (= limit 0) :cutoff
         :else
         ; iterate over all immediate possible takers of this giver
         (loop [ledger ledger
                rest (expand ledger giver)
                amount-left amount-left
                payments payments
                any-cutoff false]
           (if (nil? (seq rest))
             payments
             (let [[[next-giver currency step-max-amount] & rest] rest
                   result (find-indirect-payment
                           ledger
                           next-giver
                           taker
                           (min amount-left step-max-amount)
                           amount-left
                           (conj payments {:currency currency :giver giver})
                           (- limit 1))]
               (cond (= result :cutoff) (recur ledger
                                               rest
                                               amount-left
                                               payments
                                               true)
                     (nil? result) (recur ledger
                                          rest
                                          amount-left
                                          payments
                                          any-cutoff)
                     :else
                     (let [amount-paid (reduce + (map :amount result))
                           payments (concat payments result)]
                       (if (= amount-paid amount-left)
                         payments
                         (recur (:ledger (peek result))
                                rest
                                (- amount-left amount-paid)
                                payments
                                any-cutoff)))))))))))


; POC
(defn find-payment [ledger giver taker amount]
  (if-let [direct-payment (find-direct-payment ledger giver taker amount)]
    direct-payment
    (find-indirect-payment ledger giver taker amount)))

;; input:
;;    ledger: mapping of accounts, same format as in the examples
;;              above, but excluding entries with keys in `path'.
;;    path: non-empty vector of ids for the users in the debt chain.
;;    amount: maximum amount that is owed along all of the path
;;            above (that is, the minimum of the amounts owed along
;;            the path).
;; return:
;;    sequence of paths, where each path has the form
;;    {:amount amount :path ['joam' 'clara' 'lois']}
(defn loops
  [ledger path amount]
  (let [beginning (first path)]
    (mapcat (fn [[debtor amt]]
              (let [min-amount (if amount
                                 (min amt amount)
                                 amt)]
                (if (= debtor beginning)
                  [{:amount min-amount :path path}]
                  (loops (dissoc ledger (peek path))
                         (conj path debtor)
                         min-amount))))
            (:has (ledger (peek path))))))

(defn ids-match-keys? [ledger]
  (every? (fn [[k v]]
            (= k (:id v)))
        ledger))

(defn all-keys-exist? [ledger]
  (every? ledger
          (mapcat keys
                  (mapcat (juxt :owes :has :accepts :accepted-by)
                          (vals ledger)))))

(defn all-subvals-positive? [ledger]
  (every? #(and (number? %) (> % 0))
          (mapcat vals
                  (mapcat (juxt :owes :has :accepts :accepted-by)
                          (vals ledger)))))

(defn no-self-debts? [ledger]
  (not (some #(get (:has %) (:id %))
             (vals ledger))))

(defn no-owes-has-intersection? [ledger]
  (not (some #(some (:owes %) (keys (:has %)))
             (vals ledger))))

(defn complementary-maps? [ledger key1 key2]
  (every?
   (fn [[id acc]]
     (every?
      (fn [[other-id amount]]
        (= amount (get-in ledger [other-id key2 id])))
      (key1 acc)))
   ledger))

(defn account-sanity-check? [ledger]
  (and
   (ids-match-keys? ledger)
   (all-keys-exist? ledger)
   (all-subvals-positive? ledger)
   (no-self-debts? ledger)
   (no-owes-has-intersection? ledger)
   (complementary-maps? ledger :has :owes)
   (complementary-maps? ledger :accepts :accepted-by)))

(defn sanity-check? []
  (and
   (account-sanity-check? simple-ledger-no-loops)
   (account-sanity-check? simple-ledger-with-loops)
   (not (seq (loops simple-ledger-no-loops ["joam" "maria"] 10)))
   (= (loops simple-ledger-with-loops ["joam" "maria"] 10)
      [{:amount 3 :path ["joam" "maria" "clara" "lois"]}])
   (= (find-payment direct-payment-ledger "joam" "maria" 5)
      [{:giver "joam", :taker "maria", :currency "maria", :amount 5}])))

(defn -main
  "Quick test"
  [& args]
  (str "Sanity checks " (if (sanity-check?)
                          "pass."
                          "fail.")))

(-main)

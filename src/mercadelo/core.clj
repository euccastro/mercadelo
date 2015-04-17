(ns mercadelo.core
  (:gen-class))

(defn expand-accepted-by [ledger currency]
  (filter
   ;; Discard people that already have as much as they'll take
   ;; of the currency offered.
   #(> (% 2) 0)
   (map (fn [[taker taker-amt]]
          [taker currency (- taker-amt
                             (get-in ledger
                                     [taker :has currency]
                                     0))])
        (get-in ledger [currency :accepted-by]))))

(defn expand [ledger giver]
  "-> ((taker currency amount) ...)"
  (let [has (get-in ledger [giver :has])]
    ;; Our algorithm removes accounts from the ledger to avoid loops,
    ;; so ignore takers not in the ledger.
    (filter
     (comp ledger first)
     (concat
      ;; First try and directly pay out the currencies we have from
      ;; others, because that decreases outstanding currency/debt.
      (map
       (fn [[currency amount]]
         [currency currency amount])
       has)
      ;; Then try and pay the currencies we have to whoever will take
      ;; them, because that conserves outstanding currency/debt.
      (mapcat
       (fn [[currency giver-amt]]
         (map (fn [[taker currency taker-amt]]
                [taker currency (min giver-amt taker-amt)])
              (expand-accepted-by ledger currency)))
       has)
      ;; If there's no other option, try and give the giver's currency
      ;; to whoever will take it, even though that increases outstanding
      ;; currency/debt.
      (expand-accepted-by ledger giver)))))

(defn find-one-payment
  "-> {:route [{:giver :taker :currency} ...]
  :amount} or nil if not found"
  ([ledger giver taker]
   ;; iterative deepening search
   (first
    (filter #(not (= % :cutoff))
            (map #(find-one-payment ledger giver taker nil [] %)
                 (iterate inc 1)))))
  ([ledger giver taker amount route depth-limit]
   (cond (= giver taker) {:route route :amount amount}
         (= depth-limit 0) :cutoff
         :else
         (loop [takers (expand ledger giver)
                any-cutoff false]
           (cond
             (seq takers)
             (let [[[next-giver currency next-amount] & rest] takers
                   result (find-one-payment
                           ;; Avoid loops.  This makes the resulting
                           ;; ledger an invalid one, but the only
                           ;; other function that sees that is expand,
                           ;; which doesn't care.
                           ;; Note also that we don't credit the taker
                           ;; with the funds it would be given, because
                           ;; any route that depends on those funds
                           ;; would shadow a more direct one.
                           (dissoc ledger giver)
                           next-giver
                           taker
                           (if amount
                             (min amount next-amount)
                             next-amount)
                           (conj route {:giver giver :taker next-giver :currency currency})
                           (- depth-limit 1))]
               (cond (= result :cutoff) (recur rest true)
                     result result
                     :else (recur rest any-cutoff)))
             any-cutoff :cutoff
             :else nil)))))

(defn add-amt-in [m ks amt]
  (update-in m ks #(+ (or % 0) amt)))

(defn dissoc-in [m ks]
  (update-in m (pop ks) dissoc (peek ks)))

(defn sub-amt-in [m ks amt]
  (let [new-amt (- (get-in m ks) amt)]
    (if (= new-amt 0)
      ;; remove zero entries
      (dissoc-in m ks)
      (assoc-in m ks new-amt))))

(defn mv-amt-in [m from-ks to-ks amt]
  (add-amt-in (sub-amt-in m from-ks amt) to-ks amt))

(defn execute-payment-route
  "-> ledger"
  [ledger route amount]
  (reduce
   (fn [ledger {:keys [currency giver taker]}]
     (let [changed {giver (if (= currency giver)
                            (add-amt-in (ledger giver) [:owes taker] amount)
                            (sub-amt-in (ledger giver) [:has currency] amount))
                    taker (if (= currency taker)
                            (sub-amt-in (ledger taker) [:owes giver] amount)
                            (add-amt-in (ledger taker) [:has currency] amount))}]
       (merge ledger
              (if (or (= currency giver)
                      (= currency taker))
                changed
                ;; If a third party's currency has changed hands, we must update
                ;; who this party owes the involved amount.
                (conj changed
                      [currency (mv-amt-in
                                 (ledger currency)
                                 [:owes giver]
                                 [:owes taker]
                                 amount)])))))
   ledger
   route))

(defn find-payments
  "-> {:payments [{:route [{:currency :giver :taker} ...]
                   :amount} ...]
       :ledger}
   Payments may not satisfy the whole amount."
  [ledger giver taker amount]
  (loop [ledger ledger
         amount amount
         payments []]
    (if (= amount 0)
      {:payments payments :ledger ledger}
      (let [result (find-one-payment ledger giver taker)]
        (if (nil? result)
          ;; No more payments; return partial solution
          {:payments payments :ledger ledger}
          (let [payment-amount (min amount (:amount result))]
            (recur
             (execute-payment-route ledger (:route result) payment-amount)
             (- amount payment-amount)
             (conj payments (assoc result :amount payment-amount)))))))))

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

(defn ledger-errors [ledger]
  (keep
   (fn [[f s]] (when-not (f ledger) s))
   (partition
    2
    [ids-match-keys? "IDs don't match keys"
     all-keys-exist? "Some keys don't exist"
     all-subvals-positive? "Some subvals are not positive numbers"
     no-self-debts? "Some users have debts with themselves"
     no-owes-has-intersection? "Some users have the same currency they owe"
     #(complementary-maps? % :has :owes)
     "Some users' `:has's don't match the corresponding `:owe's"
     #(complementary-maps? % :accepts :accepted-by)
     "Some users' `:accepts's don't match the corresponding `:accepted-by's"])))

(def ledger-valid? (complement (comp seq ledger-errors)))

(defn -main
  "I do nothing..."
  [& args]
  "Main says hi!")

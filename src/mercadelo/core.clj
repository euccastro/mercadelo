(ns mercadelo.core
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
         (filter
          ;; Discard people that already have as much as they'll take
          ;; of the currency we have to offer.
          #(not (= (% 1) 0))
          (map (fn [[taker taker-amt]]
                 [taker currency (min giver-amt
                                      (max 0
                                           (- taker-amt
                                              (get-in ledger
                                                      [taker :has currency]
                                                      0))))])
               (get-in ledger [currency :accepted-by]))))
       has)
      ;; If there's no other option, try and give the giver's currency
      ;; to whoever will take it, even though that increases outstanding
      ;; currency/debt.
      (map
       (fn [[taker amount]]
         [taker giver amount])
       (get-in ledger [giver :accepted-by]))))))

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

(defn dissoc-in [m ks]
  (update-in m (pop ks) dissoc (peek ks)))

(defn execute-payment-route
  "-> ledger"
  [ledger route amount]
  (letfn [(add-amt [ledger who ks]
            (update-in (ledger who) ks #(+ (or % 0) amount)))
          (sub-amt [ledger who ks]
            (let [whomap (ledger who)
                  new-amt (- (get-in whomap ks) amount)]
              (if (= new-amt 0)
                ;; remove zero entries
                (dissoc-in whomap ks)
                (assoc-in whomap ks new-amt))))]
    (reduce
     (fn [ledger {:keys [currency giver taker]}]
       (assoc ledger
              giver (if (= currency giver)
                      (add-amt ledger giver [:owes taker])
                      (sub-amt ledger giver [:has currency]))
              taker (if (= currency taker)
                      (sub-amt ledger taker [:owes giver])
                      (add-amt ledger taker [:has currency]))))
     ledger
     route)))

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

(defn ledger-sanity-check? [ledger]
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
   (every? ledger-sanity-check? [simple-ledger-no-loops
                                 simple-ledger-with-loops
                                 direct-payment-ledger
                                 ledger-after-direct-payment])
   (not (seq (loops simple-ledger-no-loops ["joam" "maria"] 10)))
   (= (loops simple-ledger-with-loops ["joam" "maria"] 10)
      [{:amount 3 :path ["joam" "maria" "clara" "lois"]}])

   (= (find-payments direct-payment-ledger "joam" "maria" 5)
      {:payments [{:route [{:giver "joam", :taker "maria", :currency "maria"}],
                   :amount 5}],
       :ledger ledger-after-direct-payment})))

(defn -main
  "Quick test"
  [& args]
  (str "Sanity checks " (if (sanity-check?)
                          "pass."
                          "fail.")))

(-main)

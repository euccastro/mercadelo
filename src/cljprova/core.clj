(ns cljprova.core
  (:gen-class))

(def accounts-no-loops
  {"joam" {:id "joam" :owes 0 :has {"maria" 10}}
   "maria" {:id "maria" :owes 10 :has {"clara" 5 "lois" 20}}
   "clara" {:id "clara" :owes 10 :has {}}
   "lois" {:id "lois" :owes 20 :has {"clara" 5}}})

(def accounts-with-loops
  {"joam" {:id "joam" :owes 5 :has {"maria" 10 "clara" 5}}
   "maria" {:id "maria" :owes 70 :has {"clara" 5 "lois" 20}}
   "clara" {:id "clara" :owes 10 :has {}}
   "lois" {:id "lois" :owes 20 :has {"joam" 5 "maria" 60}}})

;; in:
;;    debtors: mapping from creditor to debtors, quantities
;;    path: vector of strings representing the possible path so far
;;    amount: minimum debt amount in the loop
;; return:
;;    sequence of paths, where each path has the form
;;    {:amount amount :path ['joam' 'clara' 'lois']}
(defn loops
  ([accounts path amount]
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
  ([accounts start]
   (loops accounts [start] nil)))

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
   (not (seq (loops accounts-no-loops "joam")))
   (= (loops accounts-with-loops "joam")
      [{:amount 5 :path ["joam" "maria" "lois"]}])))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

(sanity-check?)

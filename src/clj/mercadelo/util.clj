(ns mercadelo.util
  (:require [environ.core :refer (env)]))

(def in-development? (= (env :in-development) "indeed"))

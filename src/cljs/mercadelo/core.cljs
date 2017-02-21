(ns mercadelo.core
  (:require [rum.core :as r]))

(r/defc meu-comp [x]
  [:div "Olá " x "!"])

(defn on-reload []
  (r/mount (meu-comp "eu") (.getElementById  js/document "app_container")))

(defn main []
  (on-reload))

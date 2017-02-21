(ns mercadelo.core
  (:gen-class)
  ;; XXX: aleph, sente, datomic, auth
  (:require [compojure.core :refer (defroutes GET POST)]
            [compojure.route :refer (files not-found resources)]
            [mercadelo.util :as util]
            [ring.middleware.defaults :refer (wrap-defaults site-defaults)]
            [rum.core :as rum]))


(defn root [req]
  (rum/render-html [:html
                    [:head
                     [:title "Mercadelo"]]
                    [:body
                     [:div#app_container]
                     [:script {:type "text/javascript" :src "js/main.js"}]
                     [:script {:type "text/javascript"} "mercadelo.core.main();"]]]))

(defroutes handler
  (GET "/" req (root req))
  (resources (if util/in-development? "/public" "/"))
  (files "/")
  (not-found "Page not found."))

(def app
  (wrap-defaults handler site-defaults))

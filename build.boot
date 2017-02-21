(set-env!
 :source-paths #{"src/clj" "src/cljs"}
 :resource-paths #{"res"}
 :dependencies '[[adzerk/boot-cljs "1.7.228-2" :scope "test"]
                 [adzerk/boot-cljs-repl "0.3.3" :scope "test"]
                 [adzerk/boot-reload "0.5.1" :scope "test"]
                 [pandeiro/boot-http "0.7.6" :scope "test"]
                 [crisptrutski/boot-cljs-test "0.3.0" :scope "test"]
                 [boot-environ "1.1.0"]
                 [cljs-react-material-ui "0.2.37"]
                 ;; [clj-time "0.13.0"]
                 ;; using the alpha because that's the version of the API docs
                 ;; in their website.
                 ;;[com.andrewmcveigh/cljs-time "0.5.0-alpha2"]
                 [org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.473"]
                 [compojure "1.5.2"]
                 [org.clojure/core.async "0.2.395"]
                 ;;[crypto-random "1.2.0"]
                 [datascript "0.15.5"]
                 [com.datomic/datomic-free "0.9.5561" :exclusions [com.google.guava/guava]]
                 [environ "1.1.0"]
                 ;; used for the sente adapter in development, and for the http
                 ;; client
                 [http-kit "2.1.19"]  ;; same as used by boot-http (XXX: can I just piggyback on that, then?)
                 ;; 
                 [markdown-clj "0.9.95"]
                 ;; used for the sente adapter in deployment
                 [aleph "0.4.1"]
                 [com.cemerick/piggieback "0.2.1" :scope "test"]
                 [ring/ring-defaults "0.1.5"]
                 [rum "0.10.8"]
                 ;; we use timbre from here too
                 [com.taoensso/sente "1.11.0"]
                 [org.clojure/tools.nrepl "0.2.12" :scope "test"]
                 [weasel "0.7.0" :scope "test"]])

(require
  '[adzerk.boot-cljs      :refer [cljs]]
  '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]]
  '[adzerk.boot-reload    :refer [reload]]
  '[environ.boot :refer [environ]]
  '[crisptrutski.boot-cljs-test  :refer [test-cljs]]
  '[pandeiro.boot-http    :refer [serve]])

(deftask auto-test []
  (merge-env! :resource-paths #{"test"})
  (comp (watch)
     (speak)
     (test-cljs)))

(deftask dev []
  (comp (environ :env {:in-development "indeed"})
     (serve :handler 'mercadelo.core/app
            :resource-root "target"
            :httpkit true
            :reload true)
     (watch)
     (speak)
     (reload :on-jsload 'mercadelo.core/on-reload
             ;; XXX: make this configurable
             :open-file "emacsclient -n +%s:%s %s")
     (cljs-repl)
     (cljs :source-map true :optimizations :none)
     (target :dir #{"target"})))

(deftask build []
  (comp
   (cljs :optimizations :none)  ;; XXX: add externs to mathjax so I can reenable these
   (aot :namespace '#{mercadelo.core})
   (pom :project 'mercadelo
        :version "0.1.0-SNAPSHOT")
   (uber)
   (jar :main 'mercadelo.core)
   (target :dir #{"target"})))

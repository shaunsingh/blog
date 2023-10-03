(set-env!
  :source-paths #{"src"}
  :resource-paths #{"resources"}
  :dependencies '[[boot/core "2.8.2" :scope "provided"]
                  [nyoom-engineering/perun "0.4.2-SNAPSHOT"]
                  [hiccup "1.0.5" :exclusions [org.clojure/clojure]]
                  [org.clojure/clojurescript "1.10.439"]
                  [pandeiro/boot-http "0.6.3-SNAPSHOT"]
                  [deraen/boot-livereload "0.2.1"]
                  [deraen/boot-sass "0.3.1"]
                  [adzerk/boot-cljs "2.1.5" :scope "test"]
                  [org.clojure/tools.namespace "0.3.0-alpha4"]])

(require '[boot.core :as boot]
         '[clojure.string :as str]
         '[io.perun :as perun]
         '[pandeiro.boot-http :refer [serve]]
         '[deraen.boot-livereload :refer [livereload]]
         '[deraen.boot-sass :refer [sass]]
         '[adzerk.boot-cljs :refer [cljs]]
         '[nyoom-engineering.tasks.emoji :refer [emoji]]
         '[nyoom-engineering.tasks.clean :refer [clean]])

(defn pipeline
  [& steps]
  (->> steps
       (keep identity)
       (apply comp)))

(defn html
  [prod?]
  (pipeline
    (perun/markdown :md-exts {:all true})
    (when prod? (perun/draft))))

(defn blog?
  [file]
  (= (:type file) "post"))
  ; (re-find #"/blog/" (:path file)))

(defn project?
  [file]
  (= (:type file) "project"))
  ;; (re-find #"/projects/" (:path file)))

(defn blog-pages
  [prod?]
  (pipeline
    (perun/collection :renderer 'nyoom-engineering.blog.index/render :page "index.html" :filterer blog?)
    (perun/render :renderer 'nyoom-engineering.blog.post/render :filterer blog?)
    (perun/paginate :renderer 'nyoom-engineering.blog.paginate/render :filterer blog? :out-dir "blog")
    (perun/tags :renderer 'nyoom-engineering.blog.tags/render :filterer blog? :out-dir "blog/tags")))

(defn project-pages
  [prod?]
  (pipeline
    (perun/collection :renderer 'nyoom-engineering.projects.index/render :page "projects.html" :filterer project?)
    (perun/render :renderer 'nyoom-engineering.projects.project/render :filterer project?)))
    ; (perun/tags :renderer 'nyoom-engineering.tags/render :filterer project?)))
    ; (perun/paginate :renderer 'nyoom-engineering.paginate/render :filterer project?)))

(defn static-pages
  [prod?]
  (pipeline
    (perun/static :renderer 'nyoom-engineering.about/render :page "about.html")
    (perun/static :renderer 'nyoom-engineering.not-found/render :page "404.html")))

(defn seo-files
  [prod?]
  (pipeline
    (perun/sitemap)
    (perun/rss :filterer blog?)
    (perun/atom-feed :filterer blog?)))

(defn build-meta
  [prod?]
  (pipeline
    (perun/slug)
    (perun/ttr)
    (perun/word-count)
    (perun/build-date)
    (perun/gravatar :source-key :author-email :target-key :author-gravatar)))

(deftask generate-site
  [_ prod? bool "Production build?"]
  (pipeline
    (perun/global-metadata)
    (html prod?)
    (build-meta prod?)
    (seo-files prod?)
    (blog-pages prod?)
    (project-pages prod?)
    (static-pages prod?)
    (emoji)))

(deftask build
  "Build the blog source and output to target/public"
  []
  (let [prod? true]
   (pipeline
     (generate-site :prod? prod?)
     (cljs :ids ["prod"])
     (sass :output-style :compressed :source-map false)
     (sift :include [#".edn"] :invert true)
     (clean :exclude [#".git"])
     (target :no-clean true)
     (notify))))

(deftask develop
  []
  (pipeline
    (clean :dir "target/dev")
    (watch)
    (generate-site :prod? false)
    (cljs :ids ["dev"])
    (sass :output-style :expanded :source-map true)
    (sift :move {#"public" "dev"})
    (sift :include [#".edn"] :invert true)
    (livereload :snippet true)
    (target :no-clean true)
    (serve :dir "target/dev" :port 9000)))

(deftask dev
  []
  (set-env!
   :init-ns 'user
   :dependencies #(into % '[[proto-repl "0.3.1"]
                            [proto-repl-charts "0.3.2"]
                            [org.clojure/tools.namespace "0.2.11"]])))

(deftask uuid
  [n count COUNT int "How many UUIDs to generate?"]
  (let [gen-uuid #(str (java.util.UUID/randomUUID))
        uuids (repeatedly (or count 5) gen-uuid)]
    (dorun (map println uuids))))

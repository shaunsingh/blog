(ns nyoom-engineering.blog.paginate
  (:require [hiccup.page :refer [html5]]
            [nyoom-engineering.base :as base]
            [nyoom-engineering.blog.post :refer [render-entry]]))

(defn render [{global-meta :meta posts :entries entry :entry :as data}]
  (base/render
   :title (:site-title global-meta)
   :subtitle (format "nyoom-engineering.blog/page-%s" (:page entry))
   :current :blog
   :content [:div.blog
             [:h2.mb-5 (str "Page: " (:page entry))]
             [:ul.posts
              (for [post posts]
                  [:li.posts__item
                   (render-entry (assoc data :entry post) :description)])]
             [:div.pagination
              (when (= (:page entry) 1)
                 [:a.pagination__link
                  {:href (str "/")}
                  [:i.fa.fa-long-arrow-alt-left.mr-1]
                  "View newest posts"])
              (when (:prev-page entry)
                 [:a.pagination__link
                  {:href (str "/blog/" (:prev-page entry))}
                  [:i.fa.fa-long-arrow-alt-left.mr-1]
                  "View newer posts"])
              (when (:next-page entry)
                 [:a.pagination__link
                  {:href (str "/blog/" (:next-page entry))}
                  "View older posts"
                  [:i.fa.fa-long-arrow-alt-right.ml-1]])]]))

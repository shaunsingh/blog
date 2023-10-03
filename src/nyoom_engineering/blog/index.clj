(ns nyoom-engineering.blog.index
  (:require [nyoom-engineering.base :as base]
            [nyoom-engineering.blog.post :refer [render-entry]]))

(defn render [{global-meta :meta posts :entries post :entry :as data}]
  (let [[first-post & posts] posts]
    (base/render
     :title (:site-title global-meta)
     :subtitle "nyoom-engineering/blog"
     :current :blog
     :meta {:title (:site-title global-meta)
            :description "Shaurya's blog on functional programming and software engineering."
            :url (:base-url global-meta)}
     :content [:div.blog
               [:ul.posts
                (when first-post
                  [:li.posts__item
                   (render-entry (assoc data :entry first-post) :content
                                 :context :featured)])
                (for [post (take 2 posts)]
                  [:li.posts__item
                   (render-entry (assoc data :entry post) :description)])]
               (when (>= (count posts) 3)
                 [:div.pagination
                  [:a.pagination__link
                   {:href (str "/blog/page-1.html")}
                   "View Older Posts"
                   [:i.fa.fa-long-arrow-alt-right.ml-1]]])])))

(ns nyoom-engineering.blog.tags
  (:require [nyoom-engineering.base :as base]
            [nyoom-engineering.blog.post :refer [render-entry]]))


(defn render [{global-meta :meta posts :entries entry :entry :as data}]
  (base/render
   :title (:site-title global-meta)
   :subtitle "nyoom-engineering.blog/tags"
   :current :blog
   :content [:div.blog
             [:h2.mb-5 (str "Tag: " (:tag entry))]
             [:ul.posts
              (for [post posts]
                [:li.posts__item
                 (render-entry (assoc data :entry post) :description)])]]))

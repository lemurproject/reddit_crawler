(ns reddit-crawler.core
  (:gen-class :main true)
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]
            [clojure.walk :as walk]
            [clojure.string :as string]
            [clojure.java.io :as io]
            [clojure.tools.cli :as cli]))

; Start ID marks the first post in our time range - I picked July 2012 01 as
; the start date because a discussion might go on for a month so we move the dial forward
(def start-id "t3_vuetx")
(def dec-31-2012-epoch 1325376000)

(defn build-reddit-uri-next-100-posts
  ([init-id] (build-reddit-uri-next-100-posts init-id 100))
  ([init-id limit] (format 
                     "http://www.reddit.com/r/all/new.json?after=%s&limit=%d" 
                     init-id 
                     limit)))

(defn fetch-next-posts
  "Calls the reddit api to get next few posts after the specified 
  init-id object id. Default is 100 posts"
  ([init-id] (fetch-next-posts init-id 100))
  ([init-id limit] 
   (let [posts (map walk/keywordize-keys 
                    (-> 
    				 (client/get 
                		(build-reddit-uri-next-100-posts 
                    		init-id 
                    		limit))
    				 :body
					 json/read-str 
			 		 walk/keywordize-keys 
			 		 :data
			 		 :children))
				
         last-post-in-list (last posts)]
     (list (string/join
             "_"
             (list (:kind last-post-in-list) 
                   (-> last-post-in-list :data :id))) 
           posts))))

(defn beyond-end-epoch?
  "Pulls the post's epoch and checks if it is below the epoch we care about"
  [reddit-post]
  (< (-> reddit-post :data :created) dec-31-2012-epoch))

(defn write-posts
  "Writes out posts to a gzipped json file.
  I will move them to a WARC format later"
  [posts]
  (with-open [out (-> (io/output-stream "posts.gz" :append true)
                      java.util.zip.GZIPOutputStream.
                      io/writer)]
    (binding [*out* out]
      (doseq [post (map json/write-str posts)]
        (println post)))))

(defn crawl
  "Initiates the crawl and loops over till the job is done"
  [start-id]
  (let
    [last_ids_posts (fetch-next-posts start-id)
     last-item-id (first last_ids_posts)
     posts (second last_ids_posts)]
    (do (write-posts posts)
        (. Thread (sleep 2000))
        (when (not-any? beyond-end-epoch? posts)
      	  (recur last-item-id)))))

(defn -main
  [& args]
  (let [[maps [start-id] banner] (cli/cli args)]
    (crawl start-id)))

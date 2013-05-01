(ns reddit-crawler.read-posts
  (:gen-class :main true)
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.tools.cli :as cli]
            [clojure.walk :as walk]
            [clj-time.coerce :as clj-time-coerce]
            [clj-time.core :as clj-time]))

; Open and read the reddit crawler files
(defn read-posts
  "Applies function f to each post in the file"
  [posts-file f]
  (with-open [in (->
                  (io/input-stream posts-file)
                  java.util.zip.GZIPInputStream.
                  io/reader)]
    (let [line (line-seq in)]
      (doall (map (fn [l] (f l)) line)))))

(defn get-date
  [post-str]
  (-> post-str
      json/read-str
      walk/keywordize-keys
      :data
      :created))

(defn last-date
  [posts-file]
  (let [dates (read-posts posts-file get-date)]
    (list (count dates) (clj-time-coerce/to-date (* 1000 (long (last dates)))))))

(defn -main
  [& args]
  (let [[dict [posts-file] banner] (cli/cli args)
        [num-dates last-post-date] (last-date posts-file)]
      (spit "/bos/www/htdocs/spalakod/clueweb12pp/reddit-status.txt" 
            (format "Report Time: %s\tNumber of Posts: %d\tMost Recent Date Downloaded: %s" 
                    (.toString clj-time/now) 
                    num-dates 
                    (.toString last-post-date)))))

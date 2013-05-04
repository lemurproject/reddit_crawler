(ns reddit-crawler.find-next-id
  (:gen-class :main true)
  (:require [reddit-crawler.core :as core]
            [clojure.string :as string]))

; The purpose of this file is to identify the next id to use
; on reddit. Posts after a certain id face a bit of issues since
; there are some errors in Reddit's db.
; We essentially decrement the base 36 id as low as we can and see what responds
; That id is then used to re-initiate the crawl.

(defn test-id
  "Args:
  	- start-id : Id to begin our job"
  [start-id]
    (try
      (core/fetch-next-posts start-id)
      (catch Exception e false)))

(defn next-id
  "Args:
  	- cur-id : Current t3_* format id"
  [cur-id]
  (let [[kind id-str] (string/split cur-id #"_")
        int-id (java.lang.Integer/parseInt id-str 36)]
    (do (println (format "Trying: %s" cur-id))
      	(. Thread sleep 2000)
    	(if (test-id cur-id)
      		cur-id
      		(recur (format "%s_%s" kind (java.lang.Integer/toString (- int-id 1) 36)))))))

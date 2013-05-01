(defproject reddit_crawler "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [clj-http "0.7.2"]
                 [org.clojure/data.json "0.2.2"]
                 [gzip-util "0.1.0-SNAPSHOT"]
                 [org.clojure/tools.cli "0.2.2"]
                 [clj-time "0.5.0"]]
  :main reddit-crawler.core
  :aot [reddit-crawler.core reddit-crawler.read-posts])

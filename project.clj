(defproject xml-parser-clj "0.1.0-SNAPSHOT"
  :description "PubMed XML to CSV converter"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :main xml-parser-clj.core
  :aot [xml-parser-clj.core]
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/data.zip "0.1.1"]
                 ;; https://github.com/clojure/data.csv
                 [org.clojure/data.csv "0.1.2"]])

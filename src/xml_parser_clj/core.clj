;;; Main file to parse XML
(ns xml-parser-clj.core
  (:require [clojure.xml :as xml]
            [clojure.zip :as zip]))


;;;
;;; References
;; ClojureDocs parse: http://clojuredocs.org/clojure_core/clojure.xml/parse


;;;
;;; Code

;; convenience function, first seen at nakkaya.com later in clj.zip src
(defn zip-str
  "convenience function, first seen at nakkaya.com later in clj.zip src"
  [s]
  (zip/xml-zip (xml/parse (java.io.ByteArrayInputStream. (.getBytes s)))))


;;;
;;; Load data
;; Load PubMed XML
(def test-xml (slurp "/Users/kazuki/Documents/BWH/_Project_RheumPharm/_tools/pubmed.test.xml"))
;; Load 
(def test-formatted (zip-str test-xml))


;;;
;;; Using :tag based search

;;; Define a recursive function to look for target :tag
(defn recur-search
  "Recursively look for target :tag within an article"
  [tag a-map]
  ;;
  (cond
   ;; if tag matches :ta-map
   (= tag (:tag a-map)) (:content a-map)
   ;; if it does not match, and all vector elements are maps
   (every? map? (:content a-map)) (->> (for [elt (:content a-map)]
                                         ;; Recurse on each element
                                         (recur-search tag elt))
                                       ;; Drop empty elements
                                       (filter (complement empty?),  ))
   ;; otherwise return an empty vector
   :else []))

;; works at top level
;; (recur-search :PubmedArticleSet (first test-formatted))
;; 
;; works at deeper levels, but produces garbage
(recur-search :PMID (first test-formatted))
(filter (complement nil?) (flatten (recur-search :PMID (first test-formatted))))

(recur-search :ArticleTitle (first test-formatted))
(recur-search :Abstract (first test-formatted))

(flatten (recur-search :Abstract (first test-formatted)))

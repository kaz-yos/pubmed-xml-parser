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
;; Load as a map
(def test-formatted (first (zip-str test-xml)))


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


;; Simple elements
(flatten (recur-search :PMID test-formatted))
(flatten (recur-search :ArticleTitle test-formatted))

(interleave (flatten (recur-search :PMID test-formatted))
            (flatten (recur-search :ArticleTitle test-formatted)))


;; Abstract (multiple subelements)
(map flatten (recur-search :Abstract test-formatted))


;;; Define a function to concatenate abstract
(defn concat-abstract
  "Concatenate abstract elements from an abstract."
  [abstract]
  (map #(flatten (map :content %)) abstract))


(->> (recur-search :Abstract test-formatted)
     ;; Flatten within each article
     (map flatten,  )
     ;; Concatenate elements within each article
     (concat-abstract,  )
     )

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


;;parse from xml-strings to internal xml representation
(zip-str "<a href='nakkaya.com'/>")

;;root can be rendered with xml/emit-element
(xml/emit-element (zip/root [{:tag :a, :attrs {:href "nakkaya.com"}, :content nil} nil]))

(xml/emit-element (zip/root (zip-str "<a href='nakkaya.com'/>")))
;;printed (to assure it's not lazy and for performance), can be caught to string variable with with-out-str


;;;
;;; Load data
(def test-xml (slurp "/Users/kazuki/Documents/BWH/_Project_RheumPharm/_tools/pubmed.test.xml"))
(class test-xml)
(println test-xml)

(def test-formatted (zip-str test-xml))

(class test-formatted)

(count test-formatted)
(first test-formatted)
(last test-formatted)

;; :PubmedArticleSet is a vector
(:tag (first test-formatted))
(class (:content (first test-formatted)))


;; :PubmedArticle
(:tag (first (:content (first test-formatted))))
(class (:content (first (:content (first test-formatted)))))


;; :MedlineCitation
(:tag (first (:content (first (:content (first test-formatted))))))
(class (:content (first (:content (first (:content (first test-formatted)))))))
;; Elements are maps
(map class (:content (first (:content (first (:content (first test-formatted)))))))

(map count (:content (first (:content (first (:content (first test-formatted)))))))


;; Check :tag on all vector elements
(map :tag (:content (first (:content (first (:content (first test-formatted)))))))

;; :PMID
(:tag (get (:content (first (:content (first (:content (first test-formatted)))))) 0))
(:content (get (:content (first (:content (first (:content (first test-formatted)))))) 0))

(:content (get (:content (first (:content (first (:content (first test-formatted)))))) 0))

;; Define a helper function
(defn fst-cont
  "Get the :content element of the first vector element"
  [vctr]
  (:content (first vctr)))

(first (:content (get (fst-cont (fst-cont (fst-cont test-formatted))) 0)))


;; :DateCreated
(:tag (get (:content (first (:content (first (:content (first test-formatted)))))) 1))
(map :tag (:content (get (:content (first (:content (first (:content (first test-formatted)))))) 1)))

;; :Article
(:tag (get (:content (first (:content (first (:content (first test-formatted)))))) 2))
(map :tag (:content (get (:content (first (:content (first (:content (first test-formatted)))))) 2)))

(get (:content (get (:content (first (:content (first (:content (first test-formatted)))))) 2)) 1)


;; Filter based on :tag
(filter #(= (:tag %) :ArticleTitle) (:content (get (:content (first (:content (first (:content (first test-formatted)))))) 2)))



;;;
;;; Using :tag based
;;; Define a function to filter a vector based on :tag
(defn filter-tag
  "Filter vector elements based on :tag values, and return :content"
  [tag a-seq]
  (map :content (filter #(= (:tag %) tag) a-seq)))

;; Get a vector element with :tag :PubmedArticleSet
(filter-tag :PubmedArticleSet test-formatted)
(count (filter-tag :PubmedArticleSet test-formatted))
(first (filter-tag :PubmedArticleSet test-formatted))
;; Two :PubmedArticle's are contained
;; The recursive search should be mapped to these
(count (first (filter-tag :PubmedArticleSet test-formatted)))

;; Get a vector using :content
;; and filter elements with :tag :PubmedArticle
(filter-tag :PubmedArticle (first (filter-tag :PubmedArticleSet test-formatted)))
(count (filter-tag :PubmedArticle (first (filter-tag :PubmedArticleSet test-formatted))))





;; map to seq of seq
(map #(filter-tag :MedlineCitation %) (filter-tag :PubmedArticle (first (filter-tag :PubmedArticleSet test-formatted))))

(first (first (map #(filter-tag :MedlineCitation %) (filter-tag :PubmedArticle (first (filter-tag :PubmedArticleSet test-formatted))))))

(count (first (first (map #(filter-tag :MedlineCitation %) (filter-tag :PubmedArticle (first (filter-tag :PubmedArticleSet test-formatted)))))))

(map #(filter-tag :PMID %) (first (first (map #(filter-tag :MedlineCitation %) (filter-tag :PubmedArticle (first (filter-tag :PubmedArticleSet test-formatted)))))))


;;
(map #(filter-tag :ArticleTitle %) (map #(filter-tag :MedlineCitation %) (filter-tag :PubmedArticle (first (filter-tag :PubmedArticleSet test-formatted)))))







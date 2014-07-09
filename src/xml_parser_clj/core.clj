;;; Main file to parse XML
(ns xml-parser-clj.core
  (:require [clojure.xml :as xml]
            [clojure.zip :as zip]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io]))


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
   ;; Stop at comments
   (= :CommentsCorrectionsList (:tag a-map)) []
   ;; if tag matches :target a-map
   (= tag (:tag a-map)) (:content a-map)
   ;; if it does not match, and all vector elements are maps
   (every? map? (:content a-map)) (->> (for [elt (:content a-map)]
                                         ;; Recurse on each element
                                         (recur-search tag elt))
                                       ;; Drop empty elements
                                       (filter (complement empty?),  ))
   ;; otherwise return an empty vector
   :else []))


;;; Simple elements
(flatten (recur-search :PMID test-formatted))
(flatten (recur-search :ArticleTitle test-formatted))


;;; Abstract (multiple subelements)
(map flatten (recur-search :Abstract test-formatted))


;;; Define a function to concatenate abstract
(defn concat-abstract
  "Concatenate abstract elements from an abstract."
  [abstract]
  ;;
  (->> (map :content abstract)
       ;; Flatten to a single seq of strings
       (flatten,  )
       ;; Make it a single string
       (apply str,  )))


(->> (recur-search :Abstract test-formatted)
     ;; Flatten within each article
     (map flatten,  )
     ;; Concatenate elements within each article
     (map concat-abstract,  ))


;;; Date (require conversion)
(first (map flatten (recur-search :PubDate test-formatted)))

;;; Define a function to get :content by :tag
(defn content-by-tag
  "get :content by :tag"
  [tag a-map]
  (->> (filter #(= tag (:tag %)) a-map)
       (map :content,  )))


;;; Define a map to convert month to month number
(def month-number {"Jan"  1, "Feb"  2, "Mar"  3, "Apr"  4, "May"  5, "Jun"  6,
                   "Jul"  7, "Aug"  8, "Sep"  9, "Oct" 10, "Nov" 11, "Dec" 12})


;;; Define a function to pad with a specific letter to the left
(defn padding
  "Pad with a specific letter to the left"
  [pad-str n orig-str]
  (->> (str pad-str orig-str)
       (reverse,  )
       (take n,  )
       (reverse,  )
       (map str,  )
       (apply str,  )))

(padding "0" 2 "12")
(padding "0" 2  "1")


;;; Define a function to create R style date string
(defn date-string
  "Create R style date string given a list of PubMed date maps"
  [lst-of-date-elts]
  (let [year      (first (first (content-by-tag :Year  lst-of-date-elts)))
        ;;
        month     (first (first (content-by-tag :Month lst-of-date-elts)))
        month-pad (padding "000" 2 (month-number month))
        ;;
        day       (first (first (content-by-tag :Day   lst-of-date-elts)))
        day-pad   (padding "000" 2 day)]
    ;;
    (str (if (empty? year)      "0000" year)
         "-"
         (if (= "00" month-pad) "00"   month-pad)
         "-"
         (if (= "00" day-pad)   "00"   day-pad))))


(map date-string (map flatten (recur-search :PubDate test-formatted)))



;;;
;;; Combine output
(defn parse-pubmed
  "Parse PubMed XML map (PubmedArticle) to format appropriate for CSV"
  [PubmedArticle]
  (let [ArticleTitle (flatten (recur-search :ArticleTitle PubmedArticle))
        Abstract     (->> (recur-search :Abstract PubmedArticle)
                          (map flatten,  )
                          (map concat-abstract,  ))
        PMID         (flatten (recur-search :PMID PubmedArticle))
        PubDate      (map date-string (map flatten (recur-search :PubDate PubmedArticle)))]
    (map vector
         ArticleTitle
         (if (empty? Abstract) [""] Abstract)
         PMID
         PubDate)))


(parse-pubmed test-formatted)
(count (parse-pubmed test-formatted))
(first (parse-pubmed test-formatted))




;;;
;;; Test on actual data
;; Load PubMed XML
(def ra-bio-inf-xml (slurp "/Users/kazuki/Documents/BWH/_Project_RheumPharm/20140709-RA-bDMARDs-Infection.xml"))
;;
;; Delete these lines
;; <?xml version="1.0"?>
;; <!DOCTYPE PubmedArticleSet PUBLIC "-//NLM//DTD PubMedArticle, 1st January 2014//EN" "http://www-ncbi-nlm-nih-gov.ezp-pqrod1.hul.harvard.edu/corehtml/query/DTD/pubmed_140101.dtd">
;;
;; Load as a map
(def ra-bio-inf-map (first (zip-str ra-bio-inf-xml)))


;; Work at the article level
(def parsed-pubmed (map parse-pubmed (:content ra-bio-inf-map)))

(class parsed-pubmed)
(count parsed-pubmed)
(map count parsed-pubmed)
(filter #(zero? (count %)) parsed-pubmed)

(nth parsed-pubmed 37)
(nth parsed-pubmed 38) ; This one lacks abstract
(nth parsed-pubmed 39)
(nth parsed-pubmed 40)

(first (map first parsed-pubmed))



;;;
;;; Sepcify a directory for conversion
;; http://clojuredocs.org/clojure_core/clojure.core/file-seq
;; http://stackoverflow.com/questions/8566531/listing-files-in-a-directory-in-clojure
(def directory (io/file "/Users/kazuki/Documents/BWH/_Project_RheumPharm/_tools/xml-to-csv-dir"))
(def files (file-seq directory))
(take 10 files)



;;;
;;; -main function
(defn -main
  "-main function to be called when the program is run"
  [pm-xml-file-path]
  ;;
  (let [parsed-pm (->> (slurp pm-xml-file-path)
                       (zip-str,  )
                       (first,  )
                       (:conent,  )
                       (map parse-pubmed,  ))]
    ;;
    ;; write to a csv file
    (with-open [out-file (io/writer (str pm-xml-file-path ".csv"))]
      (csv/write-csv out-file (map first parsed-pm)))))

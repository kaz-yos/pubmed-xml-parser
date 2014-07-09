;;; Main file to parse XML

;;;
;;; Namespace and dependencies
(ns xml-parser-clj.core
  ;; To make .jar executable
  (:gen-class)
  ;; Dependencies
  (:require [clojure.xml :as xml]
            [clojure.zip :as zip]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io]))


;;;
;;; References
;; ClojureDocs parse: http://clojuredocs.org/clojure_core/clojure.xml/parse


;;;
;;; Code


;;; Define a function to convert an XML string to clojure internal representation
;; convenience function, first seen at nakkaya.com later in clj.zip src
(defn zip-str
  "convert an XML string to clojure internal representation.

convenience function, first seen at nakkaya.com later in clj.zip src"
  [s]
  (zip/xml-zip (xml/parse (java.io.ByteArrayInputStream. (.getBytes s)))))



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


;;;
;;; Define a function to combine output
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


;;;
;;; -main function
(defn -main
  "-main function to be called when the program is run"
  []
  ;;
  (let [pm-xml-file-path (first *command-line-args*)
        ;; parsed-pm        (->> (slurp pm-xml-file-path)
        ;;                       (zip-str,  )
        ;;                       (first,  )
        ;;                       (:conent,  )
        ;;                       (map parse-pubmed,  ))
        ]
    ;;
    (println pm-xml-file-path)
    ;; write to a csv file
    ;; (with-open [out-file (io/writer (str pm-xml-file-path ".csv"))]
    ;;   (csv/write-csv out-file (map first parsed-pm))))
  )

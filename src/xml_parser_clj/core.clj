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


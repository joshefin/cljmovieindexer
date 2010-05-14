
; Copyright (c) Zeljko Zirikovic. All rights reserved.
; You must not remove this notice, or any other, from this software.

(ns com.revolucion.movieindexer.pdf 
	"MovieIndexer PDF exporter. Experimental." 
	(:use [clojure.contrib.duck-streams :as fileio]
				[clojure.contrib.java-utils :as jutils])
	(:import
		(javax.xml.parsers DocumentBuilderFactory)
		(java.io FileOutputStream StringReader)
		(org.xml.sax InputSource)
		(org.xhtmlrenderer.pdf ITextRenderer)))

; Currently not used.
; Test: (save-as-pdf (jutils/file "C:\\") "fajl.pdf" "<html><body><p>aaaa</p></body></html>")

(defn save-as-pdf [dir file-name text]
	(let [pdf-file (jutils/file dir file-name)
				out (FileOutputStream. pdf-file)
				document (.. DocumentBuilderFactory (newInstance) (newDocumentBuilder) (parse (InputSource. (StringReader. text))))
				renderer (ITextRenderer.)]
		(do
			(.setDocument renderer document nil)
			(.layout renderer)
			(.createPDF renderer out)
			(.close out)
			(println (str "Saved movie description as: " pdf-file)))))


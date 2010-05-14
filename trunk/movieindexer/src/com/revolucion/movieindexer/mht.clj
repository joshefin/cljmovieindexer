
; Copyright (c) Zeljko Zirikovic. All rights reserved.
; You must not remove this notice, or any other, from this software.

(ns com.revolucion.movieindexer.mht
	"MovieIndexer MHT exporter."
	(:use hiccup.core
				clojure.contrib.str-utils
				com.revolucion.movieindexer.tmdb
				[com.revolucion.movieindexer.utils :as utils]
				[clojure.contrib.java-utils :as fio])
	(:require [clojure.contrib.logging :as logger])
	(:import
		(java.io FileOutputStream)
		(javax.mail Session)
		(javax.activation DataHandler FileDataSource)
		(javax.mail.internet MimeBodyPart MimeMultipart MimeMessage)))

(def description-file-name "description.mht")

(defn load-style
	"Reads default style CSS into string." 
	[]
		(slurp 
			(str (fio/file (utils/get-template-dir "default") "style.css")) 
			"UTF-8"))

(defn generate-html
	"Generates HTML document of the movie struct." 
	[movie]
		(do
			(logger/info (str "Generating html for movie '" (get movie :name) "'"))
			(html [:html
				[:head 
					[:meta {:http-equiv "content-type" :content "text/html; charset=utf-8"}]
					[:title (get movie :name "Unknown Title")]
					[:style {:type "text/css" :media "all"} (load-style)]]
				[:body 
					[:div#content_center 
						[:div#maincontent 
							[:div.header
								[:h1 (get movie :name "Unknown Title")]
								[:p (string-from-date (date-from-string (get movie :released)) "yyyy")]]
							[:div.subheader (str-join " <span>|</span> " (get movie :genres))]
							[:div.details
								[:div.cover
									[:img {:src "cid:cover"}]]
								[:div.details_text
									(when-not (empty? (get movie :tagline))
										(list 
											[:h1 "Tagline"]
											[:p (get movie :tagline)]))
									[:h1 "Plot"]
									[:p (get movie :plot "/")]
									[:h1 "Trailer"]
									[:p
										[:a {:href (get movie :trailer)} (get movie :trailer)]]]
								[:div.details_column
									[:h1 "Director"]
									[:p (str-join "<br/>" (get movie :directors))]]
								[:div.details_column
									[:h1 "Cast"]
									[:p (str-join "<br/>" (get movie :actors))]]]
							[:div.footer "Created with " [:b "cljMovieIndexer"]]]]]])))

(defn create-file-body-part
	"Creates part of a MHT using file as a source." 
	[f]
		(let [part (MimeBodyPart.)
					source (FileDataSource. f)]
			(doto part
				(.setHeader "Content-ID" (str "<" (.getName f) ">"))
				(.setFileName (.getName source))
				(.setDataHandler (DataHandler. source)))
			part))

(defn create-and-save
	"Creates HTML document with embeded images and style and saves it as a movie description file.
	Also downloads cover image if available." 
	[[dir movie]]
	  (try 
			(let [movie-html (generate-html movie)
						cover-url (when-not (empty? (get movie :images)) (fio/as-url (first (get movie :images))))
						out-file (fio/file dir description-file-name) 
						out-stream (FileOutputStream. out-file) 
						session (Session/getDefaultInstance (System/getProperties) nil)
						msg (MimeMessage. session)
						msg-body (MimeMultipart.)
						part-content (MimeBodyPart.)
						part-cover (MimeBodyPart.)
						resources (utils/get-files (fio/file (utils/get-template-dir "default") "images"))]
				(do
					(logger/debug "Creating MHT ...")
					(.setContent part-content movie-html "text/html")
					(logger/debug "HTML content set, processing cover ...")
					(when-not (nil? cover-url)
						(logger/info (str "Cover image URL: " cover-url)) 
						(doto part-cover
							(.setHeader "Content-ID" "<cover>")
							(.setDataHandler (DataHandler. cover-url))
							(.setFileName (.getPath cover-url))))
					(logger/debug "Cover stuff done, adding main parts ...")
					(doto msg-body
						(.setSubType "related")
						(.addBodyPart part-content)
						(.addBodyPart part-cover))
					(when-not (and (nil? resources) (empty? resources))
						(logger/debug "Adding resource parts ...")
						(doseq [f resources]
							(.addBodyPart msg-body (create-file-body-part f))))
					(.setContent msg msg-body)
					(logger/info (str "Writing movie '" (get movie :name) "' description to file ..."))
					(.writeTo msg out-stream)
					(.close out-stream)
					(logger/info (str "Saved movie '" (get movie :name) "' description to file " out-file))
					(get movie :name)))
		(catch Exception e 
			(logger/error (str "Can't save movie description. " (.getMessage e))))))



; Copyright (c) Zeljko Zirikovic. All rights reserved.
; You must not remove this notice, or any other, from this software.

(ns com.revolucion.movieindexer.utils 
	"MovieIndexer helper utils." 
  (:use [clojure.contrib.duck-streams :as fileio]
      [clojure.contrib.java-utils :as jutils]))

(defn get-directories
	"Get subdirectories of specified directory." 
	[dir] 
  	(filter #(.isDirectory %) (file-seq dir)))

(defn get-files
	"Get files in specified directory." 
	[dir]
  	(filter #(.isFile %) (file-seq dir)))

(defn get-file-names
	"Get names of files in specified directory." 
	[dir]
  	(map #(.getName %) (get-files dir)))

(defn get-file-name
	"Get file name." 
	[file]
  	(.getName file))

(defn write-to-file
	"Write text to file." 
	[dir file-name text]
  	(fileio/spit (jutils/file dir file-name) text))

(defn get-home-dir
	"Get user home directory." 
	[]
  	(jutils/file (System/getProperty "user.home") "movieindexer"))

(defn get-app-dir
	"Get app home directory." 
	[]
  	(jutils/file (System/getProperty "user.dir")))

(defn get-template-dir
	"Get template directory." 
	[template-dir]
  	(let [dir (jutils/file (get-app-dir) "templates" template-dir)]
			(if (.exists dir)
				dir
				(jutils/file (get-home-dir) "templates" template-dir))))

(defn date-from-string
	"Convert date to string." 
	[s]
  	(.parse (java.text.SimpleDateFormat. "yyyy-mm-DD") s))

(defn string-from-date
	"Convert string to date." 
	[d date-format]
  	(.format (java.text.SimpleDateFormat. date-format) d))

(defn not-empty-dir?
	"Check whether the directory exists and is not empty." 
	[dir]
		(let [dir-file (jutils/as-file dir)]
			(and 
				(not (nil? dir))
				(.exists dir-file)
				(.isDirectory dir-file) 
				(not-empty (file-seq dir-file)))))



; Copyright (c) Zeljko Zirikovic. All rights reserved.
; You must not remove this notice, or any other, from this software.

(ns com.revolucion.movieindexer.main
	"MovieIndexer core."
  (:gen-class) 
  (:use [com.revolucion.movieindexer.utils :as utils]
      [com.revolucion.movieindexer.pdf :as pdf]
      [com.revolucion.movieindexer.tmdb :as moviedb]
      [com.revolucion.movieindexer.mht :as mht-gen]
      [clojure.contrib.java-utils :as jutils]
      [clojure.contrib.command-line])
  (:require [clojure.contrib.logging :as logger])
  (:import (java.util.regex Pattern)))

(def words-to-replace ["\\.", "-", "_", "\\(", "\\)", "\\[", "\\]", "(?i)AC3", "(?i)DTS", "(?i)XVID", "(?i)DIVX", "(?i)H264", "(?i)X264", "(?i)720p", "(?i)1080i", "(?i)1080p", 
     "(?i)HDTV", "(?i)PDTV", "(?i)DVDRip", "(?i)DVDSCR", "(?i)DSRip", "(?i)\\.CAM\\.", "(?i)R5", "(?i)HD2DVD", 
     "(?i)HRHDTV", "(?i)MVCD", "(?i)VCD", "(?i)BLURAY", "(?i)BDRIP", "(?i)BLURAYRIP", "(?i)BLU-RAY", "(?i)HDDVD", "(?i)HDDVDRIP", 
     "(?i)\\.DVD\\.", "aXXo", "(?i)CD\\d", "(?i)DVD\\d", "METiS", "DiVERSE", "VoMiT", "Noir", "NeDiVx", "SiNNERS", 
     "SPRiNTER", "CHD", "FxM", "MAXSPEED", "RELOADED", "AMIABLE", "IMAGiNE", "REFiNED", "CBGB", "FXG", "WiKi", "ViSiON", "CtrlHD", "ESiR", "(?i)TELESYNC", "(?i)SCREENER",
     "(?i)TELECINE", "(?i)BDR", "(?i)UNRATED", "[12]\\d{3}"])

(defn get-movie-dirs
	"Gets all subdirectories of the specified root directory." 
	[root-dir] 
  	(rest (utils/get-directories (jutils/as-file root-dir))))

(defn find-movie-dirs-to-process
	"Gets all movie directories to process. Can skip directories that already contain description." 
	[dir include-existing]
		(let [dirs (get-movie-dirs dir)]
			(if include-existing
				dirs 
				(filter 
			  	#(nil? (some #{description-file-name} (utils/get-file-names %))) 
					dirs))))

(defn process-movie-dirs
	"Creates list of movie names from directory names." 
	[dirs]
	  (let [pattern (->> words-to-replace 
	   (map #(Pattern/compile %)) 
	   (interpose \|) 
	   (apply str))]
	     (logger/info (str "Parsing " (count dirs) " file names ..."))
	     (map #(.trim (.replaceAll % pattern " ")) 
	      (map utils/get-file-name dirs))))

(defn download-movie-desc 
	"Search for movie. If found, gets detailed info."
	[movie-name]
		(let [result (moviedb/search movie-name)]
			(if (nil? result)
				(logger/warn (str "No results for '" movie-name "'"))
	  		(moviedb/info result))))

(defn run 
	"Start processing. Gets directories, downloads info, generate description files and save."
	[root-dir do-update]
		(try 
		  (let [dirs (sort (find-movie-dirs-to-process root-dir do-update))
						mapping (process-movie-dirs dirs)]
		  	(logger/info (str "Root directory: " root-dir))
		   	(logger/info (str "Updating existing ? " do-update))
		   	(logger/info (str "Started processing " (count mapping) " movie files ...")) 
			  (->> mapping 
			  	(pmap download-movie-desc) 
			  	(zipmap dirs)
					(remove #(nil? (val %)))
			  	(pmap mht-gen/create-and-save)
					(doall))) 
		(catch Exception e
			(logger/error (.getMessage e)))
		(finally
			(do 
				(logger/info "All done.") 
				(System/exit 0)))))

; Run from REPL: (-main "-dir" "/home/zeljko/Videos/" "-update" "true")

(defn -main
	"Main method." 
	[& args]
	  (with-command-line args
	      "Movie Indexer\nScans your movie directory, analyzes directory names and collects movie info from online movie database.\nMovie description and cover image is saved in movie directory as a web page archive.\n"
	      [[dir "Directory with movies"]
	       [update? "Update existing descriptions" false]
	       remaining]
			(when-not (empty? args) 
		    (if (utils/not-empty-dir? dir) 
					(run dir update?)
		    	(logger/error "You must specify valid movies directory path.")))))


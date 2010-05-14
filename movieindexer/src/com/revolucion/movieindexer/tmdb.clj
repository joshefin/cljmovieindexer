
; Copyright (c) Zeljko Zirikovic. All rights reserved.
; You must not remove this notice, or any other, from this software.

(ns com.revolucion.movieindexer.tmdb 
	"MovieIndexer TMDB client." 
	(:require [clojure.contrib.http.agent :as httpa]
						[clojure.xml :as xxml]
						[clojure.zip :as zzip]
						[clojure.contrib.zip-filter.xml :as zipf]
						[clojure.contrib.logging :as logger])
	(:import (java.net URL URLEncoder)))

; Using TMDB 2.1 API
(def tmdb-api-url (atom "http://api.themoviedb.org/2.1/"))

; API key ONLY for Movie Indexer app !
(def tmdb-key (atom "620e60652f05bb811b9d3405235515c4"))

(defstruct movie :name :released :imdb :tagline :plot :rating :trailer :genres :images :actors :directors)

(defn process-tmdb-search-response 
	"Extracts movie id from search response."
	[response]
		(let [movie-xml (zzip/xml-zip (xxml/parse response))] 
			(zipf/xml1-> movie-xml :movies :movie :id zipf/text)))

(defn extract-value
	"Get element textual value from the XML document." 
	[m el] 
		(zipf/xml1-> m :movies :movie el zipf/text))

(defn extract-genres
	"Get genre names from the XML response." 
	[m]
		(zipf/xml-> m :movies :movie :categories :category [(zipf/attr= :type "genre")] (zipf/attr :name)))

(defn extract-directors
	"Get movie directors from the XML response." 
	[m]
		(zipf/xml-> m :movies :movie :cast :person [(zipf/attr= :job "Director")] (zipf/attr :name)))

(defn extract-actors
	"Get actors from the XML response." 
	[m]
		(zipf/xml-> m :movies :movie :cast :person [(zipf/attr= :job "Actor")] (zipf/attr :name)))

(defn extract-covers
	"Get all movie images (covers and posters) from the XML response." 
	[m]
		(zipf/xml-> m :movies :movie :images :image [(zipf/attr= :type "poster") (zipf/attr= :size "cover")] (zipf/attr :url)))

(defn process-tmdb-info-response
	"Create movie struct with data extracted from XML response." 
	[response]
		(let [movie-xml (zzip/xml-zip (xxml/parse response))] 
			(reduce 
				(fn [current-movie mapping] 
					(assoc current-movie 
						(key mapping) 
						(cond 
							(= (key mapping) :directors) (extract-directors movie-xml)
							(= (key mapping) :actors) (extract-actors movie-xml)
							(= (key mapping) :images) (extract-covers movie-xml)
							(= (key mapping) :genres) (extract-genres movie-xml)
							:default (extract-value movie-xml (val mapping))))) 
				(struct movie) 
				{:name :name, :released :released, :imdb :imdb_id, :rating :rating, :trailer :trailer, :tagline :tagline, :plot :overview, :genres nil, :images nil, :actors nil, :directors nil})))

(defn search
	"Search TMDB for movies with specified name." 
	[movie-name] 
		(when-not (nil? movie-name)
			(logger/info (str "Searching for '" movie-name "'"))
			(process-tmdb-search-response 
				(httpa/stream 
					(httpa/http-agent 
						(str @tmdb-api-url "Movie.search/en/xml/" @tmdb-key "/" (URLEncoder/encode movie-name "UTF-8"))
						:connect-timeout 10000
						:read-timeout 10000)))))

(defn info
	"Get movie info from TMDB for movie with specified id." 
	[movie-id]
		(when-not (nil? movie-id) 
			(logger/info (str "Get details for " movie-id))
			(process-tmdb-info-response 
				(httpa/stream 
					(httpa/http-agent 
						(str @tmdb-api-url "Movie.getInfo/en/xml/" @tmdb-key "/" movie-id)
						:connect-timeout 10000
						:read-timeout 10000)))))


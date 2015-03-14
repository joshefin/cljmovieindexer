# cljMovieIndexer #

**cljMovieIndexer** scans your movie directory, analyzes directory names and collects movie info from online movie database.
Movie description and cover image is saved in movie directory as a web page archive.

## How to use ? ##

First, you'll need to install latest [Java](http://www.java.com/en/download/).
Then, download **cljMovieIndexer**, unpack it somewhere, go to console/terminal and do something like:

`java -jar movieindexer-standalone.jar -dir YOUR_MOVIES_DIR`

There is a sh script included, so if on Linux, you can run it like: `./run.sh -dir YOUR_MOVIES_DIR`

To update existing description files, add `-update` parameter.

## How does it work ? ##

When you specify directory with your movies, **cljMovieIndexer** scans the directory and extracts movie names from sub-directory names (it will ignore unnecessary words like resolution, scene names, dots etc).
Movie info like release year, plot, actors, cover image etc. is collected from [TMDb](http://www.themoviedb.org/) and saved in each movie directory as a [single file web archive](http://en.wikipedia.org/wiki/MHTML).

You can change the look of the movie description page by editing the template file (_style.css_ in templates directory).

It's written in Clojure, so it works on Windows, Linux & Mac OS. Also, it will use as many cores your CPU has, so it's quite fast.
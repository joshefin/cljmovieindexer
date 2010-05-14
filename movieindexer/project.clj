
(defproject movieindexer "0.1"
    :dependencies [[org.clojure/clojure "1.1.0"]
                   [org.clojure/clojure-contrib "1.1.0"]
									 [org.xhtmlrenderer/core-renderer "R8pre2"]
									 [hiccup "0.2.4"]
									 [com.lowagie/itext "2.1.7" :exclusions [bouncycastle/bcmail-jdk14 bouncycastle/bcprov-jdk14 bouncycastle/bctsp-jdk14]]
									 [log4j/log4j "1.2.16" :exclusions [javax.mail/mail]]
									 [javax.mail/mail "1.4.1" :exclusions [javax.activation/activation]]]
    :main com.revolucion.movieindexer.main)

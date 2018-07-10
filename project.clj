(defproject com.nomistech/clj-utils "0.6.0-SNAPSHOT"
  :description "Simon's Clojure utilities"
  :url "https://github.com/simon-katz/clj-utils"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]]
  :profiles {:dev {:source-paths ["dev"]
                   :dependencies [[nomis-clj-repl-tools "0.1.2"]
                                  [midje "1.9.1"]]
                   :plugins [[lein-midje "3.2.1"]]}})

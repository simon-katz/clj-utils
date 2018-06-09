(ns user
  "Namespace to support hacking at the REPL."
  (:require [clojure.java.javadoc :refer [javadoc]]
            [clojure.pprint :refer [pp pprint]]
            [clojure.repl :refer :all ; [apropos dir doc find-doc pst source]
             ]
            [clojure.tools.namespace.move :refer :all]
            [clojure.tools.namespace.repl :refer :all]
            [midje.repl :refer :all]
            [nomis-clj-repl-tools :refer :all]))

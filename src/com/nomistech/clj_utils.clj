(ns com.nomistech.clj-utils
  (:require [clojure.string :as str]))

;;;; ___________________________________________________________________________
;;;; ---- do1 ----

(defmacro do1
  "Evaluates all the forms and returns the result of the first form."
  {:style/indent 1}
  [form-1 & other-forms]
  `(let [result# ~form-1]
     ~@other-forms
     result#))

;;;; ___________________________________________________________________________
;;;; ---- do2 ----

(defmacro do2
  "Evaluates all the forms and returns the result of the second form."
  {:style/indent 2}
  [form-1 form-2 & other-forms]
  `(do
     ~form-1
     (do1
         ~form-2
       ~@other-forms)))

;;;; ___________________________________________________________________________
;;;; ---- econd ----

(defmacro econd
  "Like `cond`, except throws a RuntimeException if no clause matches."
  [& clauses]
  `(cond ~@clauses
         :else (throw (RuntimeException. "econd has no matching clause"))))

;;;; ___________________________________________________________________________
;;;; Maybe use the following approach instead of `map-keys` and `map-vals`

#_
(->> m
     (map (fn [[k v]] [k (f1 v)]))
     (map (fn [[k v]] [(f2 k) v]))
     (map (fn [[k v]] [(f3 k) (f4 v)]))
     (into {}))

;;;; ___________________________________________________________________________
;;;; ---- map-keys ----

(defn map-keys [f m]
  (into {}
        (for [[k v] m]
          [(f k) v])))

;;;; ___________________________________________________________________________
;;;; ---- map-vals ----

(defn map-vals [f m]
  (into {}
        (for [[k v] m]
          [k (f v)])))

;;;; ___________________________________________________________________________
;;;; ---- map-kv ----

(defn map-kv [f m]
  (into {}
        (for [[k v] m]
          (f k v))))

;;;; ___________________________________________________________________________
;;;; ---- group-by-kv ----

(defn group-by-kv [f m]
  (map-vals #(into {} %)
            (group-by (fn [[k v]] (f k v))
                      m)))

;;;; ___________________________________________________________________________
;;;; ---- group-by-k ----

(defn group-by-k [f m]
  (group-by-kv (fn [k _] (f k))
               m))

;;;; ___________________________________________________________________________
;;;; ---- group-by-v ----

(defn group-by-v [f m]
  (group-by-kv (fn [_ v] (f v))
               m))

;;;; ___________________________________________________________________________
;;;; ---- transitive-closure ----

(defn ^:private transitive-closure-helper [f visited sofar vs]
  (letfn [(helper [visited sofar vs]
            (if (empty? vs)
              sofar
              (let [next-vs (set (->> (mapcat f vs)
                                      (remove visited)
                                      (remove vs)))]
                (recur (into visited next-vs)
                       (into sofar next-vs)
                       next-vs))))]
    (helper visited sofar vs)))

(defn transitive-closure
  "The set of values obtained by starting with v, then applying f to v,
  then applying f to each of the results, and so on. v and all
  intermediate values are included in the result."
  [f v]
  (transitive-closure-helper f #{v} #{v} #{v}))

;;;; ___________________________________________________________________________
;;;; ---- transitive-closure-excluding-self ----

(defn transitive-closure-excluding-self
  "The set of values obtained by applying f to v,
  then applying f to each of the results, and so on. All
  intermediate values are included in the result."
  [f v]
  (transitive-closure-helper f #{v} #{} #{v}))

;;;; ___________________________________________________________________________
;;;; ---- invert-function invert-relation ----

(defn invert-function [f domain-subset]
  "Return a map that represents the inverse of `f`.
  `f` takes elements of `domain-subset` (and possibly other values, not
  relevant here) as argument, and returns a single value.
  For explanations of terminology, see:
    https://www.mathsisfun.com/sets/domain-range-codomain.html"
  (dissoc (group-by f domain-subset)
          nil))

(defn invert-relation [rel domain-subset]
  "Return a map which represents the inverse of `rel`.
  `rel` takes elements of `domain-subset` (and possibly other values, not
  relevant here) as argument, and returns a collection of values.
  For explanations of terminology, see:
    https://www.mathsisfun.com/sets/domain-range-codomain.html"
  (let [domain-range-pairs (for [d domain-subset
                                 r (rel d)]
                             [d r])]
    (reduce (fn [sofar [d r]]
              (update sofar
                      r
                      (fnil conj [])
                      d))
            {}
            domain-range-pairs)))

;;;; ___________________________________________________________________________
;;;; ---- with-extras ----

(defmacro with-extras [[& {:keys [before after]}]
                       & body]
  "Does `before`, then `body`, then `after`. Returns the result of `body`.
  If `body` throws an exception, `after` is still done."
  `(do ~before
       (try (do ~@body)  
            (finally 
              ~after))))

;;;; ___________________________________________________________________________
;;;; ---- member? ----

(defn member? [item coll]
  (some #{item} coll))

;;;; ___________________________________________________________________________
;;;; ---- submap? ----

(defn submap? [m1 m2]
  (= m1 (select-keys m2 (keys m1))))

(defn submap?-v2 [m1 m2]
  (clojure.set/subset? (set m1) (set m2)))

;;;; ___________________________________________________________________________
;;;; ---- deep-merge ----

(defn deep-merge
  "Recursively merges maps. If vals are not maps, the last value wins."
  [& vals]
  (if (every? map? vals)
    (apply merge-with deep-merge vals)
    (last vals)))

;;;; ___________________________________________________________________________
;;;; ---- select-keys-recursively ----

(defn select-keys-recursively
  "Similar to `select-keys`, but with key paths digging in to a nested map.
  `key-paths` is a sequence. Each element of `key-paths` begins with a key, k,
  to be selected and is followed by a vector of key paths that specify the
  things to be selected from the value corresponding to k.
  Example:
      (select-keys-recursively {:k-1 \"v-1\"
                                :k-2 {:k-2-1 \"v-2-1\"
                                      :k-2-2 {:k-2-2-1 \"v-2-2-1\"
                                              :k-2-2-2 \"v-2-2-2\"
                                              :k-2-2-3 \"v-2-2-3\"}}
                                :k-3 \"v-3\"}
                               [[:k-1]
                                [:k-2 [:k-2-2
                                        [:k-2-2-1]
                                        [:k-2-2-3]]]])
      => {:k-1 \"v-1\"
          :k-2 {:k-2-2 {:k-2-2-1 \"v-2-2-1\"
                        :k-2-2-3 \"v-2-2-3\"}}}"
  [m key-paths]
  (or (apply merge
             (for [p key-paths]
               (let [n (count p)]
                 (case n
                   0 (throw (Error. "Empty path in key-paths"))
                   1 (select-keys m [(first p)])
                   (if-not (contains? m (first p))
                     {}
                     {(first p) (select-keys-recursively (get m (first p))
                                                         (rest p))})))))
      {}))

;;;; ___________________________________________________________________________
;;;; ---- indexed ----

(defn indexed
  ;; From http://stackoverflow.com/questions/4830900, with changes.
  "Returns a lazy sequence of [index, item] pairs, where items come
  from 's' and indexes count up from zero.
  eg:
      (indexed '(a b c d))  =>  ([0 a] [1 b] [2 c] [3 d])"
  [s]
  (map-indexed vector s))

;;;; ___________________________________________________________________________
;;;; ---- position ----
;;;; ---- positions ----

(defn positions
  ;; From http://stackoverflow.com/questions/4830900.
  "Returns a lazy sequence containing the positions at which pred
  is true for items in coll."
  [pred coll]
  (for [[idx elt] (indexed coll)
        :when (pred elt)]
    idx))

(defn position
  [pred coll]
  (first (positions pred coll)))

;;;; ___________________________________________________________________________
;;;; ---- unchunk ----

(defn unchunk
  "Produce a fully-lazy sequence from `s`.
  Sometimes Clojure sequences are not fully lazy.
  See http://blog.fogus.me/2010/01/22/de-chunkifying-sequences-in-clojure/
  for details.
  See also Stuart Sierra comment at https://stackoverflow.com/questions/3407876/how-do-i-avoid-clojures-chunking-behavior-for-lazy-seqs-that-i-want-to-short-ci"
  [s]
  ;; TODO Is there a reason to prefer one of these over the other?
  ;;      - Isn't `rest` lazier than `next`?
  ;;        - Add auto-test that would distinguish. (Or do you already have one?
  (case 2
    1 (when (seq s)
        (lazy-seq
         (cons (first s)
               (unchunk (next s)))))
    2 (lazy-seq
       (when-let [[x] (seq s)]
         (cons x (unchunk (rest s)))))))

;;;; ___________________________________________________________________________
;;;; ---- last-index-of-char-in-string ----

(defn last-index-of-char-in-string [^Character char ^String string]
  ;; Effect of type hints:
  ;;   Without:
  ;;     (time (dotimes [i 1000000] (last-index-of-char-in-string \c "abcdef")))
  ;;     "Elapsed time: 2564.688 msecs"
  ;;   With:
  ;;     (time (dotimes [i 1000000] (last-index-of-char-in-string \c "abcdef")))
  ;;     "Elapsed time: 18.44 msecs"
  (.lastIndexOf string (int char)))

;;;; ___________________________________________________________________________
;;;; Detection of Emacs temp files
;;;; - Copied from `stasis.core`.

(def ^:private fsep (java.io.File/separator))
(def ^:private fsep-regex (java.util.regex.Pattern/quote fsep))

(defn ^:private normalize-path [^String path]
  (if (= fsep "/")
    path
    (.replaceAll path fsep-regex "/")))

(defn ^:private get-path [^java.io.File path]
  (-> path
      .getPath
      normalize-path))

(defn ^:private path->filename [^String path]
  (last (str/split path #"/")))

(defn emacs-temp-file-path? [^String path]
  (let [filename (path->filename path)]
    (or (.startsWith filename ".#")
        (and (.startsWith filename "#")
             (.endsWith filename "#")))))

(defn emacs-temp-file? [^java.io.File file]
  (-> file get-path emacs-temp-file-path?))

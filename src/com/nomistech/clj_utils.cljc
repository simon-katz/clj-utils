(ns com.nomistech.clj-utils
  #?(:cljs (:require-macros [com.nomistech.clj-utils]))
  (:require [clojure.pprint :as pp]
            [clojure.set :as set]
            [clojure.string :as str]
            [clojure.walk :as walk]))

;;;; ___________________________________________________________________________
;;;; ---- my-error ----

(defn my-error
  {:deprecated "0.12.0"}
  [cl-format-string & format-args]
  (let [message (apply pp/cl-format nil cl-format-string format-args)]
    #?(:clj  (Error. message)
       :cljs (js/Error. message))))

;;;; ___________________________________________________________________________
;;;; ---- cl-exception ----

(defn cl-exception [cl-format-string & format-args]
  (let [message (apply pp/cl-format nil cl-format-string format-args)]
    #?(:clj  (Exception. message)
       :cljs (js/Error. message))))

(def Exception-or-js-Error
  #?(:clj  Exception
     :cljs js/Error))

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
         :else (throw (cl-exception "econd has no matching clause"))))

;;;; ___________________________________________________________________________
;;;; ---- only-once ----

(defn only-once
  "`f` is a function of no args. Return a function that on its first
  invocation calls `f`, and on subsequent invocations does nothing and
  returns `::already-run`.
  Unlike `memoize`, the function is called only once even if invoked from
  multiple threads at the same time."
  [f]
  (let [done?& (atom false)]
    #(if (compare-and-set! done?& false true)
       (f)
       ::already-run)))

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

(defn map-keys
  "Apply `f` to each of the keys of `m`."
  [f m]
  (into {}
        (for [[k v] m]
          [(f k) v])))

;;;; ___________________________________________________________________________
;;;; ---- map-vals ----

(defn map-vals
  "Apply `f` to each of the vals of `m`."
  [f m]
  (into {}
        (for [[k v] m]
          [k (f v)])))

;;;; ___________________________________________________________________________
;;;; ---- map-kv ----

(defn map-kv
  "Apply `f` to each of the entries of `m`. `f` takes two args, a key and
  a value."
  [f m]
  (into {}
        (for [[k v] m]
          (f k v))))

;;;; ___________________________________________________________________________
;;;; ---- postwalk-applying-to-maps ----

(defn ^:private postwalk-applying-to-maps [entry-fn x]
  (walk/postwalk (fn [xx] (if (map? xx)
                            (into {} (map entry-fn xx))
                            xx))
                 x))

;;;; ___________________________________________________________________________
;;;; ---- map-keys-recursively-applying-to-maps ----

(defn map-keys-recursively-applying-to-maps
  "Walk `x`, applying `f` to the keys of any maps.
  Call `f` when the keys are themselves maps."
  [f x]
  (let [entry-fn (fn [[k v]] [(f k) v])]
    (postwalk-applying-to-maps entry-fn x)))

;;;; ___________________________________________________________________________
;;;; ---- map-keys-recursively ----

(defn map-keys-recursively
  "Walk `x`, applying `f` to the keys of any maps.
  Don't call `f` when the keys are themselves maps."
  [f x]
  (let [entry-fn (fn [[k v]] [(if (map? k)
                                ;; leave untouched; walking deeper levels will
                                ;; deal with this
                                k
                                (f k))
                              v])]
    (postwalk-applying-to-maps entry-fn x)))

;;;; ___________________________________________________________________________
;;;; ---- map-vals-recursively-applying-to-maps ----

(defn map-vals-recursively-applying-to-maps
  "Walk `x`, applying `f` to the vals of any maps.
  Call `f` when the keys are themselves maps."
  [f x]
  (let [entry-fn (fn [[k v]] [k (f v)])]
    (postwalk-applying-to-maps entry-fn x)))

;;;; ___________________________________________________________________________
;;;; ---- map-vals-recursively ----

(defn map-vals-recursively
  "Walk `x`, applying `f` to the vals of any maps.
  Don't call `f` when the keys are themselves maps."
  [f x]
  (let [entry-fn (fn [[k v]] [k
                              (if (map? v)
                                ;; leave untouched; walking deeper levels will
                                ;; deal with this
                                v
                                (f v))])]
    (postwalk-applying-to-maps entry-fn x)))

;;;; ___________________________________________________________________________
;;;; ---- map-kv-recursively ----

(defn map-kv-recursively
  "Walk `x`, applying `f` to any map entries."
  [f x]
  (let [entry-fn (fn [[k v]] (f k v))]
    (postwalk-applying-to-maps entry-fn x)))

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

(defn ^:private transitive-closure-helper [f sofar vs]
  (letfn [(helper [sofar vs]
            (if (empty? vs)
              sofar
              (let [;; If `vs` is not a set in the call of 'mapcat`, computation
                    ;; takes ages with "large" input. I don't understand why.
                    vs (set vs)
                    next-vs (->> (mapcat f vs)
                                 (remove vs)
                                 (remove sofar))]
                (recur (into sofar next-vs)
                       next-vs))))]
    (helper sofar vs)))

(defn transitive-closure
  "The set of values obtained by starting with v, then applying f to v,
  then applying f to each of the results, and so on. v and all
  intermediate values are included in the result."
  [f v]
  (transitive-closure-helper f #{v} #{v}))

;;;; ___________________________________________________________________________
;;;; ---- transitive-closure-excluding-self ----

(defn transitive-closure-excluding-self
  "The set of values obtained by applying f to v,
  then applying f to each of the results, and so on. All
  intermediate values are included in the result."
  [f v]
  (transitive-closure-helper f #{} #{v}))

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

(defn fun-with-extras [before-f after-f finally-f f]
  (when before-f (before-f))
  (let [done-after?& (atom false)]
    (try (let [result (f)]
           (when after-f
             (try (after-f result)
                  (finally (reset! done-after?& true))))
           result)
         (finally
           (when (and after-f (not @done-after?&))
             (after-f ::non-local-exit))
           (when finally-f (finally-f))))))

(defmacro with-extras
  "Does `before`, then `body`, then `after`. Returns the result of `body`.
  Within `after`, the result of `body` is available as `%result%`. In
  the event of a non-local exit, the value of `%result%` is
  `::non-local-exit`."
  {:style/indent 1}
  [{:keys [before after finally]}
   & body]
  (when finally (println "WARNING: In `with-extras`: `:finally` is deprecated"))
  `(fun-with-extras ~(when before `(fn [] ~before))
                    ~(when after `(fn [~'%result%] ~after))
                    ~(when finally `(fn [] ~finally))
                    (fn [] ~@body)))

;;;; ___________________________________________________________________________
;;;; ---- member? ----

(defn member? [item coll]
  (some #{item} coll))

;;;; ___________________________________________________________________________
;;;; ---- submap? ----

(defn submap? [m1 m2]
  (= m1 (select-keys m2 (keys m1))))

(defn submap?-v2 [m1 m2]
  (set/subset? (set m1) (set m2)))

;;;; ___________________________________________________________________________
;;;; ---- deep-merge ----

(defn deep-merge
  "Recursively merges maps.
  `nil` is treated as an empty map (to match the behaviour of `merge`).
  If vals are not maps, the last value wins."
  [& vals]
  (let [vals (replace {nil {}} vals)]
    (if (every? map? vals)
      (apply merge-with deep-merge vals)
      (last vals))))

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
                   0 (throw (cl-exception "Empty path in key-paths"))
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
  "Return a lazy sequence of [index, item] pairs, where items come
  from 'coll' and indexes count up from zero.
  eg:
      (indexed '(a b c d))  =>  ([0 a] [1 b] [2 c] [3 d])"
  [coll]
  (map-indexed vector coll))

;;;; ___________________________________________________________________________
;;;; ---- position ----
;;;; ---- positions ----

(defn positions
  ;; From http://stackoverflow.com/questions/4830900.
  "Return a lazy sequence containing the positions at which pred
  is true for items in coll."
  [pred coll]
  (for [[idx elt] (indexed coll)
        :when (pred elt)]
    idx))

(defn position
  "Return the index of the first item in 'coll' that satisfies 'pred',
  or nil if there is no such item."
  [pred coll]
  (first (positions pred coll)))

;;;; ___________________________________________________________________________
;;;; ---- drop-nth ----

(defn drop-nth
  ;; From https://stackoverflow.com/a/24553906/2148181
  "Return a lazy sequence of all items in coll apart from the n'th item."
  [n coll]
  (keep-indexed #(if (= %1 n) nil %2)
                coll))

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

(defn last-index-of-char-in-string [char string]
  (or (str/last-index-of string char)
      ;; Make this -1 for backwards-compatibility.
      -1))

;;;; ___________________________________________________________________________
;;;; ---- limiting-n-executions ----

;;;; Functionality for limiting the number of concurrent executions of a
;;;; piece or pieces of code.

(def ^:private limiter-id->n-executions
  (atom {}))

(defn limiting-n-executions
  "If we currently do not have too many concurrent executions, call `fun`.
  Otherwise call `fun-when-too-many`.
  Not having too many concurrent executions is defined like this:
  - consider the number of current executions of this function that share the
    supplied `limiter-id`
  - this number must be less than `max-executions`."
  [limiter-id
   max-executions
   fun
   fun-when-too-many]
  (let [m (swap! limiter-id->n-executions update limiter-id (fnil inc 0))]
    (try (if (<= (get m limiter-id)
                 max-executions)
           (fun)
           (fun-when-too-many))
         (finally (swap! limiter-id->n-executions update limiter-id dec)))))

;;;; ___________________________________________________________________________
;;;; ---- with-return-429-if-too-many-requests ----

(defn ^:private retry-after-value-for-429-responses
  "Return a random integer that is between the min and max retry-after values,
  inclusive."
  [min max]
  (+ min (rand-int (inc (- max min)))))

(defn fun-with-return-429-if-too-many-requests
  "`options` is a map with the following keys:
    - :limiter-id
    - :max-concurrent-requests
    - :min-retry-after-secs
    - :max-retry-after-secs
  If we currently do not have too many concurrent executions, call `fun`.
  Otherwise return a Ring response map with status 429 and a Retry-After value
  that is between `min-retry-after-secs` and `max-retry-after-secs`.
  Not having too many concurrent executions is defined like this:
  - consider the number of current executions of this function that share the
    supplied `limiter-id`
  - this number must be less than `max-concurrent-requests`."
  [options fun]
  (let [{:keys [limiter-id
                max-concurrent-requests
                min-retry-after-secs
                max-retry-after-secs]} options]
    (assert (not (nil? limiter-id)))
    (assert (not (nil? max-concurrent-requests)))
    (assert (not (nil? min-retry-after-secs)))
    (assert (not (nil? max-retry-after-secs)))
    (let [retry-after-value (retry-after-value-for-429-responses
                             min-retry-after-secs
                             max-retry-after-secs)]
      (limiting-n-executions limiter-id
                             max-concurrent-requests
                             fun
                             (fn []
                               {:status 429
                                :headers {"Retry-After" (str
                                                         retry-after-value)}
                                :body nil})))))

(defmacro with-return-429-if-too-many-requests
  "Macro wrapper for `fun-with-return-429-if-too-many-requests`."
  {:style/indent 1}
  [options & body]
  `(fun-with-return-429-if-too-many-requests ~options
                                             (fn [] ~@body)))

;;;; ___________________________________________________________________________
;;;; Detection of Emacs temp files
;;;; - Copied from `stasis.core`.

#?(:clj
   (do
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
     ))

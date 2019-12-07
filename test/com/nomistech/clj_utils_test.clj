(ns com.nomistech.clj-utils-test
  (:require [clojure.core.async :as a]
            [com.nomistech.clj-utils :as sut]
            [compojure.core :refer [GET]]
            [midje.sweet :refer :all]
            [ring.mock.request :as mock]))

;;;; ___________________________________________________________________________
;;;; ---- sut/do1 ----

(fact "`sut/do1` works"

  (fact "Fails to compile when there are no forms"
    (macroexpand-1 '(sut/do1))
    => (throws clojure.lang.ArityException))

  (fact "Returns value of first form when there is one form"
    (sut/do1 :a)
    => :a)

  (fact "Returns value of first form when there are two forms"
    (sut/do1
        :a
      :b)
    => :a)

  (fact "Returns value of first form when there are three forms"
    (sut/do1
        :a
      :b
      :c)
    => :a)

  (fact "Forms are evaluated in correct order"
    (let [side-effect-place (atom [])]
      (sut/do1
          (swap! side-effect-place conj 1)
        (swap! side-effect-place conj 2)
        (swap! side-effect-place conj 3))
      => anything
      (fact
        @side-effect-place => [1 2 3]))))

;;;; ___________________________________________________________________________
;;;; ---- sut/do2 ----

(fact "`sut/do2` works"

  (fact "Fails to compile when there are no forms"
    (macroexpand-1 '(sut/do2))
    => (throws clojure.lang.ArityException))

  (fact "Fails to compile when there is one forms"
    (macroexpand-1 '(sut/do2 :a))
    => (throws clojure.lang.ArityException))

  (fact "Returns value of second form when there are two forms"
    (sut/do2
        :a
        :b)
    => :b)

  (fact "Returns value of second form when there are three forms"
    (sut/do2
        :a
        :b
      :c)
    => :b)

  (fact "Forms are evaluated in correct order"
    (let [side-effect-place (atom [])]
      (sut/do2
          (swap! side-effect-place conj 1)
          (swap! side-effect-place conj 2)
        (swap! side-effect-place conj 3))
      => anything
      (fact
        @side-effect-place => [1 2 3]))))

;;;; ___________________________________________________________________________
;;;; ---- sut/econd ----

(fact "`sut/econd` works"
  (fact "no clauses"
    (sut/econd)
    => (throws "econd has no matching clause"))
  (fact "many clauses"
    (fact "last clause truthy"
      (sut/econd false 1
                 nil   2
                 :this-one 3)
      => 3)
    (fact "non-last clause truthy"
      (sut/econd false 1
                 nil   2
                 :this-one 3
                 :not-this-one 4)
      => 3)
    (fact "none truthy"
      (sut/econd false 1
                 nil   2
                 false 3
                 nil   4)
      => (throws "econd has no matching clause"))))

;;;; ___________________________________________________________________________
;;;; ---- sut/map-keys ----

(fact "`sut/map-keys` works"
  (sut/map-keys keyword
                {"a" 1
                 "b" 2})
  => {:a 1
      :b 2})

;;;; ___________________________________________________________________________
;;;; ---- sut/map-vals ----

(fact "`sut/map-vals` works"
  (sut/map-vals inc
                {:a 1
                 :b 2})
  => {:a 2
      :b 3})

;;;; ___________________________________________________________________________
;;;; ---- sut/map-kv ----

(fact "`sut/map-kv` works"
  (sut/map-kv (fn [k v] [(keyword k)
                         (inc v)])
              {"a" 1
               "b" 2})
  => {:a 2
      :b 3})

;;;; ___________________________________________________________________________
;;;; ---- sut/map-keys-recursively-applying-to-maps ----

(fact "`sut/map-keys-recursively-applying-to-maps` works"
  (sut/map-keys-recursively-applying-to-maps
   (fn [x] (if (map? x)
             (assoc x :transformed? true)
             (keyword x)))
   {"a" 1
    "b" {"c" 3
         "d" {"e" 5}}
    {"f" 0} {{"g" 0} 7}})
  => {:a 1
      :b {:c 3
          :d {:e 5}}
      {:f 0
       :transformed? true} {{:g 0
                             :transformed? true} 7}})

;;;; ___________________________________________________________________________
;;;; ---- sut/map-keys-recursively ----

(fact "`sut/map-keys-recursively` works"
  (sut/map-keys-recursively keyword
                            {"a" 1
                             "b" {"c" 3
                                  "d" {"e" 5}}
                             {"f" 0} {{"g" 0} 7}})
  => {:a 1
      :b {:c 3
          :d {:e 5}}
      {:f 0} {{:g 0} 7}})

;;;; ___________________________________________________________________________
;;;; ---- sut/map-vals-recursively-applying-to-maps ----

(fact "`sut/map-vals-recursively-applying-to-maps` works"
  (sut/map-vals-recursively-applying-to-maps
   (fn [x] (if (map? x)
             (assoc x :transformed? true)
             (inc x)))
   {:a 1
    :b {:c 3
        :d {:e 5}}
    {:f 0} {{:g 0} 7}})
  => {:a 2
      :b {:c 4
          :d {:e 6
              :transformed? true}
          :transformed? true}
      {:f 1} {{:g 1} 8
              :transformed? true}})

;;;; ___________________________________________________________________________
;;;; ---- sut/map-vals-recursively ----

(fact "`sut/map-vals-recursively` works"
  (sut/map-vals-recursively inc
                            {:a 1
                             :b {:c 3
                                 :d {:e 5}}
                             {:f 0} {{:g 0} 7}})
  => {:a 2
      :b {:c 4
          :d {:e 6}}
      {:f 1} {{:g 1} 8}})

;;;; ___________________________________________________________________________
;;;; ---- sut/map-kv-recursively ----

(fact "`sut/map-kv-recursively` works"
  (sut/map-kv-recursively (fn [k v] [(if (map? k) k (keyword k))
                                     (if (map? v) v (inc v))])
                          {"a" 1
                           "b" {"c" 3
                                "d" {"e" 5}}
                           {"f" 0} {{"g" 0} 7}})
  => {:a 2
      :b {:c 4
          :d {:e 6}}
      {:f 1} {{:g 1} 8}})

;;;; ___________________________________________________________________________
;;;; ---- sut/group-by-kv ----

(fact "`sut/group-by-kv` works"

  (fact "keys"
    (sut/group-by-kv (fn [k v] (rem k 3))
                     {0 :zero
                      1 :one
                      2 :two
                      3 :three
                      4 :four
                      5 :five})
    => {0 {0 :zero
           3 :three}
        1 {1 :one
           4 :four}
        2 {2 :two
           5 :five}})

  (fact "vals"
    (sut/group-by-kv (fn [k v] (rem v 3))
                     {:zero  0
                      :one   1
                      :two   2
                      :three 3
                      :four  4
                      :five  5})
    => {0 {:zero  0
           :three 3}
        1 {:one  1
           :four 4}
        2 {:two  2
           :five 5}}))

;;;; ___________________________________________________________________________
;;;; ---- sut/group-by-k ----

(fact "`sut/group-by-k` works"
  (sut/group-by-k (fn [k] (rem k 3))
                  {0 :zero
                   1 :one
                   2 :two
                   3 :three
                   4 :four
                   5 :five})
  => {0 {0 :zero
         3 :three}
      1 {1 :one
         4 :four}
      2 {2 :two
         5 :five}})

;;;; ___________________________________________________________________________
;;;; ---- sut/group-by-v ----

(fact "`sut/group-by-v` works"
  (sut/group-by-v (fn [v] (rem v 3))
                  {:zero  0
                   :one   1
                   :two   2
                   :three 3
                   :four  4
                   :five  5})
  => {0 {:zero  0
         :three 3}
      1 {:one  1
         :four 4}
      2 {:two  2
         :five 5}})

;;;; ___________________________________________________________________________
;;;; ---- sut/transitive-closure ----

(fact "`sut/transitive-closure` works"

  (fact "A function that returns no values"
    (letfn [(f [_] [])]
      (fact (sut/transitive-closure f 0) => #{0})))

  (fact "The identity function (with result as a singleton collection)"
    (letfn [(f [x] [x])]
      (fact (sut/transitive-closure f 1) => #{1})))

  (fact "A function that returns a single value"
    (letfn [(f [x] (filter #(< % 1000) [(* x 10)]))]
      (fact (sut/transitive-closure f 0) => #{0})
      (fact (sut/transitive-closure f 1) => #{1 10 100})))

  (fact "A function that returns its arg and sometimes another value"
    (letfn [(f [x] (filter #(< % 1000) [x
                                        (* x 10)]))]
      (fact (sut/transitive-closure f 0) => #{0})
      (fact (sut/transitive-closure f 1) => #{1 10 100})))

  (fact "A function that returns either its arg or two other values"
    (letfn [(f [x] (filter #(< % 1000) [(* x 10) (* x 20)]))]
      (fact (sut/transitive-closure f 0) => #{0})
      (fact (sut/transitive-closure f 1) => #{1 10 20 100 200 400}))))

;;;; ___________________________________________________________________________
;;;; ---- sut/transitive-closure-excluding-self ----

;; TODO: More substantial tests of `sut/transitive-closure-excluding-self`,
;;       e.g. edge cases.

(fact "`sut/transitive-closure-excluding-self` works"
  (fact "test #1"
    (sut/transitive-closure-excluding-self (fn [x]
                                             (case x
                                               1 [1 2 3 4]
                                               2 [200 1]
                                               3 [300 1]
                                               4 [1]
                                               []))
                                           1)
    => #{1 2 3 4 200 300})
  (fact "test #2"
    (sut/transitive-closure-excluding-self (fn [x]
                                             (case x
                                               1 [2 3 4]
                                               2 [200]
                                               3 [300]
                                               []))
                                           1)
    => #{2 3 4 200 300}))

;;;; ___________________________________________________________________________
;;;; ---- sut/invert-function sut/invert-relation ----

(fact "`sut/invert-function` works"
  (sut/invert-function {:a 1
                        :b 2
                        :c 3
                        :d 1}
                       [:a :b :c :d :e])
  =>
  {1 [:a :d]
   2 [:b]
   3 [:c]})

(fact "`sut/invert-relation` works"
  (sut/invert-relation {:a [1 2]
                        :b [2 3]
                        :c []
                        :d [2]}
                       [:a :b :c :d :e])
  =>
  {1 [:a]
   2 [:a :b :d]
   3 [:b]})

;;;; ___________________________________________________________________________
;;;; ---- sut/with-extras ----

(fact "`sut/with-extras` works"

  (fact "without an exception"
    (let [side-effect-place (atom [])]
      (fact "Value is correct"
        (sut/with-extras {:before (swap! side-effect-place conj 1)
                          :after  (swap! side-effect-place conj %result%)}
          (do (swap! side-effect-place conj 2)
              (swap! side-effect-place conj 3)
              4))
        => 4)
      (fact "Forms are evaluated in correct order"
        @side-effect-place => [1 2 3 4])))

  (fact "with an exception"
    (let [side-effect-place (atom [])]
      (fact "throws"
        (sut/with-extras {:before (swap! side-effect-place conj 1)
                          :after  (swap! side-effect-place conj %result%)}
          (do (swap! side-effect-place conj 2)
              (/ 0 0)
              (swap! side-effect-place conj 3)
              4))
        => throws)
      (fact "`after` not done"
        @side-effect-place => [1 2]))))

;;;; ___________________________________________________________________________
;;;; ---- sut/member? ----

(fact "`sut/member?` works"
  (fact "Returns truthy if the item is in the collection"
    (sut/member? :b [:a :b :c]) => truthy)
  (fact "Returns falsey if the item is not in the collection"
    (sut/member? :d []) => falsey
    (sut/member? :d [:a :b :c]) => falsey))

;;;; ___________________________________________________________________________
;;;; ---- sut/submap? ----

(fact "`sut/submap?` works"
  (do
    (fact (sut/submap? {}     {}) => true)
    (fact (sut/submap? {:a 1} {}) => false))
  (do
    (fact (sut/submap? {}               {:a 1 :b 2}) => true)
    (fact (sut/submap? {:a 1}           {:a 1 :b 2}) => true)
    (fact (sut/submap? {:a 1 :b 2}      {:a 1 :b 2}) => true))
  (do
    (fact (sut/submap? {:a 1 :b 2 :c 3} {:a 1 :b 2}) => false)
    (fact (sut/submap? {:a 9}           {:a 1 :b 2}) => false)
    (fact (sut/submap? {:a 9 :b 2}      {:a 1 :b 2}) => false)))

(fact "`sut/submap?-v2` works
  (do
    (fact (sut/submap?-v2 {}     {}) => true)
    (fact (sut/submap?-v2 {:a 1} {}) => false))"
  (do
    (fact (sut/submap?-v2 {}               {:a 1 :b 2}) => true)
    (fact (sut/submap?-v2 {:a 1}           {:a 1 :b 2}) => true)
    (fact (sut/submap?-v2 {:a 1 :b 2}      {:a 1 :b 2}) => true))
  (do
    (fact (sut/submap?-v2 {:a 1 :b 2 :c 3} {:a 1 :b 2}) => false)
    (fact (sut/submap?-v2 {:a 9}           {:a 1 :b 2}) => false)
    (fact (sut/submap?-v2 {:a 9 :b 2}      {:a 1 :b 2}) => false)))

;;;; ___________________________________________________________________________
;;;; ---- sut/deep-merge ----

(fact "`sut/deep-merge` works"

  (fact "non-conflicting merge"
    (sut/deep-merge {:a 1
                     :b 2}
                    {:c 3})
    => {:a 1
        :b 2
        :c 3})

  (fact "replacing merge"
    (sut/deep-merge {:a 1
                     :b {:bb 22}}
                    {:b 999})
    => {:a 1
        :b 999})

  (fact "deep merge"
    (sut/deep-merge {:a 1
                     :b {:bb 22}}
                    {:b {:ba 21
                         :bb 999}})
    => {:a 1
        :b {:ba 21
            :bb 999}})

  (fact "merge in an empty map"
    (sut/deep-merge {:a 1 :b {:bb 22}}
                    {:b {}})
    => {:a 1 :b {:bb 22}})

  (fact "merge multiple maps"
    (sut/deep-merge {:a 1 :b 2 :c 3}
                    {:a 11 :b 12}
                    {:a 101})
    => {:a 101 :b 12 :c 3})

  (fact "`nil` is treated as an empty map at top level"
    (sut/deep-merge nil
                    {:a 1
                     :b 2}
                    nil
                    {:c 3}
                    nil)
    => {:a 1
        :b 2
        :c 3})

  (fact "`nil` is treated as an empty map at non-top level"
    (sut/deep-merge {:b nil}
                    {:a 1
                     :b {:bb 22}}
                    {:b nil})
    => {:a 1
        :b {:bb 22}}))

;;;; ___________________________________________________________________________
;;;; ---- sut/select-keys-recursively ----

(fact "`sut/select-keys-recursively` works"

  (let [m {:k-1 "v-1"
           :k-2 {:k-2-1 "v-2-1"
                 :k-2-2 {:k-2-2-1 "v-2-2-1"
                         :k-2-2-2 "v-2-2-2"
                         :k-2-2-3 "v-2-2-3"}}
           :k-3 "v-3"}]

    (fact
      (sut/select-keys-recursively m [])
      => {})

    (fact
      (sut/select-keys-recursively m [[]])
      => (throws))

    (fact
      (sut/select-keys-recursively m [[:no-such-key]])
      => {})

    (fact
      (sut/select-keys-recursively m [[:k-1]])
      => {:k-1 "v-1"})

    (fact
      (sut/select-keys-recursively m [[:k-1]
                                      [:k-2]])
      => {:k-1 "v-1"
          :k-2 {:k-2-1 "v-2-1"
                :k-2-2 {:k-2-2-1 "v-2-2-1"
                        :k-2-2-2 "v-2-2-2"
                        :k-2-2-3 "v-2-2-3"}}})

    (fact
      (sut/select-keys-recursively m [[:k-1]
                                      [:k-2 [:k-2-2
                                             [:k-2-2-1]
                                             [:k-2-2-3]]]])
      => {:k-1 "v-1"
          :k-2 {:k-2-2 {:k-2-2-1 "v-2-2-1"
                        :k-2-2-3 "v-2-2-3"}}})))

;;;; ___________________________________________________________________________
;;;; ---- sut/indexed ----

(fact "`sut/indexed` works"
  (sut/indexed [:a :b :c :d])
  => [[0 :a]
      [1 :b]
      [2 :c]
      [3 :d]])

;;;; ___________________________________________________________________________
;;;; ---- sut/position ----
;;;; ---- sut/positions ----

(fact "sut/`position` and sut/`positions` work"
  (fact "`sut/position` tests"
    (sut/position even? []) => nil
    (sut/position even? [12]) => 0
    (sut/position even? [11 13 14]) => 2
    (sut/position even? [11 13 14 14]) => 2)
  (fact "`sut/positions` tests"
    (sut/positions even? []) => []
    (sut/positions even? [12]) => [0]
    (sut/positions even? [11 13 14]) => [2]
    (sut/positions even? [11 13 14 14 15]) => [2 3]))

;;;; ___________________________________________________________________________
;;;; ---- sut/unchunk ----

(defn fun-with-return-args-to-even?-and-identity [fun]
  (let [clj-even?     even?
        clj-identity  identity
        even?-acc     (atom [])
        identity?-acc (atom [])]
    (with-redefs [even? (fn [x]
                          (swap! even?-acc conj x)
                          (clj-even? x))
                  identity (fn [x]
                             (swap! identity?-acc conj x)
                             x)]
      (fun)
      [@even?-acc
       @identity?-acc])))

(defmacro with-return-args-to-even?-and-identity
  "Helper for testing `sut/unchunk`.
  Execute `body` in a scope that redefines `even?` and `identity to make a note
  of their argument. After executing `body`, return a vector whose first element
  is a sequence of all the arguments passed to `even?` (in successive calls)
  and whose second element is a sequence of all the arguments passed to
  `identity` (in successive calls)."
  [& body]
  `(fun-with-return-args-to-even?-and-identity (fn [] ~@body)))

(fact "`sut/unchunk` works"
  (let [my-chunked-seq (range 10)]

    (fact "`my-chunked-seq` is indeed chunked"
      (fact (chunked-seq? my-chunked-seq) => true)
      (fact (chunked-seq? (-> my-chunked-seq rest)) => true)
      (fact (chunked-seq? (-> my-chunked-seq rest rest)) => true))

    (fact "`(sut/unchunk my-chunked-seq)` is not chunked"
      (fact (chunked-seq? (sut/unchunk my-chunked-seq)) => false)
      (fact (chunked-seq? (-> (sut/unchunk my-chunked-seq) rest)) => false)
      (fact (chunked-seq? (-> (sut/unchunk my-chunked-seq) rest rest)) => false))

    (fact "sut/unchunking a sequence doesn't change its value"
      (= my-chunked-seq
         (sut/unchunk my-chunked-seq))))

  (fact "A more explicit exploration of the values that are realised"
    (fact "Without `sut/unchunk`, `map` consumes elements we don't ultimately need"
      (with-return-args-to-even?-and-identity
        (every? even?
                (map identity [2 4 6 7 8 10])))
      =>
      [[2 4 6 7]
       [2 4 6 7 8 10]])
    (fact "With `sut/unchunk`, `map` only consumes elements we ultimately need"
      (with-return-args-to-even?-and-identity
        (every? even?
                (map identity
                     (sut/unchunk [2 4 6 7 8 10]))))
      =>
      [[2 4 6 7]
       [2 4 6 7]])))

;;;; ___________________________________________________________________________
;;;; ---- sut/last-index-of-char-in-string ----

(fact "`sut/last-index-of-char-in-string` works"
  (fact (sut/last-index-of-char-in-string \c "") => -1
    (sut/last-index-of-char-in-string \c "xyz") => -1
    (sut/last-index-of-char-in-string \c "c") => 0
    (sut/last-index-of-char-in-string \c "abc") => 2
    (sut/last-index-of-char-in-string \c "abcde") => 2
    (sut/last-index-of-char-in-string \c "abcce") => 3))

;;;; ___________________________________________________________________________
;;;; ---- limiting-n-executions ----

(fact "`sut/limiting-n-executions` works"
  ;; Do the following:
  ;; - Set up the maximum number of concurrent executions, and have them block.
  ;; - Try another concurrent execution and check the result.
  ;; - Tidy up, checking the results of all the concurrent executions.
  (let [n-execs-in-this-test (atom 0)]
    (let [max-concurrent-requests-for-test 5
          unblock-chan (a/chan)
          results-of-blocking-requests (atom [])
          limiter-id (gensym)]
      (letfn [(do-call-with-limited-executions-wrapping []
                (sut/limiting-n-executions
                 limiter-id
                 max-concurrent-requests-for-test
                 (fn []
                   ;; Block until we are told to unblock.
                   (swap! n-execs-in-this-test inc)
                   (a/<!! unblock-chan)
                   (swap! n-execs-in-this-test dec)
                   :not-too-many-executions)
                 (fn []
                   :too-many-executions)))
              (wait-for [f]
                (loop []
                  (when-not (f)
                    (Thread/sleep 100)
                    (recur))))
              (wait-for-blocking-requests-to-be-in-progress []
                (wait-for #(>= @n-execs-in-this-test
                               max-concurrent-requests-for-test)))
              (wait-for-blocking-requests-to-be-completed []
                (wait-for #(zero? @n-execs-in-this-test)))
              (setup-concurrent-blocking-requests []
                (dotimes [_ max-concurrent-requests-for-test]
                  (future (swap! results-of-blocking-requests
                                 conj
                                 (do-call-with-limited-executions-wrapping))))
                (wait-for-blocking-requests-to-be-in-progress))
              (complete-the-blocked-requests []
                (dotimes [_ max-concurrent-requests-for-test]
                  (a/>!! unblock-chan :unblock))
                (wait-for-blocking-requests-to-be-completed)
                (assert (= @results-of-blocking-requests
                           (repeat max-concurrent-requests-for-test
                                   :not-too-many-executions))
                        "The blocked requests have all been unblocked"))
              (tidy-up []
                (complete-the-blocked-requests)
                (a/close! unblock-chan))]

        (setup-concurrent-blocking-requests)
        (fact "We avoid too many concurrent executions"
          (do-call-with-limited-executions-wrapping)
          => :too-many-executions)

        (tidy-up)))))

;;;; ___________________________________________________________________________
;;;; ---- with-return-429-if-too-many-requests ----

(fact "`sut/retry-after-value-for-429-responses` returns a value between the min and max inclusive"
  (let [min-retry-after-secs 10
        max-retry-after-secs 20
        retry-after-value-ok? (fn [x]
                                (<= min-retry-after-secs
                                    x
                                    max-retry-after-secs))]
    (dotimes [i 100] ; check many times because we have randomness
      (#'sut/retry-after-value-for-429-responses min-retry-after-secs
                                                 max-retry-after-secs)
      => retry-after-value-ok?)))

(fact "`sut/with-return-429-if-too-many-requests` works"
  ;; Keep this test simple by setting max requests to zero and one.
  (let [retry-after-secs-for-429-response-tests
        1234
        ;;
        expected-429-response-for-tests
        {:status  429
         :headers {"Retry-After"  (str retry-after-secs-for-429-response-tests)}
         :body    nil}
        ;;
        make-test-handler
        (fn [max-concurrent-requests]
          (GET "/limited-url" []
            (sut/with-return-429-if-too-many-requests
                {:limiter-id              (gensym)
                 :max-concurrent-requests max-concurrent-requests
                 :min-retry-after-secs    retry-after-secs-for-429-response-tests
                 :max-retry-after-secs    retry-after-secs-for-429-response-tests}
              "a response")))
        ;;
        do-request
        (fn [max-concurrent-requests]
          (let [test-handler (make-test-handler max-concurrent-requests)]
            (test-handler (mock/request :get "/limited-url"))))]

    (fact "max requests exceeded"
      (do-request 0)
      => expected-429-response-for-tests)

    (fact "max requests not exceeded"
      (do-request 1)
      => {:status 200
          :headers {"Content-Type" "text/html; charset=utf-8"}
          :body "a response"})))

;;;; ___________________________________________________________________________
;;;; Detection of Emacs temp files

(fact "Emacs temp files"

  (fact "sut/emacs-temp-file-path?"

    (fact "lock file"
      (sut/emacs-temp-file-path? "/a/b/.#foo")
      => truthy)

    (fact "autosave file"
      (sut/emacs-temp-file-path? "/a/b/#foo#")
      => truthy)

    (fact "ordinary file"
      (sut/emacs-temp-file-path? "/a/b/foo")
      => falsey))

  (fact "emacs-temp-file?"

    (fact "lock file"
      (sut/emacs-temp-file? (java.io.File. "/a/b/.#foo"))
      => truthy)

    (fact "autosave file"
      (sut/emacs-temp-file? (java.io.File. "/a/b/#foo#"))
      => truthy)

    (fact "ordinary file"
      (sut/emacs-temp-file? (java.io.File. "/a/b/foo"))
      => falsey)))

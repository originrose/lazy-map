(ns lazy-map.core-test
  (:require [lazy-map.core :refer :all]
            [clojure.test :refer :all]))

(defmacro error
  [& [msg]]
  `(let [msg# ~msg]
     (throw (if msg#
              (AssertionError. msg#)
              (AssertionError.)))))

(deftest map-keys-test
  (is (= (map-keys name {:a 1 :b 2})
         {"a" 1 "b" 2}))
  (is (= (map-keys inc {1 2 3 4})
         {2 2 4 4}))
  (is (= (map-keys #(error "key fn should not be called") {})
         {})))

(deftest map-vals-test
  (is (= (map-vals dec {:a 1 :b 2})
         {:a 0 :b 1}))
  (is (= (map-vals #(cons 0 %) {[1 2] [3 4] [5 6] [7 8]})
         {[1 2] [0 3 4] [5 6] [0 7 8]})))

(deftest map-entry-test
  (is (= (key (map-entry :a :b)) :a))
  (is (= (val (map-entry :a :b)) :b))
  (is (= (vec (map-entry nil nil)) [nil nil])))

(defn make-lazy-map
  []
  (->LazyMap
    {:a (delay (error "value :a should not be realized"))
     :b 50
     :c (delay (error "value :c should not be realized"))}))

(deftest lazy-map-test
  (testing "lazy maps are lazy"
    (is (= (:b (make-lazy-map))
           50))
    (is (= (with-out-str
             (:c (->LazyMap
                   {:a (delay (error "value :a should not be realized"))
                    :b 50
                    :c (delay (print "value :c was realized"))})))
           "value :c was realized")))
  (testing "lazy maps can be called as fns"
    (is (= (let [m (->LazyMap
                     {:a 1
                      :b 2})]
             (m :b))
           2)))
  (testing "keys and vals work on lazy maps"
    (is (= (set
             (keys (make-lazy-map)))
           #{:a :b :c}))
    (is (= (->> (->LazyMap
                  {:a (delay (println "value :a was realized"))
                   :b (delay (println "value :b was realized"))
                   :c (delay (println "value :c was realized"))})
             (vals)
             (take 2)
             (dorun)
             (with-out-str)
             (re-seq #"value :[a-c] was realized")
             (count))
           2)))
  (testing "assoc and dissoc work with lazy maps"
    (is (= (-> (make-lazy-map)
             (assoc :d (delay (error "value :d should not be realized")))
             (keys)
             (set))
           #{:a :b :c :d}))
    (is (= (-> (make-lazy-map)
             (dissoc :a :b)
             (keys)
             (set))
           #{:c})))
  (testing "lazy maps support default value for lookup"
    (is (= (:d (make-lazy-map) :default)
           :default))
    (is (= (get (make-lazy-map) :d :default)
           :default)))
  (testing "seqs for lazy maps do not contain delays"
    (is (= (set (->LazyMap
                  {:a 1
                   :b (delay 2)}))
           #{[:a 1] [:b 2]})))
  (testing "equality for lazy maps"
    (is (= (->LazyMap
             {:a 1
              :b (delay 2)})
           {:a 1
            :b 2})))
  (testing "reduce and kv-reduce for lazy maps"
    (is (= (reduce (fn [m [k v]]
                     (assoc m k v))
                   {}
                   (->LazyMap
                     {:a 1
                      :b (delay 2)}))
           {:a 1 :b 2}))
    (is (= (reduce-kv (fn [m k v]
                        (assoc m k v))
                      {}
                      (->LazyMap
                        {:a 1
                         :b (delay 2)}))
           {:a 1 :b 2})))
  (testing "string representation of lazy maps"
    (is (= (pr-str (->LazyMap {:a 1}))
           "{:a 1}"))
    (is (= (pr-str (->LazyMap {:a (delay 1)}))
           "{:a <unrealized>}")))
  (testing "string representation of lazy map entries"
    (is (= (pr-str (lazy-map-entry :a 1))
           "[:a 1]"))
    (is (= (pr-str (lazy-map-entry :a (delay 1)))
           "[:a <unrealized>]")))
  (testing "lazy-map macro"
    (is (= (with-out-str
             (let [m (lazy-map
                       {:a (println "value :a was realized")
                        :b (println "value :b was realized")
                        :c (println "value :c was realized")})]
               (doseq [k [:b :c]]
                 (k m))))
           (format "value :b was realized%nvalue :c was realized%n"))))
  (testing "forcing a lazy map"
    (is (= (->> (lazy-map
                  {:a (println "value :a was realized")
                   :b (println "value :b was realized")})
             (force-map)
             (with-out-str)
             (re-seq #"value :[ab] was realized")
             (set))
           #{"value :a was realized" "value :b was realized"}))))

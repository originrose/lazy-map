(defproject thinktopic/lazy-map "0.1.1-SNAPSHOT"
  :description "Lazy maps for Clojure"
  :url "https://github.com/thinktopic/lazy-map"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :think/meta {:type :library
               :tags [:clojure :utility :data-type]}
  :dependencies [[org.clojure/clojure "1.8.0"]]
  :profiles {:repl {:main lazy-map.core}})

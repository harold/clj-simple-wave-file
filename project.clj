(defproject clj-simple-wave-file "0.1.0-SNAPSHOT"
  :description "A short Clojure program that produces a wave file."
  :url "http://github.com/harold/clj-simple-wave-file"
  :license {:name "Copyright © 2022 Harold"
            :url "http://github.com/harold/clj-simple-wave-file"}
  :dependencies [[org.clojure/clojure "1.11.1"]]
  :main ^:skip-aot clj-simple-wave-file.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})

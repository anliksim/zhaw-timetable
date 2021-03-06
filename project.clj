(defproject timetable "0.10.5"
  :dependencies [[org.clojure/clojure        "1.8.0"]
                 [org.clojure/clojurescript  "1.9.908"]
                 [reagent  "0.7.0"]
                 [re-frame "0.10.5"]
                 [cljs-ajax "0.7.4"]
                 [secretary "1.2.3"]
                 [com.cognitect/transit-cljs "0.8.256"]
                 [day8.re-frame/http-fx "0.1.6"]]

  :plugins [[lein-cljsbuild "1.1.7"]
            [lein-figwheel  "0.5.14"]]

  :hooks [leiningen.cljsbuild]

  :profiles {:dev {:cljsbuild
                   {:builds {:client {:figwheel     {:on-jsload "timetable.core/run"}
                                      :compiler     {:main "timetable.core"
                                                     :asset-path "js"
                                                     :closure-defines {timetable.core/api-uri "http://localhost:3000/api/v1/schedules/students/"}
                                                     :optimizations :none
                                                     :source-map true
                                                     :source-map-timestamp true}}}}}

             :prod {:cljsbuild
                    {:builds {:client {:compiler    {:optimizations :advanced
;                                                     :closure-defines {timetable.core/api-uri "https://zhaw-timetable-server.herokuapp.com/api/v1/schedules/students/"}
                                                     :closure-defines {timetable.core/api-uri "https://coherent-sphere-217913.appspot.com/api/v1/schedules/students/"}
                                                     :elide-asserts true
                                                     :pretty-print false}}}}}}

  :figwheel {:repl false}

  :clean-targets ^{:protect false} ["resources/public/js"]

  :cljsbuild {:builds {:client {:source-paths ["src"]
                                :compiler     {:output-dir "resources/public/js"
                                               :output-to  "resources/public/js/client.js"}}}})

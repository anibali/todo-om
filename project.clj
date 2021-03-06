(defproject todo "0.1.0-SNAPSHOT"
  :description "A silly little Todo list app I created to learn about ClojureScript and Om"
  :url "https://github.com/anibali/todo-om"

  :source-paths ["src/clj" "src/cljs"]

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2505" :scope "provided"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [ring/ring-devel "1.3.2"]
                 [javax.servlet/servlet-api "2.5"]
                 [http-kit "2.1.16"]
                 [compojure "1.3.1"]
                 [enlive "1.1.5"]
                 [om "0.8.0-rc1"]
                 [prismatic/om-tools "0.3.10"]
                 [potemkin "0.3.4"]
                 [figwheel "0.1.4-SNAPSHOT"]
                 [environ "1.0.0"]
                 [com.cemerick/piggieback "0.1.3"]
                 [sablono "0.2.22"]
                 [leiningen "2.5.0"]]

  :plugins [[lein-cljsbuild "1.0.3"]
            [lein-environ "1.0.0"]
            [org.clojars.cemerick/cljx "0.6.0-SNAPSHOT"]]

  :min-lein-version "2.5.0"

  :uberjar-name "todo.jar"

  :cljsbuild {:builds {:app {:source-paths ["src/cljs" "target/generated/cljs"]
                             :compiler {:output-to     "resources/public/js/app.js"
                                        :output-dir    "resources/public/js/out"
                                        :source-map    "resources/public/js/out.js.map"
                                        :preamble      ["react/react.min.js"]
                                        :optimizations :none
                                        :pretty-print  true}}}}

  :prep-tasks [["cljx" "once"] "javac" "compile"]

  :cljx {:builds [{:source-paths ["src/cljx"]
                   :output-path "target/classes"
                   :rules :clj}

                  {:source-paths ["src/cljx"]
                   :output-path "target/generated/cljs"
                   :rules :cljs}]}

  :profiles {:dev {:repl-options {:init-ns todo.server
                                  :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl
                                                     cljx.repl-middleware/wrap-cljx]}

                   :plugins [[lein-figwheel "0.1.4-SNAPSHOT"]]

                   :aliases {"cleantest" ["do" "clean," "cljx" "once," "test," "cljsbuild" "test"]}

                   :figwheel {:http-server-root "public"
                              :port 3449
                              :css-dirs ["resources/public/css"]}

                   :env {:is-dev true}

                   :main todo.server

                   :cljsbuild {:builds {:app {:source-paths ["env/dev/cljs"]}}}}

             :uberjar {:hooks [leiningen.cljsbuild]
                       :env {:production true}
                       :omit-source true
                       :aot :all
                       :cljsbuild {:builds {:app
                                            {:source-paths ["env/prod/cljs"]
                                             :compiler
                                             {:optimizations :advanced
                                              :pretty-print false}}}}}})

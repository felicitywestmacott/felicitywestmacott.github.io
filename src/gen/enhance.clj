(ns gen.enhance
  (:require [clojure.pprint :as pprint]
            [clojure.edn :as edn]))



(def clients (edn/read-string (slurp "./resources/clients.edn")))



(spit "./resources/clients.edn" (with-out-str (pprint/pprint enhanced-clients)))

(println "Done.")

(ns gen.generate
  (:require [clojure.edn :as edn]))


(def known-filter-categories ["Theme" "Category" "Detail" "Colour" "Fabric"])

(def filters (edn/read-string (slurp "./resources/filters.edn")))
(def portfolio (edn/read-string (slurp "./resources/portfolio.edn")))
(def clients (edn/read-string (slurp "./resources/clients.edn")))


(def filter-usage (frequencies (mapcat :classes portfolio)))
(def used-filters (into #{} (keys filter-usage)))
(def defined-filters (into #{} (flatten (for [[_ category] filters]
                                          (for [[tag _] category] tag)))))

(defn lightly-used-filters [usage-count]
  (filter #(= (get filter-usage % 0) usage-count) defined-filters))
(println "Unused filters:" (lightly-used-filters 0))
(println "Only used once:" (lightly-used-filters 1))
(println "Only used twice:" (lightly-used-filters 2))

(def undefined-filters (filter (complement defined-filters) used-filters))
(println "Undefined filters:" undefined-filters)





(def filters-yaml (apply str (for [cat known-filter-categories]
                               (str "\n"
                                    " - label: " cat "\n"
                                    "   options:\n"
                                    "    - value: all\n"
                                    "      label: All\n"
                                    (apply str (for [[tag label] (filters cat) :when (used-filters tag)]
                                                 (str "    - value: " tag "\n"
                                                      "      label: " label "\n")))
                                    "\n"))))

(spit "./_data/filters.yml" filters-yaml)


(defn build-portfolio-yaml [text-so-far entry]
  (if-let [label (:label entry)]
    (str text-so-far "\n\n - label: " label "\n\n")
    (str text-so-far "\n"
         " - link: " (:link entry) "\n"
         "   tt: " (:tt entry) "\n"
         "   classes: " (apply str (interpose " " (:classes entry))) "\n")))

(def portfolio-yaml (reduce build-portfolio-yaml "" portfolio))

(spit "./_data/portfolio.yml" portfolio-yaml)
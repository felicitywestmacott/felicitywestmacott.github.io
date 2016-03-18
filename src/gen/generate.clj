(ns gen.generate
  (:require [clojure.edn :as edn]))


(def known-filter-categories ["Theme" "Category" "Detail" "Colour" "Fabric"])
(def numbers-from-one (iterate inc 1))

(def filters (edn/read-string (slurp "./resources/filters.edn")))
(def portfolio (edn/read-string (slurp "./resources/portfolio.edn")))
(def clients (edn/read-string (slurp "./resources/clients.edn")))


(def filter-usage (frequencies (mapcat :classes portfolio)))
(def used-filters (into #{} (keys filter-usage)))
(def defined-filters (into #{} (flatten (for [[_ category] filters]
                                          (for [[tag _] category] tag)))))

(defn lightly-used-filters [usage-count]
  (filter #(and (not= % "all") (= (get filter-usage % 0) usage-count)) defined-filters))
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

(defn write-filters []
  (spit "./_data/filters.yml" filters-yaml))





(defn build-portfolio-yaml [text-so-far entry]
  (if-let [label (:label entry)]
    (str text-so-far "\n\n - label: " label "\n\n")
    (str text-so-far "\n"
         " - link: " (:link entry) "\n"
         "   tt: " (:tt entry) "\n"
         "   classes: " (apply str (interpose " " (:classes entry))) "\n")))

(def portfolio-yaml (reduce build-portfolio-yaml "" portfolio))

(defn write-portfolio []
  (spit "./_data/portfolio.yml" portfolio-yaml))







(defn pic-name [pic] (.replace (:image pic) ".jpg" ""))

(defn find-next [pics current]
  (first (filter #(= current (:prevpic %)) pics)))

(defn pics-chain [pic pics]
  (if (nil? (:nextpic pic))
    [pic]
    (let [next (find-next pics (pic-name pic))]
      (concat [pic] (pics-chain next pics)))))

(defn sorted-pics [pics]
  (let [first-pic (find-next pics nil)]
    (pics-chain first-pic pics)))



(defn build-client-page [title client-blurb total counter {:keys [image nextpic prevpic blurb]}]
  (let [content (str "---\n"
                     "layout: client\n"
                     "title: " title "\n"
                     "image: " image "\n"
                     (if nextpic (str "nextpic: " nextpic "\n") "\n")
                     (if prevpic (str "prevpic: " prevpic "\n") "\n")
                     "counter: " counter " / " total "\n"
                     "---\n"
                     "\n"
                     client-blurb
                     "\n"
                     blurb)
        filename (str "./portfolio/" (.replace image ".jpg" ".html"))]
    (spit filename content)))

(defn build-client-pages [{:keys [title pics blurb]}]
  (let [builder (partial build-client-page title blurb (count pics))]
    (mapv builder numbers-from-one (sorted-pics pics))))

(mapv build-client-pages (take 2 (vals clients)))


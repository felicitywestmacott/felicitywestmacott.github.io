(ns gen.generate
  (:require [clojure.edn :as edn]
            [hiccup.core :refer :all]))


(def known-filter-categories ["Theme" "Category" "Detail" "Colour" "Fabric"])
(def numbers-from-one (iterate inc 1))

(def filters (edn/read-string (slurp "./resources/filters.edn")))
(def portfolio (edn/read-string (slurp "./resources/portfolio.edn")))
(def clients (edn/read-string (slurp "./resources/clients.edn")))


(def filter-usage (frequencies (mapcat :classes (vals clients))))
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

(write-filters)



(defn build-portfolio-yaml [text-so-far entry]
  (if-let [label (:label entry)]
    (str text-so-far "\n\n - label: " label "\n\n")
    (let [client (clients (:client entry))]
      (str text-so-far "\n"
           " - link: " (:link client) "\n"
           "   tt: " (:tt client) "\n"
           "   classes: " (apply str (interpose " " (:classes client))) "\n"))))

(def portfolio-yaml (reduce build-portfolio-yaml "" portfolio))

(defn write-portfolio []
  (spit "./_data/portfolio.yml" portfolio-yaml))

(write-portfolio)



(defn basically-empty [string]
  (= 0 (count (-> string
                  (.replaceAll " " "")
                  (.replaceAll "\n" "")
                  (.replaceAll "<br/>" "")))))

(defn has-content? [string]
  (and string (< 0 (count string))))


(defn build-client-page [title
                         client-blurb
                         {photo-url :url photo-name :name}
                         clean
                         total
                         counter
                         [{prev :name} {:keys [name blurb photo]} {next :name}]]
  (let [
        header (str "---\n"
                    "layout: client\n"
                    "title: " title "\n"
                    "image: " name ".jpg\n"
                    (if next (str "nextpic: " next "\n") "\n")
                    (if prev (str "prevpic: " prev "\n") "\n")
                    "counter: " counter " / " total "\n"
                    "---\n"
                    "\n")
        photo-credit (if photo
                       (str "<br/><br/>\n"
                            "Official photographs by <a href=\""
                            photo-url
                            "\" target=\"_blank\">"
                            photo-name
                            "</a>"
                            "<br/>\n")
                       "")
        joiner (if (and clean
                        (has-content? blurb)
                        (or photo
                            (has-content? client-blurb)))
                 "<hr/>\n"
                 "")
        content (str header
                     client-blurb "\n"
                     photo-credit
                     joiner
                     (or blurb ""))
        filename (str "./portfolio/" name ".html")]
    (spit filename content)))

(defn build-client-pages [{:keys [title blurb photo clean pics]}]
  (let [builder (partial build-client-page title blurb photo clean (count pics))
        pic-chain (partition 3 1 (concat [nil] pics [nil]))]
    (mapv builder numbers-from-one pic-chain)))



(mapv build-client-pages (vals clients))



(def thankyous
  (apply concat
         (for [[_ client] clients]
           (for [{:keys [name blurb thanks]} (:pics client) :when thanks]
             [(str name ".jpg") (:tt client) blurb]))))

(def sides (cycle [{:style "left" :fn identity}
                   {:style "right" :fn reverse}]))

(defn generate-thank-you [[thanks-image tt-image text] side]
  (html [:div {:class "section"}
         [:div {:class (:style side)}
          ((:fn side) (list [:img {:class "thanks" :src (str "{{ site.baseimageref }}/images/" tt-image)}]
                            [:img {:class "thanks" :src (str "{{ site.baseimageref }}/images/" thanks-image)}]))]
         [:div {:class "sectiontext"} text]]))


(println
  (.replace
    (apply str
           (map generate-thank-you thankyous sides))
    ">"
    ">\n"))

(println "Done.")

;
;(def done (atom 0))
;
;(defn gen-some-clients [n]
;  (let [so-far @done]
;    (mapv build-client-pages (take n (drop so-far (vals clients))))
;    (swap! done + n)))

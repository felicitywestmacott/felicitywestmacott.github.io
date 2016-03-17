(ns gen.scrape
  (:require [clojure.edn :as edn]
            [clojure.pprint :as pprint]
            [clj-yaml.core :as yaml]))



(defn try-read [filename]
  (try
    (edn/read-string (slurp filename))
    (catch Exception _ nil)))

(defn persist-state [file-name _ _ _ new-state]
  (spit file-name (with-out-str (pprint/pprint new-state))))

(defn persistent-atom [filename content]
  (let [state (atom (or (try-read filename) content))]
    (add-watch state :persistor (partial persist-state filename))))

(def data (persistent-atom "./resources/data.edn" {}))





(def directory (clojure.java.io/file "./portfolio"))
(def files (file-seq directory))
(take 10 files)

(defn inc+ [n]
  (if n (inc n) 1))

(def empty-vec (vec (repeat 100 nil)))

(defn add-pic [pics n image next prev blurb]
  (let [size (max n (count (or pics [])))
        new-pics (vec (take size (concat pics (repeat nil))))]
    (assoc new-pics (dec n) {:image image :nextpic next :prevpic prev :blurb blurb})))

(defn reduction [collector file]
  (let [filename (.getName file)]
    (if-let [[_ name number] (re-matches #"(\D+)(\d+)\.html" filename)]
      (do                                                   ;(println name number)
          (let [content (slurp file)
                n (Integer/parseInt number)
                [_ title] (re-find #"title: (.+)" content)
                [_ image] (re-find #"image: (.+)" content)
                [_ nextpic] (re-find #"nextpic: (.+)" content)
                [_ prevpic] (re-find #"prevpic: (.+)" content)
                blurb (.trim (.substring content (+ (.indexOf content "---\n" 1) 4)))]
            (-> collector
                ;(update-in [name] #(or % {:pics empty-vec}))
                (assoc-in [name :title] title)
                (update-in [name :pics] add-pic n image nextpic prevpic blurb)
                ;(assoc-in [name :pics n :image] image)
                ;(assoc-in [name :pics n :nextpic] nextpic)
                ;(assoc-in [name :pics n :prevpic] prevpic)
                )))
      collector)))

(def client (reduce reduction {} files))

(spit "./resources/clients.edn" (with-out-str (pprint/pprint client)))




(def clients (edn/read-string (slurp "./resources/clients.edn")))
(spit "./resources/clients.edn" (with-out-str (pprint/pprint clients)))

(def filters
  (let [raw (slurp "./_data/filters.yml")
        data (yaml/parse-string raw)]

    (println data)

    ))



;;
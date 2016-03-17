(ns gen.scrape
  (:require [clojure.edn :as edn]
            [clojure.pprint :as pprint]
            [clj-yaml.core :as yaml]))




(def directory (clojure.java.io/file "./portfolio"))
(def files (file-seq directory))
(take 10 files)



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



(def clients-1 (reduce reduction {} files))
(spit "./resources/clients_1.edn" (with-out-str (pprint/pprint clients-1)))



;;!;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(defn common-prefix-length
  ([strings] (common-prefix-length 0 strings))
  ([n strings]
   (if (and (first (first strings))
            (apply = (map first strings)))
     (recur (inc n) (map rest strings))
     n)))

(defn reverse-to-safety [string]
  (if (< (.lastIndexOf string ">") (.lastIndexOf string "<"))
    (recur (.substring string 0 (dec (count string))))
    string))

(defn extract-common-blurb [{:keys [pics] :as data}]
  (let [prefix-length (common-prefix-length (map :blurb pics))
        common-blurb (.substring (:blurb (first pics)) 0 prefix-length)
        safe-common-blurb (reverse-to-safety common-blurb)
        safe-prefix-length (count safe-common-blurb)
        prefix-remover #(when % (.substring % safe-prefix-length))
        cut-pics (mapv #(update % :blurb prefix-remover) pics)]
    (merge data {:blurb common-blurb
                 :pics cut-pics})))

(def clients-2 (into {} (for [[name data] clients-1] [name (extract-common-blurb data)])))
(spit "./resources/clients_2.edn" (with-out-str (pprint/pprint clients-2)))


;;!;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


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

(def clients-3 (into {} (for [[n v] clients-2] [n (update v :pics #(into [] (sorted-pics %)))])))

(spit "./resources/clients.edn" (with-out-str (pprint/pprint clients-3)))


;;!;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;
;(def filters
;  (let [raw (slurp "./_data/filters.yml")
;        data (yaml/parse-string raw)]
;
;    (println data)
;
;    ))



;;
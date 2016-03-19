(ns gen.enhance
  (:require [clojure.pprint :as pprint]
            [clojure.edn :as edn]))



(def clients (edn/read-string (slurp "./resources/clients.edn")))
(def portfolio (edn/read-string (slurp "./resources/portfolio.edn")))


(defn jpg->html [x]
  (.replace x ".jpg" ".html"))

(defn find-port [p-name]
  (first (filter #(= p-name (:link %)) portfolio)))

(def enhanced-clients
  (into {}
        (for [[k v] clients]
          (let [p-name (-> v :pics first :image jpg->html)
                port (find-port p-name)]
            [k (merge v port)]))))


(spit "./resources/clients.edn" (with-out-str (pprint/pprint enhanced-clients)))


(defn nhtml->_ [x]
  (second (re-find #"(\D+)\d+.html" x)))

(def enhanced-portfolio
  (map (fn [x]
         (if (:label x)
           x
           {:client (nhtml->_ (:link x))})) portfolio))

(spit "./resources/portfolio.edn" (with-out-str (pprint/pprint enhanced-portfolio)))

(println "Done.")


;;; todo

; tyler-doel
; alexandra
; parkinson
; madelei

; catz
; gibbon
; snow
; lilac
; mckay

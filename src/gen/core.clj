(ns gen.core
  (:require [seesaw.core :as s]
            [clojure.core.async :as a]
            [clojure.edn :as edn]
            [clojure.pprint :as pprint])
  (:import (java.io File)))


(s/native!)

(def f (doto
         (s/frame :title "Input")
         (.setAlwaysOnTop true)))

(defn display [title content]
  (s/config! f :title title)
  (s/config! f :content content)
  (-> f s/pack! s/show! s/move-to-front!)
  content)

(defn get-user-input [msg multiline]
  (let [channel (a/chan 1)
        button (s/button :text "Click Me")
        text-height (if multiline 300 30)
        text-area (s/text :multi-line? multiline
                          :font "MONOSPACED-PLAIN-14"
                          :text ""
                          :size [1000 :by text-height])]
    (display msg (s/border-panel
                       :center text-area
                       :south button))

    (s/listen button :action (fn [e]
                               (a/>!! channel (s/text text-area))
                               (s/hide! f)))
    (a/<!! channel)))

(defn p [text]
  (if (seq text)
    (str "\n<p>\n" text "\n</p>\n")
    ""))




(defn gen-client []

  (let [name (get-user-input "Enter the client name:" false)
        title (get-user-input "Enter the page title:" false)
        number (Integer/parseInt (get-user-input "How many pictures are there:" false))
        client-blurb (get-user-input "Enter the client level blurb:" true)
        photographer-name (get-user-input "Enter the photographer's name (if any):" false)
        photographer-url (when (seq photographer-name)
                           (get-user-input "Enter the photographer's url (if any):" false))]

    (for [n (range 1 (inc number))]

      (do
        (let [header (apply str
                            (interpose "\n"
                                       ["---"
                                        "layout: client"
                                        (str "title: " title)
                                        (str "image: " name n ".jpg")
                                        (if (< n number) (str "nextpic: " name (inc n)) "")
                                        (if (< 1 n) (str "prevpic: " name (dec n)) "")
                                        (str "counter: " n " / " number)
                                        "---"
                                        ""]))
              photo-credit (if (seq photographer-name)
                             (str "<i>Photographs by <a href=\""
                                  photographer-url
                                  "\" target=\"_blank\">"
                                  photographer-name
                                  "</a></i>"))
              picture-blurb (get-user-input (str "Enter the blurb for: " name n) true)
              divider (if (and (or (seq client-blurb) (seq photo-credit))
                               (seq picture-blurb))
                        "<hr/>"
                        "")
              file-content (str header
                                (p client-blurb)
                                (p photo-credit)
                                divider
                                (p picture-blurb))]

          (println file-content)
          )

        (let [file (File. (str "./portfolio/" name n ".html"))]

          (if (.exists file)
            (println "EXISTS!")
            (println "new file.")))))))



;;; server - ring? / compojure?
;;;   list of clients




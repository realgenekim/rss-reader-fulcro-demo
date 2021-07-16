(ns feedly
  (:require
    [clojure.java.io :as io]))

(defn read-file []
  (-> ;(slurp (str "/Users/genekim/src.local/feedly" "/" "clojure.txt"))
    ;(slurp "../../feedly/ccsp2.txt")
    ;(slurp "../../feedly/clojure.txt")
    (io/resource "clojure.txt")
    slurp
    read-string))

(comment

  (def stories (read-file))

  (io/resource "clojure.txt")

  (count stories)
  (first stories)

  ,)
(ns feedly)


(defn read-file []
  ; "ccsp2.txt"
  ; "clojure.txt"
  (-> ;(slurp (str "/Users/genekim/src.local/feedly" "/" "clojure.txt"))
    ;(slurp "../../feedly/ccsp2.txt")
    (slurp "../../feedly/clojure.txt")
    (read-string)))

(comment

  (def stories (read-file))

  (count stories)
  (first stories)

  (-> ;(slurp (str "/Users/genekim/src.local/feedly" "/" "clojure.txt"))
    (slurp "../../feedly/resources/stories.txt")
    (read-string))

  ,)
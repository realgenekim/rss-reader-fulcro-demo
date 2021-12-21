(ns feedly
  (:require
    [clojure.java.io :as io])
  (:import
    (java.util.zip GZIPInputStream)))

(defn gunzip
  "decompress data.
    input: gzipped data  which can be opened by io/input-stream.
    output: something which can be copied to by io/copy (e.g. filename ...)."
  [input output & opts]
  (with-open [input (-> input io/input-stream GZIPInputStream.)]
    (apply io/copy input output opts)))

(defn read-file-gzip []
  (with-open [in (java.util.zip.GZIPInputStream.
                   (io/input-stream
                     (clojure.java.io/resource
                       "clojure.txt.gz")))]
    (read-string (slurp in))))

(defn read-file []
  (-> ;(slurp (str "/Users/genekim/src.local/feedly" "/" "clojure.txt"))
    ;(slurp "../../feedly/ccsp2.txt")
    ;(slurp "../../feedly/clojure.txt")
    ;(io/resource "clojure.txt")
    (io/resource "clojure.txt.gz")
    (io/input-stream)
    (GZIPInputStream.)
    slurp
    read-string))

(comment

  (def stories (read-file))
  (def stories (read-file-gzip))

  (io/resource "clojure.txt")

  (count stories)
  (first stories)

  ,)
(ns GraphNamedThings.buildclusters
  (:require [GraphNamedThings.config :as config]
            [GraphNamedThings.dbio :as dbio]
            [GraphNamedThings.louvain :as louvain]
            [GraphNamedThings.recordprocessing :as processing]
            [GraphNamedThings.document :as document]
            [clj-time.core :as t]
            [clj-time.coerce :as coerce])
  (:import [edu.stanford.nlp pipeline.StanfordCoreNLP pipeline.Annotation]
           [GraphNamedThings.dbio doccluster]))


(defn community-item-to-doc-cluster
  [c]
  (doccluster.
    (key c)
    (val c)))

(defn louvain-output-to-clusters
  "Takes a community set from louvain iteration method and returns a collection of doc-cluster records"
  [cs]
  (map community-item-to-doc-cluster cs))

(defn create-document-clusters-between
  "Return set of document clusters from a start and end date as clj-time"
  [nlp-pipe start end]
  (let [start-epoch (coerce/to-epoch start)
        end-epoch (coerce/to-epoch end)
        docs (dbio/docs-from-time-range start-epoch end-epoch)
        cached (dbio/select-best-cluster-set start end)]
    (->> docs
        (processing/create-document-records-batched nlp-pipe config/batch-size)
        (document/create-document-graph)
        (louvain/iterate-louvain-modularity '())
        (first) ;above should return a data structure that's more clear than a vector, but while it returns a vector, the first element is the list of communities
        (louvain-output-to-clusters))))

(defn create-document-clusters-between-new
  "Return set of document clusters from a start and end date as clj-time"
  [start end]
  (let [start-epoch (coerce/to-epoch start)
        end-epoch (coerce/to-epoch end)
        docs (dbio/processed-docs-from-time-range start-epoch end-epoch)
        ids (map :id docs)
        cached (dbio/select-best-cluster-set start end)]
    (->> ids
         (processing/create-document-records-newish)
         (document/create-document-graph)
         (louvain/iterate-louvain-modularity '())
         (first) ;above should return a data structure that's more clear than a vector, but while it returns a vector, the first element is the list of communities
         (louvain-output-to-clusters))))

(defn write-and-return-clusters
  "Calculate, write to database, and return document clusters"
  [nlp-pipe start end]
  (let [clusters (create-document-clusters-between nlp-pipe start end)]
    (do (dbio/write-clusters clusters start end)
        clusters)))

(defn write-and-return-clusters-overlap
  "Calculate, write to database, and return document clusters"
  [nlp-pipe start end]
  (let [clusters (create-document-clusters-between nlp-pipe start end)]
    (do (dbio/write-clusters clusters start end)
        clusters)))

(defn get-clusters
  [nlp-pipe start end]
  (let [processed-id (dbio/select-best-cluster start end)]
    (if (nil? processed-id) ;if no id was returned for preprocessed clusters
      (write-and-return-clusters nlp-pipe start end)  ;calculate, write to database, and return clusters
      (dbio/read-doc-cluster processed-id)))) ;else read from database

(defn get-clusters-overlap
  [nlp-pipe start end]
  (let [processed-id (dbio/select-best-cluster start end)]
    (if (nil? processed-id) ;if no id was returned for a preprocessed cluster
      (write-and-return-clusters nlp-pipe start end)  ;calculate, write to database, and return clusters
      (dbio/read-doc-cluster processed-id)))) ;else read from database





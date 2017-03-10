(ns GraphNamedThings.weboutput
  (:require [GraphNamedThings.config :as config]
            [GraphNamedThings.buildclusters :as bc]
            [GraphNamedThings.dbio :as dbio]
            [loom.graph :as graph]
            [clj-time.core :as t]
            [clj-time.coerce :as coerce])
  (:import [edu.stanford.nlp pipeline.StanfordCoreNLP pipeline.Annotation]
           [GraphNamedThings.dbio doccluster]))


(defn gen-results [start-epoch end-epoch]
  ;TODO: Add other statistics here
  (let [lcomms (:comms (bc/louvain-comms start-epoch end-epoch))]
    (for [comm (vals lcomms)]
      (dbio/get-doc-out comm))))

(defn update-results [app-state start-epoch end-epoch]
  (let [new-ids (into #{} (map :id (dbio/processed-docs-from-time-range start-epoch end-epoch))) ;TODO: Replace with query that only retreives IDs
        cur-ids (into #{} (map :id (flatten @app-state)))]
    (println "Getting docs between " start-epoch "and " end-epoch)
    (if (not= new-ids cur-ids)
      (doall
        (reset! app-state (gen-results start-epoch end-epoch)))
      app-state)))

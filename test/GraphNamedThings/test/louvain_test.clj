(ns GraphNamedThings.test.louvain_test
  (:require [GraphNamedThings.louvain :refer :all]
            [loom.graph]
            [loom.alg]
            [clojure.test :refer :all]))

; https://www.cs.ucsb.edu/~xyan/classes/CS595D-2009winter/MCL_Presentation2.pdf pg 10

;A graph that should have one cluster at :1
(def g4
  (loom.graph/weighted-graph
    [:1 :2 2]
    [:1 :3 4]
    [:1 :4 1]
    [:2 :3 7]
    [:2 :4 4]
    [:2 :5 2]
    [:3 :4 4]
    [:5 :6 8]
    [:5 :7 2]
    [:6 :7 6]
    [:8 :5 3]
    [:8 :1 4]
    [:9 :8 1]
    [:9 :10 4]
    [:3 :11 6]))

(deftest iterate-louvain-test
         (is (= (:adj (first (iterate-louvain g4)))
                {:3 {:8 7, :6 2}, :8 {:3 7, :6 3, :9 1}, :6 {:8 3, :3 2}, :9 {:8 1}})))

(deftest inside-edges-test
         (is
           (= (inside-edges '(:3 :2 :4) g4)
              '([:4 :2] [:4 :3] [:2 :3]))))

(deftest get-keyval-node-test
         (is
           (=
             (get-keyval-node (:adj g4) :5)
             {#{:2 :5} 2, #{:5 :6} 8, #{:7 :5} 2, #{:8 :5} 3})))

(deftest community-edges-test
         (is
           (=
             (community-edges '(:3 :4) g4)
             {#{:1 :3} 4, #{:2 :3} 7, #{:4 :3} 4, #{:11 :3} 6, #{:4 :1} 1, #{:4 :2} 4})))

(deftest connected-nodes-test
         (is
           (=
             (connected-nodes '(:3 :4) g4)
             #{:11 :4 :1 :2 :3})))

(deftest inside-edges-sum-test
         (is
           (=
             (inside-edges-sum '(:3 :4 :11) g4)
             10)))

(deftest outside-connections-sum-test
         (is
           (=
             (outside-connections-sum '(:3 :4 :11) g4)
             16)))

(deftest ki-in-test
         (is
           (=
             (ki-in g4 '(:3 :4 :11) :2)
             11)))

(deftest sum-connections-between-test
         (is
           (=
             (sum-connections-between g4 '(:8 :6) '(:3 :6 :9))
             1)))


(deftest sum-all-weights-test
         (is
           (=
             (sum-all-weights g4)
             58)))

(deftest dq-i-c-test
  (let [ki-l (ki g4 :4)
        m (sum-all-weights g4)]
         (is
           (=
             (map (partial dQ-i-c m ki-l g4 :4) '((:1) (:2) (:3)))
             '(-0.006093935790725331 0.014417360285374572 0.006391200951248538)))))



(ns com.miyamofigo.web.util
  (:require [com.miyamofigo.web.core 
             :refer [first-arg add in? fields prop prop!]
             :as core]
            [com.miyamofigo.web.string :as string]))

;;
;; handling pair utilities
;;
;; pair => [ str-list, str-set ]
;; for creator function generation

(defmulti get-name first-arg) 

(defmethod get-name :project [_]
  (-> *ns* str string/split-n-first-withA))

(defmethod get-name :class [_ cl] (core/get-name cl))

(defmethod get-name :field [_ f] (core/get-name f))

(defn classpath [nam] (str (get-name :project) nam))

(defn as-field [cl]
  (-> cl (get-name :class) string/split-n-last-withA string/lc-first-char)) 

(defn add-clname-as-field [s cl] (->> cl as-field (add :set s))) 

(defn fieldnames [cl] (map #(get-name :field %) (fields cl)))

(defn pair [lst s] (vector lst s))

(defn fetch-pair [cl old]
  (let [new-s (add-clname-as-field cl old)]
    (pair (fieldnames cl) new-s)))

(defn merge-pair [p old]
  (pair (concat old (first p)) (fnext p)))

(defn instance [path] (core/instance :class path))

(defn fetch-pair-helper [nam s]
  (-> nam classpath instance (fetch-pair s)))

(declare ev)

(defn eval-pair [[names ignored] pred special]
  (loop [src names, res [], ignored ignored]
    (if (empty? src)
      (pair res ignored)
      (let [nam (first src),
            [res* ignored*] (cond
                              (in? nam ignored) (pair res ignored)
                              (contains? special nam) (pair (concat res (special nam)) ignored)
                              (pred nam) (-> nam 
                                           (ev ignored pred special)
                                           (merge-pair res))
                              :else (pair (conj (vec res) nam) ignored))]
        (recur (rest src) res* ignored*)))))
 
(defn- ev [nam ignored pred special]
  (-> nam string/uc-first-char (fetch-pair-helper ignored) (eval-pair pred special)))
;;pair utilities ends



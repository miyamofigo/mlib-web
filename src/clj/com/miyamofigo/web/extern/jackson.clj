(ns com.miyamofigo.web.extern.jackson
  (require [com.miyamofigo.web.core :refer [first-arg]])
  (:import com.fasterxml.jackson.databind.ObjectMapper))

(defn om [] (ObjectMapper.))

(defmulti read-string first-arg)

(defmethod read-string :default [_ & args] (apply clojure.core/read-string args))
(defmethod read-string :value 
  [_, ^ObjectMapper m, ^java.lang.String s, ^java.lang.Class cl]
  (. m (readValue s cl)))

(defn read-bytes [^ObjectMapper m, bs, ^java.lang.Class cl]
  (. m (readValue bs cl)))  

(defmulti write-string first-arg)

(defmethod write-string :value [^ObjectMapper m, v] 
  (. m (writeValueAsString v)))


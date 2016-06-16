(ns com.miyamofigo.web.http
  (:require [clojure.core :as core] 
            [clojure.xml :as xml]
            [com.miyamofigo.web.core :refer [first-arg]] 
            [com.miyamofigo.web.crypto :refer [gen-hash]]
            [com.miyamofigo.web.string :as string]
            [com.miyamofigo.web.extern.spring :as spring]))

(defn current-ctx [] (spring/current-ctx))

(defn set-ctx! [ctx] (spring/set-ctx! ctx))

(defn current-user [] (spring/get-user (current-ctx)))

(defn set-auth! [ctx auth] (spring/set-auth! ctx auth))

(defmulti parse first-arg)

(defmethod parse :xml [_ s & [startparse]]
  (if startparse
    (xml/parse s startparse)
    (xml/parse s)))

(defmethod parse :http-headers [_ hs] 
  (let [seq* (for [[k v] hs] [(keyword k) v])] 
    (->> seq* 
      (apply concat) (apply assoc {}))))

(defn parse-headers 
  ([hs] (parse-headers hs :http-headers))
  ([hs typ] (parse typ hs)))

(defn replace-headers
  ([req] 
    (replace-headers parse-headers))
  ([req func] 
    (update req :headers func)))

(defn req->auth [req] (get-in req [:headers :authorization]))

(defmulti fetch first-arg)

(defmethod fetch :auth-basic-token [_ code] (string/split-n-take-second code))

(defn fetch-token 
  ([code] (fetch-token code :auth-basic-token))
  ([code typ] (fetch typ code))) 

(defn req->hash 
  ([req] (req->hash req fetch-token))
  ([req fetcher] (-> req req->auth fetcher gen-hash)))

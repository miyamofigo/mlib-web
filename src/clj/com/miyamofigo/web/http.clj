(ns com.miyamofigo.web.http
  (:require [clojure.core :as core] 
            [clojure.xml :as xml]
            [com.miyamofigo.web.core :refer [first-arg]] 
            [com.miyamofigo.web.crypto :refer [gen-hash]]
            [com.miyamofigo.web.string :as string]
            [com.miyamofigo.web.extern.spring :as spring]))

(defmulti current-ctx first-arg)
(defmulti fetch first-arg)
(defmulti prepare first-arg)

(defmethod current-ctx :security [_] 
  (spring/current-ctx :security))

(defn ctx! [ctx] (spring/ctx! :security ctx))

(defn curr-user [] 
  (spring/get-user (current-ctx)))

(defn auth! [ctx auth] 
  (spring/auth! ctx auth))

(defn convert-keys [raw-map func]
  (let [converted (for [[k v] raw-map] 
                    (vector (func k) v)),
        target {}]
    (->> converted
      (apply concat)
      (apply assoc target))))

(defn as-cljmap [raw-map]
  (convert-keys raw-map keyword))

(defmethod prepare :req-headers [_ req]
  (update req :headers as-cljmap))

(defn req->auth [req] (get-in req [:headers :authorization]))

(defmethod fetch :auth-basic-token [_ code] (string/split-n-take-second code))

(defn fetch-token 
  ([code] (fetch-token code :auth-basic-token))
  ([code typ] (fetch typ code))) 

(defn req->hash 
  ([req] (req->hash req fetch-token))
  ([req fetcher] (-> req req->auth fetcher gen-hash)))

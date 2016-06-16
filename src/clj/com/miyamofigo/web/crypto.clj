(ns com.miyamofigo.web.crypto
  (:require [com.miyamofigo.web.core :refer [first-arg random] :as core]
            [com.miyamofigo.web.string :refer [str->bytes bytes->str]]
            [com.miyamofigo.web.settings :as settings]
            [com.miyamofigo.web.extern.bouncycastle :as bc]))

(defn salt 
  ([] (salt (random :default))) 
  ([r] (core/salt r settings/salt-bytes)))

(defmulti decode first-arg)
(defmethod decode :hex [_ salt] (-> salt str->bytes bc/decode-bytes))

(defn decode-hex [salt] (decode :hex salt)) 

(defmulti crypt first-arg)
(defmethod crypt :bcrypt [_ auth salt cost] (bc/bcrypt-generate auth salt cost))

(declare salt-str cost)

(defn- generate
  ([auth] (generate auth :bcrypt)) 
  ([auth typ] (crypt typ auth (decode-hex salt-str) cost)))

(defonce salt-str settings/salt-str)
(defonce cost settings/crypto-cost)

(defmulti encode first-arg)
(defmethod encode :hex [_ b] (bc/encode-bytes b))

(defn encode-hex [b] (encode :hex b))

(defn gen-hash [auth] 
  (-> auth str->bytes generate encode-hex bytes->str))


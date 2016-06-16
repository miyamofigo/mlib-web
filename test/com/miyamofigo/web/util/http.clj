(ns com.miyamofigo.web.util.http
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [com.miyamofigo.web.http :as http] 
            [com.miyamofigo.web.util.extern.spring :as spring]))

(defn udmock [user] (spring/udmock user))

(defn username [ud] (spring/username ud))

(defn authmock [detail cred] (spring/authmock detail cred))

(defn creds [auth] (spring/creds auth))
(defn details [auth] (spring/details auth))
(defn princ [auth] (spring/princ auth))

(defn build-mock-auth [user cred] (-> user udmock (authmock cred))) 

(defn set-mock-auth! [user cred]
  (let [amock (build-mock-auth user cred)] 
    (http/set-ctx! (doto (http/current-ctx) (http/set-auth! amock)))))

(defn attach-cred [req cred] 
  (assoc-in req [:headers :authorization] cred))

(defn build-mock-request [method url cred]
  (-> method keyword (mock/request url) (attach-cred cred)))

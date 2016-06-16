(ns com.miyamofigo.web.util.extern.spring
  (:require [com.miyamofigo.web.core :refer [fetch-class]]) 
  (:import org.springframework.security.core.Authentication
           org.springframework.security.core.userdetails.UserDetails))

(defn udmock [user] 
  (reify UserDetails
    (getUsername [this] user)))

(defmulti username fetch-class)
(defmethod username UserDetails [d] (.getUsername d)) 

(defn authmock [detail cred]
  (reify Authentication
    (getCredentials [this] [cred])
    (getDetails [this] [detail])
    (getPrincipal [this] detail)))

(defmulti creds fetch-class)
(defmethod creds Authentication [a] (.getCredentials a)) 

(defmulti details fetch-class)
(defmethod details Authentication [a] (.getDetails a))

(defmulti princ fetch-class)
(defmethod princ Authentication [a] (.getPrincipal a))

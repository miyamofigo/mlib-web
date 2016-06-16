(ns com.miyamofigo.web.extern.spring
  (:require [com.miyamofigo.web.core :refer [first-arg]])
  (:import 
    org.springframework.context.ApplicationContext
    org.springframework.context.annotation.AnnotationConfigApplicationContext
    org.springframework.security.core.Authentication
    [org.springframework.security.core.context SecurityContext SecurityContextHolder]))

(defn current-ctx [] 
  (SecurityContextHolder/getContext))

(defn auth [^SecurityContext ctx] (.getAuthentication ctx))

(defn princ [^Authentication auth] (.getPrincipal auth))

(defn username [principal] (.getUsername principal))

(defn get-user [^SecurityContext ctx] (-> ctx auth princ username))

(defn set-ctx! [^SecurityContext ctx] 
  (SecurityContextHolder/setContext ctx)) 

(defn set-auth! [^SecurityContext ctx, ^Authentication auth] 
  (. ctx (setAuthentication auth)))

(defn app-ctx  
  ([] (AnnotationConfigApplicationContext.))
  ([& cls] (AnnotationConfigApplicationContext. (into-array java.lang.Class cls))))

(defmulti register! first-arg)
(defmethod register! :app-ctx [_, ^ApplicationContext ctx, & configs]
  (when-not (empty? configs) 
    (.register ctx (into-array java.lang.Class configs))))

(defmulti refresh! first-arg)
(defmethod refresh! :app-ctx [_, ^ApplicationContext ctx] (.refresh ctx))

(defn get-bean [^ApplicationContext ctx, ^java.lang.Class target] 
  (.getBean ctx target))


(ns com.miyamofigo.web.extern.spring
  (:require [com.miyamofigo.web.core :refer [first-arg]])
  (:import 
    java.util.Properties
    org.springframework.context.ApplicationContext
    org.springframework.context.annotation.AnnotationConfigApplicationContext
    [org.springframework.core.env ConfigurableEnvironment MutablePropertySources PropertiesPropertySource]
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

(defmulti get-env first-arg)
(defmethod get-env :app-ctx [_, ^ApplicationContext ctx]
  (.getEnvironment ctx))

(defn prop-sources [^ConfigurableEnvironment env]
  (.getPropertySources env))

(defmulti add! first-arg)
(defmethod add! :props-source 
  [_, ^MutablePropertySources sources, ^PropertiesPropertySource new-source]
  (.addLast sources new-source))

(defn wrap-props [nam, ^Properties props] (PropertiesPropertySource. nam props))

(defn add-props! [^ApplicationContext ctx, nam, ^Properties props]
  (let [sources (-> (get-env :app-ctx ctx) prop-sources)]
    (add! :props-source (wrap-props nam props)))) 


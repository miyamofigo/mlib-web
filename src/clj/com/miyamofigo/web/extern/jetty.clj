(ns com.miyamofigo.web.extern.jetty
  (:require [com.miyamofigo.web.core :refer [fetch-class]]) 
  (:import java.net.URL
           org.eclipse.jetty.annotations.AnnotationConfiguration
           [org.eclipse.jetty.server Server Handler]
           org.eclipse.jetty.servlet.ServletHolder
           org.eclipse.jetty.util.resource.Resource
           [org.eclipse.jetty.webapp WebAppContext Configuration MetaData]))

(defmulti add-resource! fetch-class)

(defn ctx [] (WebAppContext.))

(defn metadata [^WebAppContext ctx] (.getMetaData ctx))

(defn config [] (new AnnotationConfiguration)) 

(defn urls [^AnnotationConfiguration conf] 
  (.. conf getClass getClassLoader getURLs))

(defmethod add-resource! MetaData [metadata, ^Resource res] 
  (.addContainerResource metadata res))

(defn into-conf-array [conf] (into-array Configuration [conf]))

(defn set-config! [^WebAppContext ctx, conf] 
  (.setConfigurations ctx (into-conf-array conf)))

(defn resource [^URL url] (Resource/newResource url))

(defn add-servlet! [^WebAppContext ctx, ^ServletHolder serv, path] (.addServlet ctx serv path))

(defn servlet-holder [servlet] (ServletHolder. servlet))

(defn set-name! [^ServletHolder serv, nam] (.setName serv nam))

(defn set-handler! [^Server serv, ^Handler handler] (.setHandler serv handler))

(defmethod add-resource! WebAppContext [ctx conf]
  (let [data (metadata ctx)]
    (loop [lst (urls conf)]
      (when-not (empty? lst) 
        (do (add-resource! data (-> lst first resource)) 
            (recur (rest lst)))))))

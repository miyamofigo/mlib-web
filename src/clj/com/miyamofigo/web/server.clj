(ns com.miyamofigo.web.server
  (:require [ring.adapter.jetty :as ring-adapter]
            [ring.util.servlet :as ring-servlet] 
            [com.miyamofigo.web.core :refer [->2 do2 first-arg]] 
            [com.miyamofigo.web.extern.jetty :as jetty]
            [com.miyamofigo.web.settings :as settings]))

(defmulti ctx identity) 
(defmulti config identity)
(defmulti servlet-holder first-arg)

(defmethod ctx :jetty [_] (jetty/ctx))
(defmethod config :jetty [_] (jetty/config))
(defmethod servlet-holder :jetty [_ app] (jetty/servlet-holder app)) 

(defmulti set-config! first-arg)
(defmulti add-resource! first-arg)
(defmulti add-servlet! first-arg)

(defmethod set-config! :jetty [_ ctx conf] (jetty/set-config! ctx conf))
(defmethod add-resource! :jetty [_ ctx conf] (jetty/add-resource! ctx conf))
(defmethod add-servlet! :jetty [_ ctx serv path] (jetty/add-servlet! ctx serv path))

(defmulti set-name! first-arg)
(defmulti set-handler! first-arg)

(defmethod set-name! :jetty [_ serv nam] (jetty/set-name! serv nam))
(defmethod set-handler! :jetty [_ serv handler] (jetty/set-handler! serv handler))

(defmulti servlet first-arg)
(defmethod servlet :ring [_ app] (ring-servlet/servlet app))

(defn build-servlet 
  ([app] (build-servlet app :ring))
  ([app typ] (servlet typ app))) 

(defn build-servlet-holder 
  ([app nam] (build-servlet-holder app nam :jetty)) 
  ([app nam typ] (do2 [typ (servlet-holder typ (build-servlet app))] (set-name! nam))))

(defn create-handler 
  ([app nam path] 
    (create-handler app nam path :jetty)) 
  ([app nam path typ]
    (let [conf (config typ)]
      (do2 [typ (ctx typ)]
        (add-resource! conf) (set-config! conf) (add-servlet! (build-servlet-holder app nam) path)))))

(defn build-configurator 
  ([app nam path] 
    (build-configurator app nam path :jetty))
  ([app nam path typ]
    (fn [server] 
      (do2 [typ server] 
        (set-handler! (create-handler app nam path)))))) 

(declare default-name root-path)

(defn run-jetty 
  ([handler cfg-builder options] 
    (run-jetty handler cfg-builder options ring-adapter/run-jetty))
  ([handler cfg-builder options driver]
    (run-jetty handler cfg-builder options driver default-name root-path))
  ([handler cfg-builder options driver nam path]
    (let [dummy (fn [] nil)]
      (driver dummy (assoc options :configurator (cfg-builder handler nam path)))))) 

(defonce default-name settings/default-name)
(defonce root-path settings/root-path)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;for testing;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
;(require '[compojure.core :refer :all])
;(require '[compojure.route :as route])
;(require '[ring.middleware.defaults :refer [wrap-defaults site-defaults]]));
;
;;setting 
;
;(defonce servername "jetty-spring")
;(defonce portnumber 8080)
;(defonce hostname "localhost")
;
;(defonce default-name "default") 
;(defonce root-path "/")
;
;;application
;
;(defroutes app-routes
;  (GET "/" [] "Hello World")
;  (route/not-found "Not Found"))
;
;;driver 
;
;(def app (-> app-routes (wrap-defaults site-defaults)))
;
;(defn -main []
;  (run-jetty app build-configurator 
;    {:servname servername, :port portnumber, :host hostname}))
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(ns com.miyamofigo.web.data
  (:require [clojure.data.json :as json] 
            [cognitect.transit :as transit]
            [com.miyamofigo.web.core :refer [to-str first-arg] :as core]
            [com.miyamofigo.web.string :as string]
            [com.miyamofigo.web.extern.jackson :as jackson]
            [com.miyamofigo.web.extern.spring :as spring]))

(defn- out [] (core/ostream :byte))

(defmulti writer first-arg)

(defmethod writer :output-stream [_ & args] (apply core/writer :output-stream args))
(defmethod writer :transit [_ & args] (apply transit/writer args))

(defmulti write! first-arg)

(defmethod write! :transit [_ writer value] (transit/write writer value))
(defmethod write! :clj-json [_ data] (json/write-str data))

(defn write-handler [tag handler & args]
  (apply transit/write-handler tag handler args))

(defn handlers [generator & args]
  (assoc {} :handlers (reduce generator {} args)))

(defmulti build-gen first-arg)

(defmethod build-gen :write-handler [_ handler]
  (fn [init {:keys 
              [type tag-fn rep-fn 
               str-rep-fn verbose-handler-fn]}]
    (assoc init type (handler tag-fn rep-fn str-rep-fn verbose-handler-fn))))

(defn write-handlers [& infos]
  (handlers (build-gen :write-handler write-handler) {} infos))

(defn type-info 
  ([type tag-fn from-rep] 
    (assoc {} :type type :tag-fn tag-fn :from-rep from-rep))
  ([type tag-fn from-rep rep-fn]
    (assoc (type-info tag-fn from-rep) :rep-fn rep-fn))
  ([type tag-fn from-rep rep-fn str-rep-fn]
    (assoc (type-info tag-fn from-rep rep-fn) :str-rep-fn str-rep-fn))
  ([type tag-fn from-rep rep-fn str-rep-fn verbose-handler-fn]
    (assoc (type-info tag-fn from-rep rep-fn str-rep-fn) :verbose-handler-fn verbose-handler-fn)))
   
(defn- in [barray] (core/istream :byte barray))

(defmulti reader first-arg)

(defmethod reader :transit [_ & args] (apply transit/reader args))

(defmulti read! first-arg) 

(defmethod read! :transit [_ reader] (transit/read reader)) 

(defn read-handler [from-rep]
  (transit/read-handler from-rep))

(defmethod build-gen :read-handler [_ handler]
  (fn [init {:keys [tag-fn from-rep]}]
    (assoc init 
           (if (fn? tag-fn) (tag-fn) tag-fn)
           (handler from-rep))))  

(defn read-handlers [& infos]
  (handlers (build-gen :read-handler read-handler) {} infos))

(defmulti -to-json first-arg)

(defmethod -to-json :map [_ data type] 
  (write! type data))

(defn map->json [data] 
  (-to-json :map data :clj-json))

(defmulti read-string first-arg)

(defmethod read-string :default [_ & args]
  (apply clojure.core/read-string args))
(defmethod read-string :jackson [_ & args]
  (apply jackson/read-string :value args))

(defn json->javaObj [string cl]
  (read-string :jackson (jackson/om) string cl))

(defn map->javaObj [m cl] 
  (json->javaObj (map->json m) cl))

(defn write-string [obj]
  (jackson/write-string :value obj))

(defn javaObj->json [obj] (write-string obj))

(defmethod read-string :clj-json [_ & args]
  (apply json/read-str args))

(defn json->map [json-str] 
  (read-string :clj-json json-str :key-fn keyword))

(defn javaObj->map [obj]
  (json->map (javaObj->json obj)))

(defn map->prop [m]
  (let [dst (core/prop)]
    (loop [src m]
      (when-let [[k v] (first src)]
        (core/prop! dst (string/keyword->str k) (str v))
        (recur (rest src))))
    dst))

(defn add-all-props! [ctx pseq]
  (loop [target (spring/prop-sources :app-ctx ctx), 
         pseq pseq]
    (when-let [propsmap (first pseq)]
      (spring/add! :props-source 
                   target 
                   (spring/wrap-props (string/keyword->str (first propsmap))
                                      (-> propsmap fnext map->prop)))
      (recur target (rest pseq)))))


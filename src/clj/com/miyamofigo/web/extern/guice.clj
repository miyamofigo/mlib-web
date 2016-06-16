(ns com.miyamofigo.web.extern.guice
  (:import java.util.Properties 
           [com.google.inject AbstractModule Binder Guice Injector Module]
           [com.google.inject.binder AnnotatedBindingBuilder ScopedBindingBuilder]))

(defmacro extend-module [& forms]
  `(proxy [AbstractModule] [] (configure [] ~@forms)))

(defn injector [& mods] 
  (Guice/createInjector (into-array Module mods)))

(defn instance [^Injector i, ^java.lang.Class cl] (.getInstance cl))

(defmacro get-binder [] `(proxy-super binder))

(defmacro bind! [^java.lang.Class cl] `(proxy-super bind cl))

(defn to [^AnnotatedBindingBuilder builder, ^java.lang.Class cl] (. builder (to cl)))

(defn inject-names! [^Binder b, ^Properties props] (. b (bindProperties props)))

(defn in [^ScopedBindingBuilder builder, ^java.lang.Class scope] (. builder (in scope)))  

(ns com.miyamofigo.web.core
  (:import com.miyamofigo.java8.nursery.Tuple))

(defn fetch-class [x & _] (class x))

(defn first-arg [x & _] x)

(defmacro ->2 [[typ x] & forms]
  (loop [x x, forms forms]
    (if forms
      (let [form (first forms)
            threaded (if (seq? form)
                       (with-meta `(~(first form) ~typ ~x ~@(next form)) (meta form))
                       (list form typ x))]
        (recur threaded (next forms)))
      x)))   
    
(defmacro do2 [[typ x] & forms]
  (let [gx (gensym), ty (gensym)]
    `(let [~gx ~x, ~ty ~typ]
       ~@(map (fn [f]
                (if (seq? f)
                  `(~(first f) ~ty ~gx ~@(next f))
                  `(~f ~ty ~gx)))
            forms)
       ~gx)))

(defmulti add first-arg)

(defmethod add :set [_ s el] (conj s el))

(defn in? [el seq+] (some #(= el %) seq+))  

(defmulti random identity)

(defmethod random :default [_] (new java.security.SecureRandom)) 

(defn salt [^java.security.SecureRandom r, seed] (.generateSeed r seed))

(defn predicate [f] 
  (reify java.util.function.Predicate (test [this v] (f v))))

(defn consumer [f!]
  (reify java.util.function.Consumer
    (accept [this arg] (f! arg))
    (andThen [this after!] (fn [x] (-> x f! after!)))))

(defn supplier [f] 
  (reify java.util.function.Supplier (get [this] (f)))) 

(defprotocol Wrapper
  (unwrap [this])
  (unwrap-or [this other])
  (unwrap-or-else [this f])
  (and* [this x])
  (and-then [this f])
  (or* [this other])
  (or-else [this f])
  (map* [this mapper])
  (filter* [this pred]))

(defprotocol Hashable 
  (hash* [this]))

(defprotocol Eq
  (eq?? [this other]))

(defprotocol Option
  (some?? [this])
  (none?? [this]))

(deftype Opt [optional])

(defn somew [v] (-> v java.util.Optional/of Opt.))
(defn none [] (Opt. (java.util.Optional/empty)))
(defn wrap-opt [v] (-> v java.util.Optional/ofNullable Opt.))

(extend-type Opt
  Wrapper
  (unwrap [this] 
    (.. this optional get))
  (unwrap-or [this other] 
    (.. this optional (orElse other)))
  (unwrap-or-else [this f]
    (.. this (orElseGet (supplier f))))
  (and* [this _]
    (throw (UnsupportedOperationException. "and method is not implemented.")))
  (and-then [this f]
    (if (some? this) (wrap-opt (f)) this))
  (or* [this other]
    (if (some? this) this other))
  (or-else [this f]
    (-> this (unwrap-or-else f) somew))
  (map* [this mapper] 
    (.. this optional (map mapper)))
  (filter* [this pred] 
    (.. this optional (filter (predicate pred))))

  Hashable
  (hash* [this] 
    (.. this optional hashCode))
  
  Eq
  (eq?? [this other]
    (.. this optional (equals (.optional other))))

  Option
  (some?? [this] (.. this optional isPresent))
  (none?? [this] (not (.. this optional isPresent))))

(defprotocol ResultHelper
  (ok? [this])
  (err? [this])
  (unwrap-err [this])
  (map-err [this f]))

(deftype Res [result])

(defn ok [v] (-> v com.miyamofigo.java8.nursery.Result/ok Res.))
(defn err [e] (-> e com.miyamofigo.java8.nursery.Result/err Res.))

(deftype Res [result]
  Wrapper
  (unwrap [this] 
    (.. this result unwrap))
  (unwrap-or [this optb]
    (.. this result (unwrapOr optb)))
  (unwrap-or-else [this f]
    (.. this result (unwrapOrElse f)))
  (and* [this res] 
    (Res. (.. this result (and res))))
  (and-then [this f]
    (Res. (.. this result (andThen f))))
  (or* [this optb]
    (Res. (.. this result (or optb))))
  (or-else [this f]
    (Res. (.. this result (orElse f))))
  (map* [this f]
    (Res. (.. this result (map f))))
  (filter* [this pred]
    (.. this result iter (filter pred) (rewrap ok)))

  ResultHelper
  (ok? [this] 
    (.. this result isOk))
  (err? [this]
    (.. this result isErr))
  (unwrap-err [this]
    (.. this result unwrapErr))
  (map-err [this f]
    (Res. (.. this result (map-err f)))))

(defn ordinal [en] 
  (if (instance? Enum en) 
    (ok (.ordinal en))
    (err "an Enum instance expected")))

(defn -enum->int [maybe & [optb]]
  (let [res (ordinal maybe)] 
    (if optb (unwrap-or res optb) res)))

(defmulti enum->int first-arg)

(defmethod enum->int :default 
  [_ maybe & [optb]] (-enum->int maybe (if optb optb -1)))
(defmethod enum->int :wrapped [_ maybe] (-enum->int maybe))

(defn subclass? [cl base] (. base isAssignableFrom cl)) 

(defn prop [] (java.util.Properties.))

(defmulti load first-arg)

(defmethod load :default [_ & paths] (apply clojure.core/load paths))
(defmethod load :properties [_, ^java.util.Properties prop, istream] 
  (. prop (load istream)))

(defn classloader [] (java.lang.ClassLoader/getSystemClassLoader))

(defn resource [^java.lang.ClassLoader loader, filename] (. loader (getResource filename)))

(defn url->uri [^java.net.URL url] (.toURI url))

(defn uri->file [^java.net.URI uri] (java.io.File. uri))

(defn istream [^java.net.URI uri] (-> uri uri->file java.io.FileInputStream.))

(defn file->istream [filename] (-> (classloader) (resource filename) url->uri istream))

(defmulti load-file first-arg)

(defmethod load-file :default [_ & paths] (apply clojure.core/load-file paths))
(defmethod load-file :properties [_ prop filename] (load :properties (file->istream filename)))

(defn prop! [p k v] (. p (setProperty k v)))

(defmulti get-name fetch-class)

(defmethod get-name java.lang.Class [cl] (.getName cl))

(defn fields [cl] (.getDeclaredFields cl))

(defmethod get-name java.lang.reflect.Field [f] (.getName f))

(defmulti instance first-arg)

(defmethod instance :class [_ classpath] (java.lang.Class/forName classpath)) 

(defmulti ostream first-arg)

(defmethod ostream :byte [_ & [size]] 
  (if size
    (java.io.ByteArrayOutputStream. size)
    (java.io.ByteArrayOutputStream.)))

(defmulti to-str fetch-class)

(defmethod to-str java.io.OutputStream [stream] (.toString stream))

(defmulti byte-array first-arg)

(defmethod byte-array :default [_ & args] (apply clojure.core/byte-array args))
(defmethod byte-array :output-stream [_ stream] 
  (.toByteArray stream))

(defmulti istream first-arg)

(defmethod istream :byte 
  ([_ buf] (java.io.ByteArrayInputStream. buf))
  ([_ buf offset len] (java.io.ByteArrayInputStream. buf offset len)))

(defmulti writer first-arg)

(defmethod writer :output-stream 
  ([_ out] (java.io.OutputStreamWriter. out))
  ([_ out charset] (java.io.OutputStreamWriter. out charset)))

(defmulti builder first-arg)
(defmethod builder :calendar [_]  (java.util.Calendar$Builder.))

(defn week-date! [^java.util.Calendar$Builder builder, year, cnt, dayOfWeek]
  (.setWeekDate builder year cnt dayOfWeek))

(defn date! [^java.util.Calendar$Builder builder, year, month, day]
  (.setDate builder year month day))

(defmulti build first-arg)
(defmethod build :calendar [_, ^java.util.Calendar$Builder builder]
  (.build builder))
  
(defn tuple 
  ([] (Tuple/empty))
  ([t1] (Tuple/of t1))
  ([t1 t2] (Tuple/of t1 t2))
  ([t1 t2 t3] (Tuple/of t1 t2 t3))
  ([t1 t2 t3 t4] (Tuple/of t1 t2 t3 t4))
  ([t1 t2 t3 t4 t5] (Tuple/of t1 t2 t3 t4 t5)))

(defmulti key-gen first-arg)
(defmethod key-gen :secret-key [_ algo] 
  (javax.crypto.KeyGenerator/getInstance algo))

(defmulti generate first-arg)
(defmethod generate :secret-key 
  [_, ^javax.crypto.KeyGenerator g]
  (.generateKey g))

(defn -encoded [^javax.crypto.SecretKey k] 
  (.getEncoded k))

(defmethod generate :secret-bytes-key [_, ^javax.crypto.KeyGenerator g]
  (->> g (generate :secret-key) -encoded)


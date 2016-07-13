(ns com.miyamofigo.web.extern.spring
  (:require [com.miyamofigo.web.core :refer [first-arg fetch-class]])
  (:import 
    java.util.Properties
    org.springframework.context.ApplicationContext
    org.springframework.context.annotation.AnnotationConfigApplicationContext
    [org.springframework.core.env ConfigurableEnvironment MutablePropertySources PropertiesPropertySource]
    org.springframework.security.core.Authentication
    [org.springframework.security.core.context SecurityContext SecurityContextHolder]
    org.springframework.security.crypto.password.PasswordEncoder
    [org.springframework.security.jwt Jwt JwtHelper]
    [org.springframework.security.jwt.crypto.sign MacSigner SignatureVerifier Signer]))

(defmulti add! first-arg)
(defmulti auth fetch-class)
(defmulti auth! fetch-class)
(defmulti current-ctx first-arg)
(defmulti ctx! first-arg)
(defmulti decode first-arg)
(defmulti encode first-arg)
(defmulti encoded first-arg)
(defmulti get-env first-arg)
(defmulti get-user fetch-class)
(defmulti matches first-arg)
(defmulti princ fetch-class)
(defmulti prop-sources first-arg) 
(defmulti refresh! first-arg)
(defmulti register! first-arg)
(defmulti signer first-arg)
(defmulti verify! first-arg)

(defmethod current-ctx :security [_]
  (SecurityContextHolder/getContext))

(defmethod auth SecurityContext [ctx]
  (.getAuthentication ctx))

(defmethod princ Authentication [auth]
  (.getPrincipal auth))

(defn- -username [principal] 
  (.getUsername principal))

(defmethod get-user SecurityContext [ctx]
  (-> ctx auth princ -username))

(defmethod ctx! :security [_, ^SecurityContext ctx] 
  (SecurityContextHolder/setContext ctx)) 

(defmethod auth! SecurityContext [ctx, ^Authentication auth]
  (. ctx (setAuthentication auth)))

(defn app-ctx  
  ([] (AnnotationConfigApplicationContext.))
  ([& cls] (AnnotationConfigApplicationContext. (into-array java.lang.Class cls))))

(defmethod register! :app-ctx [_, ^ApplicationContext ctx, & configs]
  (when-not (empty? configs) 
    (.register ctx (into-array java.lang.Class configs))))

(defmethod refresh! :app-ctx [_, ^ApplicationContext ctx] 
  (.refresh ctx))

(defn get-bean 
  ([^ApplicationContext ctx, ^java.lang.Class target] 
    (.getBean ctx target))
  ([^ApplicationContext ctx, ^java.lang.Class target, ^java.lang.String nam] 
    (.getBean ctx nam target)))

(defmethod get-env :app-ctx [_, ^ApplicationContext ctx]
  (.getEnvironment ctx))

(defmethod prop-sources :env [_, ^ConfigurableEnvironment env]
  (.getPropertySources env))

(defmethod prop-sources :app-ctx [_, ^ApplicationContext ctx]
  (prop-sources :env (get-env :app-ctx ctx)))

(defmethod add! :props-source 
  [_, ^MutablePropertySources sources, ^PropertiesPropertySource new-source]
  (.addLast sources new-source))

(defn wrap-props [nam, ^Properties props] (PropertiesPropertySource. nam props))

(defn add-props! [^ApplicationContext ctx, nam, ^Properties props]
  (let [sources (-> (get-env :app-ctx ctx) prop-sources)]
    (add! :props-source (wrap-props nam props)))) 

(defmethod encode :password [_, ^PasswordEncoder encoder, ^java.lang.String s] 
  (. encoder (encode s)))

(defmethod matches :password 
  [_, ^PasswordEncoder encoder, ^java.lang.String raw, ^java.lang.String encoded]
  (. encoder (matches raw encoded))) 

(defmethod signer :mac [_ secret]
  (MacSigner. secret))

(defmethod ^Jwt encode :jwt 
  [_, ^java.lang.CharSequence content, ^Signer s]
  (JwtHelper/encode content s))

(defmethod ^Jwt decode :jwt [_, ^java.lang.String token]
  (JwtHelper/decode token))

(defmethod ^java.lang.String encoded :jwt [_, ^Jwt token]
  (.getEncoded token))

(defmethod verify! :jwt [_, ^Jwt token, ^SignatureVerifier v]
  (. token (verifySignature v)))

(defn ^java.lang.String claims [^Jwt token]
  (.getClaims token))

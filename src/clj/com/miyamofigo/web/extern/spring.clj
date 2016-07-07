(ns com.miyamofigo.web.extern.spring
  (:require [com.miyamofigo.web.core :refer [first-arg]])
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

(defn get-bean 
  ([^ApplicationContext ctx, ^java.lang.Class target] 
    (.getBean ctx target))
  ([^ApplicationContext ctx, ^java.lang.Class target, ^java.lang.String nam] 
    (.getBean ctx nam target)))

(defmulti get-env first-arg)
(defmethod get-env :app-ctx [_, ^ApplicationContext ctx]
  (.getEnvironment ctx))

(defmulti prop-sources first-arg) 
(defmethod prop-sources :env [_, ^ConfigurableEnvironment env]
  (.getPropertySources env))

(defmethod prop-sources :app-ctx [_, ^ApplicationContext ctx]
  (prop-sources :env (get-env :app-ctx ctx)))

(defmulti add! first-arg)
(defmethod add! :props-source 
  [_, ^MutablePropertySources sources, ^PropertiesPropertySource new-source]
  (.addLast sources new-source))

(defn wrap-props [nam, ^Properties props] (PropertiesPropertySource. nam props))

(defn add-props! [^ApplicationContext ctx, nam, ^Properties props]
  (let [sources (-> (get-env :app-ctx ctx) prop-sources)]
    (add! :props-source (wrap-props nam props)))) 

(defmulti encode first-arg)
(defmethod encode :password [_, ^PasswordEncoder encoder, ^java.lang.String s] 
  (. encoder (encode s)))

(defmulti matches first-arg)
(defmethod matches :password 
  [_, ^PasswordEncoder encoder, ^java.lang.String raw, ^java.lang.String encoded]
  (. encoder (matches raw encoded))) 

(defmulti signer first-arg)
(defmethod signer :mac [_ secret]
  (MacSigner. secret))

(defmethod ^Jwt encode :jwt 
  [_, ^java.lang.CharSequence content, ^Signer s]
  (JwtHelper/encode content s))

(defmulti decode first-arg)
(defmethod ^Jwt decode :jwt [_, ^java.lang.String token]
  (JwtHelper/decode token))

(defmulti encoded first-arg)
(defmethod ^java.lang.String encoded :jwt [_, ^Jwt token]
  (.getEncoded token))

(defmulti verify! first-arg)
(defmethod verify! :jwt [_, ^Jwt token, ^SignatureVerifier v]
  (. token (verifySignature v)))

(defn ^java.lang.String claims [^Jwt token]
  (.getClaims token))

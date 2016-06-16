(defproject mlib-web "0.1.0-SNAPSHOT"
  :description "Library for developing web media api with Java, Clojure"
  :source-paths ["src/clj"]
  :java-source-paths ["test/java"]
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/data.json "0.2.6"] 
                 [ring "1.4.0"]
                 [ring/ring-defaults "0.2.0"]
                 [compojure "1.5.0"]
                 [com.cognitect/transit-clj "0.8.285"]
                 [com.fasterxml.jackson.core/jackson-core "2.7.4"]
	               [com.fasterxml.jackson.core/jackson-databind "2.7.4"]
                 [com.google.inject/guice "4.0"]
                 [org.apache.commons/commons-pool2 "2.4.2"]
                 [org.bouncycastle/bcprov-ext-jdk15on "1.54"]
                 [org.eclipse.jetty/jetty-annotations  "9.3.9.M1"]
                 [org.eclipse.jetty/jetty-server  "9.3.9.M1"]
                 [org.eclipse.jetty/jetty-servlet "9.3.9.M1"]
                 [org.eclipse.jetty/jetty-util "9.3.9.M1"]
                 [org.eclipse.jetty/jetty-webapp "9.3.9.M1"]
                 [org.springframework/spring-beans "4.3.0.RELEASE"]
                 [org.springframework/spring-context "4.3.0.RELEASE"]
                 [org.springframework.security/spring-security-core "4.1.0.RELEASE"]
                 [org.springframework.security/spring-security-web "4.1.0.RELEASE"]
                 [com.miyamofigo/java8.nursery "0.2.0-SNAPSHOT"]]
  :repositories [["ghpages" "http://miyamofigo.github.io/java8-nursery"]]
  :profiles
  {:dev {:dependencies [[javax.servlet/javax.servlet-api "3.1.0"]
                        [ring/ring-mock "0.3.0"]]}})

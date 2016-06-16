(ns com.miyamofigo.web.extern.spring.config-test
  (:require 
    [clojure.test :refer :all]
    [com.miyamofigo.web.core :refer [do2]]
    [com.miyamofigo.web.extern.spring :refer :all])
  (:import 
    [com.miyamofigo.web.extern.spring 
      AnnotationConfigTestContext$AutowiredMethodConfig
      AnnotationConfigTestContext$ColorConfig Colour TestBean]))

(defn get-name [^TestBean bean] (.getName bean))

(deftest config-test
  (testing "autowired configuration method dependencies with ctr"
    (let [ctx (app-ctx AnnotationConfigTestContext$AutowiredMethodConfig 
                       AnnotationConfigTestContext$ColorConfig)]
      (is (= (get-bean ctx Colour) Colour/RED))
      (is (= (get-name (get-bean ctx TestBean)) "RED-RED"))))
  (testing "autowired configuration method dependencies with registerar"
    (let [ctx (do2 [:app-ctx (app-ctx)]
                (register! AnnotationConfigTestContext$AutowiredMethodConfig 
                           AnnotationConfigTestContext$ColorConfig)
                refresh!)]
      (is (= (get-bean ctx Colour) Colour/RED))
      (is (= (get-name (get-bean ctx TestBean)) "RED-RED")))))



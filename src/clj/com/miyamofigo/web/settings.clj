(ns com.miyamofigo.web.settings)

(defonce default-name "default")
(defonce root-path "/")

(defonce salt-bits 128)
(defonce salt-bytes (/ salt-bits 8))  
(defonce salt-str "afdc02011503cac2821815273722fcea")
(defonce crypto-cost 4) 

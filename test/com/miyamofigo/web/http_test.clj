(ns com.miyamofigo.web.http-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [com.miyamofigo.web.crypto :refer [gen-hash]]
            [com.miyamofigo.web.http :refer :all]
            [com.miyamofigo.web.util.http :refer [build-mock-request set-mock-auth!]]))

(defonce method "get")
(defonce url "/")
(defonce credential "Basic dXNlcjpwYXNzd29yZA==")

(deftest hasher-test
  (testing "req->hash function"
    (let [req (build-mock-request method url credential)]
      (is (= (gen-hash "dXNlcjpwYXNzd29yZA==") (req->hash req))))))
         

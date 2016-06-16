(ns com.miyamofigo.web.string
  (:require [clojure.string :refer [split upper-case lower-case starts-with?]]))

(defonce REG_SPACE #" ")
(defonce REG_APOSTROPHY #"\.")

(defn- split-n-? [src func & [delim]] 
  (-> src (split (if (nil? delim) REG_SPACE delim)) func))

(defn split-n-first [src delim] (split-n-? src first delim))
(defn split-n-first-withA [string] (split-n-first string REG_APOSTROPHY))

(defn split-n-last [src delim] (split-n-? src last delim))
(defn split-n-take-second [src] (split-n-? src fnext))
(defn split-n-last-withA [string] (split-n-last string REG_APOSTROPHY))

(defn uppercase? [^java.lang.Character c] (java.lang.Character/isUpperCase c)) 
(defn lowercase? [^java.lang.Character c] (java.lang.Character/isLowerCase c)) 

(defonce CHAR_SPACE \space)
(defonce CHAR_HYPHEN \-)

(defn vecc-with-prefix [prefix c] (vector prefix c))
(defn vecc-with-space [c] (vecc-with-prefix CHAR_SPACE c))
(defn vecc-with-hyp [c] (vecc-with-prefix CHAR_HYPHEN c))

(defn lower-case-char [^java.lang.Character c] (java.lang.Character/toLowerCase c))
(defn upper-case-char [^java.lang.Character c] (java.lang.Character/toUpperCase c))

(defn uc->lc->vec 
  ([c] (uc->lc->vec c vector))
  ([c func] (if (uppercase? c) (-> c lower-case-char func) (vector c)))) 

(defn uc->space+lc->vec [c] (uc->lc->vec c vecc-with-space))
(defn uc->hyp+lc->vec [c] (uc->lc->vec c vecc-with-hyp))

(defn op-first-char [op, ^java.lang.String s] (str (-> s first op) (subs s 1)))

(defn uc-first-char [^java.lang.String s] (op-first-char upper-case-char s))
(defn lc-first-char [^java.lang.String s] (op-first-char lower-case-char s))

(defn no-uppercase? [^java.lang.String s] (not-any? uppercase? s))

(declare str-converter-skelton)

(defn javaStr-converter-helper
  ([^java.lang.String s, str->carray] (javaStr-converter-helper s str->carray nil)) 
  ([^java.lang.String s, str->carray, finalizer]
     (javaStr-converter-helper s str->carray (partial drop 1) finalizer))
  ([^java.lang.String s, str->carray, char-op, finalizer]
     (str-converter-skelton s str->carray uc->hyp+lc->vec char-op finalizer)))

(defn ClassName->clj-str [^java.lang.String s] (javaStr-converter-helper s char-array)) 

(defn getterName->clj-key [^java.lang.String s] (javaStr-converter-helper s (partial drop 3) keyword)) 

(defn extra-op-helper [func arg] (if (nil? func) (identity arg) (func arg))) 

(defn str-converter-skelton 
  [^java.lang.String s, str->carray, converter & [char-op finalizer]]
  (->> s str->carray (map converter) (apply concat)
    (extra-op-helper char-op) (apply str) (extra-op-helper finalizer))) 

(defn javaFieldName->clj-str [^java.lang.String s] (javaStr-converter-helper s char-array nil nil))

(defn ClassName->clj-key [^java.lang.String s] (-> s ClassName->clj-str keyword))

(defn drop-last-n-tostring [^java.lang.String s, n] (->> s (drop-last n) (apply str)))

(defn keyword->str [k] (->> k str (drop 1) (apply str)))

(defn str->bytes [^java.lang.String s] (.getBytes s))

(defn bytes->str [b] (java.lang.String. b)) 


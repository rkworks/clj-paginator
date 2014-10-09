(ns clj-paginator.core-test
  (:require [midje.sweet :refer :all]
            [clj-paginator.core :refer :all]
            [clj-paginator.utils :as u]
            [korma.db :refer :all]
            [korma.core :refer :all]
            [clojure.java.jdbc :as sql]))


;; (defdb mem-db (h2 {:db "mem:test"}))

;; (defn truncate [table]
;;   (exec-raw [(str "TRUNCATE " table)]))

;; (exec-raw "CREATE TABLE user (
;;   id int(11) NOT NULL,
;;   name varchar(255) DEFAULT NULL,
;;   PRIMARY KEY (\"id\")
;; )")



;; (defentity user
;;   (table "USER"))

;; (select user)

;; (facts "guess-target-type"
;;   (fact "collection"
;;     (guess-target-type [0 1 2]) => :collection)
;;   (fact "lazy seq"
;;     (guess-target-type (range 10)) => :lazy)
;;   (fact "Korma"
;;     (guess-target-type (select* :user)) => :korma))

;; (facts "paginate"
;;   (fact "should have valid schema."
;;     (paginate [1 2 3] 1) => (contains {:type :collection
;;                                        :page integer?
;;                                        :limit integer?
;;                                        ;; :total-count (some-checker integer? nil?)
;;                                        :target [1 2 3]
;;                                        ;; :count integer?
;;                                        :window integer?
;;                                        :renderer anything
;;                                        ;; :outer-window integer?
;;                                        ;; :has-next? anything
;;                                        ;; :has-previous?? anything
;;                                        ;; :previous (some-checker integer? nil?)
;;                                        ;; :next (some-checker integer? nil?)
;;                                        ;; :first-item-index (some-checker integer? nil?)
;;                                        ;; :last-item-index (some-checker integer? nil?)
;;                                        }))
;;   (fact "custom target type"
;;     (paginate [] 1 {:limit 1 :type :my-type}) => (contains {:type :my-type})))

;; (facts "get total count"
;;   (fact ":collection"
;;     (get-total-count (paginate [1 2 3] 1)) => 3)
;;   (fact ":lazy return nil"
;;     (get-total-count (paginate (range 1) 1)) => nil))


(facts "get-pages-in-window"
  (tabular
   (fact "center"
     (get-pages-in-window ?page 10 1) => ?ret)
   ?page ?ret
   5     [4 5 6]
   1     [1 2 3]
   2     [1 2 3]
   3     [2 3 4]
   4     [3 4 5]
   9     [8 9 10]
   10    [8 9 10]))

(facts "renderer"
  (facts "default renderer"
    (fact "simple collection"
      (render (paginate (range 1 6) 3 {:limit 1})) => [:ul {:class "pagination"}
                                                       [:li nil [:a {:href "#"} "&laquo;"]]
                                                       [:li nil [:a {:href "#"} "1"]]
                                                       [:li nil [:a {:href "#"} "2"]]
                                                       [:li {:class "active"}
                                                        [:a {:href "#"} "3"]]
                                                       [:li nil [:a {:href "#"} "4"]]
                                                       [:li nil [:a {:href "#"} "5"]]
                                                       [:li nil [:a {:href "#"} "&raquo;"]]])
    (fact "link attributes"
      (render (paginate (range 1 3) 1 {:limit 1})) => [:ul {:class "pagination"}
                                                       [:li {:class "disabled"} [:a {:href "#"} "&laquo;"]]
                                                       [:li {:class "active"} [:a {:href "#"} "1"]]
                                                       [:li nil [:a {:href "#"} "2"]]
                                                       [:li nil [:a {:href "#"} "&raquo;"]]]
      (render (paginate (range 1 3) 2 {:limit 1})) => [:ul {:class "pagination"}
                                                       [:li nil [:a {:href "#"} "&laquo;"]]
                                                       [:li nil [:a {:href "#"} "1"]]
                                                       [:li {:class "active"} [:a {:href "#"} "2"]]
                                                       [:li {:class "disabled"} [:a {:href "#"} "&raquo;"]]])

    (fact "sliding"
      (render (paginate (range 1 6) 1 {:limit 1 :window 0})) => [:ul {:class "pagination"}
                                                                 [:li {:class "disabled"} [:a {:href "#"} "&laquo;"]]
                                                                 [:li {:class "active"} [:a {:href "#"} "1"]]
                                                                 [:li {:class "disabled"}
                                                                  [:span "&hellip;"]]
                                                                 [:li nil [:a {:href "#"} "5"]]
                                                                 [:li nil [:a {:href "#"} "&raquo;"]]]

      (render (paginate (range 1 6) 2 {:limit 1 :window 0})) => [:ul {:class "pagination"}
                                                                 [:li nil [:a {:href "#"} "&laquo;"]]
                                                                 [:li nil [:a {:href "#"} "1"]]
                                                                 [:li {:class "active"} [:a {:href "#"} "2"]]
                                                                 [:li {:class "disabled"}
                                                                  [:span "&hellip;"]]
                                                                 [:li nil [:a {:href "#"} "5"]]
                                                                 [:li nil [:a {:href "#"} "&raquo;"]]]

      (render (paginate (range 1 6) 3 {:limit 1 :window 0})) => [:ul {:class "pagination"}
                                                                 [:li nil [:a {:href "#"} "&laquo;"]]
                                                                 [:li nil [:a {:href "#"} "1"]]
                                                                 [:li nil [:a {:href "#"} "2"]]
                                                                 [:li {:class "active"} [:a {:href "#"} "3"]]
                                                                 [:li nil [:a {:href "#"} "4"]]
                                                                 [:li nil [:a {:href "#"} "5"]]
                                                                 [:li nil [:a {:href "#"} "&raquo;"]]]

      (render (paginate (range 1 6) 4 {:limit 1 :window 0})) => [:ul {:class "pagination"}
                                                                 [:li nil [:a {:href "#"} "&laquo;"]]
                                                                 [:li nil [:a {:href "#"} "1"]]
                                                                 [:li {:class "disabled"}
                                                                  [:span "&hellip;"]]
                                                                 [:li {:class "active"} [:a {:href "#"} "4"]]
                                                                 [:li nil [:a {:href "#"} "5"]]
                                                                 [:li nil [:a {:href "#"} "&raquo;"]]]

      (render (paginate (range 1 6) 5 {:limit 1 :window 0})) => [:ul {:class "pagination"}
                                                                 [:li nil [:a {:href "#"} "&laquo;"]]
                                                                 [:li nil [:a {:href "#"} "1"]]
                                                                 [:li {:class "disabled"}
                                                                  [:span "&hellip;"]]
                                                                 [:li {:class "active"} [:a {:href "#"} "5"]]
                                                                 [:li {:class "disabled"} [:a {:href "#"} "&raquo;"]]])))

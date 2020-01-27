(ns br.com.souenzzo.eql-datomic-test
  (:require [br.com.souenzzo.eql-datomic :as eqld]
            [clojure.test :refer [deftest is testing]]
            [datomic.api :as d]
            [edn-query-language.core :as eql]))


(deftest eql-datomic
  (let [db-uri (doto "datomic:mem://test-db"
                 (d/create-database))
        query `[:user/id
                (:user/name {:as "name"})
                {(:user/address {:as "address"})
                 [:address/street]}]
        conn (d/connect db-uri)
        tx-schema [{:db/ident       :user/id
                    :db/valueType   :db.type/bigint
                    :db/cardinality :db.cardinality/one
                    :db/unique      :db.unique/identity}
                   {:db/ident       :user/name
                    :db/valueType   :db.type/string
                    :db/cardinality :db.cardinality/one}
                   {:db/ident       :user/address
                    :db/valueType   :db.type/ref
                    :db/cardinality :db.cardinality/one}
                   {:db/ident       :address/street
                    :db/valueType   :db.type/string
                    :db/cardinality :db.cardinality/one}]
        tx-data [{:user/id      1N
                  :user/name    "Alex"
                  :user/address "home"}
                 {:db/id          "home"
                  :address/street "Atlantica"}]
        {:keys [db-after]} @(d/transact conn tx-schema)
        {:keys [db-after tempids]} (d/with db-after tx-data)]
    (is (= (-> query
               eql/query->ast
               eqld/ast->query)
           `[:user/id
             (:user/name :as "name")
             {(:user/address :as "address")
              [:address/street]}]))
    (is (= (-> query
               eql/query->ast
               eqld/ast->query
               eqld/query->ast)
           (eql/query->ast query)))
    (is (=
          (d/pull db-after (-> query
                               eql/query->ast
                               eqld/ast->query)
                  [:user/id 1])
          {"address" #:address{:street "Atlantica"}
           "name"    "Alex"
           :user/id  1N}))
    (d/delete-database db-uri)))

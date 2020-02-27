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
          {"address" {:address/street "Atlantica"}
           "name"    "Alex"
           :user/id  1N}))
    (d/delete-database db-uri)))

(deftest recursive-query
  (let [query '[{:foo ...}]]
    (is (= (eql/query->ast query)
           (eqld/query->ast query)))
    (is (= query
           (eqld/ast->query (eqld/query->ast query))))))


(deftest join+params
  (let [query '[({:foo-out [:bar-out]} :as :out)
                {(:foo-in :as :in)
                 [:bar-in]}]]
    (is (= '[({:foo-out [:bar-out]}
              {:as :out})
             ({:foo-in [:bar-in]}
              {:as :in})]
           (eql/ast->query (eqld/query->ast query))))
    (is (= '[{(:foo-out
                :as
                :out) [:bar-out]}
             {(:foo-in
                :as
                :in) [:bar-in]}]
           (eqld/ast->query (eqld/query->ast query))))))


(deftest ast-metadata
  (let [datomic-query '[(:foo :as :bar)]]
    (is (= {:children [{:dispatch-key :foo
                        :key          :foo
                        :meta         {:column 25
                                       :line   84}
                        :params       {:as :bar}
                        :type         :prop}]
            :type     :root}
           (eqld/query->ast datomic-query)))))

(deftest union-query
  (let [query [{:foo {:a [:a]
                      :b [:b]}}]]
    (is (= [{:foo [:a :b]}]
           (eqld/ast->query (eql/query->ast query))))))

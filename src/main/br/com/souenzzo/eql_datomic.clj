(ns br.com.souenzzo.eql-datomic
  (:require [edn-query-language.core :as eql]))

(defn ast->query
  [{:keys [children]}]
  (into []
        (map (fn [{:keys [dispatch-key params children] :as node}]
               (cond
                 (and children params) {(cons dispatch-key (mapcat identity params))
                                        (ast->query node)}
                 children {dispatch-key (ast->query node)}
                 params (cons dispatch-key (mapcat identity params))
                 :else dispatch-key)))
        children))

(declare query->ast)

(defn- el->node
  [el]
  (cond
    (map? el) (let [[el children] (first el)
                    ast (query->ast children)]
                (assoc (el->node el)
                  :type :join
                  :query (eql/ast->query ast)
                  :children (:children ast)))
    (coll? el) {:type         :prop
                :dispatch-key (first el)
                :key          (first el)
                :params       (apply hash-map (rest el))}
    (keyword? el) {:type         :prop
                   :dispatch-key el
                   :key          el}
    :else {:dispatch-key el
           :key          el}))

(defn query->ast
  [query]
  {:type     :root
   :children (into []
                   (map el->node)
                   query)})

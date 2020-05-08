(ns br.com.souenzzo.eql-datomic
  (:require [edn-query-language.core :as eql]))

(defn ast->query
  [{:keys [children]}]
  (into []
        (mapcat (fn [{:keys [dispatch-key params children type query] :as node}]
                  (cond
                    (= type :union) (into []
                                          (mapcat ast->query)
                                          children)
                    (and children params) [{(cons dispatch-key (mapcat identity params))
                                            (ast->query node)}]
                    children [{dispatch-key (ast->query node)}]
                    params [(cons dispatch-key (mapcat identity params))]
                    query [{dispatch-key query}]
                    :else [dispatch-key])))
        children))

(declare query->ast)

(defn- el->node
  [el]
  (let [meta-value (meta el)]
    (cond
      (map? el) (for [[el children] el
                      :let [ast (when (coll? children)
                                  (query->ast children))]]
                  (cond-> (assoc (first (el->node el))
                            :type :join
                            :query (if ast
                                     (eql/ast->query ast)
                                     children))
                          ast (assoc :children (:children ast))
                          meta-value (assoc :meta meta-value)))
      (coll? el) [(cond-> (assoc (first (el->node (first el)))
                            :params (apply hash-map (rest el)))
                          meta-value (assoc :meta meta-value))]
      (keyword? el) [(cond-> {:type         :prop
                              :dispatch-key el
                              :key          el}
                             meta-value (assoc :meta meta-value))]
      :else [{:dispatch-key el
              :key          el}])))

(defn query->ast
  [query]
  {:type     :root
   :children (into []
                   (mapcat el->node)
                   query)})

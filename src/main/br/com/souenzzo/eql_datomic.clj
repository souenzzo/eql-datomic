(ns br.com.souenzzo.eql-datomic)

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
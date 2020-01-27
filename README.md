# eql-datomic

Same functions to connect datomic [pull syntax](https://docs.datomic.com/on-prem/pull.html) into/back from [EQL](http://edn-query-language.org).

## Usage

Add to your `deps.edn`
```clojure
br.com.souenzzo/eql-datomic{:git/url "https://github.com/souenzzo/eql-datomic.git"
                            :sha     "8b964b2f09af40a5e62e81648b34510875a70933"}
```

Turn datomic pull into a EQL query

```clojure
;; (require '[br.com.souenzzo.eql-datomic :as eqld]
;;          '[edn-query-language.core :as eql])

(-> `[(:user/name :as "name")]
    eqld/query->ast
    eql/ast->query)
```

Turn a EQL Query into datomic pull

```clojure
;; (require '[br.com.souenzzo.eql-datomic :as eqld]
;;          '[edn-query-language.core :as eql])

(-> `[(:user/name {:as "name"})]
    eql/query->ast
    eqld/ast->query)
```

# eql-datomic

Same functions to connect datomic [pull syntax](https://docs.datomic.com/on-prem/pull.html) into/back from [EQL](http://edn-query-language.org).

## Usage

Add to your `deps.edn`
```clojure
br.com.souenzzo/eql-datomic{:git/url "https://github.com/souenzzo/eql-datomic.git"
                            :sha     "3b96d90b2dc3a6a46935d11a1dffb630a0287725"}
```

Turn datomic pull into a EQL query

```clojure
;; (require '[br.com.souenzzo.eql-datomic :as eqld]
;;          '[edn-query-language.core :as eql])

(-> `[(:user/name :as "name")]
    eqld/query->ast
    eql/ast->query)
;; => [(:user/name {:as "name"})]
```

Turn a EQL Query into datomic pull

```clojure
;; (require '[br.com.souenzzo.eql-datomic :as eqld]
;;          '[edn-query-language.core :as eql])

(-> `[(:user/name {:as "name"})]
    eql/query->ast
    eqld/ast->query)
;; => [(:user/name :as "name")]
```

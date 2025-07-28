# clj-utils

Simon's Clojure utilities.

**This documentation tracks the `master` branch. Consult
the relevant Git tag (e.g. `0.1.2`) if you need documentation for a
specific release.**


## Philosophy

Are utility libraries A Bad Thing?

- One answer: Yes.

- Another answer: If they get to be a bucket for everything, then yes; if care is taken about what goes in them, then no.

This library is intended to be a set of low-level things that one could argue are missing from Clojure. (However, by that rule some of the things included should probably not be — they are perhaps not low-level enough.)

I'm happy to use this library from my own higher-level libraries and projects. If other people use it, so be it.


## Installation

Current version:

[![Clojars Project](https://img.shields.io/clojars/v/com.nomistech/clj-utils.svg)](https://clojars.org/com.nomistech/clj-utils)

To include in your Leiningen project, add the above to the dependencies in
your `project.clj`.


## Usage

Add something like the following to your namespace declaration(s):

```clj
    (:require [com.nomistech.clj-utils :as nu])
```

## Function Categories

### Control Flow
- `do1`, `do2` - Execute multiple forms, return specific results
- `econd` - Enhanced cond that throws on no match
- `dotdot` - Flexible threading macro
- `with-extras` - Enhanced try/finally with before/after hooks

### Error Handling  
- `cl-exception` - Create exceptions with formatted messages
- `try-or`, `try-or-nil` - Safe function execution with defaults
- `safely` - Execute code returning [result error] tuples

### Map Utilities
- `map-keys`, `map-vals`, `map-kv` - Transform map keys/values
- `map-*-recursively` - Recursive map transformations
- `deep-merge` - Recursive map merging
- `select-keys-recursively` - Select nested keys with paths
- `submap?` - Check if one map is a subset of another
- `group-by-k`, `group-by-v`, `group-by-kv` - Enhanced grouping

### Collection Utilities
- `indexed` - Add indices to collections  
- `position`, `positions` - Find item positions
- `drop-nth`, `edit-nth` - Modify collections at specific indices
- `dups` - Find duplicate items preserving order
- `unchunk` - Create fully lazy sequences
- `member?` - Check collection membership
- `partition-by-pred` - Split collection by predicate
- `find-first`, `find-last` - Find items by predicate
- `distinct-by` - Remove duplicates using custom key function

### Data Validation
- `not-empty?` - Check if collection is non-empty
- `all-keys-present?` - Validate required map keys
- `validate-map` - Comprehensive map validation with specs

### Concurrency & Rate Limiting
- `only-once` - Ensure function runs only once across threads
- `limiting-n-executions` - Limit concurrent function executions
- `with-return-429-if-too-many-requests` - HTTP rate limiting

### Graph & Relation Utilities
- `transitive-closure` - Compute transitive closure of relations
- `invert-function`, `invert-relation` - Invert functions and relations

### String Utilities  
- `last-index-of-char-in-string` - Find last character position

### File System
- `emacs-temp-file?` - Detect Emacs temporary files

For detailed documentation, see the doc strings in the source code.


## License

Copyright © 2018-2021 Simon Katz

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

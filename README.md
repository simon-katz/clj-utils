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

For now, the only documentation is doc strings.


## License

Copyright © 2018 Simon Katz

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

# Changelog

All notable changes to this project will be documented in this file.

This changelog mostly follows the conventions of
[Keep a Changelog](http://keepachangelog.com/en/1.0.0/); it deviates from those
conventions by not making versions and sections linkable.

This project uses [Semantic Versioning](http://semver.org/spec/v2.0.0.html).
In addition, breaking changes between successive
initial development versions (that is 0.y.z versions)
are explicitly noted.

## Unreleased

- In `with-extras`, deprecate `finally` and call `after` when there's a
  non-local exit.

### Diffs

https://github.com/simon-katz/clj-utils/compare/0.15.0...HEAD


## 0.15.0 — 2021-03-09

- Add `finally` option to `with-extras`.

### Diffs

https://github.com/simon-katz/clj-utils/compare/0.14.0...0.15.0


## 0.14.0 — 2020-10-27

- Make `last-index-of-char-in-string` work with cljs.

### Diffs

https://github.com/simon-katz/clj-utils/compare/0.13.0...0.14.0


## 0.13.0 — 2020-08-21

- Change uses of `my-error` to uses of `cl-exception`
- Upgrade to midje 1.9.9 and core.async 1.3.610.

### Diffs

https://github.com/simon-katz/clj-utils/compare/0.12.0...0.13.0


## 0.12.0 — 2020-08-21

- Fix bug in `transitive-clojure`.
- Add `cl-exception` and deprecate `my-error`

### Diffs

https://github.com/simon-katz/clj-utils/compare/0.11.0...0.12.0


## 0.11.0 — 2019-09-25

- Add `:require-macros` of self in `com.nomistech.clj-utils`

### Diffs

https://github.com/simon-katz/clj-utils/compare/0.10.0...0.11.0


## 0.10.0 — 2019-09-22

- Make most things work with ClojureScript.

### Diffs

https://github.com/simon-katz/clj-utils/compare/0.9.0...0.10.0


## 0.9.0 — 2018-10-20

- Change `deep-merge` to treat nil values as empty maps.

### Diffs

https://github.com/simon-katz/clj-utils/compare/0.8.0...0.9.0


## 0.8.0 — 2018-08-13

- Add `with-return-429-if-too-many-requests`.

### Diffs

https://github.com/simon-katz/clj-utils/compare/0.7.0...0.8.0


## 0.7.0 — 2018-08-12

- Add `limiting-n-executions`.

### Diffs

https://github.com/simon-katz/clj-utils/compare/0.6.0...0.7.0



## 0.6.0 — 2018-08-05

- Add `map-keys-recursively-applying-to-maps`.
- Add `map-keys-recursively`.
- Add `map-vals-recursively-applying-to-maps`.
- Add `map-vals-recursively`.
- Add `map-kv-recursively`.


### Diffs

https://github.com/simon-katz/clj-utils/compare/0.5.0...0.6.0



## 0.5.0 — 2018-07-10

### Changed

- Change `with-extras` to not catch exceptions.


### Diffs

https://github.com/simon-katz/clj-utils/compare/0.4.0...0.5.0


## 0.4.0 — 2018-07-09

### Added

- Add `%result%` to `with-extras`.

### Diffs

https://github.com/simon-katz/clj-utils/compare/0.3.0...0.4.0


## 0.3.0 — 2018-06-21

### Added

- Add philosophy to README.

### Changed

- Change `with-extras` to take a map as its first arg.


### Diffs

https://github.com/simon-katz/clj-utils/compare/0.2.0...0.3.0


## 0.2.0 — 2018-06-09

### Added

- Add `map-kv`.
- Add `group-by-kv`.
- Add `group-by-k`.
- Add `group-by-v`.
- Add `emacs-temp-file-path`.
- Add `emacs-temp-file`.


### Diffs

https://github.com/simon-katz/clj-utils/compare/0.1.0...0.2.0


## 0.1.0 — 2018-06-09

- Initial release: https://github.com/simon-katz/clj-utils/tree/0.1.0

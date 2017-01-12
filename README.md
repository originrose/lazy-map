# lazy-map

> Lazy maps for Clojure

Documentation hopefully will be coming soon.

## See also

**[Malabarba's implementation] of lazy maps in Clojure.**

[Malabarba's implementation]: https://github.com/Malabarba/lazy-map-clojure

Features unique to `malabarba/lazy-map`:

* ClojureScript support
* Transform Java classes into lazy maps (methods become keys)

Features unique to `thinktopic/lazy-map`:

* More robust handling of laziness: all possible operations on maps
  are supported correctly (e.g. `seq` and `reduce-kv`)
* Pretty string representation and support for pretty-printing

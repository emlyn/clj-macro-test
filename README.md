# macro-test

A demonstration of an issue when trying to test macros

## Usage

The core namespace contains a macro.
It is not important what it is for this demonstration, any macro will do.
In this case I copied a simple `infix` macro from [here](https://www.braveclojure.com/writing-macros/#Anatomy_of_a_Macro).

Run the tests with `lein test`. One assertion will fail:

``` shell
> lein test

lein test macro-test.core-test

lein test :only macro-test.core-test/infix-test

FAIL in (infix-test) (core_test.clj:7)
This fails: the macro is not expanded
expected: (= (quote (+ 1 1)) (macroexpand (quote (infix (1 + 1)))))
  actual: (not (= (+ 1 1) (infix (1 + 1))))

Ran 1 tests containing 3 assertions.
1 failures, 0 errors.
Tests failed.
```

Notice how `macroexpand` did not expand the macro, but just returned the form unchanged.
Let's try running the test from a repl. First start up a repl with `lein repl`, then:

``` clojure
macro-test.core=> (require 'macro-test.core-test)
nil
macro-test.core=> (in-ns 'macro-test.core-test)
#object[clojure.lang.Namespace 0x4ae0e040 "macro-test.core-test"]
macro-test.core-test=> (infix-test)
nil
```

Nothing is printed, which means the test passed. How come?

It turns out that `clojure.test` calls the test function from a different namespace.
Let's try doing that:

``` clojure
macro-test.core-test=> (in-ns 'user)
#object[clojure.lang.Namespace 0x58d743 "user"]
user=> (macro-test.core-test/infix-test)

FAIL in (infix-test) (core_test.clj:7)
This fails: the macro is not expanded
expected: (= (quote (+ 1 1)) (macroexpand (quote (infix (1 + 1)))))
  actual: (not (= (+ 1 1) (infix (1 + 1))))
nil
```

Now we see the same failure!
We can replicate this `macroexpand` behaviour by calling it from the same or different namespaces:

``` clojure
macro-test.user=> (macroexpand '(infix (1 + 1)))
(infix (1 + 1))
macro-test.user=> (in-ns 'macro-test.core-test)
#object[clojure.lang.Namespace 0x4ae0e040 "macro-test.core-test"]
macro-test.core-test=> (macroexpand '(infix (1 + 1)))
(+ 1 1)
```

Now it becomes clearer what's going wrong.
Trying to macroexpand a macro can only work if there is a macro in scope to expand,
but in the `user` namespace, there is no `infix` macro.

We could work around this by fully qualifying the macro name (and any symbols passed in as arguments),
like `(macroexpand '(macro-test.core/infix (1 + 1)))`.

Or we could use a syntax-quote instead: `(macroexpand ``(infix (1 + 1)))`,
which will automatically qualify all symbols.
Although that includes the `+`, so instead of the expected `(+ 1 1)` result,
we get `(clojure.core/+ 1 1)`.

But maybe it would be better if clojure would run the tests in their own namespace
so that we don't have to use any workarounds?

## License

Copyright © 2019 FIXME

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.

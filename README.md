java-seed
=========

Helper classes for java project jumpstart.

Modules
-------
Each module has its own `Seed` class used for static function and
helper classes grouping and some specific stuff described below.

### Core

#### `core.Seed`
Contains general-purpose helper functions

Some more are probably coming


#### `core.Chain`
`Chain` is an abstraction of sequential process where each step is
independent yet must be executed only if preceding step succeeds.

`Chain` is composed of `Chain.Callable` implementing steps-functions,
probably a lambda expressions. Each function expects no input and produces no output.

If some step throws any `Exception` chain is terminated by throwing a
`Chain.BrokenException` with original exception as its cause. `Chain.BrokenException`
message can be more informative if broken step implementation overrides `toString()`
method. In such case step's string representation will be included into
`Chain.BrokenException` message

```java
// Usage example

public static void main(String... args) throws Flow.InterruptedException {
  Chain.start(first::thing)
      .then(() -> second.thing(someInput))
      .then(last::thing())
      .end();

  // ...
}
```

#### `core.Flow`
`Flow` is an abstraction of a pipeline-like process where each step receives
upstream step's output.

`Flow` is composed of two kinds of steps: `Flow.Producer` step which must be a
starting step of a `Flow` and `Flow.Processor` which represents all of the downstream steps

`Flow.Producer`'s job is to produce some data which can be sequentially processed
by downstream `Flow.Processor`'s therefore it recieves no input and returns an output
of some kind

`Flow.Processor`'s job is to do some job based on the input, optionally transform this input and
pass some data further down the stream

```java
// Usage example

public static void main(String... args) throws Flow.InterruptedException {
  ExecutionResult res = Flow
      .start(() -> args)          // passes args array to the downstream step
      .then(Main::parseArguments) // takes args parses them and returns Parameters object
      .then(Main::initLogging)    // takes Parameters object sets logging up and passes Parameters object further
      .then(Main::configuration)  // takes Parameters object, reads some defaults and constructs Configuration object
      .then(Main::registerTasks)  // takes Configuration object and registers some tasks passing Configuration further
      .then(Main::execute)        // takes Configuration object and runs some tasks producing ExecutionResult
      .end();

  // ...
}
```


### Test

#### `test.Seed.Test`

This class serves as a base class for all tests initializing mockito mocks in
its `@Before` method and delegating its static `assertThat()` functions to corresponding
`org.junit.Assert.assertThat()` function. This saves us from the necessity of explicit imports
in each test.

Some more delegations are probably coming like `org.hamcrest.CoreMatchers.is()` etc


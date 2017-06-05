[![Build Status](https://travis-ci.org/kroepke/luna.svg?branch=master)](https://travis-ci.org/kroepke/luna)

# Luna

(*Luna* is Spanish for *Moon*.)

## About

Luna is an implementation of Lua 5.3 for the Java Virtual Machine (JVM), written in
pure Java with minimal dependencies.
The goal of the Luna project is to develop a correct, complete and scalable Lua
implementation for running sandboxed Lua programs on the JVM.

Luna implements Lua 5.3 as specified by the
[Lua Reference Manual](http://www.lua.org/manual/5.3/manual.html), explicitly attempting to mimic
the behaviour of PUC-Lua whenever possible. This includes language-level features (such
as metamethods and coroutines) and the standard library.

## Credits

Luna is a fork of [Rembulan](https://github.com/mjanicek/rembulan) created by Miroslav Janíček.
Sadly his excellent project seems to be abandoned and is also not published on Maven Central.

In order to make Lua 5.3 useful in JVM projects, I decided to fork and publish the project
so that his great work does not get lost.

## Status

The majority of language-level features is implemented, and may be expected
to work. If you find behaviour that does not conform to Lua 5.3 as defined by the Lua Reference
Manual, please [open a new issue](https://github.com/kroepke/luna/issues).

See also the [completeness table](doc/CompletenessTable.md) that maps out the current
completeness status of Luna with regard to PUC-Lua, in particular the standard library.

## Frequently asked questions (FAQ)

#### What is Luna good for?

Lua is a small, beautifully-designed and simple-yet-powerful programming language.
Lua has been traditionally used as an embedded scripting language. Luna aims to serve
a similar purpose on the JVM, with an explicit focus on sandboxing the client Lua programs.

There are two main use-cases for Luna: running untrusted Lua scripts on the JVM,
and enhancing Java applications by adding the ability to script them with Lua.

#### Does Luna implement the Lua C API?

No, at this point Luna requires libraries to be written against its Java interface.
 
#### Does Luna work with Lua bytecode?

No. The Lua bytecode (i.e., the bytecode generated by PUC-Lua's `luac`) is considered
an implementation detail by both Luna and the Lua Reference Manual. Luna implements
its own compiler and compiles to Java bytecode directly. It uses its own
intermediate representation (IR) annotated with statically-inferred type information,
but does not expose it to the user, and the IR has no serialisable form.
 
For more information about the Luna compiler, see the [compiler overview](doc/CompilerOverview.md). 
 
#### How are coroutines implemented?
 
See [How are coroutines implemented?](doc/CoroutinesOverview.md)  

## Using Luna

Luna requires a Java Runtime Environment (JRE) version 7 or higher.

### Documentation

Generated JavaDocs are available online:

 * [Runtime module](https://mjanicek.github.io/luna/apidocs/luna-runtime/index.html)
 * [Compiler](https://mjanicek.github.io/luna/apidocs/luna-compiler/index.html)
 * [Standard Library](https://mjanicek.github.io/luna/apidocs/luna-stdlib/index.html)

There are also a few short texts in the `doc` folder:

 * [How are coroutines implemented?](doc/CoroutinesOverview.md)
 * [Overview of the Luna compiler](doc/CompilerOverview.md)
 * [Luna completeness table](doc/CompletenessTable.md)

### Building from source

To build Luna, you will need the following:

 * Java Development Kit (JDK) version 7 or higher
 * Maven version 3 or higher

Maven will pull in the remaining dependencies as part of the build process.

To fetch the latest code on the `master` branch and build it, run

```sh
git clone https://github.com/kroepke/luna.git
cd luna    
mvn install
```

This will build all modules, run tests and finally install all artifacts into your local
Maven repository.    

#### Standalone REPL

Much like PUC-Lua, Luna contains a standalone REPL. This is provided in the module
`luna-standalone`. To build the REPL, run

```sh
mvn package -DskipTests -Dmaven.javadoc.skip=true -DstandaloneFinalName=luna
```

The standalone REPL is packaged as a self-contained, executable [Capsule](http://www.capsule.io)
and is placed in the directory `luna-standalone/target`.
 
To run the REPL:

```sh
cd luna-standalone/target
./luna-capsule.x
```

The standalone REPL mimics the behaviour or the standalone PUC-Lua interpreter and may be
used as its drop-in replacement.

```
$ ./luna-capsule.x
Luna 0.1-SNAPSHOT (Java HotSpot(TM) 64-Bit Server VM, Java 1.8.0_60)
> print("hello world!")
hello world!
```

### Using Luna from Maven

There are no releases yet, but snapshot artifacts are published to the Sonatype OSSRH Snapshot
Repository. To use the snapshot artifacts, add the following configuration to your `pom.xml`:

```xml
<repositories>
  <repository>
    <id>sonatype-ossrh-snapshots</id>
    <name>Sonatype OSSRH (Snapshots)</name>
    <url>https://oss.sonatype.org/content/repositories/snapshots/</url>
    <snapshots />
  </repository>
</repositories>
```

To include the **runtime** as a dependency:

```xml
<dependency>
  <groupId>org.classdump.luna</groupId>
  <artifactId>luna-runtime</artifactId>
  <version>0.1-SNAPSHOT</version>
</dependency>
```

To include the **compiler** as a dependency:

```xml
<dependency>
  <groupId>org.classdump.luna</groupId>
  <artifactId>luna-compiler</artifactId>
  <version>0.1-SNAPSHOT</version>
</dependency>
```

To include the **standard library** as a dependency:

```xml
<dependency>
  <groupId>org.classdump.luna</groupId>
  <artifactId>luna-stdlib</artifactId>
  <version>0.1-SNAPSHOT</version>
</dependency>
```

Note that `luna-compiler` and `luna-stdlib` both pull in `luna-runtime` as
a dependency, but are otherwise independent. (I.e., to use the compiler and the standard
library, you need to declare both `-compiler` and `-stdlib` as dependencies, but do not need
to include `-runtime`).

## Getting started

Luna compiles Lua functions into Java classes and loads them into the JVM;
the compiler performs a type analysis of the Lua programs in order to generate a more
tightly-typed code whenever feasible.

Since the JVM does not directly support coroutines, Luna treats Lua functions as state
machines and controls their execution (i.e., yields, resumes and pauses) using exceptions.
Since the Luna runtime retains control of the control state, this technique is also used
to implement CPU accounting and scheduling of asynchronous operations.

#### Example: Hello world  

The following snippet loads the Lua program `print('hello world!')`, compiles it, loads
it into a (non-sandboxed) state, and runs it:

(From [`luna-examples/.../HelloWorld.java`](luna-examples/src/main/java/net/sandius/luna/examples/HelloWorld.java))

```java
String program = "print('hello world!')";

// initialise state
StateContext state = StateContexts.newDefaultInstance();
Table env = StandardLibrary.in(RuntimeEnvironments.system()).installInto(state);

// compile
ChunkLoader loader = CompilerChunkLoader.of("hello_world");
LuaFunction main = loader.loadTextChunk(new Variable(env), "hello", program);

// execute
DirectCallExecutor.newExecutor().call(state, main);
```

The output (printed to `System.out`) is:

```
hello world!
```

#### Example: CPU accounting

Lua functions can be called in a mode that automatically pauses their execution once the
given number of operations has been performed:

(From [`luna-examples/.../InfiniteLoop.java`](luna-examples/src/main/java/net/sandius/luna/examples/InfiniteLoop.java))

```java
String program = "n = 0; while true do n = n + 1 end";

// initialise state
StateContext state = StateContexts.newDefaultInstance();
Table env = StandardLibrary.in(RuntimeEnvironments.system()).installInto(state);

// compile
ChunkLoader loader = CompilerChunkLoader.of("infinite_loop");
LuaFunction main = loader.loadTextChunk(new Variable(env), "loop", program);

// execute at most one million ops
DirectCallExecutor executor = DirectCallExecutor.newExecutorWithTickLimit(1000000);

try {
    executor.call(state, main);
    throw new AssertionError();  // never reaches this point!
}
catch (CallPausedException ex) {
    System.out.println("n = " + env.rawget("n"));
}
```

Prints:

```
n = 199999
```

The [`CallPausedException`](https://mjanicek.github.io/luna/apidocs/luna-runtime/net/sandius/luna/exec/CallPausedException.html) contains a *continuation* of the call. The call can be resumed:
the pause is transparent to the Lua code, and the loop does not end with an error (it is merely
paused).

#### Further examples

For further examples, see the classes in
[`luna-examples/src/main/java/net/sandius/luna/examples`](luna-examples/src/main/java/net/sandius/luna/examples).

### Project structure

Luna is a multi-module Maven build, consisting of the following modules that are deployed
to Sonatype OSSRH:

 * `luna-runtime` ... the core classes and runtime;
 * `luna-compiler` ... a compiler of Lua sources to Java bytecode;
 * `luna-stdlib` ... the Lua standard library;
 * `luna-standalone` ... standalone REPL, a (mostly) drop-in replacement
                             for the `lua` command from PUC-Lua.

There are also auxiliary modules that are not deployed:

 * `luna-tests` ... project test suite, including benchmarks from
                        the Benchmarks Game;
 * `luna-examples` ... examples of the Luna API.                       


## Contributing

Contributions of all kinds are welcome!


## License

Luna is licensed under the Apache License Version 2.0. See the file
[LICENSE.txt](LICENSE.txt) for details.

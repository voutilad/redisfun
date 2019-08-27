# Redis Streams!

This project is some experimentation with Redis Streams, mainly
setting up a producer that `xadd`'s to a stream and a consumer group
that...consumes...from the stream.

It's using [lettuce](https://lettuce.io), a comprehensive Java Redis
client funded by [Pivotal](https://pivotal.io) and providing three
ways of interacting with the Redis API:

1. Synchronous calls that block
2. Asynchronous calls that returns Java Futures[1]
3. Reactive Streams[2] from the Reactor[3] project (also from Pivotal)


## Pre-reqs
Grab a very recent [Redis 5.x](https://redis.io) version that supports
streams. Install it and get it running locally just using the default
settings for now.

This project was built using JDK 11, but _should_ work with JDK 8.

## Building & Running
Assuming you've got a recent JDK and javac in your path, this project
uses [Gradle](https://gradle.org) for dependency and build
management. The included wrapper script will download and bootstrap
Gradle for you, so all you need is the JDK.

### Quickstart
Simply execute the Gradle wrapper with the `run` task:

```bash
$ ./gradlew run
```
>> Use `gradlew.bat` on Windows

You can run the producer ("ticker") or consumer ("taker") individually
using gradle as well:

```bash
$./gradlew ticker
```

### Building a standalone Jar
I've included a Gradle task for bundling up the project into an
"uberjar" to make it easier to use without messing with Java
classpaths.

#### Build the uberjar
```bash
$ ./gradlew shadowJar
```

#### Run the App
```bash
$ ./java -jar ./build/libs/redisfun-0.1.0-SNAPSHOT-all.jar
```

The exposed entrypoints:
* main App: `com.sisu.redisfun.App`
* producer: `com.sisu.redisfun.Ticker`
* consumer: `com.sisu.redisfun.Taker`

Which you can easily run via:
```bash
$ ./java -cp ./build/libs/redisfun-0.1.0-SNAPSHOT-all.jar \
  com.sisu.redisfun.Ticker
```

# Todo's
Explore the following:
  * stand-alone consumers that use reactor-style consumption models
  * more complicated POJO storage...can I stick POJOs in the stream?
  * compare with how this stream-based messaging approach would differ
    from a "brokerless" model like from [zeromq](https://zeromq.org)

# Footnotes
(These never render well in Github.)

[1](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/Future.html)
[2](http://www.reactive-streams.org/)
[3](https://projectreactor.io/)

# Redis Streams!

Some experimentation with Redis Streams, mainly setting up a producer that
`xadd`'s to a stream and a consumer group that...consumes...from the stream.

## Pre-reqs
Grab a very recent [Redis 5.x](https://redis.io) version that supports streams.
Install it and get it running locally just using the default settings for now.

## Building & Running
Assuming you've got a recent JRE and java in your path, run the gradle wrapper
("gradlew" or "gradlew.bat" if on Windows):

```bash
$ ./gradlew run
```

It should bootstrap the project, pull dependencies, build, and run the app. The
joys of Gradle!

# Todo's
Explore the following:
  * stand-alone consumers that use reactive-style consumption models
  * more complicated POJO storage...can I stick POJOs in the stream?


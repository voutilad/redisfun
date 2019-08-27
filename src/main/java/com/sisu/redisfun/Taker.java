package com.sisu.redisfun;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import io.lettuce.core.Consumer;
import io.lettuce.core.RedisBusyException;
import io.lettuce.core.RedisClient;
import io.lettuce.core.StreamMessage;
import io.lettuce.core.XReadArgs;
import io.lettuce.core.XReadArgs.StreamOffset;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisStreamCommands;

public class Taker implements Runnable {
	public static final String GROUP = "MYGROUP";
	public static final String STREAM = "MYSTREAM";

	private boolean timeToDie = false;
	private Random rnd = new Random();
	private RedisClient client;

	public Taker(RedisClient client) {
		this.client = client;
	}

	private String getName() {
		return String.format("[Taker/%s]", Thread.currentThread().getName());
	}

	private void speak(String s) {
		System.out.println(String.format("%s %s", this.getName(), s));
	}

	private String process(StreamMessage<String, String> msg) {
		try {
			speak("procesing " + msg);
			Thread.sleep((long) rnd.nextInt(3) * 970);
		} catch (Exception e) {
			// nop
		}
		return msg.getId();
	}

	@Override
	public String toString() {
		return this.getName();
	}

	@Override
	public void run() {
		speak("starting...");
		StatefulRedisConnection<String, String> conn = client.connect();
		RedisStreamCommands<String, String> redis = conn.sync();

		speak("creating consumer group...");
		// redis.xgroupDestroy(STREAM, GROUP);

		try {
			String result = redis.xgroupCreate(StreamOffset.latest(STREAM), GROUP);
			if (result != "OK") {
				speak("failed to created group?! (" + result + ")");
			} else {
				speak("group created!");
			}
		} catch (RedisBusyException e) {
			speak(e.getLocalizedMessage());
		}

		while (!timeToDie) {
			try {
				speak("reading from group...");
				Consumer<String> consumer = Consumer.from(GROUP, this.getName());
				StreamOffset<String> offset = StreamOffset.lastConsumed(STREAM);

				// This next line generates type warnings due to the odd nature of the xreadgroup api
				// and probably warrants a pull-request...hmm. See this post for some good details:
				// https://medium.com/@BladeCoder/fixing-ugly-java-apis-read-only-generic-varargs-ee2d2e464ac1
				List<StreamMessage<String, String>> messages = redis.xreadgroup(consumer, XReadArgs.Builder.count(3).block(5000), offset);

				List<String> ids = messages.stream().map(msg -> msg.getId()).collect(Collectors.toList());
				speak("claiming " + ids.size() + " messages");
				List<StreamMessage<String, String>> claimed = redis.xclaim(STREAM, consumer, 0,
						ids.toArray(new String[ids.size()]));

				if (claimed.size() > 0) {
					speak("claimed " + claimed.size() + " messages");

					List<String> claimed_ids = claimed.stream()
					  .map(msg -> process(msg))
					  .collect(Collectors.toList());

					long acked = redis.xack(STREAM, GROUP, claimed_ids.toArray(new String[claimed_ids.size()]));
					speak("processed " + acked + " messages (" + claimed_ids + ")");
				}
			} catch (Exception e) {
				speak(e.getMessage());
				this.timeToDie = true;
				break;
			}
		}
		speak("finished.");
	}

	public static void main(String[] args) {
		RedisClient client = RedisClient.create("redis://localhost");
		Taker taker = new Taker(client);
		taker.run();
	}
}

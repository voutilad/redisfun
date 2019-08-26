package com.sisu.redisfun;

import java.util.List;
import java.util.stream.Collectors;

import io.lettuce.core.Consumer;
import io.lettuce.core.RedisClient;
import io.lettuce.core.StreamMessage;
import io.lettuce.core.XReadArgs.StreamOffset;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisStreamCommands;

public class Taker implements Runnable {
	public static final String GROUP = "MYGROUP";
	public static final String STREAM = "MYSTREAM";

	private boolean timeToDie = false;
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

	@Override
	public String toString() {
		return this.getName();
	}

	@Override
	public void run() {
		speak("starting...");
		StatefulRedisConnection<String, String> conn = client.connect();
		RedisStreamCommands<String, String> commands = conn.sync();

		speak("creating consumer group...");
		commands.xgroupDestroy(STREAM, GROUP);

		String result = commands.xgroupCreate(StreamOffset.latest(STREAM), GROUP);
		if (result != "OK") {
			speak("failed to created group?! (" + result + ")");
			return;
		} else {
			speak("group created");
		}

		while (!timeToDie) {
			try {
				speak("reading from group...");
				Consumer<String> consumer = Consumer.from(GROUP, this.getName());
				StreamOffset<String> offset = StreamOffset.lastConsumed(STREAM);
				List<StreamMessage<String, String>> messages = commands.xreadgroup(
					consumer, offset
				);

				if (messages.size() == 0) {
					speak("sleeping for 3s...");
					Thread.sleep(3000);
				} else {
					List<String> ids = messages.stream()
					  .map(msg -> msg.getId())
					  .collect(Collectors.toList());
					speak("claiming " + ids.size() + "messages");
					List<StreamMessage<String, String>> claimed = commands.xclaim(STREAM, consumer, 0, ids.toArray(new String[ids.size()]));

					if (claimed.size() > 0) {
						speak("claimed " + claimed.size() + " messages");

						List<String> acked = claimed.stream().map(msg -> {
							speak("processing " + msg);
							return msg.getId();
						  }).collect(Collectors.toList());

						speak("processed " + acked + " messages!");
					}
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

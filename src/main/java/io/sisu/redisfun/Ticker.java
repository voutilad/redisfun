package io.sisu.redisfun;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.async.RedisStreamAsyncCommands;

public class Ticker implements Runnable {
	private boolean timeToDie = false;
	private RedisClient client;

	public Ticker(RedisClient client) {
		this.client = client;
	}

	private String getName() {
		return String.format("[Ticker/%s]", Thread.currentThread().getName());
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
		RedisStreamAsyncCommands<String, String> commands = client.connect().async();

		  while (!timeToDie) {
			try {
				String msg = String.format("The time is now %d", System.currentTimeMillis());
				speak("ticking " + msg);

				commands.xadd("MYSTREAM", "msg", msg);
				Thread.sleep(350);
			} catch (InterruptedException e) {
				this.timeToDie = true;
				break;
			}
		}
		speak("finished.");
	}

	public static void main(String[] args) {
		RedisClient client = RedisClient.create("redis://localhost");
		Ticker ticker = new Ticker(client);
		ticker.run();
	}
}

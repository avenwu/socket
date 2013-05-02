package com.badlogic.task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import android.util.Log;

import com.badlogic.constant.Config;
import com.badlogic.model.PingCallable;

public class PingTask {

	private CompletionService<String> executorService = new ExecutorCompletionService<String>(
			Executors.newFixedThreadPool(30));
	private ArrayList<String> onlineIps = new ArrayList<String>();
	private ArrayList<Future<String>> futureList = new ArrayList<Future<String>>();
	private volatile boolean isCancelled;

	public ArrayList<String> pingIP(String ip) {
		long startTime = System.currentTimeMillis();
		int index = ip.lastIndexOf(".");
		int start = 1;
		while (start < 255 && !isCancelled) {
			PingCallable<String> callable = new PingCallable<String>(ip, index,
					start) {
				@Override
				public String call() throws Exception {
					if (isCancelled) {
						return null;
					}
					return pingTask(ip, index, start);
				}
			};
			Future<String> future = executorService.submit(callable);
			futureList.add(future);
			start++;
		}
		if (isCancelled) {
			return onlineIps;
		}
		start = 1;
		int waittingCount = 0;
		int sleepTime = 0;
		try {
			while (start < futureList.size() && !isCancelled) {
				Future<String> future = executorService.poll();
				while (future == null && waittingCount < 5) {
					sleepTime = Config.SLEEP_TIME * (2 << waittingCount);
					Log.e("PingTask", "get future failed, sleep " + sleepTime);
					Thread.sleep(sleepTime);
					future = executorService.poll();
					waittingCount++;
				}
				waittingCount = 0;
				Log.e("IPHelper", "start=" + start);
				if (future.get() != null) {
					System.out.println("ad ip in list:" + future.get());
					onlineIps.add(future.get());
				}
				start++;
			}
		} catch (Exception e) {
			Thread.currentThread().interrupt();
			e.printStackTrace();
		}
		long endTime = System.currentTimeMillis();
		System.out.println("finished:" + endTime + ", time used="
				+ (endTime - startTime));
		return onlineIps;
	}
	/**
	 * try to cancel all the ping thread;
	 */
	public void cancelTask() {
		isCancelled = true;
		if (futureList.size() > 0) {
			for (Future<String> future : futureList) {
				Log.d("PingTask",
						"ping task has been cancelled" + future.toString());
				future.cancel(true);
			}
		}
	}
	public String pingTask(String ip, int index, int start) throws IOException {
		if (isCancelled) {
			Thread.currentThread().interrupt();
			return null;
		}
		String subIp = ip.substring(0, index + 1);
		String newIp = subIp + start;
		Process process = Runtime.getRuntime().exec("ping " + newIp);
		System.out.println("ping ip:" + newIp);
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				process.getInputStream()));
		String result;
		reader.readLine();// the first is useless
		if ((result = reader.readLine()) == null
				|| (!result.contains(com.badlogic.constant.Config.PING_SUCCESS) && !result
						.contains(com.badlogic.constant.Config.PING_SUCCESS
								.toLowerCase()))) {
			reader.close();
			process.destroy();
			return null;
		}
		System.out.println("find online ip:" + newIp);
		reader.close();
		process.destroy();
		return newIp;
	}
}

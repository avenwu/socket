package com.badlogic.task;

import java.util.ArrayList;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import android.util.Log;

import com.badlogic.model.PingCallable;
import com.badlogic.utils.IPHelper;

public class PingTask {

	private CompletionService<String> executorService = new ExecutorCompletionService<String>(
			Executors.newFixedThreadPool(30));
	private ArrayList<String> onlineIps = new ArrayList<String>();
	private ArrayList<Future<String>> futureList = new ArrayList<Future<String>>();

	public ArrayList<String> pingIP(String ip) {
		long startTime = System.currentTimeMillis();
		int index = ip.lastIndexOf(".");
		int start = 1;
		while (start < 255) {
			PingCallable<String> callable = new PingCallable<String>(ip, index,
					start) {
				@Override
				public String call() throws Exception {
					return IPHelper.pingTask(ip, index, start);
				}
			};
			Future<String> future = executorService.submit(callable);
			futureList.add(future);
			start++;
		}
		start = 1;
		try {
			while (start < 254) {
				Future<String> future = executorService.take();
				Log.e("IPHelper", "start=" + start);
				if (future.get() != null) {
					System.out.println("ad ip in list:" + future.get());
					onlineIps.add(future.get());
				}
				start++;
			}
		} catch (Exception e) {
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
		if (futureList.size() > 0) {
			for (Future<String> future : futureList) {
				future.cancel(true);
			}
		}
	}
}

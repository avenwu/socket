package com.badlogic.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class IPHelper
{
	public static InetAddress[] getAllNetWork()
	{
		Vector v = new Vector(50);
		try
		{
			Process process = Runtime.getRuntime().exec("arp -a");
			InputStreamReader inputStr = new InputStreamReader(process.getInputStream());
			BufferedReader br = new BufferedReader(inputStr);
			br.readLine();
			br.readLine();
			br.readLine();
			String temp = "";
			while ((temp = br.readLine()) != null)
			{
				StringTokenizer tokens = new StringTokenizer(temp);
				String x = tokens.nextToken();
				InetAddress add = InetAddress.getByName(x);
				// System.out.println(x);
				v.add(add);
			}
			v.add(InetAddress.getLocalHost());
			process.destroy();
			br.close();
			inputStr.close();
		} catch (Exception e)
		{
			System.out.println("可能是网络不可用。");
			e.printStackTrace();
		}
		int cap = v.size();
		InetAddress[] addrs = new InetAddress[cap];
		for (int i = 0; i < cap; i++)
		{
			addrs[i] = (InetAddress) v.elementAt(i);
			System.out.println(addrs[i]);
		}
		return addrs;

	}

	public static ArrayList<String> getAvailableIP() throws IOException, InterruptedException,
			ExecutionException
	{
		InetAddress[] ips = getAllNetWork();
		ArrayList<String> result = pingIP(ips);
		System.out.println("-------------------all usefull ip--------------------------");
		for (String string : result)
		{
			System.out.println(string);
		}
		return result;
	}

	public static ArrayList<String> pingIP(InetAddress[] ips) throws IOException,
			InterruptedException, ExecutionException
	{
		long startTime = System.currentTimeMillis();
		ArrayList<String> onlineIps = new ArrayList<String>();
		CompletionService<String> executorService = new ExecutorCompletionService<String>(
				Executors.newFixedThreadPool(50));

		for (int i = 0; i < ips.length; i++)
		{
			InetAddress inetAddress = ips[i];
			String ip = inetAddress.getHostAddress();
			int index = ip.lastIndexOf(".");
			int start = Integer.parseInt(ip.substring(index + 1));
			if (!ip.contains("192.168"))
			{
				continue;
			}
			while (start < 255)
			{
				PingCallable<String> callable = new PingCallable<String>(ip, index, start)
				{
					@Override
					public String call() throws Exception
					{
						return pingTask(ip, index, start);
					}
				};
				executorService.submit(callable);
				start++;
				System.out.println("start=" + start);
			}
			start = Integer.parseInt(ip.substring(index + 1));
			while (start < 255)
			{
				Future<String> future = executorService.take();
				if (future.get() != null)
				{
					System.out.println("ad ip in list:" + future.get());
					onlineIps.add(future.get());
				}
				start++;
			}
		}
		long endTime = System.currentTimeMillis();
		System.out.println("finished:" + endTime + ", time used=" + (endTime - startTime));
		return onlineIps;
	}

	public abstract static class PingCallable<V> implements Callable<V>
	{
		protected String ip;
		protected int index;
		protected int start;

		public PingCallable(String ip, int index, int start)
		{
			this.ip = ip;
			this.index = index;
			this.start = start;
		}
	}

	public static String pingTask(String ip, int index, int start) throws IOException
	{
		System.out.println("start ping...");
		String subIp = ip.substring(0, index + 1);
		String newIp = subIp + start;
		Process process = Runtime.getRuntime().exec("ping " + newIp);
		System.out.println("ping ip:" + newIp);
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String result;
		reader.readLine();// the first is useless
		while ((result = reader.readLine()) != null)
		{
			if (result.contains("TTL"))
			{
				System.out.println("find online ip:" + newIp);
				reader.close();
				process.destroy();
				return newIp;
			}
		}
		reader.close();
		process.destroy();
		return null;
	}
}

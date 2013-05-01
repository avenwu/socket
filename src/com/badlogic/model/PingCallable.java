package com.badlogic.model;

import java.util.concurrent.Callable;
/**
 * define for Ping task thread pool
 * 
 * @author Aven
 * 
 * @param <V>
 */
public abstract class PingCallable<V> implements Callable<V> {
	protected String ip;
	protected int index;
	protected int start;
	/**
	 * 
	 * @param ip
	 *            human readable format such as: 192.168.43.1
	 * @param index
	 * @param start
	 */
	public PingCallable(String ip, int index, int start) {
		this.ip = ip;
		this.index = index;
		this.start = start;
	}
}
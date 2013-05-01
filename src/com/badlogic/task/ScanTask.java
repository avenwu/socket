package com.badlogic.task;

import java.net.InetAddress;
import java.util.ArrayList;

import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;

import com.badlogic.constant.Cons;
import com.badlogic.utils.IPHelper;
/**
 * ScanTask is used to scan all the possible IP in same the network with local
 * IP address, only inner IPV4 available;
 * 
 * @author Aven
 * 
 */
public class ScanTask extends AsyncTask<Void, Void, ArrayList<String>> {
	private Context context;
	private Handler handler;
	private PingTask ipTask;

	public ScanTask(Context context, Handler handler) {
		this.context = context;
		this.handler = handler;
	}

	@Override
	protected ArrayList<String> doInBackground(Void... arg0) {
		InetAddress localIP = IPHelper.getIPWifi(context);
		context = null;
		ipTask = new PingTask();
		return ipTask.pingIP(localIP.getHostAddress());
	}
	@Override
	protected void onPostExecute(ArrayList<String> result) {
		super.onPostExecute(result);
		if (result != null) {
			handler.obtainMessage(Cons.STOP_SCANNING, result).sendToTarget();
			return;
		}
		handler.obtainMessage(Cons.SCANNING_FAILED).sendToTarget();
	}
	/**
	 * try to cancell all the inner worker thread, too.
	 * 
	 * @param mayInteruptIfRunning
	 */
	public void cancelTask(boolean mayInteruptIfRunning) {
		super.cancel(mayInteruptIfRunning);
		if (ipTask != null) {
			ipTask.cancelTask();
		}
		ipTask = null;
	}
}

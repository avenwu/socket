package com.badlogic.socketchatter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.badlogic.utils.StringHelper;

public class MainService extends Service {
	private MessageListener messageListener;
	private Socket clientRequestSocket;
	private Socket serverResponseSocket;
	private ServerSocket serverReceiver;
	private BufferedReader serveReader;
	private BufferedReader clientReader;
	private PrintWriter serverWriter;
	private PrintWriter clientWriter;
	boolean isServerConnected;
	boolean isConnecttingServer;
	private boolean isClient;
	private ServiceBinder serviceBinder = new ServiceBinder();

	@Override
	public void onCreate() {
		super.onCreate();

	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i("LocalService", "Received start id " + startId + ": " + intent);
		return START_STICKY;
	}
	@Override
	public IBinder onBind(Intent arg0) {
		return serviceBinder;
	}

	public MessageListener getMessageListener() {
		return messageListener;
	}

	public void setMessageListener(MessageListener messageListener) {
		this.messageListener = messageListener;
	}

	public static interface MessageListener {
		public void onReceive(String content);
		public void onComplete();
		public void onFailed();
	}

	public class ServiceBinder extends Binder {
		public MainService getServices() {
			return MainService.this;
		}
	}
	protected void onPost(String content) {
		Log.e("SendFeed", "send content" + content);
		if (isClient) {
			clientWriter.write(StringHelper.decodeContent(content));
			clientWriter.flush();
		} else {
			serverWriter.write(StringHelper.decodeContent(content));
			serverWriter.flush();
		}
		Log.e("SendFeed", "flush content" + content);
	}

	protected void connectServer(String IP, int port) throws IOException,
			InterruptedException {
		isClient = true;
		isConnecttingServer = true;
		Log.e("SendFeed", "IP:" + IP);
		clientRequestSocket = new Socket(IP, port);
		clientReader = new BufferedReader(new InputStreamReader(
				clientRequestSocket.getInputStream()));
		clientWriter = new PrintWriter(new OutputStreamWriter(
				clientRequestSocket.getOutputStream()), true);
		Log.e("SendFeed", "requset connect to server");
		StringBuffer line = new StringBuffer();
		char[] buffer = new char[1024];
		int length = 0;
		while (isConnecttingServer) {
			while ((length = clientReader.read(buffer, 0, buffer.length)) != -1) {
				Log.e("SendFeed", "receive conetent from client:"
						+ new String(buffer));
				messageListener.onReceive(StringHelper
						.undecodeContent(new String(buffer)));
			}
			Thread.sleep(100);
		}
	}
	protected void startServer(int port) throws IOException,
			InterruptedException {
		serverReceiver = new ServerSocket(port);
		isServerConnected = true;
		serverResponseSocket = serverReceiver.accept();
		serveReader = new BufferedReader(new InputStreamReader(
				serverResponseSocket.getInputStream()));
		serverWriter = new PrintWriter(new OutputStreamWriter(
				serverResponseSocket.getOutputStream()));
		Log.e("SendFeed", "get server reader");
		StringBuffer line = new StringBuffer();
		char[] buffer = new char[1024];
		int length = 0;
		while (isServerConnected) {
			while ((length = serveReader.read(buffer, 0, buffer.length)) != -1) {
				Log.e("SendFeed", "receive conetent from client:"
						+ new String(buffer));
				messageListener.onReceive(StringHelper
						.undecodeContent(new String(buffer)));
			}
			Thread.sleep(100);
		}
		Log.e("SendFeed", "server end:");
	}
	protected void shutDownConnect() throws IOException {
		if (clientReader != null)
			clientReader.close();
		if (clientWriter != null)
			clientWriter.close();
		if (clientRequestSocket != null)
			clientRequestSocket.close();
		clientReader = null;
		clientWriter = null;
		clientRequestSocket = null;
		isConnecttingServer = false;
	}
	void shutDownServer() throws IOException {
		if (serveReader != null)
			serveReader.close();
		if (serverResponseSocket != null)
			serverResponseSocket.close();
		serveReader = null;
		serverResponseSocket = null;
		isServerConnected = false;
	}
	@Override
	public void onDestroy() {
		try {
			shutDownConnect();
			shutDownServer();
		} catch (IOException e) {
			e.printStackTrace();
		}
		super.onDestroy();
	}
}

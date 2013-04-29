package com.badlogic.socket;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ContentHandler;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.ClipData.Item;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.badlogic.R;
import com.badlogic.adapter.EmotionStaticAdapter;
import com.badlogic.adapter.MessageHistoryAdapter;
import com.badlogic.model.MessageItem;
import com.badlogic.providers.DataProvider;
import com.badlogic.utils.Config;
import com.badlogic.utils.Cons;
import com.badlogic.utils.StringHelper;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

public class SendFeed extends Activity
{
	private static int PORT = 3000;
	private ImageView mBack;
	private ImageView mPublish;
	private EditText mContent;
	// private Button mPlace;
	// private ImageView mSperator;
	// private ImageView mList;
	private TextView mCount;
	private ImageButton mVoice;
	private ImageButton mPoi;
	private ImageButton mImage;
	private ImageButton mAt;
	private ImageButton mEmoticon;
	private GridView mEmoticons;
	private EmotionStaticAdapter emotionStaticAdapter;

	private LocationClient mClient;
	private LocationClientOption mOption;

	private boolean mLBSIsReceiver;
	private String mLBSAddress;
	private Drawable mPoi_off_icon;
	private Drawable mPoi_on_icon;
	private Button setting;
	private ProgressDialog mPublishDialog;
	private ListView historyMessageListView;
	private MessageHistoryAdapter messageHistoryAdapter;
	private ArrayList<MessageItem> messageHistoryList;
	private Socket clientRequestSocket;
	private Socket serverResponseSocket;
	private ServerSocket serverReceiver;
	private BufferedReader serveReader;
	private BufferedReader clientReader;
	private PrintWriter serverWriter;
	private PrintWriter clientWriter;
	private boolean isServerConnected;
	private boolean isConnecttingServer;

	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.newsfeedpublish);

		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads()
				.detectDiskWrites().detectNetwork().penaltyLog().build());
		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects()
				.penaltyLog().penaltyDeath().build());

		initLBS();
		findViewById();
		setListeners();
		initRes();

	}

	public void initRes()
	{
		mPoi_off_icon = getResources().getDrawable(R.drawable.v5_0_1_publisher_poi_icon);
		mPoi_off_icon.setBounds(0, 0, mPoi_off_icon.getMinimumWidth(),
				mPoi_off_icon.getMinimumHeight());
		mPoi_on_icon = getResources().getDrawable(R.drawable.v5_0_1_publisher_poi_active_icon);
		mPoi_on_icon.setBounds(0, 0, mPoi_on_icon.getMinimumWidth(),
				mPoi_on_icon.getMinimumHeight());
		emotionStaticAdapter = new EmotionStaticAdapter(this.getApplicationContext(),
				DataProvider.imageRes);
		mEmoticons.setAdapter(emotionStaticAdapter);
		messageHistoryList = getData();
		messageHistoryAdapter = new MessageHistoryAdapter(this.getApplicationContext(),
				messageHistoryList);
		historyMessageListView.setAdapter(messageHistoryAdapter);
		mClient.start();
		mLBSIsReceiver = true;
		mClient.requestLocation();
	}

	private void findViewById()
	{
		mBack = (ImageView) findViewById(R.id.newsfeedpublish_back);
		mPublish = (ImageView) findViewById(R.id.newsfeedpublish_publish);
		mContent = (EditText) findViewById(R.id.newsfeedpublish_content);
		// mPlace = (Button) findViewById(R.id.newsfeedpublish_poi_place);
		// mSperator = (ImageView)
		// findViewById(R.id.newsfeedpublish_poi_sperator);
		// mList = (ImageView) findViewById(R.id.newsfeedpublish_poi_list);
		mCount = (TextView) findViewById(R.id.newsfeedpublish_count);
		mVoice = (ImageButton) findViewById(R.id.newsfeedpublish_voice);
		mPoi = (ImageButton) findViewById(R.id.newsfeedpublish_poi);
		mImage = (ImageButton) findViewById(R.id.newsfeedpublish_image);
		mAt = (ImageButton) findViewById(R.id.newsfeedpublish_at);
		mEmoticon = (ImageButton) findViewById(R.id.newsfeedpublish_emoticon);
		mEmoticons = (GridView) findViewById(R.id.newsfeedpublish_emoticons);
		historyMessageListView = (ListView) findViewById(R.id.listview_message);
		setting = (Button) findViewById(R.id.btn_settting);

	}

	/*
	 * TODO
	 */
	private ArrayList<MessageItem> getData()
	{
		ArrayList<MessageItem> data = new ArrayList<MessageItem>();
		// for (int i = 0; i < 20; i++) {
		// data.add("How are you? " + i);
		// }

		return data;
	}

	private void setListeners()
	{
		mBack.setOnClickListener(new OnClickListener()
		{

			public void onClick(View v)
			{
				if (mContent.getText().toString().trim().length() > 0)
				{
					backDialog();
				} else
				{
					finish();
					overridePendingTransition(0, R.anim.roll_down);
				}
			}
		});
		mPublish.setOnClickListener(new OnClickListener()
		{

			public void onClick(View v)
			{
				if (mContent.getText().toString().trim().length() == 0)
				{
					Toast.makeText(SendFeed.this, R.string.content_empty, Toast.LENGTH_SHORT)
							.show();
				} else
				{
					try
					{
						publishNewsFeed(mContent.getText().toString().trim());
					} catch (IOException e)
					{
						e.printStackTrace();
					}
					mContent.setText("");
				}
			}
		});
		mContent.setOnClickListener(new OnClickListener()
		{

			public void onClick(View v)
			{
				if (mEmoticons.isShown())
				{
					mEmoticons.setVisibility(View.GONE);
					mEmoticon.setImageResource(R.drawable.v5_0_1_publisher_emotion_button);
				}
			}
		});
		mContent.addTextChangedListener(new TextWatcher()
		{
			private CharSequence temp;
			private int selectionStart;
			private int selectionEnd;

			public void onTextChanged(CharSequence s, int start, int before, int count)
			{
				temp = s;
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after)
			{

			}

			public void afterTextChanged(Editable s)
			{
				int number = s.length();
				mCount.setText(String.valueOf(number) + "/140");
				selectionStart = mContent.getSelectionStart();
				selectionEnd = mCount.getSelectionEnd();
				if (temp.length() > 140)
				{
					s.delete(selectionStart - 1, selectionEnd);
					int tempSelection = selectionEnd;
					mContent.setText(s);
					mContent.setSelection(tempSelection);
				}
			}
		});
		/*
		 * mPlace.setOnClickListener(new OnClickListener() {
		 * 
		 * public void onClick(View v) { if
		 * (mPlace.getText().toString().equals("��ӵص�")) { mLBSIsReceiver =
		 * true; mPlace.setText("���ڶ�λ...");
		 * mSperator.setVisibility(View.VISIBLE);
		 * mPoi.setImageResource(R.drawable.v5_0_1_publisher_poi_button_on); if
		 * (!mClient.isStarted()) { mClient.start(); }
		 * mClient.requestLocation(); } else if
		 * (mPlace.getText().toString().equals("���ڶ�λ...")) { if
		 * (mClient.isStarted()) { mClient.stop(); mLBSIsReceiver = false;
		 * mLBSAddress = null; mPlace.setCompoundDrawables(mPoi_off_icon, null,
		 * null, null); mPlace.setText("��ӵص�");
		 * mSperator.setVisibility(View.INVISIBLE);
		 * mList.setVisibility(View.INVISIBLE);
		 * mPoi.setImageResource(R.drawable.v5_0_1_publisher_poi_button); } }
		 * else { // startActivity(new Intent(NewsFeedPublish.this, //
		 * CurrentLocation.class)); // overridePendingTransition(R.anim.roll_up,
		 * R.anim.roll); } } });
		 */
		mVoice.setOnClickListener(new OnClickListener()
		{

			public void onClick(View v)
			{
				try
				{
					if (isServerConnected)
					{
						Toast.makeText(SendFeed.this, "关闭Server服务", Toast.LENGTH_SHORT).show();
						shutDownServer();
					} else
					{
						Toast.makeText(SendFeed.this, "开启Server服务", Toast.LENGTH_SHORT).show();
						new Thread(new Runnable()
						{
							@Override
							public void run()
							{
								try
								{
									try
									{
										startServer();
									} catch (InterruptedException e)
									{
										e.printStackTrace();
									}
								} catch (IOException e)
								{
									e.printStackTrace();
								}
							}
						}).start();
					}
				} catch (IOException e)
				{
					e.printStackTrace();
					Toast.makeText(getApplicationContext(), "Start Server failed...",
							Toast.LENGTH_SHORT).show();
				}
			}
		});
		/*
		 * mPoi.setOnClickListener(new OnClickListener() {
		 * 
		 * public void onClick(View v) { if (mLBSIsReceiver) { mLBSIsReceiver =
		 * false; mLBSAddress = null; mPlace.setCompoundDrawables(mPoi_off_icon,
		 * null, null, null); mPlace.setText("��ӵص�");
		 * mSperator.setVisibility(View.INVISIBLE);
		 * mList.setVisibility(View.INVISIBLE);
		 * mPoi.setImageResource(R.drawable.v5_0_1_publisher_poi_button); } else
		 * { mLBSIsReceiver = true; mPlace.setText("���ڶ�λ...");
		 * mSperator.setVisibility(View.VISIBLE);
		 * mPoi.setImageResource(R.drawable.v5_0_1_publisher_poi_button_on); if
		 * (!mClient.isStarted()) { mClient.start(); }
		 * mClient.requestLocation(); } } });
		 */
		mPoi.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				try
				{
					if (isConnecttingServer)
						shutDownConnect();
					else
						new Thread(new Runnable()
						{
							@Override
							public void run()
							{
								try
								{
									try
									{
										connectServer();
									} catch (InterruptedException e)
									{
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								} catch (IOException e)
								{
									e.printStackTrace();
								}
							}
						}).start();
				} catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		});

		mImage.setOnClickListener(new OnClickListener()
		{

			public void onClick(View v)
			{
				Toast.makeText(SendFeed.this, "��ʱ�޷��ṩ�˹���", Toast.LENGTH_SHORT).show();
			}
		});
		mAt.setOnClickListener(new OnClickListener()
		{

			public void onClick(View v)
			{
				Toast.makeText(SendFeed.this, "��ʱ�޷��ṩ�˹���", Toast.LENGTH_SHORT).show();
			}
		});
		mEmoticon.setOnClickListener(new OnClickListener()
		{

			public void onClick(View v)
			{
				if (mEmoticons.isShown())
				{
					mEmoticons.setVisibility(View.GONE);
					mEmoticon.setImageResource(R.drawable.v5_0_1_publisher_emotion_button);
				} else
				{
					mEmoticons.setVisibility(View.VISIBLE);
					mEmoticon.setImageResource(R.drawable.v5_0_1_publisher_pad_button);
					((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
							.hideSoftInputFromWindow(SendFeed.this.getCurrentFocus()
									.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
				}
			}
		});
		mEmoticons.setOnItemClickListener(new OnItemClickListener()
		{

			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				int temp = DataProvider.imageRes[position];
				sendSmileIcon(position);
			}

		});
		mClient.registerLocationListener(new BDLocationListener()
		{

			public void onReceivePoi(BDLocation arg0)
			{

			}

			public void onReceiveLocation(BDLocation arg0)
			{
				// mLBSAddress = arg0.getAddrStr();
				// mApplication.mLocation = arg0.getAddrStr();
				// mApplication.mLatitude = arg0.getLatitude();
				// mApplication.mLongitude = arg0.getLongitude();
				// handler.sendEmptyMessage(2);
			}
		});
		setting.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{

			}
		});
	}

	protected void connectServer() throws IOException, InterruptedException
	{
		isConnecttingServer = true;
		InetAddress address = InetAddress.getLocalHost();
		String IP = address.getHostAddress();
		Log.e("SendFeed", "IP:" + IP);
		clientRequestSocket = new Socket(IP, PORT);
		clientReader = new BufferedReader(new InputStreamReader(
				clientRequestSocket.getInputStream()));
		clientWriter = new PrintWriter(
				new OutputStreamWriter(clientRequestSocket.getOutputStream()), true);
		// clientWriter.print("request connect\n");
		Log.e("SendFeed", "requset connect to server");
		// clientWriter.flush();
		while (isConnecttingServer)
		{
			String content = clientReader.readLine();
			Log.e("SendFeed", "Received content from server:%s" + content);
			Thread.sleep(1000);
		}
	}

	protected void shutDownConnect() throws IOException
	{
		if (clientReader != null)
			clientReader.close();
		if (clientWriter != null)
			clientWriter.close();
		if (clientRequestSocket != null)
			clientRequestSocket.close();
		isConnecttingServer = false;
	}

	private void startServer() throws IOException, InterruptedException
	{
		serverReceiver = new ServerSocket(PORT);
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
		while (isServerConnected)
		{
			while ((length = serveReader.read(buffer, 0, buffer.length)) != -1)
			{
				Log.e("SendFeed", "receive conetent from client:" + new String(buffer));
				handler.obtainMessage(Cons.UPDATE_MSG_RECEIVED_CLIENT,
						StringHelper.undecodeContent(new String(buffer))).sendToTarget();
			}
			Thread.sleep(100);
		}
		Log.e("SendFeed", "server end:");
	}

	private void shutDownServer() throws IOException
	{
		if (serveReader != null)
			serveReader.close();
		if (serverResponseSocket != null)
			serverResponseSocket.close();
		isServerConnected = false;
	}

	private void sendSmileIcon(int position)
	{
		MessageItem item = new MessageItem();
		item.setSmileIconName(position);
		messageHistoryList.add(item);
		notifyChange();
	}

	private void notifyChange()
	{
		messageHistoryAdapter.notifyDataSetChanged();
		historyMessageListView.setSelection(messageHistoryList.size() - 1);
	}

	private void initLBS()
	{
		mOption = new LocationClientOption();
		mOption.setOpenGps(true);
		mOption.setCoorType("bd09ll");
		mOption.setAddrType("all");
		mOption.setScanSpan(100);
		// mOption.disableCache(true);
		// mOption.setPoiNumber(20);
		// mOption.setPoiDistance(1000);
		// mOption.setPoiExtraInfo(true);
		mClient = new LocationClient(getApplicationContext(), mOption);
	}

	private void publishNewsFeed(String status) throws IOException
	{
		MessageItem item = new MessageItem();
		item.setContent(status);
		item.setUser(true);
		messageHistoryList.add(item);
		notifyChange();
		Log.e("SendFeed", "send content" + status);
		clientWriter.write(StringHelper.decodeContent(status));
		clientWriter.flush();
		Log.e("SendFeed", "flush content" + status);
	}

	Handler handler = new Handler()
	{

		public void handleMessage(Message msg)
		{
			super.handleMessage(msg);
			switch (msg.what)
			{
			case 0:
				publishDialogShow();
				break;
			case 1:
				publishDialogDismiss();
				switch (Integer.parseInt(msg.obj.toString()))
				{
				case 1:
					mContent.setText("");
					Toast.makeText(SendFeed.this, "�����ɹ�", Toast.LENGTH_SHORT).show();
					finish();
					overridePendingTransition(0, R.anim.roll_down);
					break;

				case 10400:
					Toast.makeText(SendFeed.this, "״̬���¹���Ƶ��", Toast.LENGTH_SHORT).show();
					break;
				case 10401:
					Toast.makeText(SendFeed.this, "״̬������޶�����", Toast.LENGTH_SHORT).show();
					break;

				case 10402:
					Toast.makeText(SendFeed.this, "״̬�����ݺ��зǷ��ַ�", Toast.LENGTH_SHORT).show();
					break;
				}
				break;

			case 2:
				// if (mClient.isStarted()) {
				// mClient.stop();
				// }
				// if (mLBSAddress != null) {
				// mPlace.setText(mLBSAddress);
				// mPlace.setCompoundDrawables(mPoi_on_icon, null, null, null);
				// mList.setVisibility(View.VISIBLE);
				// mSperator.setVisibility(View.VISIBLE);
				// }
				break;
			case Cons.UPDATE_MSG_RECEIVED_CLIENT:
				String result = (String) msg.obj;
				if (result != null)
				{
					MessageItem item = new MessageItem();
					item.setContent(result);
					item.setUser(false);
					messageHistoryList.add(item);
					Log.e("SendFeed", "update contnet" + result);
					messageHistoryAdapter.notifyDataSetChanged();
				}
				break;
			default:
				break;
			}
		}
	};

	private void backDialog()
	{
		AlertDialog.Builder builder = new Builder(SendFeed.this);
		builder.setTitle("��ʾ");
		builder.setMessage("�Ƿ�ȡ��?");
		builder.setPositiveButton("ȷ��", new DialogInterface.OnClickListener()
		{

			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
				finish();
				overridePendingTransition(0, R.anim.roll_down);
			}
		});
		builder.setNegativeButton("ȡ��", new DialogInterface.OnClickListener()
		{

			public void onClick(DialogInterface dialog, int which)
			{
				dialog.cancel();
			}
		});
		builder.create().show();
	}

	private void publishDialogShow()
	{
		if (mPublishDialog == null)
		{
			mPublishDialog = new ProgressDialog(SendFeed.this);
			mPublishDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			mPublishDialog.setMessage("���ڷ���");
		}
		mPublishDialog.show();
	}

	private void publishDialogDismiss()
	{
		if (mPublishDialog != null && mPublishDialog.isShowing())
		{
			mPublishDialog.dismiss();
		}
	}

	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			if (mContent.getText().toString().trim().length() > 0)
			{
				backDialog();
			} else
			{
				finish();
				overridePendingTransition(0, R.anim.roll_down);
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

}

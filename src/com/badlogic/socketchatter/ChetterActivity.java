package com.badlogic.socketchatter;

import java.io.IOException;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import com.badlogic.constant.Cons;
import com.badlogic.model.MessageItem;
import com.badlogic.providers.DataProvider;
import com.badlogic.socketchatter.ChatterService.MessageListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

public class ChetterActivity extends Activity {
	private static int PORT = 3000;
	private ImageView mBack;
	private ImageView mPublish;
	private EditText mContent;
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

	private Drawable mPoi_off_icon;
	private Drawable mPoi_on_icon;
	private Button setting;
	private ProgressDialog mPublishDialog;
	private ListView historyMessageListView;
	private MessageHistoryAdapter messageHistoryAdapter;
	private ArrayList<MessageItem> messageHistoryList;
	private String IP;
	private MessageListener messageListener = new MessageListener() {
		@Override
		public void onReceive(String content) {
			handler.obtainMessage(Cons.UPDATE_MSG_RECEIVED_CLIENT, content)
					.sendToTarget();
		}

		@Override
		public void onFailed() {

		}

		@Override
		public void onComplete() {

		}
	};
	private ChatterService chatterService;
	private ServiceConnection serviceConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			chatterService = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			chatterService = ((ChatterService.ServiceBinder) service)
					.getServices();
			chatterService.setMessageListener(messageListener);
			showToast("start listen for message");
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						try {
							chatterService.startServer(PORT);
						} catch (InterruptedException e) {
							handler.obtainMessage(Cons.START_SERVICE_FAILED)
									.sendToTarget();
							e.printStackTrace();
						}
					} catch (IOException e) {
						handler.obtainMessage(Cons.START_SERVICE_FAILED)
								.sendToTarget();
						e.printStackTrace();
					}
				}
			}).start();

		}
	};
	private void showToast(String content) {
		Toast.makeText(getApplicationContext(), content, Toast.LENGTH_SHORT)
				.show();
	}
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.newsfeedpublish);
		bindService(new Intent(ChetterActivity.this, ChatterService.class),
				serviceConnection, Context.BIND_AUTO_CREATE);

		IP = getIntent().getStringExtra("ip_address");
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
				.detectDiskReads().detectDiskWrites().detectNetwork()
				.penaltyLog().build());
		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
				.detectLeakedSqlLiteObjects().penaltyLog().penaltyDeath()
				.build());

		initLBS();
		findViewById();
		setListeners();
		initRes();

	}

	public void initRes() {
		mPoi_off_icon = getResources().getDrawable(
				R.drawable.v5_0_1_publisher_poi_icon);
		mPoi_off_icon.setBounds(0, 0, mPoi_off_icon.getMinimumWidth(),
				mPoi_off_icon.getMinimumHeight());
		mPoi_on_icon = getResources().getDrawable(
				R.drawable.v5_0_1_publisher_poi_active_icon);
		mPoi_on_icon.setBounds(0, 0, mPoi_on_icon.getMinimumWidth(),
				mPoi_on_icon.getMinimumHeight());
		emotionStaticAdapter = new EmotionStaticAdapter(
				this.getApplicationContext(), DataProvider.imageRes);
		mEmoticons.setAdapter(emotionStaticAdapter);
		messageHistoryList = getData();
		messageHistoryAdapter = new MessageHistoryAdapter(
				this.getApplicationContext(), messageHistoryList);
		historyMessageListView.setAdapter(messageHistoryAdapter);
		mClient.start();
		mClient.requestLocation();
	}

	private void findViewById() {
		mBack = (ImageView) findViewById(R.id.newsfeedpublish_back);
		mPublish = (ImageView) findViewById(R.id.newsfeedpublish_publish);
		mContent = (EditText) findViewById(R.id.newsfeedpublish_content);
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
	private ArrayList<MessageItem> getData() {
		ArrayList<MessageItem> data = new ArrayList<MessageItem>();
		return data;
	}

	private void setListeners() {
		mBack.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				onBackPressed();
			}
		});
		mPublish.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				if (mContent.getText().toString().trim().length() == 0) {
					Toast.makeText(ChetterActivity.this,
							R.string.content_empty, Toast.LENGTH_SHORT).show();
				} else {
					try {
						publishNewsFeed(mContent.getText().toString().trim());
					} catch (IOException e) {
						e.printStackTrace();
					}
					mContent.setText("");
				}
			}
		});
		mContent.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				if (mEmoticons.isShown()) {
					mEmoticons.setVisibility(View.GONE);
					mEmoticon
							.setImageResource(R.drawable.v5_0_1_publisher_emotion_button);
				}
			}
		});
		mContent.addTextChangedListener(new TextWatcher() {
			private CharSequence temp;
			private int selectionStart;
			private int selectionEnd;

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				temp = s;
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			public void afterTextChanged(Editable s) {
				int number = s.length();
				mCount.setText(String.valueOf(number) + "/140");
				selectionStart = mContent.getSelectionStart();
				selectionEnd = mCount.getSelectionEnd();
				if (temp.length() > 140) {
					s.delete(selectionStart - 1, selectionEnd);
					int tempSelection = selectionEnd;
					mContent.setText(s);
					mContent.setSelection(tempSelection);
				}
			}
		});

		mVoice.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Toast.makeText(ChetterActivity.this, "开启Server服务",
						Toast.LENGTH_SHORT).show();
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							try {
								chatterService.startServer(PORT);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}).start();
			}
		});

		mPoi.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					if (chatterService.isConnecttingServer)
						chatterService.shutDownConnect();
					else
						new Thread(new Runnable() {
							@Override
							public void run() {
								try {
									try {
										chatterService.connectServer(IP, PORT);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}).start();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

		mEmoticon.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				if (mEmoticons.isShown()) {
					mEmoticons.setVisibility(View.GONE);
					mEmoticon
							.setImageResource(R.drawable.v5_0_1_publisher_emotion_button);
				} else {
					mEmoticons.setVisibility(View.VISIBLE);
					mEmoticon
							.setImageResource(R.drawable.v5_0_1_publisher_pad_button);
					((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
							.hideSoftInputFromWindow(ChetterActivity.this
									.getCurrentFocus().getWindowToken(),
									InputMethodManager.HIDE_NOT_ALWAYS);
				}
			}
		});
		mEmoticons.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				int temp = DataProvider.imageRes[position];
				sendSmileIcon(position);
			}

		});

		setting.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

			}
		});
	}

	private void sendSmileIcon(int position) {
		MessageItem item = new MessageItem();
		item.setSmileIconName(position);
		messageHistoryList.add(item);
		notifyChange();
	}

	private void notifyChange() {
		messageHistoryAdapter.notifyDataSetChanged();
		historyMessageListView.setSelection(messageHistoryList.size() - 1);
	}

	private void initLBS() {
		mOption = new LocationClientOption();
		mOption.setOpenGps(true);
		mOption.setCoorType("bd09ll");
		mOption.setAddrType("all");
		mOption.setScanSpan(100);
		mClient = new LocationClient(getApplicationContext(), mOption);
	}

	private void publishNewsFeed(String status) throws IOException {
		MessageItem item = new MessageItem();
		item.setContent(status);
		item.setUser(true);
		messageHistoryList.add(item);
		notifyChange();
		chatterService.onPost(status);
	}

	@SuppressLint("HandlerLeak")
	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
				case Cons.UPDATE_MSG_RECEIVED_CLIENT :
					String result = (String) msg.obj;
					if (result != null) {
						MessageItem item = new MessageItem();
						item.setContent(result);
						item.setUser(false);
						messageHistoryList.add(item);
						Log.e("SendFeed", "update contnet" + result);
						notifyChange();
					}
					break;
				case Cons.START_SERVICE_FAILED :
					showToast("failed to listen for message");
				default :
					break;
			}
		}
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindService(serviceConnection);
	}
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(0, R.anim.roll_down);
	}
}

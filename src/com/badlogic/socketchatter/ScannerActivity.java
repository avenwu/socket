package com.badlogic.socketchatter;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.badlogic.R;
import com.badlogic.constant.Config;
import com.badlogic.constant.Cons;
import com.badlogic.task.ScanTask;
import com.badlogic.utils.IPHelper;
/**
 * User Interface, to scan the IP or enter Chat Page
 * 
 * @author AvenWu
 * @2013-5-1
 */
public class ScannerActivity extends Activity {
	private ImageView radaRotate;
	private ImageButton startScan;
	private ImageButton startConnect;
	private ListView ipListView;
	private TextView tvLoaclIP;
	private EditText editViewIp;
	private TextView tvTimePassed;
	private Animation animation;
	private ArrayList<String> ipList;
	private ArrayAdapter<String> ipAdapter;
	private boolean isRotating;
	private ScanTask scanTask;;
	private long passedTime = 0;
	private String timeSub;
	private Timer timer;
	private TimerTask timerTask;
	private String ip = "";

	/**
	 * update used time for scan IP;
	 */
	private Handler timeHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			updateTimeTextView();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ip_scanner_layout);
		initRes();
		setListeners();
		InetAddress ipString = IPHelper.getIPWifi(this);
		if (ipString == null) {
			showToast(R.string.wifi_off);
		} else {
			String ip = getString(R.string.local_ip)
					+ ipString.getHostAddress();
			tvLoaclIP.setText(ip);
		}

	}
	/**
	 * increase the time escaped every 1 second;
	 */
	protected void updateTimeTextView() {
		passedTime++;
		tvTimePassed.setText(timeSub + passedTime + "s");
	}
	/**
	 * start task to scan the IP in work thread;
	 */
	private void startScan() {
		radaRotate.startAnimation(animation);
		if (scanTask != null) {
			scanTask.cancelTask(true);
		}
		InetAddress local = IPHelper.getIPWifi(getApplicationContext());
		if (local == null) {
			tvLoaclIP.setText(R.string.local_ip);
		} else {
			String ip = getString(R.string.local_ip) + local.getHostAddress();
			tvLoaclIP.setText(ip);
		}
		scanTask = new ScanTask(local, mHandler);
		scanTask.execute();
	}

	private void setListeners() {
		/**
		 * start to scan the IP or stop currently task for scanning;
		 */
		startScan.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isRotating) {
					clearTask();
					clearData();
					isRotating = false;
				} else {
					resetData();
					increaseTime();
					isRotating = true;
					startScan();
				}
				resetScanBtn();
			}
		});
		/**
		 * enter the chat page with selected/default IP
		 */
		startConnect.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				clearTask();
				if (!editViewIp.getText().toString().isEmpty()) {
					String text = editViewIp.getText().toString();
					int index = text.lastIndexOf(Config.IP_SEPARATE) + 1;
					ip = text.substring(index).trim();
				}
				Intent intent = new Intent(ScannerActivity.this,
						ChetterActivity.class);
				intent.putExtra("ip_address", ip);
				startActivity(intent);
				overridePendingTransition(android.R.anim.slide_in_left,
						android.R.anim.fade_out);
			}
		});

		ipListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View v,
					int position, long id) {
				Log.d("ScannerActivity", "item clicked, posiyion=" + position);
				ip = ipList.get(position);
				String text = editViewIp.getText().toString();
				int index = text.lastIndexOf(Config.IP_SEPARATE) + 1;
				editViewIp.setText(text.replace(text.substring(index), ip));
			}
		});
	}
	protected void resetData() {
		if (ipList != null && ipAdapter != null) {
			ipList.clear();
			ipAdapter.notifyDataSetChanged();
			editViewIp.setText("");
		}
		passedTime = 0;
	}
	public void resetScanBtn() {
		startScan.setBackgroundResource(isRotating
				? R.drawable.stop_scan_btn_bg
				: R.drawable.scan_btn_bg);
	}
	private void initRes() {
		radaRotate = (ImageView) findViewById(R.id.iv_rada_scan);
		startScan = (ImageButton) findViewById(R.id.iv_scan_btn);
		startConnect = (ImageButton) findViewById(R.id.iv_enter_btn);
		ipListView = (ListView) findViewById(R.id.lv_ip);
		tvLoaclIP = (TextView) findViewById(R.id.tv_local_ip);
		editViewIp = (EditText) findViewById(R.id.edi_ip);
		tvTimePassed = (TextView) findViewById(R.id.tv_time_passed);
		tvTimePassed.setText(getString(R.string.time_passed, "0"));
		animation = AnimationUtils.loadAnimation(this, R.anim.scaner_rotate);
	}
	/**
	 * cancel the scan task & time task
	 */
	public void clearTask() {
		if (scanTask != null) {
			scanTask.cancelTask(true);
		}
		radaRotate.clearAnimation();
		isRotating = false;
		if (timer != null) {
			timer.cancel();
		}
		timer = null;
		timerTask = null;
	}
	/**
	 * reset the ip list & passed time to initial state;
	 */
	public void clearData() {
		if (ipList != null) {
			ipList.clear();
			ipAdapter.notifyDataSetChanged();
		}
		ipList = null;
		ipAdapter = null;
		passedTime = 0;
		editViewIp.setText("");
		tvTimePassed.setText("");
		tvTimePassed.setVisibility(View.GONE);
	}

	private void showToast(int content) {
		Toast.makeText(this, content, Toast.LENGTH_SHORT).show();
	}
	/**
	 * deal with the message from ip-scan work thread;
	 */
	private Handler mHandler = new Handler() {
		@SuppressWarnings("unchecked")
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
				case Cons.STOP_SCANNING :
					isRotating = false;
					ipList = (ArrayList<String>) msg.obj;
					radaRotate.clearAnimation();
					resetScanBtn();
					if (ipList != null && ipList.size() > 0) {
						ipAdapter = new ArrayAdapter<String>(
								getApplicationContext(), R.layout.ip_item,
								R.id.tv_ip, ipList);
						ipListView.setAdapter(ipAdapter);
						ip = ipList.get(0);
						editViewIp.setText(getString(R.string.connect_to, ip));
					} else {
						showToast(R.string.failed_to_get_ip);
					}
					clearTask();
					break;
				case Cons.SCANNING_FAILED :
					showToast(R.string.failed_to_get_ip);
					clearTask();
					resetScanBtn();
					break;
				default :
					break;
			}
		}
	};

	public void increaseTime() {
		passedTime = 0;
		timeSub = getString(R.string.time_passed);
		tvTimePassed.setVisibility(View.VISIBLE);
		tvTimePassed.setText(timeSub + passedTime + "s");
		timer = new Timer();
		timerTask = new TimerTask() {
			@Override
			public void run() {
				timeHandler.obtainMessage(Cons.UPDATE_TIME).sendToTarget();
			}
		};
		timer.schedule(timerTask, Config.DELAY_TIME, Config.INTERAL_TIME);
	}

	public void onBackPressed() {
		super.onBackPressed();
		if (scanTask != null) {
			scanTask.cancel(true);
		}
		overridePendingTransition(0, R.anim.roll_down);
	}
}

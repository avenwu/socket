package com.badlogic.socketchatter;

import java.net.InetAddress;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.badlogic.R;
import com.badlogic.constant.Cons;
import com.badlogic.task.ScanTask;
import com.badlogic.utils.IPHelper;
/**
 * User Interface, to scan the IP or enter Chat Page
 * 
 * @author AvenWu
 * @2013-5-1
 */
public class IPScannerActivity extends Activity {
	private ImageView radaRotate;
	private ImageButton startScan;
	private ImageButton startConnect;
	private TextView tvIP;
	private Animation animation;
	private ArrayList<String> ipList;
	private boolean isRotating;
	private ScanTask scanTask;;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ip_scanner_layout);
		initRes();
		setListeners();
		InetAddress ipString = IPHelper.getIPWifi(this);
		tvIP.setText(ipString.getHostAddress());
	}

	private void startScan() {
		radaRotate.startAnimation(animation);
		if (scanTask != null) {
			scanTask = new ScanTask(getApplicationContext(), mHandler);
			scanTask.execute();
		}
	}

	private void setListeners() {
		startScan.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isRotating) {
					return;
				}
				isRotating = true;
				startScan();
			}
		});
		startConnect.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				scanTask.cancel(true);
				radaRotate.clearAnimation();
				Intent intent = new Intent(IPScannerActivity.this,
						SendFeed.class);
				intent.putExtra("ip_address", "192.168.43.1");
				startActivity(intent);
				overridePendingTransition(android.R.anim.slide_in_left,
						android.R.anim.fade_out);
			}
		});
	}

	private void initRes() {
		radaRotate = (ImageView) findViewById(R.id.iv_rada_scan);
		startScan = (ImageButton) findViewById(R.id.iv_scan_btn);
		startConnect = (ImageButton) findViewById(R.id.iv_enter_btn);
		tvIP = (TextView) findViewById(R.id.tv_ip_available);
		animation = AnimationUtils.loadAnimation(this, R.anim.scaner_rotate);
	}

	private void showToast(int content) {
		Toast.makeText(this, content, Toast.LENGTH_SHORT).show();
	}

	private Handler mHandler = new Handler() {
		@SuppressWarnings("unchecked")
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
				case Cons.STOP_SCANNING :
					ipList = (ArrayList<String>) msg.obj;
					radaRotate.clearAnimation();
					if (ipList != null && ipList.size() > 0) {
						tvIP.setText(R.string.available_ip);
						for (String ip : ipList) {
							tvIP.append(ip + "\n");
						}
					} else {
						showToast(R.string.failed_to_get_ip);
					}
					break;
				case Cons.SCANNING_FAILED :
					showToast(R.string.failed_to_get_ip);
					break;
				default :
					break;
			}
		}
	};
	public void onBackPressed() {
		super.onBackPressed();
		if (scanTask != null) {
			scanTask.cancel(true);
		}
		overridePendingTransition(0, R.anim.roll_down);
	}
}

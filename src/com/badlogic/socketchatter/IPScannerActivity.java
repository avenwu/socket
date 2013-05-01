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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.badlogic.R;
import com.badlogic.constant.Cons;
import com.badlogic.utils.IPHelper;

public class IPScannerActivity extends Activity {
	private ImageView radaRotate;
	private Button startScan;
	private Button startConnect;
	private TextView tvIP;
	private Animation animation;
	private ArrayList<String> ipList;
	private boolean isRotating;

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
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					InetAddress localIP = IPHelper
							.getIPWifi(IPScannerActivity.this);
					ipList = IPHelper.pingIP(localIP.getHostAddress());
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					mHandler.obtainMessage(Cons.STOP_SCANNING).sendToTarget();
				}
			}
		}).start();

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
				Intent intent = new Intent(IPScannerActivity.this,
						SendFeed.class);
				intent.putExtra("ip_address", "192.168.43.1");
				startActivity(intent);
				finish();
			}
		});
	}

	private void initRes() {
		radaRotate = (ImageView) findViewById(R.id.iv_rada_scan);
		startScan = (Button) findViewById(R.id.btn_start_scan);
		startConnect = (Button) findViewById(R.id.btn_start_connect);
		tvIP = (TextView) findViewById(R.id.tv_ip_available);
		animation = AnimationUtils.loadAnimation(this, R.anim.scaner_rotate);
	}

	private void getIPFailed() {
		Toast.makeText(this, "Failed to get IP", Toast.LENGTH_SHORT).show();
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
				case Cons.STOP_SCANNING :
					radaRotate.clearAnimation();
					if (ipList != null && ipList.size() > 0) {
						tvIP.setText("Available IP:\n");
						for (String ip : ipList) {
							tvIP.append(ip + "\n");
						}
					} else {
						getIPFailed();
					}
					break;
				default :
					break;
			}

		}

	};
}

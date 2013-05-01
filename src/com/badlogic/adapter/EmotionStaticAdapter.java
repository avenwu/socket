package com.badlogic.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.badlogic.R;
/**
 * Adapter used for Emotion list
 * 
 * @author AvenWu
 * @2013-5-1
 */
public class EmotionStaticAdapter extends BaseAdapter {
	private Context context;
	private int[] emotionsRes;
	private int width;

	public EmotionStaticAdapter(Context context, int[] res) {
		this.context = context;
		this.emotionsRes = res;
		this.width = (int) context.getResources().getDimension(
				R.dimen.emotion_width);
	}

	@Override
	public int getCount() {
		return emotionsRes.length;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			ImageView iv = new ImageView(context);
			convertView = iv;
		}
		convertView.setBackgroundResource(emotionsRes[position]);
		return convertView;
	}
}

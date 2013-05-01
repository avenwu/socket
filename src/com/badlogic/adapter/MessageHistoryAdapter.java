package com.badlogic.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.badlogic.R;
import com.badlogic.model.MessageItem;
import com.badlogic.providers.DataProvider;
/**
 * Adapter for chat/message ListView;
 * 
 * @author AvenWu
 * @2013-5-1
 */
public class MessageHistoryAdapter extends BaseAdapter {
	private Context context;
	private ArrayList<MessageItem> dataList;
	private LayoutInflater inflater;

	public MessageHistoryAdapter(Context context,
			ArrayList<MessageItem> dataList) {
		this.context = context;
		this.dataList = dataList;
		this.inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return dataList.size();
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
		ViewHolder viewHolder;
		MessageItem dataItem = dataList.get(position);
		if (convertView == null) {
			View view = inflater.inflate(R.layout.chat_item, null);
			viewHolder = new ViewHolder();
			viewHolder.leftTextContent = (TextView) view
					.findViewById(R.id.tv_left_content);
			viewHolder.rightTextContent = (TextView) view
					.findViewById(R.id.tv_right_content);
			viewHolder.leftLayout = (LinearLayout) view
					.findViewById(R.id.ll_left_chat_item);
			viewHolder.rightLayout = (LinearLayout) view
					.findViewById(R.id.ll_right_chat_item);
			viewHolder.rightImageContent = (ImageView) view
					.findViewById(R.id.iv_right_avatar);
			view.setTag(viewHolder);
			convertView = view;
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		setData(dataItem, viewHolder);
		return convertView;
	}

	private void setData(MessageItem data, ViewHolder viewHolder) {
		if (data.isUser()) {
			viewHolder.rightTextContent.setText(data.getContent());
			viewHolder.rightLayout.setVisibility(View.VISIBLE);
			viewHolder.leftLayout.setVisibility(View.GONE);
			if (data.getSmileIconName() != null) {
				viewHolder.rightImageContent
						.setBackgroundResource(DataProvider.imageRes[Integer
								.parseInt(data.getSmileIconName())]);
				viewHolder.rightImageContent.setVisibility(View.VISIBLE);
			} else {
				viewHolder.rightImageContent.setVisibility(View.GONE);
			}
		} else {
			viewHolder.leftTextContent.setText(data.getContent());
			viewHolder.rightLayout.setVisibility(View.GONE);
			viewHolder.leftLayout.setVisibility(View.VISIBLE);
			if (data.getSmileIconName() != null) {
				viewHolder.rightImageContent
						.setBackgroundResource(DataProvider.imageRes[Integer
								.parseInt(data.getSmileIconName())]);
				viewHolder.rightImageContent.setVisibility(View.VISIBLE);
			} else {
				viewHolder.rightImageContent.setVisibility(View.GONE);
			}
		}

	}

	static class ViewHolder {
		public TextView leftTextContent;
		public TextView rightTextContent;
		public ImageView rightImageContent;
		public ImageView leftImageContent;
		public ImageView voiceContent;
		public LinearLayout leftLayout;
		public LinearLayout rightLayout;

	}
}

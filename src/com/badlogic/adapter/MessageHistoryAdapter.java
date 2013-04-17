package com.badlogic.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.badlogic.R;

public class MessageHistoryAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<String> dataList;
    private LayoutInflater inflater;
    
    public MessageHistoryAdapter(Context context, ArrayList<String> dataList) {
        this.context = context;
        this.dataList = dataList;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
        if (convertView == null) {
            View view = inflater.inflate(R.layout.chat_item, null);
            viewHolder = new ViewHolder();
            viewHolder.leftTextContent = (TextView) view.findViewById(R.id.tv_left_content);
            viewHolder.rightTextContent = (TextView) view.findViewById(R.id.tv_right_content);
            view.setTag(viewHolder);
            convertView = view;
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.leftTextContent.setText(dataList.get(position));
        viewHolder.rightTextContent.setText(dataList.get(position));

        return convertView;
    }

    static class ViewHolder {
        public TextView leftTextContent;
        public TextView rightTextContent;
        public ImageView imageContent;
        public ImageView voiceContent;

    }
}

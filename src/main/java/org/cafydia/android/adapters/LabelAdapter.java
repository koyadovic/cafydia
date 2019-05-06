package org.cafydia.android.adapters;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.cafydia.android.R;
import org.cafydia.android.chartobjects.Label;
import org.cafydia.android.util.C;

import java.util.ArrayList;

/**
 * Created by user on 9/12/14.
 */
public class LabelAdapter extends BaseAdapter {
    private ArrayList<Label> labels;
    private Activity hostActivity;
    private int mTextColor = 0;

    public LabelAdapter(Activity host, ArrayList<Label> labels) {
        this(host, labels, null);
    }

    public LabelAdapter(Activity host, ArrayList<Label> labels, Integer textColor){
        super();
        hostActivity = host;
        this.labels = labels;

        if(textColor != null) {
            mTextColor = textColor;
        }
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View item = convertView;
        LabelHolder holder;

        if(item == null){
            LayoutInflater inflater = hostActivity.getLayoutInflater();
            item = inflater.inflate(R.layout.label_listview_item, null);
            holder = new LabelHolder();

            holder.title = (TextView) item.findViewById(R.id.tvTitle);
            holder.rangeCount = (TextView) item.findViewById(R.id.tvRangeCount);
            holder.llBackground = (LinearLayout) item.findViewById(R.id.llBackground);

            item.setTag(holder);
        } else {
            holder = (LabelHolder) item.getTag();
        }

        holder.title.setText(labels.get(position).getTitle());
        if(mTextColor == C.LABEL_TEXT_COLOR_BLACK) {
            holder.title.setTextColor(Color.BLACK);
        }
        holder.rangeCount.setText(labels.get(position).getRangeCount().toString());
        holder.rangeCount.setBackgroundColor(labels.get(position).getColor());
        //holder.llBackground.setBackgroundColor(labels.get(position).getColor());

        //holder.title.setTypeface(Typeface.DEFAULT_BOLD);
        holder.rangeCount.setTypeface(Typeface.DEFAULT_BOLD);

        return item;

    }

    private class LabelHolder {
        TextView title;
        TextView rangeCount;
        LinearLayout llBackground;
    }

    public void setLabels(ArrayList<Label> labels){
        this.labels = labels;
        notifyDataSetChanged();
    }

    public void addLabel(Label l) {
        labels.add(l);
        notifyDataSetChanged();
    }

    public void removeLabel(Label l){
        labels.remove(l);
        notifyDataSetChanged();
    }


    @Override
    public long getItemId(int position){
        return position;
    }

    @Override
    public Label getItem(int position){
        return labels.get(position);
    }

    @Override
    public int getCount(){
        return labels.size();
    }


}

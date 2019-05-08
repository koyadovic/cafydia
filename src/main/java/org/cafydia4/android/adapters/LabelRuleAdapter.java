package org.cafydia4.android.adapters;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.cafydia4.android.R;
import org.cafydia4.android.chartobjects.DataCollectionLabelRule;

import java.util.ArrayList;

/**
 * Created by user on 9/12/14.
 */
public class LabelRuleAdapter extends BaseAdapter {
    private ArrayList<DataCollectionLabelRule> rules;
    private Activity hostActivity;

    public LabelRuleAdapter(Activity host, ArrayList<DataCollectionLabelRule> rules){
        super();
        hostActivity = host;
        this.rules = rules;
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

        holder.title.setText(rules.get(position).getLabel().getTitle());
        holder.title.setTextColor(Color.BLACK);

        holder.rangeCount.setText(rules.get(position).getLabel().getRangeCount().toString());
        holder.rangeCount.setBackgroundColor(rules.get(position).getLabel().getColor());

        holder.rangeCount.setTypeface(Typeface.DEFAULT_BOLD);

        return item;

    }

    private class LabelHolder {
        TextView title;
        TextView rangeCount;
        LinearLayout llBackground;
    }

    public void setRules(ArrayList<DataCollectionLabelRule> rules){
        this.rules = rules;
        notifyDataSetChanged();
    }

    public void addRule(DataCollectionLabelRule r) {
        rules.add(r);
        notifyDataSetChanged();
    }

    public void removeRule(DataCollectionLabelRule r){
        rules.remove(r);
        notifyDataSetChanged();
    }


    @Override
    public long getItemId(int position){
        return position;
    }

    @Override
    public DataCollectionLabelRule getItem(int position){
        return rules.get(position);
    }

    @Override
    public int getCount(){
        return rules.size();
    }


}

package org.cafydia4.android.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.cafydia4.android.R;
import org.cafydia4.android.chartobjects.LabelRange;

import java.util.ArrayList;

/**
 * Created by user on 16/02/15.
 */
public class LabelRangesAdapter extends BaseAdapter {
    private ArrayList<LabelRange> ranges;
    private Activity hostActivity;

    public LabelRangesAdapter(Activity host, ArrayList<LabelRange> ranges){
        hostActivity = host;
        this.ranges = ranges;
    }

    class LabelRangeHolder {
        TextView start;
        TextView end;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View item = convertView;
        LabelRangeHolder holder;

        if(item == null){
            LayoutInflater inflater = hostActivity.getLayoutInflater();
            item = inflater.inflate(R.layout.label_date_range_item, null);
            holder = new LabelRangeHolder();

            holder.start = (TextView) item.findViewById(R.id.tvStart);
            holder.end = (TextView) item.findViewById(R.id.tvEnd);


            item.setTag(holder);
        } else {
            holder = (LabelRangeHolder) item.getTag();
        }

        holder.start.setText(ranges.get(position).getStart().getUserDateString());
        holder.end.setText(ranges.get(position).getEnd().getUserDateString());

        return item;

    }

    public void setRanges(ArrayList<LabelRange> ranges){
        this.ranges = ranges;
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    @Override
    public LabelRange getItem(int position){
        return ranges.get(position);
    }

    @Override
    public int getCount(){
        return ranges.size();
    }

}

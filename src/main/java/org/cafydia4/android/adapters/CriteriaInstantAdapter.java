package org.cafydia4.android.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.cafydia4.android.R;
import org.cafydia4.android.chartobjects.DataCollectionCriteriaInstant;

import java.util.ArrayList;

/**
 * Created by user on 21/02/15.
 */
public class CriteriaInstantAdapter extends BaseAdapter {
    private ArrayList<DataCollectionCriteriaInstant> criteriaInstants;
    private Activity hostActivity;

    public CriteriaInstantAdapter (ArrayList<DataCollectionCriteriaInstant> instants, Activity activity) {
        criteriaInstants = instants;
        hostActivity = activity;
    }

    @Override
    public int getCount(){
        return criteriaInstants.size();
    }

    class Holder {
        TextView text;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View item = convertView;
        Holder holder;

        if(item == null) {
            LayoutInflater inflater = hostActivity.getLayoutInflater();
            item = inflater.inflate(R.layout.criteria_instant_listview_item, null);
            holder = new Holder();

            holder.text = (TextView) item.findViewById(R.id.tvCriteriaInstantText);

            item.setTag(holder);
        } else {
            holder = (Holder) item.getTag();
        }
        DataCollectionCriteriaInstant ci = criteriaInstants.get(position);

        holder.text.setText(ci.toString());

        return item;
    }


    @Override
    public long getItemId(int position){
        return position;
    }


    @Override
    public DataCollectionCriteriaInstant getItem(int position){
        return criteriaInstants.get(position);
    }

}

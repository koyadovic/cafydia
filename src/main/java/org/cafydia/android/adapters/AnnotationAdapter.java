package org.cafydia.android.adapters;

import android.app.Activity;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.cafydia.android.R;
import org.cafydia.android.core.Annotation;

import java.util.ArrayList;

/**
 * Created by user on 27/11/14.
 */
public class AnnotationAdapter extends BaseAdapter {
    private ArrayList<Annotation> annotations;
    private Activity hostActivity;

    public AnnotationAdapter(Activity host, ArrayList<Annotation> a){
        super();
        annotations = a;
        hostActivity = host;

        notifyDataSetChanged();
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View item = convertView;
        AnnotationHolder holder;

        if(item == null){
            LayoutInflater inflater = hostActivity.getLayoutInflater();
            item = inflater.inflate(R.layout.annotation_item, null);
            holder = new AnnotationHolder();

            holder.number = (TextView) item.findViewById(R.id.number);
            holder.date = (TextView) item.findViewById(R.id.date);
            holder.text = (TextView) item.findViewById(R.id.text);

            item.setTag(holder);
        } else {
            holder = (AnnotationHolder) item.getTag();
        }

        holder.number.setText(annotations.get(position).getNumber().toString());
        holder.number.setTypeface(Typeface.DEFAULT_BOLD);

        if(annotations.get(position).getNumber().equals(0)) {
            holder.number.setBackgroundColor(0x77777777);
            holder.number.setTextColor(0x00000000);
        } else {
            holder.number.setBackgroundColor(hostActivity.getResources().getColor(R.color.colorAnnotations));
            holder.number.setTextColor(0xFFFFFFFF);
        }

        holder.date.setText(annotations.get(position).getUserDateStringShort());
        holder.text.setText(annotations.get(position).getAnnotation());

        holder.date.setTypeface(Typeface.DEFAULT_BOLD);

        return item;
    }
    class AnnotationHolder {
        TextView number;
        TextView date;
        TextView text;
    }

    /**
     *
     * @param as Anotaciones del adaptador
     */
    public void setAnnotations(ArrayList<Annotation> as){
        annotations = as;
        notifyDataSetChanged();
    }

    public void deleteAnnotation(Annotation a) {
        annotations.remove(a);
        notifyDataSetChanged();
    }

    public void updateAnnotation(Annotation a) {
        boolean found = false;

        for(int n=0; n<getCount(); n++){
            if(getItem(n).getId().equals(a.getId())){
                annotations.set(n, a);
                found = true;
                break;
            }
        }

        if(!found) {
            annotations.add(a);
        }
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    @Override
    public Annotation getItem(int position){
        return annotations.get(position);
    }

    @Override
    public int getCount(){
        if(annotations == null)
            return 0;

        return annotations.size();
    }

    public ArrayList<Annotation> getAnnotations(){
        return annotations;
    }



}

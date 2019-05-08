package org.cafydia4.android.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.cafydia4.android.R;

/**
 * Created by user on 9/07/14.
 */
public class AdapterIconPlusText extends ArrayAdapter<String> {
    private Activity mContext;
    private String[] mStringResources;
    private Integer[] mImageId;

    public AdapterIconPlusText(Activity context,
                      String[] strResources, Integer[] imageId) {
        super(context, R.layout.custom_simple_list_item_with_icon, strResources);

        this.mContext = context;
        this.mImageId = imageId;
        this.mStringResources = strResources;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View item = convertView;

        if(item == null){
            LayoutInflater inflater = mContext.getLayoutInflater();
            item = inflater.inflate(R.layout.custom_simple_list_item_with_icon, null);
        }

        TextView txtTitle = (TextView) item.findViewById(R.id.txt);
        ImageView imageView = (ImageView) item.findViewById(R.id.img);

        txtTitle.setText(mStringResources[position]);
        imageView.setImageResource(mImageId[position]);

        return item;
    }
}

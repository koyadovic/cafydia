package org.cafydia4.android.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.cafydia4.android.R;
import org.cafydia4.android.core.Food;
import org.cafydia4.android.util.MyFoodArrayList;
import org.cafydia4.android.util.MyRound;
import org.cafydia4.android.util.UnitChanger;

/**
 * Created by user on 28/08/14.
 */
public class FoodSelectedAdapter extends BaseAdapter {
    private MyFoodArrayList mFoodSelected;
    private Fragment mFragment;
    private FragmentActivity mActivity;
    private UnitChanger mChange;

    public FoodSelectedAdapter (Fragment fragment){
        this.mFragment = fragment;
        this.mActivity = null;

        mChange = new UnitChanger(fragment.getActivity());

        mFoodSelected = new MyFoodArrayList();
    }
    public FoodSelectedAdapter (FragmentActivity activity){
        this.mActivity = activity;
        this.mFragment = null;

        mChange = new UnitChanger(activity);

        mFoodSelected = new MyFoodArrayList();
    }


    public View getView(int position, View convertView, ViewGroup parent) {
        View item = convertView;
        FoodHolder holder;

        if (item == null) {
            LayoutInflater inflater = mFragment != null ? mFragment.getActivity().getLayoutInflater() : mActivity.getLayoutInflater();

            item = inflater.inflate(R.layout.fragment_selected_food_food_listview_element, null);


            holder = new FoodHolder();
            holder.name = (TextView) item.findViewById(R.id.tvName);
            holder.percentage = (TextView) item.findViewById(R.id.tvCarbohydratePercentage);
            holder.weightOrUnits = (TextView) item.findViewById(R.id.tvWeightOrUnits);
            holder.weightOrUnitsTitle = (TextView) item.findViewById(R.id.tvWeightOrUnitsTitle);
            holder.carbohydratesWeight = (TextView) item.findViewById(R.id.tvCarbohydratesWeight);
            holder.circle = item.findViewById(R.id.circle);

            item.setTag(holder);
        } else {
            holder = (FoodHolder) item.getTag();
        }

        Food food = mFoodSelected.getFoodArrayList().get(position);

        holder.percentage.setText(MyRound.round(food.getCPercent()).toString() + "%");

        if(food.getCPercent() < 20f){
            holder.circle.setBackgroundResource(R.drawable.food_drawable_low_carbohydrate_percentage);
        }
        else if(food.getCPercent() < 50){
            holder.circle.setBackgroundResource(R.drawable.food_drawable_medium_carbohydrate_percentage);
        }
        else {
            holder.circle.setBackgroundResource(R.drawable.food_drawable_high_carbohydrate_percentage);
        }

        holder.name.setText(food.getName());
        Float carb = MyRound.round((float) (food.getCPercent() / 100.0) * food.getWeightInGrams()).floatValue();

        holder.carbohydratesWeight.setText(mChange.toUIFromInternalWeight(carb).toString() + mChange.getStringUnitForWeightShort().toLowerCase());

        if(food.getWeightPerUnitInGrams() == 0.0) {
            holder.weightOrUnitsTitle.setText(mFragment != null ? mFragment.getString(R.string.food_selected_fragment_food_element_weight) : mActivity.getString(R.string.food_selected_fragment_food_element_weight));
            float weight = mChange.toUIFromInternalWeight(food.getWeightInGrams());
            int decimals = mChange.getDecimalsForWeight();

            holder.weightOrUnits.setText(MyRound.round(weight, decimals).toString() + mChange.getStringUnitForWeightShort().toLowerCase());
        } else {
            holder.weightOrUnitsTitle.setText(mFragment != null ? mFragment.getString(R.string.food_selected_fragment_food_element_units) : mActivity.getString(R.string.food_selected_fragment_food_element_units));
            // we do not convert weight in grams and weight for units here, because
            // in the ui will be shown units
            Integer value = MyRound.roundToInteger(food.getWeightInGrams() / food.getWeightPerUnitInGrams());

            holder.weightOrUnits.setText(value.toString());


        }
        return item;
    }
    class FoodHolder {
        TextView name;
        TextView percentage;
        TextView weightOrUnits;
        TextView weightOrUnitsTitle;
        TextView carbohydratesWeight;
        View circle;
    }

    @Override
    public Food getItem(int position){
        return mFoodSelected.getFoodArrayList().get(position);
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    @Override
    public int getCount() {
        return mFoodSelected.getFoodArrayList().size();
    }


    public MyFoodArrayList getFoodSelected() {
        return mFoodSelected;
    }

    public void setFoodSelected(MyFoodArrayList mFoodSelected) {
        this.mFoodSelected = mFoodSelected;
        notifyDataSetChanged();
    }

    public void addFood(Food food){
        mFoodSelected.addFood(food);
        notifyDataSetChanged();
    }

    public void removeFood(Food food){
        mFoodSelected.removeFood(food);
        notifyDataSetChanged();
    }

}

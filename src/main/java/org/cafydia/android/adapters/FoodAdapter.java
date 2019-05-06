package org.cafydia.android.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import org.cafydia.android.R;
import org.cafydia.android.core.Food;
import org.cafydia.android.fragments.FoodFragment;
import org.cafydia.android.util.MyFoodArrayList;
import org.cafydia.android.util.MyRound;

import java.util.ArrayList;

/**
 * Created by user on 23/08/14.
 */
public class FoodAdapter extends BaseAdapter implements Filterable {
    private FoodFragment mHostFragment;

    private ArrayList<Food> mAllFood = null;
    private MyFoodArrayList mFoodShown;

    public FoodAdapter(FoodFragment hostFragment, ArrayList<Food> allFood){
        super();
        mHostFragment = hostFragment;

        mAllFood = allFood;

        mFoodShown = new MyFoodArrayList(mAllFood);

        notifyDataSetChanged();
    }

    public FoodAdapter(FoodFragment hostFragment){
        super();
        mHostFragment = hostFragment;
        mAllFood = new ArrayList<>();

        mFoodShown = new MyFoodArrayList(mAllFood);
        notifyDataSetChanged();
    }

    public void setAllFood(ArrayList<Food> allFood) {
        mAllFood = allFood;
        mFoodShown.setFoods(mAllFood);
        notifyDataSetChanged();
    }

    public boolean isFiltered(){
        return mFoodShown.getFoodArrayList().size() != mAllFood.size();
    }

    public void addOrUpdateFood(Food food){
        for (Food f : mAllFood){
            if(f.getId().equals(food.getId())){
                mAllFood.set(mAllFood.indexOf(f), food);
                mFoodShown.setFoods(mAllFood);

                notifyDataSetChanged();
                return;
            }
        }
        mAllFood.add(food);
        mFoodShown.sort();

        notifyDataSetChanged();
    }

    public void removeFood(Food food){
        for (Food f : mAllFood){
            if(f.getId().equals(food.getId())){
                mAllFood.remove(mAllFood.indexOf(f));
                mFoodShown.setFoods(mAllFood);

                notifyDataSetChanged();
                return;
            }
        }
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View item = convertView;
        FoodHolder holder;

        if (item == null) {
            LayoutInflater inflater = mHostFragment.getActivity().getLayoutInflater();
            item = inflater.inflate(R.layout.fragment_food_food_listview_element, null);
            holder = new FoodHolder();
            holder.name = (TextView) item.findViewById(R.id.tvName);
            holder.percentage = (TextView) item.findViewById(R.id.tvCarbohydratePercentage);
            holder.circle = item.findViewById(R.id.circle);
            item.setTag(holder);
        } else {
            holder = (FoodHolder) item.getTag();
        }

        holder.name.setText(mFoodShown.getFoodArrayList().get(position).getName());
        holder.percentage.setText(MyRound.round(mFoodShown.getFoodArrayList().get(position).getCPercent()).toString() + "%");

        if(mFoodShown.getFoodArrayList().get(position).getCPercent() < 20f){
            holder.circle.setBackgroundResource(R.drawable.food_drawable_low_carbohydrate_percentage);
        }
        else if(mFoodShown.getFoodArrayList().get(position).getCPercent() < 50){
            holder.circle.setBackgroundResource(R.drawable.food_drawable_medium_carbohydrate_percentage);
        }
        else {
            holder.circle.setBackgroundResource(R.drawable.food_drawable_high_carbohydrate_percentage);
        }

        return item;
    }
    class FoodHolder {
        TextView name;
        TextView percentage;
        View circle;
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    @Override
    public Food getItem(int position){
        return mFoodShown.getFoodArrayList().get(position);
    }

    @Override
    public int getCount(){
        return mFoodShown.getFoodArrayList().size();
    }

    @Override
    public Filter getFilter() {

        return new Filter() {

            private MyFoodArrayList localFilteredFood = new MyFoodArrayList();

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                String c = constraint.toString();

                if(c.equals("")){
                    mFoodShown.setFoods(mAllFood);
                    notifyDataSetChanged();
                }

                else if(mFoodShown.getFoodArrayList().size() != localFilteredFood.getFoodArrayList().size()) {
                    mFoodShown.setFoods(localFilteredFood.getFoodArrayList());
                    notifyDataSetChanged();
                }

            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {


                String c = constraint.toString().toLowerCase();

                if(c.equals("")){
                    return null;
                }

                c = c.replaceAll("[áäàâ]", "a");
                c = c.replaceAll("[éëèê]", "e");
                c = c.replaceAll("[íïìî]", "i");
                c = c.replaceAll("[óöòô]", "o");
                c = c.replaceAll("[úüùû]", "u");

                localFilteredFood = new MyFoodArrayList();

                for (Food food : mAllFood) {
                    String n = food.getName().toLowerCase();

                    n = n.replaceAll("[áäàâ]", "a");
                    n = n.replaceAll("[éëèê]", "e");
                    n = n.replaceAll("[íïìî]", "i");
                    n = n.replaceAll("[óöòô]", "o");
                    n = n.replaceAll("[úüùû]", "u");

                    if (n.contains(c))  {
                        localFilteredFood.addOrUpdateFood(food);
                    }
                }

                return null;
            }
        };

    }

}

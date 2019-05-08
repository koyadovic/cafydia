package org.cafydia.android.interfaces;

import org.cafydia.android.core.Food;

public interface OnFoodModifiedInterface {
    void onFoodModified(int action, int positionInTheViewPager, Food food);
}

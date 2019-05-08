package org.cafydia4.android.interfaces;

import org.cafydia4.android.core.Food;

public interface OnFoodModifiedInterface {
    void onFoodModified(int action, int positionInTheViewPager, Food food);
}

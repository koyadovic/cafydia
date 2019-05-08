package org.cafydia4.android.bedcasearch;

import android.app.backup.BackupManager;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.cafydia4.android.core.Food;
import org.cafydia4.android.util.C;
import org.cafydia4.android.util.MyFoodArrayList;

/**
 * Created by user on 11/05/15.
 */
public class BedcaLocalDatabase {
    private Context mContext;

    private final SQLOpenHelperBedca mHelperBedca;
    private SQLiteDatabase mDatabase;
    private BackupManager mBackupManager;

    private boolean openedWritable = false;
    private boolean openedReadable = false;

    public static final Object[] dbLock = new Object[0];

    public BedcaLocalDatabase(Context context){
        mHelperBedca = new SQLOpenHelperBedca(context);
        mBackupManager = new BackupManager(context);
        mContext = context;
    }

    public boolean openWritable(){
        if(!openedWritable) {
            openedWritable = true;
            openedReadable = false;
            mDatabase = mHelperBedca.getWritableDatabase();
            return true;
        } else {
            return false;
        }
    }

    public boolean openReadable(){
        if(openedWritable || openedReadable){
            return false;
        } else {
            mDatabase = mHelperBedca.getReadableDatabase();
            openedReadable = true;
            openedWritable = false;
            return true;
        }
    }

    public void close() {
        if(mDatabase != null) {
            mDatabase.close();
        }

        if(openedWritable) {
            mBackupManager.dataChanged();
            openedWritable = false;
        }
        else if(openedReadable) {
            openedReadable = false;
        }
    }

    public MyFoodArrayList searchFood(String s, String lang) {
        boolean firstOpened;

        MyFoodArrayList result = new MyFoodArrayList();
        Cursor cursor;

        String q = s;
        q = q.replaceAll("[aeiou]", "_");

        if(lang.equals("spa")) {
            firstOpened = openReadable();
            cursor = mDatabase.rawQuery("SELECT * FROM food WHERE name_es LIKE ?", new String[] {
                    "%" + q + "%"
            });
        } else {
            firstOpened = openReadable();
            cursor = mDatabase.rawQuery("SELECT * FROM food WHERE name_en LIKE ?", new String[] {
                    "%" + q + "%"
            });
        }

        if(cursor.moveToFirst()){
            do {
                int bedcaId = cursor.getInt(0);
                String nameEn = cursor.getString(1);
                String nameEs = cursor.getString(2);
                float carbohydrates = cursor.getFloat(3);

                Log.d("BedcaFood", "id: " + bedcaId);

                Food food = null;
                if(lang.equals("spa")) {
                    String n = nameEs.toLowerCase();


                    n = n.replaceAll("[áäàâ]", "a");
                    n = n.replaceAll("[éëèê]", "e");
                    n = n.replaceAll("[íïìî]", "i");
                    n = n.replaceAll("[óöòô]", "o");
                    n = n.replaceAll("[úüùû]", "u");

                    if(n.contains(s.toLowerCase()))
                        food = new Food(0, nameEs, C.FOOD_TYPE_SIMPLE, C.FOOD_FAVORITE_NO, carbohydrates);
                } else {
                    food = new Food(0, nameEn, C.FOOD_TYPE_SIMPLE, C.FOOD_FAVORITE_NO, carbohydrates);
                }

                if(food != null)
                    result.addFood(food);

            } while(cursor.moveToNext());

            cursor.close();
        }

        if(firstOpened)
            close();

        return result;
    }


    //
    // To insert, update and delete BedcaFood
    //

    public synchronized void updateOrInsertBedcaFood(BedcaFood food){
        if(food.getBedcaId().equals(0) || food.getNameEn().equals("") || food.getNameEs().equals("")) return;

        boolean firstOpened = openWritable();

        Cursor c = mDatabase.rawQuery("SELECT * FROM food WHERE bedca_id=?", new String[] {
                food.getBedcaId().toString()
        });

        if(c.moveToFirst()){

            // ya existía en la database
            mDatabase.execSQL("UPDATE food SET name_en=?, name_es=?, carbohydrates=? WHERE bedca_id=?", new String[] {
                    food.getNameEn(),
                    food.getNameEs(),
                    food.getCarbohydrates().toString(),
                    food.getBedcaId().toString()
            });

        } else {
            // no existía en la database

            mDatabase.execSQL("INSERT INTO food(bedca_id, name_en, name_es, carbohydrates) VALUES (?, ?, ?, ?);", new String[] {
                    food.getBedcaId().toString(),
                    food.getNameEn(),
                    food.getNameEs(),
                    food.getCarbohydrates().toString()
            });

        }

        c.close();

        if(firstOpened)
            close();
    }

    public synchronized void deleteBedcaFood(BedcaFood food){
        boolean firstOpened = openWritable();

        mDatabase.execSQL("DELETE FROM food WHERE bedca_id=?", new String[]{
                food.getBedcaId().toString()
        });

        if(firstOpened)
            close();

    }

    public synchronized void emptyBedcaFoodTable(){
        boolean firstOpened = openWritable();

        mDatabase.execSQL("DELETE FROM food");

        if(firstOpened)
            close();

    }

}

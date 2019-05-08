package org.cafydia4.android.configdatabase;

import android.app.backup.BackupManager;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.cafydia4.android.core.Food;
import org.cafydia4.android.core.Instant;
import org.cafydia4.android.recommendations.Corrective;
import org.cafydia4.android.recommendations.CorrectiveComplex;
import org.cafydia4.android.recommendations.CorrectiveSimple;
import org.cafydia4.android.recommendations.MetabolicRhythm;
import org.cafydia4.android.recommendations.MetabolicRhythmMaster;
import org.cafydia4.android.recommendations.MetabolicRhythmSlave;
import org.cafydia4.android.recommendations.ModificationStart;
import org.cafydia4.android.recommendations.ModificationStartDot;
import org.cafydia4.android.util.C;
import org.cafydia4.android.util.MyFoodArrayList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


/**
 * Created by usuario on 5/03/14.
 */
public class ConfigurationDatabase {
    private SQLOpenHelperConfiguration mHelperConfiguration;
    private SQLiteDatabase mDatabase;
    private BackupManager mBackupManager;
    private Context context;

    private boolean openedWritable = false;

    public static final Object[] dbLock = new Object[0];

    // methods for create, open and close the database
    public ConfigurationDatabase(Context context){
        this.context = context;
        mHelperConfiguration = new SQLOpenHelperConfiguration(context);
        mBackupManager = new BackupManager(context);
    }

    public void openWritable() {
        if(context != null) {
            openedWritable = true;
            mDatabase = mHelperConfiguration.getWritableDatabase();
        }
    }
    public void openReadable() {
        if(context != null) {
            mDatabase = mHelperConfiguration.getReadableDatabase();
        }
    }

    public void close(){
        mHelperConfiguration.close();
        if(openedWritable) {
            mBackupManager.dataChanged();
            openedWritable = false;
        }
    }


    // getter methods
    public MyFoodArrayList getFoodByFoodFragmentPosition(int type){
        openReadable();
        if(context == null) { return null; }

        MyFoodArrayList result = new MyFoodArrayList();


        Cursor cursor = null;

        switch (type){
            case C.FOOD_FRAGMENT_POSITION_FOOD:
                cursor = mDatabase.rawQuery("SELECT * FROM food WHERE type==? and favorite==?", new String[] {
                        Integer.toString(C.FOOD_TYPE_SIMPLE),
                        Integer.toString(C.FOOD_FAVORITE_NO)
                });
                break;
            case C.FOOD_FRAGMENT_POSITION_FAVORITE_FOOD:
                cursor = mDatabase.rawQuery("SELECT * FROM food WHERE type==? and favorite==?", new String[] {
                        Integer.toString(C.FOOD_TYPE_SIMPLE),
                        Integer.toString(C.FOOD_FAVORITE_YES)
                });
                break;
            case C.FOOD_FRAGMENT_POSITION_COMPLEX_FOOD:
                cursor = mDatabase.rawQuery("SELECT * FROM food WHERE type==?", new String[] {
                        Integer.toString(C.FOOD_TYPE_COMPLEX)
                });
                break;
        }

        if(cursor != null){
            if(cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(0);
                    String name = cursor.getString(1);
                    int ty = cursor.getInt(2);
                    int fav = cursor.getInt(3);
                    float carb = cursor.getFloat(4);
                    float unitWeight = cursor.getFloat(5);

                    result.addOrUpdateFood(new Food(id, name, ty, fav, carb, unitWeight));

                }while(cursor.moveToNext());
            }
            cursor.close();
        }
        close();

        return result;
    }



    public ArrayList<MetabolicRhythm> getMetabolicRhythmsSimple(){
        openReadable();
        if(context == null) { return null; }

        ArrayList<MetabolicRhythm> result = new ArrayList<MetabolicRhythm>();
        Cursor cursor = mDatabase.rawQuery("SELECT * FROM metabolic_rhythms", null);
        if(cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                String description = cursor.getString(2);
                int startMandatoryType = cursor.getInt(3);
                int state = cursor.getInt(4);
                long startDate = cursor.getLong(5);
                long endDate = cursor.getLong(6);

                result.add(new MetabolicRhythmSlave(id, name, description, startMandatoryType, state, new Instant(startDate), new Instant(endDate)));
            } while (cursor.moveToNext());
            cursor.close();
        }
        close();
        return result;
    }

    public ArrayList<MetabolicRhythmSlave> getPlanedSortedMetabolicRhythmsSimple(){
        openReadable();
        if(context == null) { return null; }

        ArrayList<MetabolicRhythmSlave> result = new ArrayList<MetabolicRhythmSlave>();
        Cursor cursor = mDatabase.rawQuery("SELECT * FROM metabolic_rhythms", null);
        if(cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                String description = cursor.getString(2);
                int startMandatoryType = cursor.getInt(3);
                int state = cursor.getInt(4);
                long startDate = cursor.getLong(5);
                long endDate = cursor.getLong(6);
                if(startDate == 0 || endDate == 0 || id == 1 || state == C.METABOLIC_RHYTHM_STATE_ENABLED) continue;

                result.add(new MetabolicRhythmSlave(id, name, description, startMandatoryType, state, new Instant(startDate), new Instant(endDate)));
            } while (cursor.moveToNext());
            cursor.close();
        }
        close();

        int s = result.size();
        int c;
        do {
            c = 0;
            for (int i = 0; i < s - 1; i++) {
                if(result.get(i).getStartDate().getDaysPassedFromInstant(result.get(i + 1).getStartDate()) > 0){
                    MetabolicRhythmSlave previous = result.get(i + 1);
                    MetabolicRhythmSlave later = result.get(i);

                    result.set(i, previous);
                    result.set(i + 1, later);
                } else {
                    c++;
                }
            }
        } while (c < s - 1);

        return result;
    }

    public MetabolicRhythm getMetabolicRhythmById(Integer id){
        openReadable();
        if(context == null) { return null; }

        MetabolicRhythm m = null;

        Cursor cursor = mDatabase.rawQuery("SELECT * FROM metabolic_rhythms WHERE _id=?",
                new String[] {
                        id.toString()
                });
        if(cursor.moveToFirst()) {
            String name = cursor.getString(1);
            String description = cursor.getString(2);
            int startMandatoryType = cursor.getInt(3);
            int state = cursor.getInt(4);
            long startDate = cursor.getLong(5);
            long endDate = cursor.getLong(6);

            switch(id){
                case 1:
                    m = new MetabolicRhythmMaster(id, name, description, startMandatoryType, state, startDate == 0 ? null : new Instant(startDate));
                    break;
                default:
                    m = new MetabolicRhythmSlave(id, name, description, startMandatoryType, state, startDate == 0 ? null : new Instant(startDate), endDate == 0 ? null : new Instant(endDate));
            }
            Cursor cursor2 = mDatabase.rawQuery("SELECT * FROM dots WHERE metabolic_rhythm_id=?",
                    new String[] {
                            m.getId().toString()
                    });
            if(cursor2.moveToFirst()){
                do {
                    int dotId = cursor2.getInt(0);
                    int type = cursor2.getInt(1);
                    float x = cursor2.getFloat(3);
                    float y = cursor2.getFloat(4);

                    m.addDot(new ModificationStartDot(dotId, type, m.getId() , x, y));

                }while(cursor2.moveToNext());
                cursor2.close();
            }

            cursor.close();
        }
        close();
        return m;
    }

    public MetabolicRhythm getMetabolicRhythmByIdSimple(Integer id){
        openReadable();
        if(context == null) { return null; }

        MetabolicRhythm m = null;

        Cursor cursor = mDatabase.rawQuery("SELECT * FROM metabolic_rhythms WHERE _id=?",
                new String[] {
                        id.toString()
                });
        if(cursor.moveToFirst()) {
            String name = cursor.getString(1);
            String description = cursor.getString(2);
            int startMandatoryType = cursor.getInt(3);
            int state = cursor.getInt(4);
            long startDate = cursor.getLong(5);
            long endDate = cursor.getLong(6);

            switch(id){
                case 1:
                    m = new MetabolicRhythmMaster(id, name, description, startMandatoryType, state, startDate == 0 ? null : new Instant(startDate));
                    break;
                default:
                    m = new MetabolicRhythmSlave(id, name, description, startMandatoryType, state, startDate == 0 ? null : new Instant(startDate), endDate == 0 ? null : new Instant(endDate));
            }
            cursor.close();
        }
        close();
        return m;
    }

    public ArrayList<Corrective> getCorrectivesSorted(Integer metabolicRhythmId){
        openReadable();
        if(context == null) { return null; }

        ArrayList<Corrective> result = new ArrayList<Corrective>();

        Cursor cursor = mDatabase.rawQuery("SELECT * FROM simple_correctives WHERE metabolic_rhythm_id=?",
                new String[] {
                        metabolicRhythmId.toString()
                });

        if(cursor.moveToFirst()){
            CorrectiveSimple c;
            do {
                int corId = cursor.getInt(0);
                String name = cursor.getString(1);
                String desc = cursor.getString(2);
                int type = cursor.getInt(3);
                int modificationType = cursor.getInt(5);
                float modification = cursor.getFloat(6);
                int visible = cursor.getInt(7);
                int triggers = cursor.getInt(8);

                c = new CorrectiveSimple(corId, name, desc, type, metabolicRhythmId, modificationType, modification, visible, triggers);
                result.add(c);

            }while(cursor.moveToNext());
            cursor.close();
        }

        cursor = mDatabase.rawQuery("SELECT * FROM complex_correctives WHERE metabolic_rhythm_id=?",
                new String[] {
                        metabolicRhythmId.toString()
                });

        if(cursor.moveToFirst()) {
            CorrectiveComplex c;
            do {
                int corId = cursor.getInt(0);
                String name = cursor.getString(1);
                String desc = cursor.getString(2);
                int type = cursor.getInt(3);
                int modificationType = cursor.getInt(5);
                float modificationBr = cursor.getFloat(6);
                float modificationLu = cursor.getFloat(7);
                float modificationDi = cursor.getFloat(8);
                int visible = cursor.getInt(9);
                int triggers = cursor.getInt(10);

                c = new CorrectiveComplex(corId, name, desc, type, metabolicRhythmId, modificationType, modificationBr, modificationLu, modificationDi, visible, triggers);
                result.add(c);

            } while (cursor.moveToNext());
            cursor.close();
        }

        close();

        // sorted by name
        Collections.sort(result, new CorrectiveComparator());

        return result;
    }


    private class CorrectiveComparator implements Comparator<Corrective> {
        public int compare(Corrective left, Corrective right) {
            return left.getName().compareTo(right.getName());
        }
    }


    public CorrectiveSimple getCorrectiveSimpleById(Integer id){
        openReadable();
        if(context == null) { return null; }

        CorrectiveSimple c = null;

        Cursor cursor = mDatabase.rawQuery("SELECT * FROM simple_correctives WHERE _id=?",
                new String[] {
                        id.toString()
                });
        if(cursor.moveToFirst()){
            int corId = cursor.getInt(0);
            String name = cursor.getString(1);
            String desc = cursor.getString(2);
            int type = cursor.getInt(3);
            int metabolicRhythmId = cursor.getInt(4);
            int modificationType = cursor.getInt(5);
            float modification = cursor.getFloat(6);
            int visible = cursor.getInt(7);
            int triggers = cursor.getInt(8);

            c = new CorrectiveSimple(corId, name, desc, type, metabolicRhythmId, modificationType, modification, visible, triggers);
            cursor.close();
        }
        close();

        return c;
    }

    public CorrectiveComplex getCorrectiveComplexById(Integer id){
        openReadable();
        if(context == null) { return null; }

        CorrectiveComplex c = null;

        Cursor cursor = mDatabase.rawQuery("SELECT * FROM complex_correctives WHERE _id=?",
                new String[] {
                        id.toString()
                });
        if(cursor.moveToFirst()){
            int corId = cursor.getInt(0);
            String name = cursor.getString(1);
            String desc = cursor.getString(2);
            int type = cursor.getInt(3);
            int metabolicRhythmId = cursor.getInt(4);
            int modificationType = cursor.getInt(5);
            float modificationBr = cursor.getFloat(6);
            float modificationLu = cursor.getFloat(7);
            float modificationDi = cursor.getFloat(8);
            int visible = cursor.getInt(9);
            int triggers = cursor.getInt(10);

            c = new CorrectiveComplex(corId, name, desc, type, metabolicRhythmId, modificationType, modificationBr, modificationLu, modificationDi, visible,triggers);

            cursor.close();
        }
        close();
        return c;
    }

    public ModificationStart getModificationStart(Integer metabolicRhythmId, Integer dotType){
        openReadable();
        if(context == null) { return null; }

        ModificationStart start = new ModificationStart();
        Cursor cursor = mDatabase.rawQuery("SELECT * FROM dots WHERE metabolic_rhythm_id=? AND type=?" , new String[] {
                metabolicRhythmId.toString(),
                dotType.toString()
        });
        if(cursor.moveToFirst()){
            do {
                int id = cursor.getInt(0);
                float x = cursor.getFloat(3);
                float y = cursor.getFloat(4);

                start.addDot(new ModificationStartDot(id, dotType,metabolicRhythmId, x, y));
            }while(cursor.moveToNext());
            cursor.close();
        }
        close();
        return start;
    }


    /******************************************************************************
     * Methods related to insert, update and delete Cafydia objects onto database *
     ******************************************************************************/

    // methods related to Food
    public int insertFood(Food food){
        openWritable();
        if(food.getId() != 0||context == null) { return 0; }

        mDatabase.execSQL("INSERT INTO food (name, type, favorite, c_percent, unit_weight) VALUES (?, ?, ?, ?, ?);",
                new String[]{
                        food.getName(),
                        food.getType().toString(),
                        food.getFavorite().toString(),
                        food.getCPercent().toString(),
                        food.getWeightPerUnitInGrams().toString()
                });
        close();

        openReadable();
        Cursor cursor = mDatabase.rawQuery("SELECT * FROM food ORDER BY _id DESC LIMIT 1", null);
        cursor.moveToFirst();
        Food f = buildFoodObject(cursor);
        close();

        return f.getId();
    }

    public void updateFood(Food food){
        openWritable();
        if (food.getId() == 0||context == null) { return; }
        mDatabase.execSQL("UPDATE food set name=?, type=?, favorite=?, c_percent=?, unit_weight=? WHERE _id=?",
                new String[]{
                        food.getName(),
                        food.getType().toString(),
                        food.getFavorite().toString(),
                        food.getCPercent().toString(),
                        food.getWeightPerUnitInGrams().toString(),
                        food.getId().toString()
                });
        close();
    }
    public void deleteFood(Food food){
        openWritable();
        if (food.getId() == 0||context == null) { return; }
        mDatabase.execSQL("DELETE FROM food WHERE _id=?",
                new String[]{
                        food.getId().toString()
                });

        close();
    }


    // MetabolicRhythms
    public void insertMetabolicRhythm(MetabolicRhythm metabolicRhythm){
        openWritable();
        if(metabolicRhythm.getId() != 0||context == null) { return; }
        switch(metabolicRhythm.getId()){
            case 1:
                mDatabase.execSQL("INSERT INTO metabolic_rhythms (name, description, start_mandatory_type, state, start_date) VALUES (?, ?, ?, ?, ?)",
                        new String[]{
                                metabolicRhythm.getName(),
                                metabolicRhythm.getDescription(),
                                metabolicRhythm.getStartingPreprandialType().toString(),
                                metabolicRhythm.getState().toString(),
                                metabolicRhythm.getStartDate() != null && metabolicRhythm.getStartDate().toDate() != null ? ((Long) metabolicRhythm.getStartDate().toDate().getTime()).toString() : "0"
                        }
                );
                break;

            default:
                mDatabase.execSQL("INSERT INTO metabolic_rhythms (name, description, start_mandatory_type, state, start_date, end_date) VALUES (?, ?, ?, ?, ?, ?)",
                        new String[]{
                                metabolicRhythm.getName(),
                                metabolicRhythm.getDescription(),
                                metabolicRhythm.getStartingPreprandialType().toString(),
                                metabolicRhythm.getState().toString(),
                                metabolicRhythm.getStartDate() != null && metabolicRhythm.getStartDate().toDate() != null ? ((Long) metabolicRhythm.getStartDate().toDate().getTime()).toString() : "0",
                                ((MetabolicRhythmSlave) metabolicRhythm).getEndDate() != null && ((MetabolicRhythmSlave) metabolicRhythm).getEndDate().toDate() != null ? ((Long) ((MetabolicRhythmSlave) metabolicRhythm).getEndDate().toDate().getTime()).toString() : "0"
                        }
                );
        }
        close();
    }
    public void updateMetabolicRhythm(MetabolicRhythm metabolicRhythm){
        openWritable();
        if(metabolicRhythm.getId() == 0||context == null) { return; }
        switch (metabolicRhythm.getId()){
            case 1:
                mDatabase.execSQL("UPDATE metabolic_rhythms SET name=?, description=?, start_mandatory_type=?, state=?, start_date=? WHERE _id=?",
                        new String[]{
                                metabolicRhythm.getName(),
                                metabolicRhythm.getDescription(),
                                metabolicRhythm.getStartingPreprandialType().toString(),
                                metabolicRhythm.getState().toString(),
                                metabolicRhythm.getStartDate() != null && metabolicRhythm.getStartDate().toDate() != null ? ((Long) metabolicRhythm.getStartDate().toDate().getTime()).toString() : "0",
                                metabolicRhythm.getId().toString()
                        }
                );
                break;
            default:
                mDatabase.execSQL("UPDATE metabolic_rhythms SET name=?, description=?, start_mandatory_type=?, state=?, start_date=?, end_date=? WHERE _id=?",
                        new String[]{
                                metabolicRhythm.getName(),
                                metabolicRhythm.getDescription(),
                                metabolicRhythm.getStartingPreprandialType().toString(),
                                metabolicRhythm.getState().toString(),
                                metabolicRhythm.getStartDate() != null && metabolicRhythm.getStartDate().toDate() != null ? ((Long) metabolicRhythm.getStartDate().toDate().getTime()).toString() : "0",
                                ((MetabolicRhythmSlave) metabolicRhythm).getEndDate() != null && ((MetabolicRhythmSlave) metabolicRhythm).getEndDate().toDate() != null ? ((Long) ((MetabolicRhythmSlave) metabolicRhythm).getEndDate().toDate().getTime()).toString() : "0",
                                metabolicRhythm.getId().toString()
                        }
                );

        }
        close();
    }

    public void deleteMetabolicRhythm(MetabolicRhythm metabolicRhythm){
        openWritable();
        if(metabolicRhythm.getId() == 0||context == null) { return; }

        mDatabase.execSQL("DELETE FROM metabolic_rhythms WHERE _id=?",
                new String[]{
                        metabolicRhythm.getId().toString()
                }
        );
        mDatabase.execSQL("DELETE FROM dots WHERE metabolic_rhythm_id=?",
                new String[]{
                        metabolicRhythm.getId().toString()
                });
        mDatabase.execSQL("DELETE FROM simple_correctives WHERE metabolic_rhythm_id=?",
                new String[]{
                        metabolicRhythm.getId().toString()
                });
        mDatabase.execSQL("DELETE FROM complex_correctives WHERE metabolic_rhythm_id=?",
                new String[]{
                        metabolicRhythm.getId().toString()
                });
        close();
    }

    // Dots
    private void deleteDotsInTheSameDayOfTheSameType(ModificationStartDot dot){
        openWritable();
        mDatabase.execSQL("DELETE FROM dots WHERE x=? AND type=? AND metabolic_rhythm_id=?",
                new String[]{
                        Float.toString(dot.getX()),
                        dot.getType().toString(),
                        dot.getMetabolicRhythmId().toString()
                }
        );
        close();
    }

    public void insertDot(ModificationStartDot dot){
        deleteDotsInTheSameDayOfTheSameType(dot);

        openWritable();
        if(dot.getId() != 0||context == null) { return; }

        mDatabase.execSQL("INSERT INTO dots (type, metabolic_rhythm_id, x, y) VALUES (?, ?, ?, ?)",
                new String[]{
                        dot.getType().toString(),
                        dot.getMetabolicRhythmId().toString(),
                        Float.toString(dot.getX()),
                        Float.toString(dot.getY())
                }
        );
        close();
    }
    public void updateDot(ModificationStartDot dot){
        openWritable();
        if(dot.getId() == 0||context == null) { return; }

        mDatabase.execSQL("UPDATE dots SET type=?, metabolic_rhythm_id=?, x=?, y=? WHERE _id=?",
                new String[]{
                        dot.getType().toString(),
                        dot.getMetabolicRhythmId().toString(),
                        Float.toString(dot.getX()),
                        Float.toString(dot.getY()),
                        dot.getId().toString()
                }
        );
        close();
    }
    public void deleteDot(ModificationStartDot dot){
        openWritable();
        if(dot.getId() == 0||context == null) { return; }

        mDatabase.execSQL("DELETE FROM dots WHERE _id=?",
                new String[]{
                        dot.getId().toString()
                }
        );
        close();
    }

    // CorrectiveSimple
    public void insertCorrectiveSimple(CorrectiveSimple correctiveSimple){
        openWritable();
        if(correctiveSimple.getId() != 0||context == null) { return; }

        mDatabase.execSQL("INSERT INTO simple_correctives (name, description, type, metabolic_rhythm_id, modification_type, modification, visible, triggers) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                new String[]{
                        correctiveSimple.getName(),
                        correctiveSimple.getDescription(),
                        correctiveSimple.getType().toString(),
                        correctiveSimple.getMetabolicRhythmId().toString(),
                        correctiveSimple.getModificationType().toString(),
                        correctiveSimple.getModification().toString(),
                        correctiveSimple.getVisible().toString(),
                        correctiveSimple.getTriggers().toString()
                }
        );
        close();
    }
    public void updateCorrectiveSimple(CorrectiveSimple correctiveSimple){
        openWritable();
        if(correctiveSimple.getId() == 0||context == null) { return; }

        mDatabase.execSQL("UPDATE simple_correctives SET name=?, description=?, type=?, metabolic_rhythm_id=?, modification_type=?, modification=?, visible=?, triggers=? WHERE _id=?",
                new String[]{
                        correctiveSimple.getName(),
                        correctiveSimple.getDescription(),
                        correctiveSimple.getType().toString(),
                        correctiveSimple.getMetabolicRhythmId().toString(),
                        correctiveSimple.getModificationType().toString(),
                        correctiveSimple.getModification().toString(),
                        correctiveSimple.getVisible().toString(),
                        correctiveSimple.getTriggers().toString(),
                        correctiveSimple.getId().toString()
                }
        );
        close();
    }
    public void deleteCorrectiveSimple(CorrectiveSimple correctiveSimple){
        openWritable();
        if(correctiveSimple.getId() == 0||context == null) { return; }

        mDatabase.execSQL("DELETE FROM simple_correctives WHERE _id=?",
                new String[]{
                        correctiveSimple.getId().toString()
                }
        );
        close();
    }

    // CorrectiveComplex
    public void insertCorrectiveComplex(CorrectiveComplex correctiveComplex){
        openWritable();
        if(correctiveComplex.getId() != 0||context == null) { return; }

        mDatabase.execSQL("INSERT INTO complex_correctives (name, description, type, metabolic_rhythm_id, modification_type, modification_breakfast, modification_lunch, modification_dinner, visible, triggers) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                new String[]{
                        correctiveComplex.getName(),
                        correctiveComplex.getDescription(),
                        correctiveComplex.getType().toString(),
                        correctiveComplex.getMetabolicRhythmId().toString(),
                        correctiveComplex.getModificationType().toString(),
                        correctiveComplex.getModificationBr().toString(),
                        correctiveComplex.getModificationLu().toString(),
                        correctiveComplex.getModificationDi().toString(),
                        correctiveComplex.getVisible().toString(),
                        correctiveComplex.getTriggers().toString()
                }
        );
        close();
    }
    public void updateCorrectiveComplex(CorrectiveComplex correctiveComplex){
        openWritable();
        if(correctiveComplex.getId() == 0||context == null) { return; }

        mDatabase.execSQL("UPDATE complex_correctives SET name=?, description=?, type=?, metabolic_rhythm_id=?, modification_type=?, modification_breakfast=?, modification_lunch=?, modification_dinner=?, visible=?, triggers=? WHERE _id=?",
                new String[]{
                        correctiveComplex.getName(),
                        correctiveComplex.getDescription(),
                        correctiveComplex.getType().toString(),
                        correctiveComplex.getMetabolicRhythmId().toString(),
                        correctiveComplex.getModificationType().toString(),
                        correctiveComplex.getModificationBr().toString(),
                        correctiveComplex.getModificationLu().toString(),
                        correctiveComplex.getModificationDi().toString(),
                        correctiveComplex.getVisible().toString(),
                        correctiveComplex.getTriggers().toString(),
                        correctiveComplex.getId().toString()
                });
        close();
    }
    public void deleteCorrectiveComplex(CorrectiveComplex correctiveComplex){
        openWritable();
        if(correctiveComplex.getId() == 0||context == null) { return; }

        mDatabase.execSQL("DELETE FROM complex_correctives WHERE _id=?",
                new String[]{
                        correctiveComplex.getId().toString()
                });
        close();

    }

    private Food buildFoodObject(Cursor cursor){
        int id = cursor.getInt(0);
        String name = cursor.getString(1);
        int ty = cursor.getInt(2);
        int fav = cursor.getInt(3);
        float carb = cursor.getFloat(4);
        float unitWeight = cursor.getFloat(5);

        return new Food(id, name, ty, fav, carb, unitWeight);
    }
}

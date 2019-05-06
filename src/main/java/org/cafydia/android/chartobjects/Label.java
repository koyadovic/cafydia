package org.cafydia.android.chartobjects;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.cafydia.android.R;
import org.cafydia.android.core.Instant;
import org.cafydia.android.datadatabase.DataDatabase;
import org.cafydia.android.dialogfragments.CafydiaAlertDialog;
import org.cafydia.android.util.MyToast;

import java.util.ArrayList;

/**
 * Created by user on 3/12/14.
 */
public class Label {
    private int id;
    private String title;
    private int color;
    private ArrayList<LabelRange> ranges;


    /////////////////////////////////////
    // Constructor
    /////////////////////////////////////
    public Label(int id, String title, int color){
        ranges = new ArrayList<>();
        this.id = id;
        this.title = title;
        this.color = color;
    }

    ///////////////////////////////////
    // For add a single range of dates
    ///////////////////////////////////

    public void addRange(LabelRange range){
        ranges.add(range);
    }

    public Integer getRangeCount(){
        return ranges.size();
    }

    public LabelRange getRangeAtIndex(int index){
        return ranges.get(index);
    }

    public ArrayList<LabelRange> getRanges() {
        return ranges;
    }

    public void setRanges(ArrayList<LabelRange> ranges) {
        this.ranges = ranges;
    }

    ////////////////////////////////////
    // Getters and setters
    ////////////////////////////////////
    public Integer getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void save(DataDatabase db){
        if(id == 0){
            db.insertLabel(this);
        } else {
            db.updateLabel(this);
        }

        for(LabelRange range : ranges){
            range.save(db);
        }
    }

    public void delete(DataDatabase db){
        // this call deletes also all the ranges which have the label_id equals to the object id being deleted
        db.deleteLabel(this);
    }

    public static void saveCafydiaAutomaticLabel(final Context c, final String labelTitle, final Instant start, final Instant end) {

        final OnLabelListener callback = c instanceof OnLabelListener ? (OnLabelListener) c : null;

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        boolean automaticLabels = sp.getBoolean("pref_automatic_labels", false);

        final DataDatabase db = new DataDatabase(c);

        if(automaticLabels) {
            Label l;
            int action = Integer.parseInt(sp.getString("pref_key_automatic_labels_default_action", "1"));
            switch (action) {
                case 0:
                    // no ask, add immediately
                    l = db.getLabelByTitle(labelTitle);
                    if(l == null) {
                        l = new Label(0, labelTitle, 0xFF000000);
                        l.save(db);

                        l = db.getLabelByTitle(labelTitle);
                    }
                    LabelRange range = new LabelRange(0, l.getId(), start, end);
                    l.addRange(range);
                    l.save(db);

                    new MyToast(c, c.getResources().getString(R.string.pref_automatic_labels_label_added_toast));

                    if(callback != null) {
                        callback.onLabelAdded(l);
                    }


                    break;
                case 1:
                    // dialog for query user to add or cancel the label
                    String text = "";
                    l = db.getLabelByTitle(labelTitle);
                    if(l == null) {
                        text = c.getString(R.string.dialog_automatic_label_message_new_label) + " " + labelTitle + ". ";
                    }
                    text += c.getString(R.string.dialog_automatic_label_message_new_range) + " " + start.getUserDateString() + " / " + end.getUserDateString();

                    CafydiaAlertDialog.Builder builder = new CafydiaAlertDialog.Builder(
                            c,
                            c.getResources().getColor(R.color.colorCafydiaDefault),
                            R.drawable.ic_action_warning,
                            R.string.dialog_automatic_label_title
                    );
                    builder.setMessage(c.getString(R.string.dialog_automatic_label_message) + " " + text);
                    builder.setPositiveButton(R.string.dialog_automatic_label_button_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Label label = db.getLabelByTitle(labelTitle);
                            if(label == null) {
                                label = new Label(0, labelTitle, 0xFF000000);
                                label.save(db);

                                label = db.getLabelByTitle(labelTitle);
                            }
                            LabelRange range = new LabelRange(0, label.getId(), start, end);
                            label.addRange(range);
                            label.save(db);

                            new MyToast(c, c.getResources().getString(R.string.pref_automatic_labels_label_added_toast));

                            if(callback != null) {
                                callback.onLabelAdded(label);
                            }

                        }
                    });
                    builder.setNegativeButton(R.string.dialog_automatic_label_button_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(callback != null) {
                                callback.onLabelAdded(null);
                            }
                        }
                    });
                    builder.show();

                    break;
            }

        }

    }

    private LabelRange getRangeById(int id){
        for(LabelRange range : getRanges()){
            if(range.getId() == id){
                return range;
            }
        }
        return null;
    }


    // todo hay que ver si esta mierda funciona
    @Override
    public boolean equals(Object o){
        if(!(o instanceof Label))
            return false;

        Label l = (Label) o;

        boolean result = l.getId().equals(getId());
        result = result && l.getTitle().equals(getTitle());
        result = result && l.getColor().equals(getColor());
        result = result && l.getRangeCount().equals(getRangeCount());

        if(!result)
            return false;

        for(int n=0; n<getRangeCount(); n++){

            LabelRange r1 = l.getRangeAtIndex(n);
            LabelRange r2 = getRangeById(r1.getId());

            if(r2 == null)
                return false;

            result = result && r1.getId().equals(r2.getId()) &&
                    r1.getDaysPassedBetweenStartAndEnd().equals(r2.getDaysPassedBetweenStartAndEnd()) &&
                    r1.getStartInTheMorning().toDate().getTime() == r2.getStartInTheMorning().toDate().getTime();

            if(!result)
                return false;

        }
        return true;
    }

    public static interface OnLabelListener {
        public void onLabelAdded(Label l);
    }
}

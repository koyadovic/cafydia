package org.cafydia4.android.core;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.cafydia4.android.R;
import org.cafydia4.android.datadatabase.DataDatabase;
import org.cafydia4.android.dialogfragments.CafydiaAlertDialog;
import org.cafydia4.android.util.C;
import org.cafydia4.android.util.MyToast;

/**
 * Created by user on 26/11/14.
 */
public class Annotation extends Instant {
    private Integer mNumber;
    private Integer mId;
    private Integer mCreatedBy;
    private Integer mOrderNumberScope;
    private String mAnnotation;

    /*
     * Constructors
     */
    public Annotation(int createdBy, int scope, String annotation){
        super();
        mId = 0;
        mOrderNumberScope = scope;
        mAnnotation = annotation;
        mCreatedBy = createdBy;
    }

    public Annotation(String dateString, int createdBy, int scope, String annotation){
        super(dateString);
        mId = 0;
        mOrderNumberScope = scope;
        mAnnotation = annotation;
        mCreatedBy = createdBy;
    }

    public Annotation(int id, String dateString, int createdBy, int scope, String annotation, int number){
        super(dateString);

        mId = id;
        mOrderNumberScope = scope;
        mAnnotation = annotation;
        mNumber = number;
        mCreatedBy = createdBy;
    }
    public Annotation(int id, String dateString, int createdBy, int scope, String annotation){
        super(dateString);

        mId = id;
        mOrderNumberScope = scope;
        mAnnotation = annotation;
        mCreatedBy = createdBy;
    }

    /*
     * Getters
     */
    public Integer getId() {
        return mId;
    }

    public Integer getCreatedBy() {
        return mCreatedBy;
    }

    public Integer getOrderNumberScope() {
        return mOrderNumberScope;
    }

    public void setOrderNumberScope(Integer mOrderNumberScope) {
        this.mOrderNumberScope = mOrderNumberScope;
    }

    public String getAnnotation() {
        return mAnnotation;
    }

    public Integer getNumber() {
        return mNumber;
    }
    public void setNumber(int n) {
        mNumber = n;
    }

    /*
     * To save and delete annotation to database
     */
    public void save(DataDatabase db){
        if(mId.equals(0)){
            db.insertAnnotation(this);
        } else {
            db.updateAnnotation(this);
        }
    }
    public void delete(DataDatabase db){
        db.deleteAnnotation(this);
    }

    public static void saveCafydiaAutomaticAnnotation(final Context c, final String text){

        final OnAnnotationListener callBack = c instanceof OnAnnotationListener ? (OnAnnotationListener) c : null;

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        boolean automaticAnnotations = sp.getBoolean("pref_automatic_annotations", false);

        if(automaticAnnotations){
            int action = Integer.parseInt(sp.getString("pref_key_automatic_annotations_default_action", "1"));
            switch (action){
                // no ask, add immediately
                case 0:
                    Annotation a = new Annotation(C.ANNOTATION_CREATED_BY_CAFYDIA, C.ANNOTATION_SCOPE_GLOBAL, text);
                    DataDatabase db = new DataDatabase(c);
                    a.save(db);

                    new MyToast(c, c.getResources().getString(R.string.pref_automatic_annotations_annotation_added_toast));

                    if(callBack != null) {
                        callBack.onAnnotationAdded(a);
                    }

                    break;

                // dialog for query user to add or cancel the annotation
                case 1:
                    CafydiaAlertDialog.Builder builder = new CafydiaAlertDialog.Builder(
                            c,
                            c.getResources().getColor(R.color.colorCafydiaDefault),
                            R.drawable.ic_action_warning,
                            R.string.dialog_automatic_annotation_title
                    );
                    builder.setMessage(c.getResources().getString(R.string.dialog_automatic_annotation_message) + "\n\n" + text);
                    builder.setPositiveButton(c.getResources().getString(R.string.dialog_automatic_annotation_button_ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Annotation a = new Annotation(C.ANNOTATION_CREATED_BY_CAFYDIA, C.ANNOTATION_SCOPE_GLOBAL, text);
                            DataDatabase db = new DataDatabase(c);
                            a.save(db);

                            new MyToast(c, c.getResources().getString(R.string.pref_automatic_annotations_annotation_added_toast));

                            if(callBack != null) {
                                callBack.onAnnotationAdded(a);
                            }
                        }
                    });
                    builder.setNegativeButton(c.getResources().getString(R.string.dialog_automatic_annotation_button_cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(callBack != null) {
                                callBack.onAnnotationAdded(null);
                            }
                        }
                    });

                    builder.show();

                    break;

                default:
                    if(callBack != null) {
                        callBack.onAnnotationAdded(null);
                    }
            }
        }
    }

    public boolean equals(Annotation a){
        return mId.equals(a.getId()) && mAnnotation.equals(a.getAnnotation()) && mCreatedBy.equals(a.getCreatedBy())
                && mNumber.equals(a.getNumber()) && mOrderNumberScope.equals(a.getOrderNumberScope()) &&
                getInternalDateTimeString().equals(a.getInternalDateTimeString());
    }

    public interface OnAnnotationListener {
        void onAnnotationAdded(Annotation a);
    }
}

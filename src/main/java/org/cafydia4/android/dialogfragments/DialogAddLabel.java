package org.cafydia4.android.dialogfragments;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;

import org.cafydia4.android.R;
import org.cafydia4.android.adapters.LabelRangesAdapter;
import org.cafydia4.android.chartobjects.Label;
import org.cafydia4.android.chartobjects.LabelRange;
import org.cafydia4.android.core.Instant;
import org.cafydia4.android.datadatabase.DataDatabase;
import org.cafydia4.android.genericdialogfragments.DialogConfirmation;
import org.cafydia4.android.util.MyToast;
import org.cafydia4.android.util.ViewUtil;

import java.util.ArrayList;

import yuku.ambilwarna.AmbilWarnaDialog;

/**
 * Created by user on 15/01/15.
 */
public class DialogAddLabel extends DialogFragment implements
        DialogAddLabelRange.DialogAddLabelRangeInterface,
        DialogConfirmation.OnConfirmListener {

    private Integer mId;
    private String mName;
    private Integer mColor;

    private EditText etName;
    private ImageView ivColor;
    private ImageButton ibAdd;
    private ListView lvDateRanges;

    private Label originalLabel;

    private boolean mNewLabel;

    private OnAddLabelListener mCallBack;

    DataDatabase mDb;

    public interface OnAddLabelListener {
        void onLabelAddedOrEdited(Label old, Label l);
    }

    private View.OnClickListener showSelectColorDialog = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ViewUtil.hideKeyboard(getActivity(), etName);
            AmbilWarnaDialog d = new AmbilWarnaDialog(getActivity(), mColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
                @Override
                public void onCancel(AmbilWarnaDialog dialog) {

                }

                @Override
                public void onOk(AmbilWarnaDialog dialog, int color) {
                    ivColor.setImageDrawable(new ColorDrawable(color));
                    mColor = color;
                }
            });

            d.show();
        }
    };

    // listener to onClick events for add new date ranges
    private View.OnClickListener ibAddListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(mNewLabel){
                new MyToast(getActivity(), R.string.dialog_add_label_need_to_be_saved);
            } else {
                addDateRange();
            }
        }
    };

    private void addDateRange(){
        LabelRange range = new LabelRange(0, mId, new Instant(), new Instant());
        DialogAddLabelRange.newInstance(range, DialogAddLabel.this).show(getFragmentManager(), "add_range");
    }

    public void onLabelRangeAdded(LabelRange range){
        DataDatabase db = new DataDatabase(getActivity());
        originalLabel = db.getLabelById(mId);

        ((LabelRangesAdapter) lvDateRanges.getAdapter()).setRanges(originalLabel.getRanges());
    }



    public static DialogAddLabel newInstance(){
        DialogAddLabel fragment = new DialogAddLabel();
        Bundle args = new Bundle();
        args.putInt("id", 0);
        args.putString("name", "");
        args.putInt("color", 0);
        args.putBoolean("new_label", true);

        fragment.setArguments(args);

        return fragment;
    }

    public static DialogAddLabel newInstance(int id, String name, int color){
        DialogAddLabel fragment = new DialogAddLabel();
        Bundle args = new Bundle();

        args.putInt("id", id);
        args.putString("name", name);
        args.putInt("color", color);

        args.putBoolean("new_label", false);

        fragment.setArguments(args);

        return fragment;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////// Contextual menu /////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.label_date_range_contextual_menu, menu);

        MenuItem.OnMenuItemClickListener listener = new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                onContextItemSelected(item);
                return true;
            }
        };

        for (int i = 0, n = menu.size(); i < n; i++)
            menu.getItem(i).setOnMenuItemClickListener(listener);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        LabelRange range = (LabelRange) lvDateRanges.getAdapter().getItem(info.position);

        switch (item.getItemId()){
            case R.id.edit_label_range:
                DialogAddLabelRange.newInstance(range, DialogAddLabel.this).show(getFragmentManager(), "edit_date_range");
                return true;

            case R.id.delete_label_range:
                DialogConfirmation.newInstance(
                        "delete_label_range",
                        DialogAddLabel.this,
                        R.string.dialog_confirmation_delete_date_range_title,
                        R.string.dialog_confirmation_delete_date_range_message,
                        range
                ).show(getActivity().getFragmentManager(), null);
                return true;
        }

        return super.onContextItemSelected(item);
    }

    /*
     * METHODS TO MANAGE LIFECYCLE
     */

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        mDb = new DataDatabase(getActivity());

        mCallBack = (OnAddLabelListener) getActivity();

        CafydiaAlertDialog.Builder builder = new CafydiaAlertDialog.Builder(
                getActivity(),
                getResources().getColor(R.color.colorCafydiaDefault),
                R.drawable.ic_action_labels,
                R.string.dialog_add_label_title
        );

        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_add_label, null);

        etName = (EditText) view.findViewById(R.id.etName);
        ivColor = (ImageView) view.findViewById(R.id.ivColor);
        ibAdd = (ImageButton) view.findViewById(R.id.ibAdd);
        lvDateRanges = (ListView) view.findViewById(R.id.lvDateRanges);

        registerForContextMenu(lvDateRanges);

        // to add new ranges
        ibAdd.setOnClickListener(ibAddListener);

        Bundle args = getArguments();

        // get name and color from arguments
        mId = args.getInt("id");
        mName = args.getString("name");
        mColor = args.getInt("color");
        mNewLabel = args.getBoolean("new_label");

        if(!mNewLabel) {
            originalLabel = mDb.getLabelById(mId);

            LabelRangesAdapter adapter = new LabelRangesAdapter(getActivity(), originalLabel.getRanges());
            lvDateRanges.setAdapter(adapter);

            lvDateRanges.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    LabelRange range = (LabelRange) lvDateRanges.getAdapter().getItem(position);
                    DialogAddLabelRange.newInstance(range, DialogAddLabel.this).show(getFragmentManager(), "edit_date_range");
                }
            });

        } else {

            originalLabel = new Label(mId, mName, mColor);
        }

        // set in the ui the information passed from arguments
        etName.setText(mName);
        ivColor.setImageDrawable(new ColorDrawable(mColor));

        ivColor.setOnClickListener(showSelectColorDialog);

        builder.setView(view);

        // build the dialog
        builder.setPositiveButton(R.string.dialog_add_label_button_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ViewUtil.hideKeyboard(getActivity(), etName);

                if(!etName.getText().toString().equals("")) {
                    mName = etName.getText().toString();

                    Label l = new Label(mId, mName, mColor);
                    l.save(mDb);

                    if (mCallBack != null) {
                        mCallBack.onLabelAddedOrEdited(originalLabel, l);
                    }
                }
            }
        });
        builder.setNegativeButton(R.string.dialog_add_label_button_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ViewUtil.hideKeyboard(getActivity(), etName);

                if (mCallBack != null) {
                    mCallBack.onLabelAddedOrEdited(null, null);
                }

            }
        });

        return builder.create();

    }

    public void onConfirmPerformed(String tag, boolean confirmation, Object o){
        if (confirmation && tag.equals("delete_label_range")) {
            LabelRange range = (LabelRange) o;

            range.delete(mDb);

            ArrayList<LabelRange> ranges = mDb.getLabelRangesByLabelId(originalLabel.getId());
            originalLabel.setRanges(ranges);

            ((LabelRangesAdapter) lvDateRanges.getAdapter()).setRanges(ranges);

        }
    }
}

package org.cafydia.android.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.cafydia.android.R;
import org.cafydia.android.configdatabase.ConfigurationDatabase;
import org.cafydia.android.dialogfragments.CafydiaAlertDialog;
import org.cafydia.android.recommendations.Corrective;
import org.cafydia.android.recommendations.CorrectiveSimple;
import org.cafydia.android.util.MyToast;
import org.cafydia.android.util.ViewUtil;

import java.util.ArrayList;

/**
 * Created by user on 27/09/14.
 */
public class MetabolicCorrectivesList extends Fragment {

    private ListView lvCorrectives;
    private ConfigurationDatabase confDatabase;
    private int metabolicRhythmId;
    private CorrectivesListListener mCallBack;
    private FrameLayout flNoCorrectives;
    CorrectivesAdapter mAdapter;

    public static MetabolicCorrectivesList newInstance(int metabolicRhythmId){
        MetabolicCorrectivesList f = new MetabolicCorrectivesList();
        Bundle b = new Bundle();
        b.putInt("metabolic_rhythm_id", metabolicRhythmId);
        f.setArguments(b);

        return f;

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mCallBack = (CorrectivesListListener) activity;


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_metabolic_correctives_list, container, false);
        lvCorrectives = (ListView) layout.findViewById(R.id.lvCorrectives);
        flNoCorrectives = (FrameLayout) layout.findViewById(R.id.flNoCorrectives);

        // instantiate configuration database to get correctives
        confDatabase = new ConfigurationDatabase(getActivity());

        // instantiate the adapter for the listview
        mAdapter = new CorrectivesAdapter();

        metabolicRhythmId = getArguments().getInt("metabolic_rhythm_id");
        mAdapter.setCorrectives(confDatabase.getCorrectivesSorted(metabolicRhythmId));
        lvCorrectives.setAdapter(mAdapter);

        lvCorrectives.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(mCallBack != null){
                    mCallBack.onCorrectiveSelected((Corrective)lvCorrectives.getAdapter().getItem(position));
                }
            }
        });

        setHasOptionsMenu(true);
        registerForContextMenu(lvCorrectives);

        return layout;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_new_corrective:
                CafydiaAlertDialog.Builder builder = new CafydiaAlertDialog.Builder(
                        getActivity(),
                        getResources().getColor(R.color.colorCafydiaDefault),
                        null,
                        R.string.metabolic_correctives_details_new_title
                );

                LayoutInflater inflater = getActivity().getLayoutInflater();
                View view = inflater.inflate(R.layout.dialog_add_corrective, null);
                final EditText etName = (EditText) view.findViewById(R.id.etName);
                ViewUtil.showKeyboard(getActivity(), etName);

                builder.setView(view);
                builder.setPositiveButton(getString(R.string.metabolic_correctives_details_new_button_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ViewUtil.hideKeyboard(getActivity(), etName);
                        String name = etName.getText().toString();
                        CorrectiveSimple c = new CorrectiveSimple(name, metabolicRhythmId);
                        ConfigurationDatabase db = new ConfigurationDatabase(getActivity());
                        c.save(db);

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if(mAdapter != null) {
                                    mAdapter.setCorrectives(confDatabase.getCorrectivesSorted(metabolicRhythmId));
                                    mAdapter.notifyDataSetChanged();
                                }
                            }
                        }, 500);
                    }
                });

                builder.setNegativeButton(getString(R.string.metabolic_correctives_details_new_button_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ViewUtil.hideKeyboard(getActivity(), etName);
                    }
                });

                builder.show();

                return true;


            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu items for use in the action bar
        inflater.inflate(R.menu.fragment_correctives_list_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    /*
     * Contextual menú for the listview
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.fragment_contextual_metabolic_correctives_list_menu, menu);
    }
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if (info == null) {
            return super.onContextItemSelected(item);
        }

        final Corrective c = (Corrective) lvCorrectives.getAdapter().getItem(info.position);

        switch(item.getItemId()){
            case R.id.edit_corrective:
                mCallBack.onCorrectiveSelected(c);
                return true;
            case R.id.delete_corrective:
                ConfigurationDatabase db = new ConfigurationDatabase(getActivity());
                ((CorrectivesAdapter)lvCorrectives.getAdapter()).deleteCorrective(c);
                c.delete(db);
                new MyToast(getActivity(), getString(R.string.metabolic_correctives_details_corrective_deleted_1) + " " + c.getName() + " " + getString(R.string.metabolic_correctives_details_corrective_deleted_2));
                mCallBack.onCorrectiveDeleted(c);
                return true;
            default:
                return super.onContextItemSelected(item);

        }
    }

    /*
     * The adapter
     */
    private class CorrectivesAdapter extends BaseAdapter {
        private ArrayList<Corrective> correctives;

        public CorrectivesAdapter(){
            if(correctives == null || correctives.size() > 0){
                correctives = new ArrayList<>();
            }
            notifyDataSetChanged();
        }
        public void setCorrectives(ArrayList<Corrective> correctives){
            this.correctives = correctives;
            notifyDataSetChanged();
        }
        public void deleteCorrective(Corrective c){
            correctives.remove(c);
            notifyDataSetChanged();
        }

        public ArrayList<Corrective> getCorrectives() {
            return correctives;
        }

        public void remove(Corrective c){
            correctives.remove(c);
            notifyDataSetChanged();
        }

        @Override
        public void notifyDataSetChanged(){
            super.notifyDataSetChanged();

            flNoCorrectives.setVisibility(getCount() > 0 ? View.GONE : View.VISIBLE);
            lvCorrectives.setVisibility(getCount() > 0 ? View.VISIBLE : View.GONE);
        }

        @Override
        public long getItemId(int position){
            return position;
        }

        public Corrective getItem(int item){
            return correctives.get(item);
        }

        public int getCount() {
            return correctives.size();
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            View item = convertView;
            CorrectivesHolder holder;

            if (item == null){
                LayoutInflater inflater = getActivity().getLayoutInflater();
                item = inflater.inflate(R.layout.fragment_metabolic_correctives_list_element, null);
                holder = new CorrectivesHolder();
                holder.name = (TextView) item.findViewById(R.id.tvName);
                //holder.description = (ImageButton) item.findViewById(R.id.ibDescription);
                //holder.type = (ImageView) item.findViewById(R.id.ivType);
                //holder.visible = (ImageView) item.findViewById(R.id.ivVisible);
                //holder.planned = (ImageView) item.findViewById(R.id.ivPlanned);

                item.setTag(holder);
            } else {
                holder = (CorrectivesHolder) item.getTag();
            }

            holder.name.setText(correctives.get(position).getName());
            /*
             * todo Aquí tocará añadir los putos iconos.
             */

            return item;
        }

        class CorrectivesHolder {
            TextView name;
            ImageButton description;
            ImageView type;
            ImageView visible;
            ImageView planned;
        }

    }

    public void reloadCorrectives(){
        if(lvCorrectives != null) {
            CorrectivesAdapter adapter = (CorrectivesAdapter) lvCorrectives.getAdapter();
            if(adapter != null && confDatabase != null){
                adapter.setCorrectives(confDatabase.getCorrectivesSorted(metabolicRhythmId));
            }
        }

    }

    public interface CorrectivesListListener {
        void onCorrectiveSelected(Corrective c);
        void onCorrectiveDeleted(Corrective c);
    }

}

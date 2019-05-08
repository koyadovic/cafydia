package org.cafydia4.android.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import org.cafydia4.android.R;
import org.cafydia4.android.configdatabase.ConfigurationDatabase;
import org.cafydia4.android.genericdialogfragments.DialogConfirmation;
import org.cafydia4.android.recommendations.MetabolicFramework;
import org.cafydia4.android.recommendations.MetabolicRhythm;
import org.cafydia4.android.recommendations.MetabolicRhythmMaster;
import org.cafydia4.android.recommendations.MetabolicRhythmSlave;
import org.cafydia4.android.util.C;
import org.cafydia4.android.util.ViewUtil;

import java.util.ArrayList;

/**
 * Created by user on 21/09/14.
 */
public class MetabolicList extends Fragment implements DialogConfirmation.OnConfirmListener {
    private MetabolicListListener mCallBack = null;
    private ListView mLvMetabolicRhythms;
    private ConfigurationDatabase confDb;
    private Switch sMaster;
    private LinearLayout lMaster;

    TextView tvMasterName;

    private static MetabolicFramework framework;

    public static Fragment newInstance(MetabolicFramework f){
        framework = f;

        MetabolicList fragment = new MetabolicList();

        return fragment;

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mCallBack = (MetabolicListListener) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View layout = inflater.inflate(R.layout.fragment_metabolic_list, container, false);

        // the listview
        mLvMetabolicRhythms = (ListView) layout.findViewById(R.id.lvMetabolicRhythms);
        MetabolicRhythmAdapter adapter = new MetabolicRhythmAdapter();

        tvMasterName = (TextView) layout.findViewById(R.id.tvMasterName);

        sMaster = (Switch) layout.findViewById(R.id.sMaster);



        mLvMetabolicRhythms.setAdapter(adapter);

        // listener for the listview
        mLvMetabolicRhythms.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mCallBack != null) {
                    mCallBack.onRhythmSelected(((MetabolicRhythmSlave) mLvMetabolicRhythms.getAdapter().getItem(position)).getId());
                }
            }
        });


        // the master metabolic Rhythm layout
        lMaster = (LinearLayout) layout.findViewById(R.id.lMasterMetabolicRhythm);
        lMaster.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallBack.onRhythmSelected(1);
            }
        });
        lMaster.setVisibility(View.INVISIBLE);
        mLvMetabolicRhythms.setVisibility(View.INVISIBLE);

        confDb = new ConfigurationDatabase(getActivity());

        setHasOptionsMenu(true);

        registerForContextMenu(mLvMetabolicRhythms);

        return layout;
    }

    public void notifyStateChange(){
        // load metabolic rhythms
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                MetabolicRhythmMaster master = (MetabolicRhythmMaster) confDb.getMetabolicRhythmByIdSimple(1);
                sMaster.setChecked(master.getState().equals(C.METABOLIC_RHYTHM_STATE_ENABLED));
                new LoadSlaveMetabolicRhythms().execute();
            }
        }, 300);
    }


    @Override
    public void onResume(){
        super.onResume();

        // load metabolic rhythms
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                MetabolicRhythmMaster master = (MetabolicRhythmMaster) confDb.getMetabolicRhythmByIdSimple(1);
                if (getActivity() != null) {
                    TextView masterName = (TextView) getActivity().findViewById(R.id.tvMasterName);
                    if (masterName != null) {
                        masterName.setText(master.getName());

                        sMaster.setChecked(master.getState().equals(C.METABOLIC_RHYTHM_STATE_ENABLED));
                    }
                }

                new LoadSlaveMetabolicRhythms().execute();

                ViewUtil.makeViewVisibleAnimatedly(mLvMetabolicRhythms);
                ViewUtil.makeViewVisibleAnimatedly(lMaster);
            }
        }, 300);

    }


    /*
     * Contextual menú for the listview
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.fragment_contextual_metabolic_list_menu, menu);
    }

    public void onConfirmPerformed(String tag, boolean confirmation, Object object){
        if(tag.equals("delete_metabolic_rhythm") && confirmation) {
            MetabolicRhythm m = (MetabolicRhythm) object;

            if(confDb != null){
                if(m.getState().equals(C.METABOLIC_RHYTHM_STATE_ENABLED)){
                    framework.forceDisconnectMetabolicRhythmById(m.getId());
                }
                m.delete(confDb);
                MetabolicRhythmAdapter adapter = (MetabolicRhythmAdapter) mLvMetabolicRhythms.getAdapter();
                adapter.remove((MetabolicRhythmSlave) m);
                mCallBack.onRhythmDeleted(m.getId());
            }

        }
    }
    @Override
    public boolean onContextItemSelected(MenuItem item){
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if(info == null){
            return super.onContextItemSelected(item);
        }

        final MetabolicRhythm m = (MetabolicRhythm) mLvMetabolicRhythms.getAdapter().getItem(info.position);

        switch(item.getItemId()){
            case R.id.edit_rhythm:
                mCallBack.onRhythmSelected(m.getId());
                return true;
            case R.id.delete_rhythm:
                DialogConfirmation
                        .newInstance("delete_metabolic_rhythm", this, R.string.metabolic_list_dialog_delete_title, R.string.metabolic_list_dialog_delete_message, m)
                        .show(getFragmentManager(), null);

                return true;
            default:
                return super.onContextItemSelected(item);

        }
    }

    private class LoadSlaveMetabolicRhythms extends AsyncTask<Integer, Integer, Boolean> {
        private ArrayList<MetabolicRhythmSlave> metabolicRhythms;
        protected Boolean doInBackground(Integer... params){
            ConfigurationDatabase db = new ConfigurationDatabase(getActivity());
            metabolicRhythms = new ArrayList<>();
            ArrayList<MetabolicRhythm> met = db.getMetabolicRhythmsSimple();
            if(met != null) {
                for (MetabolicRhythm m : met) {
                    if (m.getId() != 1) {
                        metabolicRhythms.add((MetabolicRhythmSlave) m);
                    }
                }

                return metabolicRhythms.size() > 0;
            } else {
                return false;
            }
        }
        protected void onPostExecute(Boolean result){
            if(result){
                MetabolicRhythmAdapter adapter = (MetabolicRhythmAdapter) mLvMetabolicRhythms.getAdapter();
                adapter.setAlMetabolicRhythm(metabolicRhythms);
            }
        }
    }

    /*
     * The adapter
     */
    private class MetabolicRhythmAdapter extends BaseAdapter {

        private ArrayList<MetabolicRhythmSlave> alMetabolicRhythm;

        public MetabolicRhythmAdapter(){
            alMetabolicRhythm = new ArrayList<>();
            notifyDataSetChanged();
        }
        public void setAlMetabolicRhythm(ArrayList<MetabolicRhythmSlave> alMetabolicRhythm){
            this.alMetabolicRhythm = alMetabolicRhythm;
            notifyDataSetChanged();
        }

        public ArrayList<MetabolicRhythmSlave> getAlMetabolicRhythm() {
            return alMetabolicRhythm;
        }

        public void remove(MetabolicRhythmSlave m){
            alMetabolicRhythm.remove(m);
            notifyDataSetChanged();
        }

        public void dataSetChanged(){
            notifyDataSetChanged();
        }

        @Override
        public long getItemId(int position){
            return position;
        }

        public MetabolicRhythmSlave getItem(int item){
            return alMetabolicRhythm.get(item);
        }

        public int getCount() {
            return alMetabolicRhythm.size();
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            View item = convertView;
            MetabolicHolder holder;

            if (item == null){
                LayoutInflater inflater = getActivity().getLayoutInflater();
                item = inflater.inflate(R.layout.fragment_metabolic_list_element, null);
                holder = new MetabolicHolder();
                holder.name = (TextView) item.findViewById(R.id.tvName);
                holder.activated = (Switch) item.findViewById(R.id.activated);

                item.setTag(holder);
            } else {
                holder = (MetabolicHolder) item.getTag();
            }

            holder.name.setText(alMetabolicRhythm.get(position).getName());



            holder.activated.setOnCheckedChangeListener(null);
            holder.activated.setChecked(alMetabolicRhythm.get(position).getState() == C.METABOLIC_RHYTHM_STATE_ENABLED);

            CompoundButton.OnCheckedChangeListener activatedListener = new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(final CompoundButton buttonView, boolean isChecked) {
                    buttonView.setEnabled(false);
                    boolean result = mCallBack.onRhythmActivateChangeInList(alMetabolicRhythm.get(position).getId(), isChecked);
                    if(! result){
                        buttonView.setOnCheckedChangeListener(null);
                        buttonView.setChecked(! isChecked);
                        buttonView.setOnCheckedChangeListener(this);
                        // todo hay que poner un toast
                    }
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            buttonView.setEnabled(true);
                        }
                    },1000);

                }
            };

            holder.activated.setOnCheckedChangeListener(activatedListener);

            return item;
        }

        class MetabolicHolder {
            TextView name;
            Switch activated;
        }

    }

    public void setMetabolicListListener(MetabolicListListener callback) {
        mCallBack = callback;
    }

    /*
     * The interface
     */
    public interface MetabolicListListener {
        void onRhythmSelected(int id);
        void onRhythmDeleted(int id);
        boolean onRhythmActivateChangeInList(int id, boolean checked);
    }

    public void onMetabolicRhythmNameTextChanged(MetabolicRhythm m){
        switch (m.getId()){
            case 1:
                if(tvMasterName != null) {
                    tvMasterName.setText(m.getName());
                }
                break;
            default:
                if(mLvMetabolicRhythms != null && mLvMetabolicRhythms.getAdapter() != null) {
                    for (int c = 0; c < mLvMetabolicRhythms.getAdapter().getCount(); c++) {
                        if (((MetabolicRhythmSlave) mLvMetabolicRhythms.getAdapter().getItem(c)).getId().equals(m.getId())) {
                            ((MetabolicRhythmSlave) mLvMetabolicRhythms.getAdapter().getItem(c)).setName(m.getName());
                            ((MetabolicRhythmAdapter) mLvMetabolicRhythms.getAdapter()).dataSetChanged();
                        }
                    }
                }
        }
    }

}

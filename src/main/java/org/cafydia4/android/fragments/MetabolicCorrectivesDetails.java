package org.cafydia4.android.fragments;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.cafydia4.android.R;
import org.cafydia4.android.configdatabase.ConfigurationDatabase;
import org.cafydia4.android.core.Annotation;
import org.cafydia4.android.recommendations.Corrective;
import org.cafydia4.android.recommendations.CorrectiveComplex;
import org.cafydia4.android.recommendations.CorrectiveSimple;
import org.cafydia4.android.util.C;
import org.cafydia4.android.util.MyToast;

import java.util.ArrayList;

/**
 * Created by user on 27/09/14.
 */
public class MetabolicCorrectivesDetails extends Fragment {
    private Corrective mCorrective;
    private Corrective mOriginalCorrective;

    private EditText etName, etDescription;
    private TextView tvDescription;
    private Button bDescriptionDone;
    private CheckBox cbVisible;
    private RadioGroup rgType, rgModificationType;
    private RadioButton rbSimple, rbComplex, rbUnits, rbPercentage;
    private LinearLayout lDescription, lSimpleModification, lComplexModification, lSimplePlan, lComplexPlan;

    private TextView tvModification, tvModificationBreakfast, tvModificationLunch, tvModificationDinner;
    private SeekBar sbModification, sbModificationBreakfast, sbModificationLunch, sbModificationDinner;

    private Switch sPlan;

    ArrayList<ToggleButton> toggleButtonsSimple = new ArrayList<ToggleButton>();
    ArrayList<ToggleButton> toggleButtonsComplex = new ArrayList<ToggleButton>();


    public static MetabolicCorrectivesDetails newInstance(Corrective corrective){
        MetabolicCorrectivesDetails f = new MetabolicCorrectivesDetails();

        Bundle args = new Bundle();
        args.putInt("id", corrective.getId());
        args.putString("name", corrective.getName());
        args.putString("description", corrective.getDescription());
        args.putInt("type", corrective.getType());
        args.putInt("metabolic_id", corrective.getMetabolicRhythmId());
        args.putInt("modification_type", corrective.getModificationType());
        args.putInt("visible", corrective.getVisible());

        args.putInt("triggers", corrective.getTriggers());

        if(corrective.getType().equals(C.CORRECTIVE_TYPE_SIMPLE)){
            args.putFloat("modification", corrective.getModification(null));
        } else {
            args.putFloat("modification_br", ((CorrectiveComplex)corrective).getModificationBr());
            args.putFloat("modification_lu", ((CorrectiveComplex)corrective).getModificationLu());
            args.putFloat("modification_di", ((CorrectiveComplex)corrective).getModificationDi());
        }

        f.setArguments(args);

        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_metabolic_correctives_details, container, false);

        Bundle args = getArguments();
        int id = args.getInt("id");
        String name = args.getString("name");
        String description = args.getString("description");
        int type = args.getInt("type");
        int metabolicId = args.getInt("metabolic_id");
        int modificationType = args.getInt("modification_type");
        int visible = args.getInt("visible");

        int triggers = args.getInt("triggers");

        if(type == C.CORRECTIVE_TYPE_SIMPLE) {
            mCorrective = new CorrectiveSimple(id, name, description,type, metabolicId, modificationType, args.getFloat("modification"), visible, triggers);
        } else {
            mCorrective = new CorrectiveComplex(id, name, description,type, metabolicId, modificationType, args.getFloat("modification_br"), args.getFloat("modification_lu"), args.getFloat("modification_di"), visible, triggers);
        }

        mOriginalCorrective = mCorrective.cloneCorrective();

        searchViews(layout);

        initializeUI();

        setListeners();

        return layout;
    }

    private void searchViews(View layout){
        etName = (EditText) layout.findViewById(R.id.etName);

        etDescription = (EditText) layout.findViewById(R.id.etDescription);
        tvDescription = (TextView) layout.findViewById(R.id.tvDescription);
        bDescriptionDone = (Button) layout.findViewById(R.id.bDescriptionDone);
        lDescription = (LinearLayout) layout.findViewById(R.id.lDescription);

        cbVisible = (CheckBox) layout.findViewById(R.id.cbVisible);

        rgType = (RadioGroup) layout.findViewById(R.id.rgType);
        rgModificationType = (RadioGroup) layout.findViewById(R.id.rgModificationType);

        rbSimple = (RadioButton) layout.findViewById(R.id.rbSimple);
        rbComplex = (RadioButton) layout.findViewById(R.id.rbComplex);
        rbUnits = (RadioButton) layout.findViewById(R.id.rbUnits);
        rbPercentage = (RadioButton) layout.findViewById(R.id.rbPercentage);

        lSimpleModification = (LinearLayout) layout.findViewById(R.id.lSimpleModification);
        lComplexModification = (LinearLayout) layout.findViewById(R.id.lComplexModification);

        lSimplePlan = (LinearLayout) layout.findViewById(R.id.lSimplePlan);
        lComplexPlan = (LinearLayout) layout.findViewById(R.id.lComplexPlan);

        tvModification = (TextView) layout.findViewById(R.id.tvModification);
        tvModificationBreakfast = (TextView) layout.findViewById(R.id.tvModificationBreakfast);
        tvModificationLunch = (TextView) layout.findViewById(R.id.tvModificationLunch);
        tvModificationDinner = (TextView) layout.findViewById(R.id.tvModificationDinner);

        sbModification = (SeekBar) layout.findViewById(R.id.sbModification);
        sbModificationBreakfast = (SeekBar) layout.findViewById(R.id.sbModificationBreakfast);
        sbModificationLunch = (SeekBar) layout.findViewById(R.id.sbModificationLunch);
        sbModificationDinner = (SeekBar) layout.findViewById(R.id.sbModificationDinner);

        sPlan = (Switch) layout.findViewById(R.id.sPlan);

        ToggleButton tbBrMo = (ToggleButton) layout.findViewById(R.id.tbBrMo);
        ToggleButton tbBrTu = (ToggleButton) layout.findViewById(R.id.tbBrTu);
        ToggleButton tbBrWe = (ToggleButton) layout.findViewById(R.id.tbBrWe);
        ToggleButton tbBrTh = (ToggleButton) layout.findViewById(R.id.tbBrTh);
        ToggleButton tbBrFr = (ToggleButton) layout.findViewById(R.id.tbBrFr);
        ToggleButton tbBrSa = (ToggleButton) layout.findViewById(R.id.tbBrSa);
        ToggleButton tbBrSu = (ToggleButton) layout.findViewById(R.id.tbBrSu);

        ToggleButton tbLuMo = (ToggleButton) layout.findViewById(R.id.tbLuMo);
        ToggleButton tbLuTu = (ToggleButton) layout.findViewById(R.id.tbLuTu);
        ToggleButton tbLuWe = (ToggleButton) layout.findViewById(R.id.tbLuWe);
        ToggleButton tbLuTh = (ToggleButton) layout.findViewById(R.id.tbLuTh);
        ToggleButton tbLuFr = (ToggleButton) layout.findViewById(R.id.tbLuFr);
        ToggleButton tbLuSa = (ToggleButton) layout.findViewById(R.id.tbLuSa);
        ToggleButton tbLuSu = (ToggleButton) layout.findViewById(R.id.tbLuSu);

        ToggleButton tbDiMo = (ToggleButton) layout.findViewById(R.id.tbDiMo);
        ToggleButton tbDiTu = (ToggleButton) layout.findViewById(R.id.tbDiTu);
        ToggleButton tbDiWe = (ToggleButton) layout.findViewById(R.id.tbDiWe);
        ToggleButton tbDiTh = (ToggleButton) layout.findViewById(R.id.tbDiTh);
        ToggleButton tbDiFr = (ToggleButton) layout.findViewById(R.id.tbDiFr);
        ToggleButton tbDiSa = (ToggleButton) layout.findViewById(R.id.tbDiSa);
        ToggleButton tbDiSu = (ToggleButton) layout.findViewById(R.id.tbDiSu);

        ToggleButton tbGlMo = (ToggleButton) layout.findViewById(R.id.tbGlMo);
        ToggleButton tbGlTu = (ToggleButton) layout.findViewById(R.id.tbGlTu);
        ToggleButton tbGlWe = (ToggleButton) layout.findViewById(R.id.tbGlWe);
        ToggleButton tbGlTh = (ToggleButton) layout.findViewById(R.id.tbGlTh);
        ToggleButton tbGlFr = (ToggleButton) layout.findViewById(R.id.tbGlFr);
        ToggleButton tbGlSa = (ToggleButton) layout.findViewById(R.id.tbGlSa);
        ToggleButton tbGlSu = (ToggleButton) layout.findViewById(R.id.tbGlSu);

        toggleButtonsSimple.add(tbBrMo);
        toggleButtonsSimple.add(tbBrTu);
        toggleButtonsSimple.add(tbBrWe);
        toggleButtonsSimple.add(tbBrTh);
        toggleButtonsSimple.add(tbBrFr);
        toggleButtonsSimple.add(tbBrSa);
        toggleButtonsSimple.add(tbBrSu);

        toggleButtonsSimple.add(tbLuMo);
        toggleButtonsSimple.add(tbLuTu);
        toggleButtonsSimple.add(tbLuWe);
        toggleButtonsSimple.add(tbLuTh);
        toggleButtonsSimple.add(tbLuFr);
        toggleButtonsSimple.add(tbLuSa);
        toggleButtonsSimple.add(tbLuSu);

        toggleButtonsSimple.add(tbDiMo);
        toggleButtonsSimple.add(tbDiTu);
        toggleButtonsSimple.add(tbDiWe);
        toggleButtonsSimple.add(tbDiTh);
        toggleButtonsSimple.add(tbDiFr);
        toggleButtonsSimple.add(tbDiSa);
        toggleButtonsSimple.add(tbDiSu);

        toggleButtonsComplex.add(tbGlMo);
        toggleButtonsComplex.add(tbGlTu);
        toggleButtonsComplex.add(tbGlWe);
        toggleButtonsComplex.add(tbGlTh);
        toggleButtonsComplex.add(tbGlFr);
        toggleButtonsComplex.add(tbGlSa);
        toggleButtonsComplex.add(tbGlSu);
    }

    private void initializeUI(){
        etName.setText(mCorrective.getName());
        etDescription.setText(mCorrective.getDescription());
        tvDescription.setText(mCorrective.getDescription());
        lDescription.setVisibility(View.GONE);
        cbVisible.setChecked(mCorrective.getVisible().equals(C.CORRECTIVE_VISIBLE_YES));

        if(mCorrective.getModificationType().equals(C.CORRECTIVE_MODIFICATION_TYPE_NUMBER)){
            rbUnits.setChecked(true);
            switchUIToUnits();

        } else {
            rbPercentage.setChecked(true);
            switchUIToPercentage();
        }

        if(mCorrective.getType().equals(C.CORRECTIVE_TYPE_SIMPLE)){
            rbSimple.setChecked(true);
            switchUIToSimple();
            tvModification.setText(Integer.toString(((CorrectiveSimple) mCorrective).getModification().intValue()));
            if(mCorrective.getModificationType().equals(C.CORRECTIVE_MODIFICATION_TYPE_NUMBER)){
                sbModification.setProgress(((CorrectiveSimple) mCorrective).getModification().intValue() + 10);
            } else {
                sbModification.setProgress(((CorrectiveSimple) mCorrective).getModification().intValue() + 50);
            }

        } else {
            rbComplex.setChecked(true);
            switchUIToComplex();
            tvModificationBreakfast.setText(Integer.toString(((CorrectiveComplex) mCorrective).getModificationBr().intValue()));
            tvModificationLunch.setText(Integer.toString(((CorrectiveComplex) mCorrective).getModificationLu().intValue()));
            tvModificationDinner.setText(Integer.toString(((CorrectiveComplex) mCorrective).getModificationDi().intValue()));

            if(mCorrective.getModificationType().equals(C.CORRECTIVE_MODIFICATION_TYPE_NUMBER)){
                sbModificationBreakfast.setProgress(((CorrectiveComplex) mCorrective).getModificationBr().intValue() + 10);
                sbModificationLunch.setProgress(((CorrectiveComplex) mCorrective).getModificationLu().intValue() + 10);
                sbModificationDinner.setProgress(((CorrectiveComplex) mCorrective).getModificationDi().intValue() + 10);
            } else {
                sbModificationBreakfast.setProgress(((CorrectiveComplex) mCorrective).getModificationBr().intValue() + 50);
                sbModificationLunch.setProgress(((CorrectiveComplex) mCorrective).getModificationLu().intValue() + 50);
                sbModificationDinner.setProgress(((CorrectiveComplex) mCorrective).getModificationDi().intValue() + 50);
            }
        }

        // if no 0, has plan
        if(! mCorrective.getTriggers().equals(0)){
            sPlan.setChecked(true);
            updateToggleButtons();
        }
    }

    private void updateToggleButtons(){
        int button=0;
        switch(mCorrective.getType()){
            case C.CORRECTIVE_TYPE_SIMPLE:
                for(int meal=C.MEAL_BREAKFAST; meal<=C.MEAL_DINNER; meal++){
                    for(int dayOfWeek=C.DAY_WEEK_MONDAY; dayOfWeek<=C.DAY_WEEK_SUNDAY;dayOfWeek++){
                        toggleButtonsSimple.get(button).setOnCheckedChangeListener(null);
                        if(mCorrective.applies(meal, dayOfWeek)){

                            toggleButtonsSimple.get(button).setChecked(true);
                        } else {
                            toggleButtonsSimple.get(button).setChecked(false);
                        }
                        toggleButtonsSimple.get(button).setOnCheckedChangeListener(listenerToggleButton);
                        button++;
                    }
                }
                break;

            case C.CORRECTIVE_TYPE_COMPLEX:
                for(int dayOfWeek=C.DAY_WEEK_MONDAY; dayOfWeek<=C.DAY_WEEK_SUNDAY;dayOfWeek++){
                    toggleButtonsSimple.get(button).setOnCheckedChangeListener(null);
                    if(mCorrective.applies(0, dayOfWeek)){
                        toggleButtonsComplex.get(button).setChecked(true);
                    } else {
                        toggleButtonsComplex.get(button).setChecked(false);
                    }
                    toggleButtonsSimple.get(button).setOnCheckedChangeListener(listenerToggleButton);
                    button++;
                }
                break;
        }

    }

    private void setListeners(){
        etName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mCorrective.setName(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        etDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mCorrective.setDescription(s.toString());
                tvDescription.setText(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        tvDescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvDescription.setVisibility(View.GONE);
                lDescription.setVisibility(View.VISIBLE);
            }
        });
        bDescriptionDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvDescription.setVisibility(View.VISIBLE);
                lDescription.setVisibility(View.GONE);
            }
        });

        rgType.setOnCheckedChangeListener(listenerType);
        rgModificationType.setOnCheckedChangeListener(listenerModificationType);

        sbModification.setOnSeekBarChangeListener(listenerModification);
        sbModificationBreakfast.setOnSeekBarChangeListener(listenerModification);
        sbModificationLunch.setOnSeekBarChangeListener(listenerModification);
        sbModificationDinner.setOnSeekBarChangeListener(listenerModification);

        for(ToggleButton tb : toggleButtonsSimple){
            tb.setOnCheckedChangeListener(listenerToggleButton);
        }

        for(ToggleButton tb : toggleButtonsComplex){
            tb.setOnCheckedChangeListener(listenerToggleButton);
        }

        sPlan.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    if(mCorrective.getType().equals(C.CORRECTIVE_TYPE_SIMPLE)){
                        lSimplePlan.setVisibility(View.VISIBLE);
                        lComplexPlan.setVisibility(View.GONE);
                    } else {
                        lComplexPlan.setVisibility(View.VISIBLE);
                        lSimplePlan.setVisibility(View.GONE);
                    }
                } else {
                    lComplexPlan.setVisibility(View.GONE);
                    lSimplePlan.setVisibility(View.GONE);
                }
            }
        });

        cbVisible.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    mCorrective.setVisible(C.CORRECTIVE_VISIBLE_YES);
                } else {
                    mCorrective.setVisible(C.CORRECTIVE_VISIBLE_NO);
                }
            }
        });
    }

    private void switchUIToComplex(){
        lSimplePlan.setVisibility(View.GONE);
        lSimpleModification.setVisibility(View.GONE);

        if(!mCorrective.getTriggers().equals(0)) {
            lComplexPlan.setVisibility(View.VISIBLE);
        } else {
            lComplexPlan.setVisibility(View.GONE);
        }
        lComplexModification.setVisibility(View.VISIBLE);



    }
    private void switchUIToSimple(){
        lComplexPlan.setVisibility(View.GONE);
        lComplexModification.setVisibility(View.GONE);

        if(!mCorrective.getTriggers().equals(0)){
            lSimplePlan.setVisibility(View.VISIBLE);
        } else {
            lSimplePlan.setVisibility(View.GONE);
        }
        lSimpleModification.setVisibility(View.VISIBLE);

    }

    private void switchUIToUnits(){
        sbModification.setOnSeekBarChangeListener(null);
        sbModificationBreakfast.setOnSeekBarChangeListener(null);
        sbModificationLunch.setOnSeekBarChangeListener(null);
        sbModificationDinner.setOnSeekBarChangeListener(null);

        sbModificationBreakfast.setMax(20);
        sbModificationLunch.setMax(20);
        sbModificationDinner.setMax(20);
        sbModification.setMax(20);

        switch(mCorrective.getType()){
            case C.CORRECTIVE_TYPE_SIMPLE:
                sbModification.setProgress(((CorrectiveSimple)mCorrective).getModification().intValue() + 10);
                tvModification.setText(Integer.toString(((CorrectiveSimple)mCorrective).getModification().intValue()));
                break;

            case C.CORRECTIVE_TYPE_COMPLEX:
                sbModificationBreakfast.setProgress(((CorrectiveComplex)mCorrective).getModificationBr().intValue() + 10);
                tvModificationBreakfast.setText(Integer.toString(((CorrectiveComplex)mCorrective).getModificationBr().intValue()));

                sbModificationLunch.setProgress(((CorrectiveComplex)mCorrective).getModificationLu().intValue() + 10);
                tvModificationLunch.setText(Integer.toString(((CorrectiveComplex)mCorrective).getModificationLu().intValue()));

                sbModificationDinner.setProgress(((CorrectiveComplex)mCorrective).getModificationDi().intValue() + 10);
                tvModificationDinner.setText(Integer.toString(((CorrectiveComplex)mCorrective).getModificationDi().intValue()));
                break;
        }

        sbModification.setOnSeekBarChangeListener(listenerModification);
        sbModificationBreakfast.setOnSeekBarChangeListener(listenerModification);
        sbModificationLunch.setOnSeekBarChangeListener(listenerModification);
        sbModificationDinner.setOnSeekBarChangeListener(listenerModification);
    }

    private void switchUIToPercentage(){
        sbModification.setOnSeekBarChangeListener(null);
        sbModificationBreakfast.setOnSeekBarChangeListener(null);
        sbModificationLunch.setOnSeekBarChangeListener(null);
        sbModificationDinner.setOnSeekBarChangeListener(null);

        sbModificationBreakfast.setMax(100);
        sbModificationLunch.setMax(100);
        sbModificationDinner.setMax(100);
        sbModification.setMax(100);

        switch(mCorrective.getType()){
            case C.CORRECTIVE_TYPE_SIMPLE:
                sbModification.setProgress(((CorrectiveSimple)mCorrective).getModification().intValue() + 50);
                tvModification.setText(Integer.toString(((CorrectiveSimple)mCorrective).getModification().intValue()));
                break;

            case C.CORRECTIVE_TYPE_COMPLEX:
                sbModificationBreakfast.setProgress(((CorrectiveComplex)mCorrective).getModificationBr().intValue() + 50);
                tvModificationBreakfast.setText(Integer.toString(((CorrectiveComplex)mCorrective).getModificationBr().intValue()));

                sbModificationLunch.setProgress(((CorrectiveComplex)mCorrective).getModificationLu().intValue() + 50);
                tvModificationLunch.setText(Integer.toString(((CorrectiveComplex)mCorrective).getModificationLu().intValue()));

                sbModificationDinner.setProgress(((CorrectiveComplex)mCorrective).getModificationDi().intValue() + 50);
                tvModificationDinner.setText(Integer.toString(((CorrectiveComplex)mCorrective).getModificationDi().intValue()));
                break;
        }

        sbModification.setOnSeekBarChangeListener(listenerModification);
        sbModificationBreakfast.setOnSeekBarChangeListener(listenerModification);
        sbModificationLunch.setOnSeekBarChangeListener(listenerModification);
        sbModificationDinner.setOnSeekBarChangeListener(listenerModification);

    }

    private RadioGroup.OnCheckedChangeListener listenerType = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId){
                case R.id.rbSimple:
                    if(mCorrective instanceof CorrectiveComplex) {
                        mCorrective = ((CorrectiveComplex) mCorrective).toSimple();
                        switchUIToSimple();
                        updateToggleButtons();
                        sPlan.setChecked(false);
                    }
                    break;
                case R.id.rbComplex:
                    if(mCorrective instanceof CorrectiveSimple) {
                        mCorrective = ((CorrectiveSimple) mCorrective).toComplex();
                        switchUIToComplex();
                        updateToggleButtons();
                        sPlan.setChecked(false);
                    }
                    break;
            }

            switch(mCorrective.getModificationType()){
                case C.CORRECTIVE_MODIFICATION_TYPE_NUMBER:
                    switchUIToUnits();
                    break;
                case C.CORRECTIVE_MODIFICATION_TYPE_PERCENTAGE:
                    switchUIToPercentage();
                    break;
            }
        }
    };

    private RadioGroup.OnCheckedChangeListener listenerModificationType = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (mCorrective.getType()){
                case C.CORRECTIVE_TYPE_SIMPLE:
                    ((CorrectiveSimple) mCorrective).setModification(0f);
                    break;
                case C.CORRECTIVE_TYPE_COMPLEX:
                    ((CorrectiveComplex) mCorrective).setModificationBr(0f);
                    ((CorrectiveComplex) mCorrective).setModificationLu(0f);
                    ((CorrectiveComplex) mCorrective).setModificationDi(0f);
                    break;
            }

            switch (checkedId){
                case R.id.rbUnits:
                    mCorrective.setModificationType(C.CORRECTIVE_MODIFICATION_TYPE_NUMBER);
                    switchUIToUnits();
                    break;
                case R.id.rbPercentage:
                    mCorrective.setModificationType(C.CORRECTIVE_MODIFICATION_TYPE_PERCENTAGE);
                    switchUIToPercentage();
                    break;
            }

        }
    };

    private SeekBar.OnSeekBarChangeListener listenerModification = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            switch (seekBar.getId()){
                case R.id.sbModification:
                    switch (mCorrective.getModificationType()){
                        case C.CORRECTIVE_MODIFICATION_TYPE_NUMBER:
                            tvModification.setText(Integer.toString(progress - 10));
                            if(mCorrective instanceof CorrectiveSimple) {
                                ((CorrectiveSimple) mCorrective).setModification(progress - 10f);
                            }
                            break;
                        case C.CORRECTIVE_MODIFICATION_TYPE_PERCENTAGE:
                            tvModification.setText(Integer.toString(progress - 50));
                            if(mCorrective instanceof CorrectiveSimple) {
                                ((CorrectiveSimple) mCorrective).setModification(progress - 50f);
                            }
                            break;
                    }
                    break;
                case R.id.sbModificationBreakfast:
                    switch (mCorrective.getModificationType()){
                        case C.CORRECTIVE_MODIFICATION_TYPE_NUMBER:
                            tvModificationBreakfast.setText(Integer.toString(progress - 10));
                            if(mCorrective instanceof CorrectiveComplex) {
                                ((CorrectiveComplex) mCorrective).setModificationBr(progress - 10f);
                            }
                            break;
                        case C.CORRECTIVE_MODIFICATION_TYPE_PERCENTAGE:
                            tvModificationBreakfast.setText(Integer.toString(progress - 50));
                            if(mCorrective instanceof CorrectiveComplex) {
                                ((CorrectiveComplex) mCorrective).setModificationBr(progress - 50f);
                            }
                            break;
                    }
                    break;
                case R.id.sbModificationLunch:
                    switch (mCorrective.getModificationType()){
                        case C.CORRECTIVE_MODIFICATION_TYPE_NUMBER:
                            tvModificationLunch.setText(Integer.toString(progress - 10));
                            if(mCorrective instanceof CorrectiveComplex) {
                                ((CorrectiveComplex) mCorrective).setModificationLu(progress - 10f);
                            }
                            break;
                        case C.CORRECTIVE_MODIFICATION_TYPE_PERCENTAGE:
                            tvModificationLunch.setText(Integer.toString(progress - 50));
                            if(mCorrective instanceof CorrectiveComplex) {
                                ((CorrectiveComplex) mCorrective).setModificationLu(progress - 50f);
                            }
                            break;
                    }
                    break;
                case R.id.sbModificationDinner:
                    switch (mCorrective.getModificationType()){
                        case C.CORRECTIVE_MODIFICATION_TYPE_NUMBER:
                            tvModificationDinner.setText(Integer.toString(progress - 10));
                            if(mCorrective instanceof CorrectiveComplex) {
                                ((CorrectiveComplex) mCorrective).setModificationDi(progress - 10f);
                            }
                            break;
                        case C.CORRECTIVE_MODIFICATION_TYPE_PERCENTAGE:
                            tvModificationDinner.setText(Integer.toString(progress - 50));
                            if(mCorrective instanceof CorrectiveComplex) {
                                ((CorrectiveComplex) mCorrective).setModificationDi(progress - 50f);
                            }
                            break;
                    }
                    break;
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    private CompoundButton.OnCheckedChangeListener listenerToggleButton = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            int meal = 0, dayOfWeek = 0;
            switch(buttonView.getId()){
                case R.id.tbBrMo:
                    meal = C.MEAL_BREAKFAST;
                    dayOfWeek = C.DAY_WEEK_MONDAY;
                    break;
                case R.id.tbBrTu:
                    meal = C.MEAL_BREAKFAST;
                    dayOfWeek = C.DAY_WEEK_TUESDAY;
                    break;
                case R.id.tbBrWe:
                    meal = C.MEAL_BREAKFAST;
                    dayOfWeek = C.DAY_WEEK_WEDNESDAY;
                    break;
                case R.id.tbBrTh:
                    meal = C.MEAL_BREAKFAST;
                    dayOfWeek = C.DAY_WEEK_THURSDAY;
                    break;
                case R.id.tbBrFr:
                    meal = C.MEAL_BREAKFAST;
                    dayOfWeek = C.DAY_WEEK_FRIDAY;
                    break;
                case R.id.tbBrSa:
                    meal = C.MEAL_BREAKFAST;
                    dayOfWeek = C.DAY_WEEK_SATURDAY;
                    break;
                case R.id.tbBrSu:
                    meal = C.MEAL_BREAKFAST;
                    dayOfWeek = C.DAY_WEEK_SUNDAY;
                    break;
                case R.id.tbLuMo:
                    meal = C.MEAL_LUNCH;
                    dayOfWeek = C.DAY_WEEK_MONDAY;
                    break;
                case R.id.tbLuTu:
                    meal = C.MEAL_LUNCH;
                    dayOfWeek = C.DAY_WEEK_TUESDAY;
                    break;
                case R.id.tbLuWe:
                    meal = C.MEAL_LUNCH;
                    dayOfWeek = C.DAY_WEEK_WEDNESDAY;
                    break;
                case R.id.tbLuTh:
                    meal = C.MEAL_LUNCH;
                    dayOfWeek = C.DAY_WEEK_THURSDAY;
                    break;
                case R.id.tbLuFr:
                    meal = C.MEAL_LUNCH;
                    dayOfWeek = C.DAY_WEEK_FRIDAY;
                    break;
                case R.id.tbLuSa:
                    meal = C.MEAL_LUNCH;
                    dayOfWeek = C.DAY_WEEK_SATURDAY;
                    break;
                case R.id.tbLuSu:
                    meal = C.MEAL_LUNCH;
                    dayOfWeek = C.DAY_WEEK_SUNDAY;
                    break;
                case R.id.tbDiMo:
                    meal = C.MEAL_DINNER;
                    dayOfWeek = C.DAY_WEEK_MONDAY;
                    break;
                case R.id.tbDiTu:
                    meal = C.MEAL_DINNER;
                    dayOfWeek = C.DAY_WEEK_TUESDAY;
                    break;
                case R.id.tbDiWe:
                    meal = C.MEAL_DINNER;
                    dayOfWeek = C.DAY_WEEK_WEDNESDAY;
                    break;
                case R.id.tbDiTh:
                    meal = C.MEAL_DINNER;
                    dayOfWeek = C.DAY_WEEK_THURSDAY;
                    break;
                case R.id.tbDiFr:
                    meal = C.MEAL_DINNER;
                    dayOfWeek = C.DAY_WEEK_FRIDAY;
                    break;
                case R.id.tbDiSa:
                    meal = C.MEAL_DINNER;
                    dayOfWeek = C.DAY_WEEK_SATURDAY;
                    break;
                case R.id.tbDiSu:
                    meal = C.MEAL_DINNER;
                    dayOfWeek = C.DAY_WEEK_SUNDAY;
                    break;

                case R.id.tbGlMo:
                    meal = 0;
                    dayOfWeek = C.DAY_WEEK_MONDAY;
                    break;
                case R.id.tbGlTu:
                    meal = 0;
                    dayOfWeek = C.DAY_WEEK_TUESDAY;
                    break;
                case R.id.tbGlWe:
                    meal = 0;
                    dayOfWeek = C.DAY_WEEK_WEDNESDAY;
                    break;
                case R.id.tbGlTh:
                    meal = 0;
                    dayOfWeek = C.DAY_WEEK_THURSDAY;
                    break;
                case R.id.tbGlFr:
                    meal = 0;
                    dayOfWeek = C.DAY_WEEK_FRIDAY;
                    break;
                case R.id.tbGlSa:
                    meal = 0;
                    dayOfWeek = C.DAY_WEEK_SATURDAY;
                    break;
                case R.id.tbGlSu:
                    meal = 0;
                    dayOfWeek = C.DAY_WEEK_SUNDAY;
                    break;
            }

            mCorrective.setTrigger(meal, dayOfWeek, isChecked);
        }
    };

    public void saveCorrectiveToDatabase(){
        if(!sPlan.isChecked()){
            mCorrective.setTriggers(0);
        }

        if(! mCorrective.equals(mOriginalCorrective)){
            ConfigurationDatabase db;
            db = new ConfigurationDatabase(getActivity());
            if(mCorrective.getId().equals(0)){
                mOriginalCorrective.delete(db);
            }
            mCorrective.save(db);
            new MyToast(getActivity(), getString(R.string.metabolic_correctives_details_changes_saved_1) + " " + mCorrective.getName() + " " + getString(R.string.metabolic_correctives_details_changes_saved_2));

            // anotacion automática
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
            if(sp.getBoolean("pref_automatic_annotations_corrective", true)) {
                String annotation = getString(R.string.automatic_annotation_corrective_change);
                annotation += ". " + getString(R.string.automatic_annotation_old) + ": " + mOriginalCorrective.toString(null);
                annotation += ", " + getString(R.string.automatic_annotation_new) + ": " + mCorrective.toString(null) + ".";
                Annotation.saveCafydiaAutomaticAnnotation(getActivity(), annotation);
            }

        }
    }
}

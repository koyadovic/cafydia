package org.cafydia4.android.recommendations;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;

import org.cafydia4.android.R;
import org.cafydia4.android.chartobjects.Label;
import org.cafydia4.android.configdatabase.ConfigurationDatabase;
import org.cafydia4.android.core.Annotation;
import org.cafydia4.android.core.GlucoseTest;
import org.cafydia4.android.core.Instant;
import org.cafydia4.android.core.Meal;
import org.cafydia4.android.datadatabase.DataDatabase;
import org.cafydia4.android.util.C;

import java.util.ArrayList;

/**
 * Created by user on 22/09/14.
 */
public class MetabolicFramework {
    // attributes
    private MetabolicFrameworkState mFrameworkState;
    private MetabolicRhythmMaster mMaster = null;
    private int metabolicRhythmIdActivated;
    private MetabolicRhythm enabledMetabolicRhythm = null;
    private MetabolicRhythm originalEnabledMetabolicRhythm;
    private MetabolicRhythm secondaryMetabolicRhythm;
    private Context mContext;

    private ModificationStart preprandialBreakfast;
    private ModificationStart preprandialLunch;
    private ModificationStart preprandialDinner;

    private ModificationStart basalBreakfast;
    private ModificationStart basalLunch;
    private ModificationStart basalDinner;

    private ConfigurationDatabase mConfigDatabase;
    private DataDatabase mDataDatabase;

    private ArrayList<MetabolicRhythmSlave> plannedSortedMetabolicRhythms = null;
    private CorrectionFactor mCorrectionFactor;

    private boolean correctionFactorAboveActivated = false;
    private boolean correctionFactorBelowActivated = false;
    private float correctionFactorAboveValue = 0f;
    private float correctionFactorBelowValue = 0f;

    private BaselinePreprandial mBaselinePreprandial;
    private BaselineBasal mBaselineBasal;

    private OnMetabolicFrameworkLoadedListener mCallback;

    public static interface OnMetabolicFrameworkLoadedListener {
        public void onLoadFinished();
    }

    public MetabolicFramework(Context activity, OnMetabolicFrameworkLoadedListener callback){
        mContext = activity;
        mCallback = callback;

        mFrameworkState = new MetabolicFrameworkState(mContext);
        mConfigDatabase = new ConfigurationDatabase(mContext);
        mDataDatabase = new DataDatabase(mContext);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity);

        correctionFactorAboveActivated = sp.getBoolean("pref_key_correction_factor_above_activated", false);
        correctionFactorBelowActivated = sp.getBoolean("pref_key_correction_factor_below_activated", false);

        // todo Este bloque de código podrá ser eliminado cuando ya no queden más personas con cafydia versión 16 o inferiores instalado.
        boolean old = sp.getBoolean("pref_key_correction_factor_activated", false);
        boolean checked = sp.getBoolean("pref_key_correction_factor_activated_checked", false);
        if(!checked){
            SharedPreferences.Editor editor = sp.edit();
            if(old){
                editor.putBoolean("pref_key_correction_factor_above_activated", true);
                editor.putBoolean("pref_key_correction_factor_below_activated", true);
                correctionFactorAboveActivated = true;
                correctionFactorBelowActivated = true;
            }
            editor.putBoolean("pref_key_correction_factor_activated_checked", true);
            editor.apply();

        }
        // todo hasta aquí

        correctionFactorAboveValue = Float.parseFloat(sp.getString("pref_key_correction_factor_above", "100"));
        correctionFactorBelowValue = Float.parseFloat(sp.getString("pref_key_correction_factor_below", "100"));

        refresh();

        if(mCallback != null) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mCallback.onLoadFinished();
                }
            }, 100);
        }
    }


    public MetabolicFramework(Context activity){
        this(activity, null);
    }

    public void refresh(){
        plannedSortedMetabolicRhythms = mConfigDatabase.getPlanedSortedMetabolicRhythmsSimple();
        metabolicRhythmIdActivated = mFrameworkState.getActivatedMetabolicRhythmId();

        enabledMetabolicRhythm = mConfigDatabase.getMetabolicRhythmById(metabolicRhythmIdActivated);
        if(secondaryMetabolicRhythm != null){
            secondaryMetabolicRhythm = mConfigDatabase.getMetabolicRhythmById(secondaryMetabolicRhythm.getId());
        }
        reassembleStarts();
        pUpdateLastCheck();

        if(mCorrectionFactor == null) {
            mCorrectionFactor = new CorrectionFactor(mContext, this);
        }
    }


    public void reloadPlannedSortedMetabolicRhythms(){
        plannedSortedMetabolicRhythms = mConfigDatabase.getPlanedSortedMetabolicRhythmsSimple();
    }



    public void loadParameters(Context c){
        if(mBaselinePreprandial == null) {
            mBaselinePreprandial = new BaselinePreprandial(c);
        }
        if(mBaselineBasal == null) {
            mBaselineBasal = new BaselineBasal(c);
        }
    }

    public BaselinePreprandial getBaselinePreprandial() {
        return mBaselinePreprandial;
    }

    public BaselineBasal getBaselineBasal() {
        return mBaselineBasal;
    }



    /*
         * Aquí hay que construir los comienzos. En preprandial es lo siguiente:
         *
         * Si el estado es global, ese estado se añadirá como comienzo en todos los comienzos del marco al inicio, día -1
         * Si el estado es especifico, se añadirá como comienzo a cada uno, de cada comida al inicio, día -1
         * Si el ritmo metabólico ensamblado es global, su comienzo se añadirá a los comienzos desayuno, comida o cena del marco
         * Si el ritmo metabólico ensamblado es especifico, sus comienzos se añadirán a la comida que corresponda.
         *
         * En definitiva tendríamos siempre tres comienzos construídos para desayuno, comida y cena
         */
    public void reassembleStarts(){

        // preprandial
        if(enabledMetabolicRhythm.getStartingPreprandialType() == C.STARTING_TYPE_SPECIFIC){
            preprandialBreakfast = enabledMetabolicRhythm.getPreprandialModificationStartBreakfast();
            preprandialLunch = enabledMetabolicRhythm.getPreprandialModificationStartLunch();
            preprandialDinner = enabledMetabolicRhythm.getPreprandialModificationStartDinner();
        } else {
            preprandialBreakfast = enabledMetabolicRhythm.getPreprandialModificationStart();
            preprandialLunch = enabledMetabolicRhythm.getPreprandialModificationStart();
            preprandialDinner = enabledMetabolicRhythm.getPreprandialModificationStart();
        }

        if(mFrameworkState.getStartingPreprandialType() == C.STARTING_TYPE_SPECIFIC){
            preprandialBreakfast.addDot(mFrameworkState.getStartingPreprandialModificationBreakfastDot());
            preprandialLunch.addDot(mFrameworkState.getStartingPreprandialModificationLunchDot());
            preprandialDinner.addDot(mFrameworkState.getStartingPreprandialModificationDinnerDot());
        } else {
            preprandialBreakfast.addDot(mFrameworkState.getStartingPreprandialModificationDot());
            preprandialLunch.addDot(mFrameworkState.getStartingPreprandialModificationDot());
            preprandialDinner.addDot(mFrameworkState.getStartingPreprandialModificationDot());
        }


        // basal
        basalBreakfast = enabledMetabolicRhythm.getBasalModificationStartBreakfast();
        basalLunch = enabledMetabolicRhythm.getBasalModificationStartLunch();
        basalDinner = enabledMetabolicRhythm.getBasalModificationStartDinner();

        basalBreakfast.addDot(mFrameworkState.getStartingBasalModificationBreakfastDot());
        basalLunch.addDot(mFrameworkState.getStartingBasalModificationLunchDot());
        basalDinner.addDot(mFrameworkState.getStartingBasalModificationDinnerDot());

    }



    /*
     * FOR THE RECOMMENDATIONS
     *
     * Aquí se llama desde el diálogo de añadir glucosa, siempre que ésta sea una glucosa de después de una comida.
     */
    public void calculateLinearFunctionsFromDatabaseIfNeeded(){
        if(! mBaselinePreprandial.areAllFunctionsGenerated()) {
            for(int a = C.MEAL_BREAKFAST; a <= C.MEAL_DINNER; a++){

                BaselinePreprandial.CalculateFromDatabase glfFromDb = new BaselinePreprandial.CalculateFromDatabase(this, a);

                Float m = glfFromDb.getM();
                Float b = glfFromDb.getB();

                if (m != null) {
                    mBaselinePreprandial.setMByMeal(m, a);
                    mBaselinePreprandial.setBByMeal(b, a);

                    if(mBaselinePreprandial.areAllFunctionsGenerated()){
                        BaselinePreprandial.Fitter.onAllBaselinesGenerated(mContext);
                    }
                }

            }
        }
    }

    public void fillRecommendationData(Meal meal, ArrayList<Corrective> correctives){
        fillBaselinePreprandial(meal);
        fillBaselineBasal(meal);
        fillMetabolicPreprandial(meal);
        fillMetabolicBasal(meal);
        fillCorrectionFactorPreprandial(meal);
        fillCorrectivesPreprandial(meal, correctives);
    }

    private void fillBaselinePreprandial(Meal meal){
        Float baselinePreprandial = mBaselinePreprandial.getPreprandialBaselineDoseForMeal(meal);

        if(baselinePreprandial != null) {
            meal.setBaselinePreprandial(baselinePreprandial);
        } else {
            meal.setBaselinePreprandial(0.0f);
        }
    }


    private void fillBaselineBasal(Meal m){
        Float dose = null;
        if(m != null) {
            if((m.getMealTime().equals(C.MEAL_BREAKFAST) && mBaselineBasal.isBasalBreakfastActivated()) ||
                    (m.getMealTime().equals(C.MEAL_LUNCH) && mBaselineBasal.isBasalLunchActivated()) ||
                    (m.getMealTime().equals(C.MEAL_DINNER) && mBaselineBasal.isBasalDinnerActivated())) {

                dose = mBaselineBasal.getBasalDose(m);
            }

            if(dose != null) {
                m.setBaselineBasal(dose);
            } else {
                m.setBaselineBasal(0.0f);
            }
        }
    }

    private void fillMetabolicPreprandial(Meal meal){
        Instant start = new Instant(mFrameworkState.getActivatedMetabolicRhythmStartDate()).setTimeToTheStartOfTheDay();
        float daysPassed = new Instant(meal).setTimeToTheStartOfTheDay().getDaysPassedFromInstant(start);

        switch (meal.getMealTime()){
            case C.MEAL_BREAKFAST:
                meal.setBeginningPreprandial(preprandialBreakfast.getModification(daysPassed));
                break;

            case C.MEAL_LUNCH:
                meal.setBeginningPreprandial(preprandialLunch.getModification(daysPassed));
                break;

            case C.MEAL_DINNER:
                meal.setBeginningPreprandial(preprandialDinner.getModification(daysPassed));
                break;
        }

    }

    private void fillMetabolicBasal(Meal meal){
        Instant start = new Instant(mFrameworkState.getActivatedMetabolicRhythmStartDate()).setTimeToTheStartOfTheDay();
        float daysPassed = new Instant(meal).setTimeToTheStartOfTheDay().getDaysPassedFromInstant(start);

        switch (meal.getMealTime()){
            case C.MEAL_BREAKFAST:
                meal.setBeginningBasal(basalBreakfast.getModification(daysPassed));
                break;

            case C.MEAL_LUNCH:
                meal.setBeginningBasal(basalLunch.getModification(daysPassed));
                break;

            case C.MEAL_DINNER:
                meal.setBeginningBasal(basalDinner.getModification(daysPassed));
                break;
        }

    }

    // a este método hay que llamarle siempre DESPUÉS de llamar al que rellena el baseline preprandial, porque depende de él
    private void fillCorrectivesPreprandial(Meal meal, ArrayList<Corrective> correctives){
        Float correctiveModification = 0.0f;
        String names = "";

        for(Corrective c : correctives){
            if(c.getTemporalState()){
                if(c.getModificationType().equals(C.CORRECTIVE_MODIFICATION_TYPE_NUMBER)){
                    correctiveModification += c.getModification(meal);
                } else {
                    correctiveModification += (c.getModification(meal) / 100.0f) * meal.getBaselinePreprandial();
                }

                if(names.equals("")){
                    names = c.toString(meal);
                } else {
                    names = names + ", " + c.toString(meal);
                }
            }
        }

        meal.setCorrectivesPreprandial(correctiveModification);
        meal.setCorrectivesAppliedName(names);
    }

    private void fillCorrectionFactorPreprandial(Meal meal){
        Float correctionModification = 0.0f;

        if (mCorrectionFactor != null) {
            GlucoseTest lastGlucoseTest = mDataDatabase.getLastGlucoseTestAdded();

            if (lastGlucoseTest != null && lastGlucoseTest.isToday()) {

                if (lastGlucoseTest.getGlucoseTime().equals(C.GLUCOSE_TEST_BEFORE_BREAKFAST) && meal.getMealTime().equals(C.MEAL_BREAKFAST) ||
                        lastGlucoseTest.getGlucoseTime().equals(C.GLUCOSE_TEST_BEFORE_LUNCH) && meal.getMealTime().equals(C.MEAL_LUNCH) ||
                        lastGlucoseTest.getGlucoseTime().equals(C.GLUCOSE_TEST_BEFORE_DINNER) && meal.getMealTime().equals(C.MEAL_DINNER)) {

                    if(correctionFactorAboveActivated && lastGlucoseTest.getGlucoseLevel() > correctionFactorAboveValue) {
                        correctionModification = mCorrectionFactor.getModificationByGlucose(lastGlucoseTest);
                    }
                    else if(correctionFactorBelowActivated && lastGlucoseTest.getGlucoseLevel() < correctionFactorBelowValue) {
                        correctionModification = mCorrectionFactor.getModificationByGlucose(lastGlucoseTest);
                    }

                }
            }
        }

        meal.setCorrectionFactorPreprandial(correctionModification);
    }

    /*
     * END OF RECOMMENDATIONS
     */











    public int requestToConnectAndActivateMetabolicRhythmById(int id){

        if(enabledMetabolicRhythm.getId() != 1 && ((MetabolicRhythmSlave) enabledMetabolicRhythm).applies()){
            return C.METABOLIC_FRAMEWORK_ERROR_METABOLIC_RHYTHM_PLANED;

        } else {
            reassembleStarts();
            pUpdateMetabolicFrameworkState();

            enabledMetabolicRhythm.setState(C.METABOLIC_RHYTHM_STATE_DISABLED);
            enabledMetabolicRhythm.save(mConfigDatabase);

            // cargamos el nuevo
            MetabolicRhythm m = mConfigDatabase.getMetabolicRhythmById(id);
            pActivateMetabolicRhythm(m);

            return C.METABOLIC_FRAMEWORK_ERROR_ALL_OK;
        }
    }
    public int requestToDisconnectMetabolicRhythmById(int id){

        if(enabledMetabolicRhythm.getId() != 1 && ((MetabolicRhythmSlave) enabledMetabolicRhythm).applies()){
            return C.METABOLIC_FRAMEWORK_ERROR_METABOLIC_RHYTHM_PLANED;

        } else {
            reassembleStarts();
            pUpdateMetabolicFrameworkState();

            MetabolicRhythm m = mConfigDatabase.getMetabolicRhythmById(id);
            m.setState(C.METABOLIC_RHYTHM_STATE_DISABLED);
            m.save(mConfigDatabase);

            //secondaryMetabolicRhythm = m;

            // cargamos el nuevo
            m = mConfigDatabase.getMetabolicRhythmById(1);
            pActivateMetabolicRhythm(m);

            return C.METABOLIC_FRAMEWORK_ERROR_ALL_OK;
        }
    }

    public void forceDisconnectMetabolicRhythmById(int id){
        reassembleStarts();
        pUpdateMetabolicFrameworkState();

        MetabolicRhythm m = mConfigDatabase.getMetabolicRhythmById(id);
        m.setState(C.METABOLIC_RHYTHM_STATE_DISABLED);
        m.save(mConfigDatabase);

        // cargamos el nuevo
        m = mConfigDatabase.getMetabolicRhythmById(1);
        pActivateMetabolicRhythm(m);
    }


    public void connectSecondaryMetabolicRhythm(MetabolicRhythm m){
        secondaryMetabolicRhythm = m;
    }















    /*
     * Getters
     */


    public MetabolicRhythm getEnabledMetabolicRhythm() {
        if(enabledMetabolicRhythm == null) {
            refresh();
        }
        return enabledMetabolicRhythm;
    }

    public MetabolicRhythm getSecondaryMetabolicRhythm() {
        if(secondaryMetabolicRhythm == null) {
            refresh();
        }
        return secondaryMetabolicRhythm;
    }

    public CorrectionFactor getCorrectionFactor() {
        return mCorrectionFactor;
    }

    public MetabolicFrameworkState getState() {
        return mFrameworkState;
    }

    public ConfigurationDatabase getConfigDatabase() {
        if(mConfigDatabase == null) {
            mConfigDatabase = new ConfigurationDatabase(mContext);
        }

        return mConfigDatabase;
    }

    public DataDatabase getDataDatabase() {
        if(mDataDatabase == null) {
            mDataDatabase = new DataDatabase(mContext);
        }

        return mDataDatabase;
    }

    public Context getContext(){
        return mContext;
    }

    public ArrayList<MetabolicRhythmSlave> getPlannedSortedMetabolicRhythms() {
        return plannedSortedMetabolicRhythms;
    }


    /*
     Preprandial
     */
    public ModificationStart getEnabledPreprandialStartForBreakfast() {
        ModificationStart start;
        if(enabledMetabolicRhythm.getStartingPreprandialType() == C.STARTING_TYPE_GLOBAL){
            start = enabledMetabolicRhythm.getPreprandialModificationStart();
        } else {
            start = enabledMetabolicRhythm.getPreprandialModificationStartBreakfast();
        }

        if(mFrameworkState.getStartingPreprandialType() == C.STARTING_TYPE_GLOBAL){
            start.addDot(mFrameworkState.getStartingPreprandialModificationDot());
        } else {
            start.addDot(mFrameworkState.getStartingPreprandialModificationBreakfastDot());
        }
        return start;
    }

    public ModificationStart getEnabledPreprandialStartForLunch() {
        ModificationStart start;
        if(enabledMetabolicRhythm.getStartingPreprandialType() == C.STARTING_TYPE_GLOBAL){
            start = enabledMetabolicRhythm.getPreprandialModificationStart();
        } else {
            start = enabledMetabolicRhythm.getPreprandialModificationStartLunch();
        }

        if(mFrameworkState.getStartingPreprandialType() == C.STARTING_TYPE_GLOBAL){
            start.addDot(mFrameworkState.getStartingPreprandialModificationDot());
        } else {
            start.addDot(mFrameworkState.getStartingPreprandialModificationLunchDot());
        }
        return start;
    }

    public ModificationStart getEnabledPreprandialStartForDinner() {
        ModificationStart start;
        if(enabledMetabolicRhythm.getStartingPreprandialType() == C.STARTING_TYPE_GLOBAL){
            start = enabledMetabolicRhythm.getPreprandialModificationStart();
        } else {
            start = enabledMetabolicRhythm.getPreprandialModificationStartDinner();
        }

        if(mFrameworkState.getStartingPreprandialType() == C.STARTING_TYPE_GLOBAL){
            start.addDot(mFrameworkState.getStartingPreprandialModificationDot());
        } else {
            start.addDot(mFrameworkState.getStartingPreprandialModificationDinnerDot());
        }
        return start;
    }
    public ModificationStart getSecondaryPreprandialStartForBreakfast() {
        ModificationStart start;
        if(secondaryMetabolicRhythm.getStartingPreprandialType() == C.STARTING_TYPE_GLOBAL){
            start = secondaryMetabolicRhythm.getPreprandialModificationStart();
        } else {
            start = secondaryMetabolicRhythm.getPreprandialModificationStartBreakfast();
        }

        float daysPassed = new Instant()
                .setTimeToTheStartOfTheDay()
                .decreaseOneDay()
                .getDaysPassedFromInstant(new Instant(mFrameworkState.getActivatedMetabolicRhythmStartDate()).setTimeToTheStartOfTheDay());

        start.addDot(new ModificationStartDot(-1, preprandialBreakfast.getModification(daysPassed)));
        return start;
    }

    public ModificationStart getSecondaryPreprandialStartForLunch() {
        ModificationStart start;
        if(secondaryMetabolicRhythm.getStartingPreprandialType() == C.STARTING_TYPE_GLOBAL){
            start = secondaryMetabolicRhythm.getPreprandialModificationStart();
        } else {
            start = secondaryMetabolicRhythm.getPreprandialModificationStartLunch();
        }

        float daysPassed = new Instant()
                .setTimeToTheStartOfTheDay()
                .decreaseOneDay()
                .getDaysPassedFromInstant(new Instant(mFrameworkState.getActivatedMetabolicRhythmStartDate()).setTimeToTheStartOfTheDay());

        start.addDot(new ModificationStartDot(-1, preprandialLunch.getModification(daysPassed)));
        return start;
    }

    public ModificationStart getSecondaryPreprandialStartForDinner() {
        ModificationStart start;
        if(secondaryMetabolicRhythm.getStartingPreprandialType() == C.STARTING_TYPE_GLOBAL){
            start = secondaryMetabolicRhythm.getPreprandialModificationStart();
        } else {
            start = secondaryMetabolicRhythm.getPreprandialModificationStartDinner();
        }

        float daysPassed = new Instant()
                .setTimeToTheStartOfTheDay()
                .decreaseOneDay()
                .getDaysPassedFromInstant(new Instant(mFrameworkState.getActivatedMetabolicRhythmStartDate()).setTimeToTheStartOfTheDay());

        start.addDot(new ModificationStartDot(-1, preprandialDinner.getModification(daysPassed)));
        return start;
    }

    /*
     BASAL
     */
    public ModificationStart getEnabledBasalStartForBreakfast() {
        ModificationStart start;
        start = enabledMetabolicRhythm.getBasalModificationStartBreakfast();
        start.addDot(mFrameworkState.getStartingBasalModificationBreakfastDot());
        return start;
    }

    public ModificationStart getEnabledBasalStartForLunch() {
        ModificationStart start;
        start = enabledMetabolicRhythm.getBasalModificationStartLunch();
        start.addDot(mFrameworkState.getStartingBasalModificationLunchDot());
        return start;
    }

    public ModificationStart getEnabledBasalStartForDinner() {
        ModificationStart start;
        start = enabledMetabolicRhythm.getBasalModificationStartDinner();
        start.addDot(mFrameworkState.getStartingBasalModificationDinnerDot());
        return start;
    }


    public ModificationStart getSecondaryBasalStartForBreakfast() {
        ModificationStart start;
        start = secondaryMetabolicRhythm.getBasalModificationStartBreakfast();

        float daysPassed = new Instant()
                .setTimeToTheStartOfTheDay()
                .decreaseOneDay()
                .getDaysPassedFromInstant(new Instant(mFrameworkState.getActivatedMetabolicRhythmStartDate()).setTimeToTheStartOfTheDay());

        start.addDot(new ModificationStartDot(-1, basalBreakfast.getModification(daysPassed)));

        return start;
    }

    public ModificationStart getSecondaryBasalStartForLunch() {
        ModificationStart start;
        start = secondaryMetabolicRhythm.getBasalModificationStartLunch();

        float daysPassed = new Instant()
                .setTimeToTheStartOfTheDay()
                .decreaseOneDay()
                .getDaysPassedFromInstant(new Instant(mFrameworkState.getActivatedMetabolicRhythmStartDate()).setTimeToTheStartOfTheDay());

        start.addDot(new ModificationStartDot(-1, basalLunch.getModification(daysPassed)));

        return start;
    }

    public ModificationStart getSecondaryBasalStartForDinner() {
        ModificationStart start;
        start = secondaryMetabolicRhythm.getBasalModificationStartDinner();

        float daysPassed = new Instant()
                .setTimeToTheStartOfTheDay()
                .decreaseOneDay()
                .getDaysPassedFromInstant(new Instant(mFrameworkState.getActivatedMetabolicRhythmStartDate()).setTimeToTheStartOfTheDay());

        start.addDot(new ModificationStartDot(-1, basalDinner.getModification(daysPassed)));

        return start;
    }


    /*
     * PRIVATE METHODS
     */

    /*
     * Este método sólo ha de ejecutarse en el caso de que el Ritmo Metabólico conectado sea el activado
     * Checkea Ritmos Metabólicos planificados y los va activando y desactivando en el caso que fuera
     * necesario.
     */
    private void pUpdateLastCheck(){
        Instant pointerStart, pointerEnd;

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);

        Instant lastCheck = mFrameworkState.getLastCheckInstant();
        Instant now = new Instant();
        now.setTimeToTheStartOfTheDay();

        boolean refreshed = false;
        lastCheck.setTimeToTheStartOfTheDay();


        while(!refreshed) {
            pointerEnd = null;

            /*
             * Actualizamos los punteros start y end de la siguiente forma:
             * 1.- Si aplica un Ritmo esclavo, el start y end se situan en el startDate u endDate.
             * 2.- Si aplica el Ritmo Maestro, el start va para el startDate y el end para el siguiente start - 1.
             */
            switch (enabledMetabolicRhythm.getId()) {
                case 1:
                    // marcamos como mMaster al enabledMetabolicRhythm
                    mMaster = (MetabolicRhythmMaster) enabledMetabolicRhythm;
                    loadParameters(mContext);

                    pointerStart = new Instant(mFrameworkState.getActivatedMetabolicRhythmStartDate());
                    pointerStart.setTimeToTheStartOfTheDay();
                    if (plannedSortedMetabolicRhythms.size() > 0) {
                        pointerEnd = new Instant(plannedSortedMetabolicRhythms.get(0).getStartDate());
                        pointerEnd.decreaseOneDay();
                        pointerEnd.setTimeToTheEndOfTheDay();
                    }

                    break;
                default:
                    // tenemos que cargar el master metabolic rhythm
                    if (mMaster == null) {
                        mMaster = (MetabolicRhythmMaster) mConfigDatabase.getMetabolicRhythmById(1);
                        loadParameters(mContext);
                    }


                    pointerStart = new Instant(mFrameworkState.getActivatedMetabolicRhythmStartDate());
                    pointerStart.setTimeToTheStartOfTheDay();


                    pointerEnd = ((MetabolicRhythmSlave) enabledMetabolicRhythm).getEndDate();
                    if((pointerEnd != null && pointerEnd.getDaysPassedFromNow() > 0) || (enabledMetabolicRhythm.getId() == 1 && plannedSortedMetabolicRhythms.size() == 0)){
                        pointerEnd = null;
                    }
            }

            /*
             * Si el pointerEnd es null, significa que como activo está el Ritmo maestro, y además,
             * no hay nada planificado para más adelante, o bien, que está sleeccionado un ritmo
             * esclavo, pero su fecha de fin aún no llegó, por lo que salimos de este bucle.
             */

            if (pointerEnd != null) {
                pointerEnd = new Instant(pointerEnd);

                pointerStart.setTimeToTheStartOfTheDay();
                pointerEnd.setTimeToTheEndOfTheDay();


                /*
                 * Si el final es mayor que cero, significa que el ritmo aún no ha expirado.
                 * Por lo que salimos del bucle.
                 */
                if (pointerEnd.getDaysPassedFromNow() >= 0) {
                    refreshed = true;
                } else {
                    float daysPassed = pointerEnd.getDaysPassedFromInstant(pointerStart);


                    float m;
                    if(enabledMetabolicRhythm.getStartingPreprandialType() == C.STARTING_TYPE_SPECIFIC){
                        m = preprandialBreakfast.getModification(daysPassed);
                        mFrameworkState.setStartingPreprandialModificationBreakfast(m);

                        m = preprandialLunch.getModification(daysPassed);
                        mFrameworkState.setStartingPreprandialModificationLunch(m);

                        m = preprandialDinner.getModification(daysPassed);
                        mFrameworkState.setStartingPreprandialModificationDinner(m);

                        mFrameworkState.setStartingPreprandialType(C.STARTING_TYPE_SPECIFIC);


                    } else {
                        if(mFrameworkState.getStartingPreprandialType() == C.STARTING_TYPE_GLOBAL) {
                            // being global, we catch one of three, for example, breakfast
                            m = preprandialBreakfast.getModification(daysPassed);
                            mFrameworkState.setStartingPreprandialModification(m);

                            mFrameworkState.setStartingPreprandialType(C.STARTING_TYPE_GLOBAL);

                        } else {
                            m = preprandialBreakfast.getModification(daysPassed);
                            mFrameworkState.setStartingPreprandialModificationBreakfast(m);

                            m = preprandialLunch.getModification(daysPassed);
                            mFrameworkState.setStartingPreprandialModificationLunch(m);

                            m = preprandialDinner.getModification(daysPassed);
                            mFrameworkState.setStartingPreprandialModificationDinner(m);

                            mFrameworkState.setStartingPreprandialType(C.STARTING_TYPE_SPECIFIC);
                        }
                    }

                    m = basalBreakfast.getModification(daysPassed);
                    mFrameworkState.setStartingBasalModificationBreakfast(m);

                    m = basalLunch.getModification(daysPassed);
                    mFrameworkState.setStartingBasalModificationLunch(m);

                    m = basalDinner.getModification(daysPassed);
                    mFrameworkState.setStartingBasalModificationDinner(m);


                    mFrameworkState.save();

                    /*
                     * Lo que hay aquí son dos posibilidades, que esté seleccionado el maestro y
                     * pueda aplicar un esclavo, o que esté seleccionado un esclavo y pueda aplicar otro.
                     */
                    switch(enabledMetabolicRhythm.getId()){
                        case 1:
                            /*
                             * Está seleccionado el maestro y hay que seleccionar el siguiente, si aplica.
                             */
                            enabledMetabolicRhythm.setState(C.METABOLIC_RHYTHM_STATE_DISABLED);

                            enabledMetabolicRhythm.save(mConfigDatabase);


                            break;

                        default:
                            /*
                             * Está seleccionado un esclavo y hay que seleccionar el siguiente, si aplica,
                             * si no, el maestro.
                             */
                            enabledMetabolicRhythm.setState(C.METABOLIC_RHYTHM_STATE_DISABLED);

                            // Antes hay que añadir el startdate y enddate como rango al label que corresponda
                            Label.saveCafydiaAutomaticLabel(
                                    mContext,
                                    enabledMetabolicRhythm.getName(),
                                    enabledMetabolicRhythm.getStartDate(),
                                    ((MetabolicRhythmSlave) enabledMetabolicRhythm).getEndDate()
                            );


                            enabledMetabolicRhythm.setStartDate(null);
                            ((MetabolicRhythmSlave) enabledMetabolicRhythm).setEndDate(null);

                            enabledMetabolicRhythm.save(mConfigDatabase);
                    }


                    pointerEnd.increaseOneDay();

                    /*
                     * Aquí seleccionamos el próximo
                     */
                    if(plannedSortedMetabolicRhythms.size() > 0) {
                        if (!plannedSortedMetabolicRhythms.get(0).appliesInInstant(pointerEnd)) {

                            enabledMetabolicRhythm = mMaster;
                        } else {

                            enabledMetabolicRhythm = mConfigDatabase.getMetabolicRhythmById(plannedSortedMetabolicRhythms.get(0).getId());


                            plannedSortedMetabolicRhythms.remove(0);
                        }


                    } else {
                        enabledMetabolicRhythm = mMaster;
                    }
                    enabledMetabolicRhythm.setState(C.METABOLIC_RHYTHM_STATE_ENABLED);
                    mFrameworkState.setActivatedMetabolicRhythmId(enabledMetabolicRhythm.getId());
                    mFrameworkState.setActivatedMetabolicRhythmStartDate(new Instant(pointerEnd).setTimeToTheStartOfTheDay());

                    // anotacion automática
                    if(sp.getBoolean("pref_automatic_annotations_metabolic_rhythm", true)) {
                        Annotation.saveCafydiaAutomaticAnnotation(mContext, mContext.getString(R.string.automatic_annotation_metabolic_rhythm_activation) + ": " + enabledMetabolicRhythm.getName());
                    }

                    reassembleStarts();
                }
            } else {
                refreshed = true;
            }

        }

        mFrameworkState.setLastCheckInstant(new Instant());

        mFrameworkState.setActivatedMetabolicRhythmId(enabledMetabolicRhythm.getId());

        mFrameworkState.save();

        if(originalEnabledMetabolicRhythm == null || ! originalEnabledMetabolicRhythm.isEqual(enabledMetabolicRhythm)){
            enabledMetabolicRhythm.save(mConfigDatabase);
            originalEnabledMetabolicRhythm = enabledMetabolicRhythm.duplicate();
        }

    }

    private void pActivateMetabolicRhythm(MetabolicRhythm m){
        m.setState(C.METABOLIC_RHYTHM_STATE_ENABLED);
        pConnectMetabolicRhythm(m);

        mFrameworkState.setActivatedMetabolicRhythmId(m.getId());
        mFrameworkState.setActivatedMetabolicRhythmStartDate(new Instant());

        enabledMetabolicRhythm.save(mConfigDatabase);
        originalEnabledMetabolicRhythm = enabledMetabolicRhythm.duplicate();
        mFrameworkState.save();

        // anotacion automática

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);

        if(sp.getBoolean("pref_automatic_annotations_metabolic_rhythm", true)) {
            Annotation.saveCafydiaAutomaticAnnotation(mContext, mContext.getString(R.string.automatic_annotation_metabolic_rhythm_activation) + ": " + enabledMetabolicRhythm.getName());
        }


    }

    private void pUpdateMetabolicFrameworkState(){
        Instant start = new Instant(mFrameworkState.getActivatedMetabolicRhythmStartDate()).setTimeToTheStartOfTheDay();
        Instant yesterday = new Instant().decreaseOneDay().setTimeToTheStartOfTheDay();

        float daysPassed = yesterday.getDaysPassedFromInstant(start);

        float m;

        if(enabledMetabolicRhythm.getStartingPreprandialType().equals(C.STARTING_TYPE_SPECIFIC) ||
                mFrameworkState.getStartingPreprandialType() == C.STARTING_TYPE_SPECIFIC){

            float m1 = preprandialBreakfast.getModification(daysPassed);
            mFrameworkState.setStartingPreprandialModificationBreakfast(m1);

            float m2 = preprandialLunch.getModification(daysPassed);
            mFrameworkState.setStartingPreprandialModificationLunch(m2);

            float m3 = preprandialDinner.getModification(daysPassed);
            mFrameworkState.setStartingPreprandialModificationDinner(m3);

            if(m1 == m2 && m1 == m3) {
                mFrameworkState.setStartingPreprandialModification(m1);
                mFrameworkState.setStartingPreprandialType(C.STARTING_TYPE_GLOBAL);
            } else {
                mFrameworkState.setStartingPreprandialType(C.STARTING_TYPE_SPECIFIC);
            }
        } else {

            m = preprandialBreakfast.getModification(daysPassed);
            mFrameworkState.setStartingPreprandialModification(m);
            mFrameworkState.setStartingPreprandialType(C.STARTING_TYPE_GLOBAL);

        }

        m = basalBreakfast.getModification(daysPassed);
        mFrameworkState.setStartingBasalModificationBreakfast(m);

        m = basalLunch.getModification(daysPassed);
        mFrameworkState.setStartingBasalModificationLunch(m);

        m = basalDinner.getModification(daysPassed);
        mFrameworkState.setStartingBasalModificationDinner(m);

    }

    private void pConnectMetabolicRhythm(MetabolicRhythm m){
        enabledMetabolicRhythm = m;
        reassembleStarts();
    }

}

package org.cafydia4.android.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.cafydia4.android.R;
import org.cafydia4.android.activities.ActivityBuildCharts;
import org.cafydia4.android.chartobjects.ChartPage;
import org.cafydia4.android.chartobjects.ChartPageElement;
import org.cafydia4.android.chartobjects.DataCollectionCriteria;
import org.cafydia4.android.chartobjects.DataCollectionCriteriaInstant;
import org.cafydia4.android.chartobjects.DataCollectionLabelRule;
import org.cafydia4.android.chartobjects.GlucoseTestsCrossedMeals;
import org.cafydia4.android.chartobjects.Label;
import org.cafydia4.android.chartobjects.StatisticalObject;
import org.cafydia4.android.core.Annotation;
import org.cafydia4.android.core.GlucoseTest;
import org.cafydia4.android.core.HbA1c;
import org.cafydia4.android.core.Instant;
import org.cafydia4.android.datadatabase.DataDatabase;
import org.cafydia4.android.dialogfragments.DialogCriteriaEditor;
import org.cafydia4.android.dialogfragments.DialogStatisticalObject;
import org.cafydia4.android.genericdialogfragments.DialogConfirmation;
import org.cafydia4.android.genericdialogfragments.DialogGetTextOrNumber;
import org.cafydia4.android.tutorial.HelpFragmentBundle;
import org.cafydia4.android.util.C;
import org.cafydia4.android.util.HbA1cHelper;
import org.cafydia4.android.util.MyRound;
import org.cafydia4.android.util.MyToast;
import org.cafydia4.android.util.ViewUtil;
import org.cafydia4.android.views.ChartHbA1c;
import org.cafydia4.android.views.ChartPageElementChartView;
import org.cafydia4.android.views.ChartPageElementTextView;
import org.cafydia4.android.views.HbA1cImageView;

import java.util.ArrayList;

/**
 * Created by user on 26/11/14.
 */
public class ChartPageFragment extends Fragment implements
        DialogGetTextOrNumber.OnTextIntroducedListener,
        DialogConfirmation.OnConfirmListener {

    private static final boolean DEBUG = false;
    private static final String DEBUG_TAG = "ChartPageFragment";

    private static DataDatabase db;

    private ChartPage mChartPage;

    private static String titles[] = new String[16];

    private LinearLayout rootLinearLayout;

    private ChartHbA1c chartHbA1c;
    private HbA1cImageView mFace;

    private Float xLow = null, xHigh = null;

    private View currentContextualView = null;

    private ArrayList<Annotation> mCachedAnnotations = new ArrayList<>();

    private boolean mRecreate = false;

    private boolean mFragmentActive = false;

    private ArrayList<Integer> annotationIdsEditedOrDeletedPendingReview = new ArrayList<>();

    private HelpFragmentBundle mHelpFragmentBundle;

    public void addAnnotationIdPendingToReview(int id) {
        annotationIdsEditedOrDeletedPendingReview.add(id);
    }

    public static ChartPageFragment newInstance(ChartPage page){
        ChartPageFragment fragment = new ChartPageFragment();

        Bundle args = new Bundle();
        args.putInt("page_id", page.getId());
        fragment.setArguments(args);

        titles[page.getPagerScopePosition()] = page.getTitle();

        debugMessage("Instanciando la página: " + page.getTitle());

        return fragment;
    }

    @Override
    public void onSaveInstanceState(Bundle savedState) {
        super.onSaveInstanceState(savedState);
        savedState.putFloat("xlow", xLow);
        savedState.putFloat("xhigh", xHigh);
    }

    @Override
    public void onPause(){
        super.onPause();

        mFragmentActive = false;
    }

    @Override
    public void onResume(){
        super.onResume();

        mFragmentActive = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mHelpFragmentBundle = new HelpFragmentBundle(getActivity(), R.id.help);

        Bundle a = getArguments();

        mFragmentActive = true;

        debugMessage("Salta el método onCreateView");

        if(savedInstanceState != null) {
            xLow = a.getFloat("xlow");
            xHigh = a.getFloat("xhigh");
        }

        int id = a.getInt("page_id");
        if(id == 0) {
            mChartPage = new ChartPage(C.PAGER_SCOPE_HBA1C_POSITION, getString(R.string.activity_charts_0_tab_hba1c), null);
        } else {
            mChartPage = getDatabase().getChartPageById(a.getInt("page_id"));
        }

        View layout;

        switch (getPage().getPagerScopePosition()){
            case C.PAGER_SCOPE_HBA1C_POSITION:
                debugMessage("Página HbA1c");

                layout = inflater.inflate(R.layout.fragment_chart_hba1c, container, false);
                chartHbA1c = (ChartHbA1c) layout.findViewById(R.id.chartHbA1c);

                mFace = (HbA1cImageView) layout.findViewById(R.id.imageViewFace);
                mFace.little();

                HbA1cHelper helper = new HbA1cHelper(getActivity());
                mFace.setHbA1cPercentage(helper.getHbA1cPercentage());

                TextView tvMmol = (TextView) layout.findViewById(R.id.tvMmol);
                TextView tvPercentage = (TextView) layout.findViewById(R.id.tvPercentage);

                tvMmol.setText(MyRound.round(helper.getHbA1cMmolMol()).toString());
                tvPercentage.setText(MyRound.round(helper.getHbA1cPercentage()).toString());

                new LoadHbA1cData().execute();

                break;

            default:
                debugMessage("Página estándar");

                layout = inflater.inflate(R.layout.fragment_chart_default, container, false);
                rootLinearLayout = (LinearLayout) layout.findViewById(R.id.rootLinearLayout);

                reloadChartPageElements();

        }

        return layout;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser){
        super.setUserVisibleHint(false);

        if(this.isVisible() && !isVisibleToUser){

        }
    }


    // para recalcular el xlow y xhigh. Util para recargar después annotations, etc.
    // se puede usar cuando se elimina un chart element o cuando se cambia el criterio en la toma de datos.
    private void recalculateXLowXhigh(){
        if(getPage().getPagerScopePosition().equals(C.PAGER_SCOPE_HBA1C_POSITION)) {
            xLow = -120f;
            xHigh = 0f;
        } else {
            xLow = 0f;
            xHigh = 0f;
            searchChartViewsForRecalculateXLowXHigh(getView());
        }
    }

    private void searchChartViewsForRecalculateXLowXHigh(View view){
        if(view instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) view;
            for (int a = 0; a < vg.getChildCount(); a++){
                View v = vg.getChildAt(a);
                if(v instanceof ViewGroup) {
                    searchChartViewsForRecalculateXLowXHigh(v);
                }
                else if(v instanceof ChartPageElementChartView) {
                    ChartPageElementChartView cv = (ChartPageElementChartView) v;
                    if(cv.getChartPageElement().getType().equals(C.CHART_PAGE_ELEMENT_TYPE_GLUCOSE_CROSSED_DATES)) {
                        if (xLow == 0f || xLow > cv.getXLow()) {
                            xLow = cv.getXLow();
                        }

                        if (xHigh == 0f || xHigh < cv.getXHigh()) {
                            xHigh = cv.getXHigh();
                        }
                    }
                }
            }
        }
    }

    private void addElementToLayout(ChartPageElement element, long delay){
        if(mFragmentActive) {

            final ChartPageElementChartView chartView;
            ChartPageElementTextView myText;

            switch (element.getType()) {

                case C.CHART_PAGE_ELEMENT_TYPE_TEXT:
                    myText = (ChartPageElementTextView) getActivity().getLayoutInflater().inflate(R.layout.chart_page_element_text, null);
                    myText.setChartPageElement(element);

                    registerForContextMenu(myText);

                    rootLinearLayout.addView(myText);

                    break;

                case C.CHART_PAGE_ELEMENT_TYPE_GLUCOSE_CROSSED_HOURS:
                case C.CHART_PAGE_ELEMENT_TYPE_GLUCOSE_CROSSED_WEEK_DAYS:
                case C.CHART_PAGE_ELEMENT_TYPE_GLUCOSE_CROSSED_DATES:

                    chartView = (ChartPageElementChartView) getActivity().getLayoutInflater().inflate(R.layout.chart_page_element_chart, null);
                    chartView.setChartPageElement(element);

                    final DataCollectionCriteria cc = element.getCriteria();

                    chartView.setGlobalCriteria(getPage().getCriteria(), getDatabase());
                    chartView.setSpecificCriteria(cc, getDatabase());

                    registerForContextMenu(chartView);
                    chartView.startLoading();

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            new LoadGlucoseTestByCriteria().execute(mixCriteria(getPage().getCriteria(), cc), chartView);

                        }
                    }, delay);

                    rootLinearLayout.addView(chartView);

                    break;

                case C.CHART_PAGE_ELEMENT_TYPE_GLUCOSE_CROSSED_CARBOHYDRATE_INTAKE:
                case C.CHART_PAGE_ELEMENT_TYPE_GLUCOSE_CROSSED_MEAL_HOUR:

                    chartView = (ChartPageElementChartView) getActivity().getLayoutInflater().inflate(R.layout.chart_page_element_chart, null);
                    chartView.setChartPageElement(element);

                    final DataCollectionCriteria ccc = element.getCriteria();

                    chartView.setGlobalCriteria(getPage().getCriteria(), getDatabase());
                    chartView.setSpecificCriteria(ccc, getDatabase());

                    registerForContextMenu(chartView);

                    chartView.startLoading();

                    // this call is the only thing that make this view change regarding to the others
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            new LoadGlucoseTestCrossedMealsByCriteria().execute(mixCriteria(getPage().getCriteria(), ccc), chartView);
                        }
                    }, delay);

                    rootLinearLayout.addView(chartView);

                    break;
            }
        }
    }

    public void addElement(ChartPageElement element){
        addElementToLayout(element, 0);
    }

    public void reloadChartPageElements(){
        if(getPage().getCriteria() != null)
            getPage().setCriteria(getDatabase().getDataCollectionCriteriaById(getPage().getCriteria().getId()));


        debugMessage("reloadChartPageElements");

        rootLinearLayout.removeAllViewsInLayout();

        xLow = 0f;
        xHigh = 0f;

        ArrayList<ChartPageElement> chartPageElements = getDatabase().getChartPageElementsByChartPageId(getPage().getId());
        long delay = 0;
        for(ChartPageElement chartPageElement : chartPageElements){
            if(chartPageElement.getType().equals(C.CHART_PAGE_ELEMENT_TYPE_GLUCOSE_CROSSED_HOURS) ||
                    chartPageElement.getType().equals(C.CHART_PAGE_ELEMENT_TYPE_GLUCOSE_CROSSED_DATES) ||
                    chartPageElement.getType().equals(C.CHART_PAGE_ELEMENT_TYPE_GLUCOSE_CROSSED_CARBOHYDRATE_INTAKE) ||
                    chartPageElement.getType().equals(C.CHART_PAGE_ELEMENT_TYPE_GLUCOSE_CROSSED_WEEK_DAYS) ||
                    chartPageElement.getType().equals(C.CHART_PAGE_ELEMENT_TYPE_GLUCOSE_CROSSED_MEAL_HOUR)) {
                delay += 750;
            }
            addElementToLayout(chartPageElement, delay);
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                recalculateXLowXHighAndGetAnnotations();
            }
        }, delay);

        rootLinearLayout.invalidate();
    }



    ////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////

    //
    //

    //
    //

    // FOR ANNOTATIONS

    //
    //

    //
    //

    /**
     *
     * Son las anotaciones que serán mostradas en el ChartHbA1c o se asignará recursivamente a todos los ChartViews de la página.
     */
    public void setCachedAnnotationsInViews(){
        if (chartHbA1c != null) {
            chartHbA1c.setAnnotations(mCachedAnnotations);
            chartHbA1c.refresh();

        } else {
            assignAnnotationsRecursively(getView());
        }
    }

    /**
     * @param v ViewGroup desde el que comenzar a asignar anotaciones.
     */
    private void assignAnnotationsRecursively(View v) {
        if(v instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) v;
            for (int a = 0; a < vg.getChildCount(); a++) {
                View view = vg.getChildAt(a);

                if (view instanceof ChartPageElementChartView) {
                    ChartPageElementChartView cv = (ChartPageElementChartView) view;

                    if (cv.getChartPageElement().getType().equals(C.CHART_PAGE_ELEMENT_TYPE_GLUCOSE_CROSSED_DATES)) {

                        cv.setAnnotations(mCachedAnnotations);
                        cv.redraw();

                    }
                }

                else if(view instanceof ViewGroup) {
                    assignAnnotationsRecursively(view);
                }

            }
        }
    }

    /**
     * Método que será llamado cuando la página esté completamente cargada
     * Se recalcula el xlow y xhigh y se consulta a la actividad padre por las anotaciones
     * De esta forma, en la actividad padre ya tendrá las anotaciones.
     * Se ejecuta tras 500 milisegundos después de ser llamado
     */
    private void recalculateXLowXHighAndGetAnnotations (){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(getParent() != null) {
                    recalculateXLowXhigh();
                    mCachedAnnotations = getParent().getAnnotationsNumberedByXLowXHigh(getPage().getPagerScopePosition(), xLow, xHigh);
                    setCachedAnnotationsInViews();
                }
            }
        }, 500);
    }

    public ArrayList<Annotation> updateCachedAnnotationsAndGetIt(){
        if(getParent() != null && getPage() != null && xLow != null && xHigh != null) {
            mCachedAnnotations = getParent().getAnnotationsNumberedByXLowXHigh(getPage().getPagerScopePosition(), xLow, xHigh);
            return mCachedAnnotations;
        }
        return null;
    }

    private void refreshChartViewFromDatabase(ChartPageElementChartView cv) {
        refreshChartViewFromDatabase(cv, 0);
    }

    private void refreshChartViewFromDatabase(final ChartPageElementChartView cv, final long delay) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (getParent() != null) {

                    ChartPageElement e = getDatabase().getChartPageElementById(cv.getChartPageElement().getId());

                    cv.setChartPageElement(e);
                    cv.setSpecificCriteria(e.getCriteria(), getDatabase());
                    cv.startLoading();

                    switch (cv.getChartPageElement().getType()) {
                        case C.CHART_PAGE_ELEMENT_TYPE_GLUCOSE_CROSSED_HOURS:
                        case C.CHART_PAGE_ELEMENT_TYPE_GLUCOSE_CROSSED_WEEK_DAYS:
                        case C.CHART_PAGE_ELEMENT_TYPE_GLUCOSE_CROSSED_DATES:
                            new LoadGlucoseTestByCriteria().execute(mixCriteria(getPage().getCriteria(), e.getCriteria()), cv);
                            break;

                        case C.CHART_PAGE_ELEMENT_TYPE_GLUCOSE_CROSSED_CARBOHYDRATE_INTAKE:
                        case C.CHART_PAGE_ELEMENT_TYPE_GLUCOSE_CROSSED_MEAL_HOUR:
                            new LoadGlucoseTestCrossedMealsByCriteria().execute(mixCriteria(getPage().getCriteria(), e.getCriteria()), cv);
                            break;
                    }
                }
            }
        }, delay);
    }

    public void checkChildrenChartViewsIfItsAffectedToAnnotationsDeletedOrChanged(){
        if(rootLinearLayout != null) {
            boolean changes = false;

            for (int n = 0; n < rootLinearLayout.getChildCount(); n++) {
                View v = rootLinearLayout.getChildAt(n);

                if (v instanceof ChartPageElementChartView) {
                    final ChartPageElementChartView cv = (ChartPageElementChartView) v;

                    final DataCollectionCriteria criteria = cv.getChartPageElement().getCriteria();

                    if (criteria.getSinceInstant() == null || (criteria.getSinceInstant() instanceof Annotation && annotationIdsEditedOrDeletedPendingReview.contains(criteria.getSince().getData().intValue()))) {
                        if (criteria.getSinceInstant() == null) {
                            criteria.setSince(new DataCollectionCriteriaInstant(getActivity(), getDatabase()));
                            criteria.save(getDatabase());
                        }

                        // todo hay que chequear si esto soluciona el problema del cambio en el criterio
                        // todo en el caso de eliminarse la anotación de la que depende, no se refresca en el chartview
                        changes = true;

                        refreshChartViewFromDatabase(cv, 300);

                    } else if (criteria.getUntilInstant() == null || (criteria.getUntilInstant() instanceof Annotation && annotationIdsEditedOrDeletedPendingReview.contains(criteria.getUntil().getData().intValue()))) {
                        if(criteria.getUntilInstant() == null) {
                            criteria.setUntil(new DataCollectionCriteriaInstant(getActivity(), getDatabase()));
                            criteria.save(getDatabase());
                        }

                        // todo hay que chequear si esto soluciona el problema del cambio en el criterio
                        // todo en el caso de eliminarse la anotación de la que depende, no se refresca en el chartview
                        changes = true;
                        refreshChartViewFromDatabase(cv, 300);
                    }
                }
            }

            annotationIdsEditedOrDeletedPendingReview.clear();

            if (changes) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(getParent() != null)
                            recalculateXLowXHighAndGetAnnotations();
                    }
                }, 400);

            }
        } else {
            recalculateXLowXHighAndGetAnnotations();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////

    //
    //

    //
    //

    // FOR LABELS

    //
    //

    //
    //


    //////////////////////////////
    // to set to charts if needed
    //////////////////////////////
    public void setLabels(ArrayList<Label> labels){

        if(getPage().getPagerScopePosition().equals(C.PAGER_SCOPE_HBA1C_POSITION)) {
            chartHbA1c.setLabels(labels);

        } else {
            // Primero tendremos que ver si el label es usado de manera global en el criterio de la página.
            // De ser así, se recargará entera.
            if(!updateChartPageFragmentIfNeededAboutLabels(labels)){

                // Si no, repasamos cada uno de los children, para ver si de manera específica se utiliza en algún criterio.
                updateChartViewsInFragmentIfNeededAboutLabels(labels);
            }
        }
    }

    private boolean updateChartPageFragmentIfNeededAboutLabels(ArrayList<Label> newLabels){
        if(isAnyLabelChangedFromChartPageCriteria(newLabels)){

            // reload chartpage elements
            reloadChartPageElements();
            return true;
        }
        return false;
    }

    private void updateChartViewsInFragmentIfNeededAboutLabels(ArrayList<Label> newLabels){
        if(chartHbA1c != null)
            chartHbA1c.setLabels(newLabels);

        if(rootLinearLayout != null) {
            for (int n = 0; n < rootLinearLayout.getChildCount(); n++) {
                View v = rootLinearLayout.getChildAt(n);

                if (v instanceof ChartPageElementChartView) {
                    ChartPageElementChartView cv = (ChartPageElementChartView) v;

                    if (isAnyLabelChangedFromChartPageChildrenCriteria(newLabels, cv)) {
                        cv.setLabels(newLabels);
                        cv.startLoading();
                        refreshChartViewFromDatabase(cv);
                    }
                }
            }
        }
    }

    private boolean isAnyLabelChangedFromChartPageCriteria(ArrayList<Label> newLabels){
        return isAnyLabelChangedFromCriteria(newLabels, getPage().getCriteria());
    }

    private boolean isAnyLabelChangedFromChartPageChildrenCriteria(ArrayList<Label> newLabels, ChartPageElementChartView cv) {
        return isAnyLabelChangedFromCriteria(newLabels, cv.getChartPageElement().getCriteria());
    }

    private boolean isAnyLabelChangedFromCriteria(ArrayList<Label> newLabels, DataCollectionCriteria criteria) {
        if(criteria == null)
            return false;

        for(Label l : newLabels){
            for(DataCollectionLabelRule rule : criteria.getLabelRules()){

                // todo hay que mirar si el método equals tira bien
                if(l.getId().equals(rule.getLabel().getId()) && !l.equals(rule.getLabel()))
                    return true;
            }
        }
        return false;
    }
    ////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////


    /**
     *
     * @return retorna true si fue recreado.
     */
    public boolean recreateIfNeeded(){
        if(mRecreate) {
            reloadChartPageElements();
            mRecreate = false;
            return true;
        }
        return false;
    }

    public void setRecreate(boolean recreate) {
        mRecreate = recreate;
    }




    public ChartPage getPage(){
        if(mChartPage == null){
            mChartPage = getDatabase().getChartPageById(getArguments().getInt("page_id"));
        }

        // si es null es que no existe en la db, suponemos que es el primer fragment.
        if(mChartPage == null && getArguments().getInt("page_id") == 0) {
            mChartPage = new ChartPage(C.PAGER_SCOPE_HBA1C_POSITION, getString(R.string.activity_charts_0_tab_hba1c), null);
        }
        return mChartPage;
    }
    public void setPage(ChartPage page){
        mChartPage = page;
    }

    public void refreshPageFromDatabase(){
        mChartPage = getDatabase().getChartPageById(getArguments().getInt("page_id"));

        // si es null es que no existe en la db, suponemos que es el primer fragment.
        if(mChartPage == null && getArguments().getInt("page_id") == 0) {
            mChartPage = new ChartPage(C.PAGER_SCOPE_HBA1C_POSITION, getString(R.string.activity_charts_0_tab_hba1c), null);
        }
    }

    public static String getTitle(int pos) {
        return titles[pos];
    }

    public static void setTitle(int pos, String title) {
        titles[pos] = title;
    }




    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////// Contextual menu /////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public void onContextMenuClosed(Menu menu) {
        currentContextualView = null;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();

        if(currentContextualView == null && (v instanceof ChartPageElementTextView || v instanceof ChartPageElementChartView)) {
            currentContextualView = v;
        }

        if(v instanceof ChartPageElementTextView) {
            inflater.inflate(R.menu.fragment_contextual_chart_page_element_text, menu);
        }

        else if(v instanceof ChartPageElementChartView) {
            inflater.inflate(R.menu.fragment_contextual_chart_page_element_chart, menu);
        }

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        if(currentContextualView instanceof ChartPageElementTextView) {
            ChartPageElementTextView text = (ChartPageElementTextView) currentContextualView;
            currentContextualView = null;

            switch (item.getItemId()){
                case R.id.edit_text:
                    String tag = "modify_text";
                    DialogGetTextOrNumber
                            .newInstance(
                                    tag,
                                    getText(R.string.dialog_new_chart_element_text_title).toString(),
                                    text,
                                    this)
                            .show(getActivity().getFragmentManager(), null);

                    return true;

                case R.id.delete_text:
                    DialogConfirmation
                            .newInstance(
                                    "delete_text_element",
                                    ChartPageFragment.this,
                                    R.string.dialog_confirmation_delete_chart_page_element_title,
                                    R.string.dialog_confirmation_delete_chart_page_element_message,
                                    text
                            )
                            .show(getActivity().getFragmentManager(), null);

                    return true;
            }
        }
        else if(currentContextualView instanceof ChartPageElementChartView) {
            final ChartPageElementChartView chart = (ChartPageElementChartView) currentContextualView;
            currentContextualView = null;

            switch (item.getItemId()){

                case R.id.data_criteria:
                    ArrayList<Annotation> annotations = getDatabase().getAnnotationsByOrderNumberScopeXLowAndXHigh(getPage().getPagerScopePosition(), -120f, 0f);

                    final DataCollectionCriteria globalCriteria = getPage().getCriteria();
                    final DataCollectionCriteria elementCriteria = chart.getChartPageElement().getCriteria();

                    DialogCriteriaEditor.newInstance(
                            annotations,
                            globalCriteria,
                            elementCriteria,
                            chart,
                            getDatabase(),
                            new DialogCriteriaEditor.OnCriteriaEditedListener() {

                        @Override
                        public void onCriteriaEdited(DataCollectionCriteria c, View targetView) {

                            c.save(getDatabase());
                            if(c.getId().equals(0)) {
                                c = getDatabase().getLastDataCollectionCriteriaAdded();
                            } else {
                                c = getDatabase().getDataCollectionCriteriaById(c.getId());
                            }

                            // to be updated when the data finish loading.
                            xLow = 0f;
                            xHigh = 0f;

                            if(targetView instanceof ChartPageElementChartView){
                                ChartPageElementChartView chart = (ChartPageElementChartView) targetView;

                                chart.getChartPageElement().setCriteria(c);
                                chart.getChartPageElement().save(getDatabase());

                                chart.setGlobalCriteria(getPage().getCriteria(), getDatabase());
                                chart.setSpecificCriteria(c, getDatabase());

                                chart.startLoading();

                                // recargamos las glucosas para la gráfica

                                if(chart.getChartPageElement().getType().equals(C.CHART_PAGE_ELEMENT_TYPE_GLUCOSE_CROSSED_CARBOHYDRATE_INTAKE)) {
                                    new LoadGlucoseTestCrossedMealsByCriteria().execute(mixCriteria(getPage().getCriteria(), c), targetView);
                                } else {
                                    new LoadGlucoseTestByCriteria().execute(mixCriteria(getPage().getCriteria(), c), targetView);
                                }

                                recalculateXLowXHighAndGetAnnotations();

                            }

                        }
                    }).show(getActivity().getFragmentManager(), null);

                    return true;

                case R.id.statistics:
                    DialogStatisticalObject.newInstance(
                            getDatabase(),
                            chart.getChartPageElement().getStatisticalObject(),
                            chart,
                            new DialogStatisticalObject.OnStatisticalObjectListener() {
                        @Override
                        public void onStatisticalObjectModified(ChartPageElementChartView target, StatisticalObject o) {
                            chart.getChartPageElement().setStatisticalObject(o);
                            chart.redraw();
                        }
                    }).show(getActivity().getFragmentManager(), null);

                    return true;

                case R.id.delete_chart:
                    DialogConfirmation
                            .newInstance(
                                    "delete_chart_element",
                                    ChartPageFragment.this,
                                    R.string.dialog_confirmation_delete_chart_page_element_title,
                                    R.string.dialog_confirmation_delete_chart_page_element_message,
                                    chart
                            )
                            .show(getActivity().getFragmentManager(), null);
                    return true;
            }
        }

        return super.onContextItemSelected(item);
    }

    public void onConfirmPerformed(String tag, boolean confirmation, Object o){
        if(confirmation && tag.equals("delete_text_element")){

            ChartPageElementTextView text = (ChartPageElementTextView) o;
            ChartPageElement e = text.getChartPageElement();

            debugMessage("Eliminando elemento texto: " + e.getTextHeader() + " id: " + e.getId().toString());

            for (int a = 0; a < rootLinearLayout.getChildCount(); a++) {
                View v = rootLinearLayout.getChildAt(a);

                if(v instanceof ChartPageElementTextView){
                    ChartPageElementTextView tv = (ChartPageElementTextView) v;

                    if(e.getId().equals(tv.getChartPageElement().getId())) {
                        e.delete(getDatabase());
                        rootLinearLayout.removeViewAt(a);
                        ((ScrollView)rootLinearLayout.getParent()).invalidate();
                    }
                }
            }
        }
        else if(confirmation && tag.equals("delete_chart_element")) {
            ChartPageElementChartView chart = (ChartPageElementChartView) o;
            ChartPageElement e = chart.getChartPageElement();

            for (int a = 0; a < rootLinearLayout.getChildCount(); a++) {
                View v = rootLinearLayout.getChildAt(a);

                if(v instanceof ChartPageElementChartView){
                    ChartPageElementChartView cv = (ChartPageElementChartView) v;

                    if(e.getId().equals(cv.getChartPageElement().getId())) {
                        e.delete(getDatabase());
                        rootLinearLayout.removeViewAt(a);
                        ((ScrollView)rootLinearLayout.getParent()).invalidate();

                        // hay que recalcular el xlow y xhigh y recargar las anotaciones.
                        recalculateXLowXHighAndGetAnnotations();
                    }
                }
            }

        }
    }





    public void setScrollYToZero(){
        if(rootLinearLayout != null) {
            View scr = (ScrollView) rootLinearLayout.getParent();

            if(scr != null) {
                scr.setScrollY(0);
            }
        }

    }

    public void onTextIntroduced(String tag, String text, View targetView){
        if(tag.equals("modify_text")){
            ChartPageElementTextView tv = (ChartPageElementTextView) targetView;

            ChartPageElement element = tv.getChartPageElement();
            element.setTextHeader(text);
            element.save(getDatabase());

            tv.setChartPageElement(element);
        }

    }

    private static void debugMessage(String message) {
        if(DEBUG) {
            Log.d(DEBUG_TAG, message);
        }
    }


    /*
     *
     */
    private class LoadHbA1cData extends AsyncTask<Void, Void, Boolean> {
        private ArrayList<HbA1c> levels;

        protected void onPreExecute(){
            chartHbA1c.setVisibility(View.INVISIBLE);
        }

        protected Boolean doInBackground(Void... params){
            levels = getDatabase().getHbA1cArrayListSinceInstant(new Instant(-120));
            return true;
        }
        protected void onPostExecute(Boolean result){
            if (result){
                chartHbA1c.setHbA1cs(levels);

                recalculateXLowXHighAndGetAnnotations();

                ViewUtil.makeViewVisibleAnimatedly(chartHbA1c);

                if (levels.size() == 0 || levels.get(levels.size() - 1).getDaysPassedFromNow() > -7f || levels.size() < 10) {

                    if(levels.size() > 0){
                        // no hay datos suficientes
                        String t = getActivity().getString(R.string.activity_charts_insufficient_data1) + " " + (levels.size() > 10 ? 0 : (10 - levels.size()));
                        t += " " + getActivity().getString(R.string.activity_charts_insufficient_data2) + " ";
                        t += 14 + (levels.get(levels.size() - 1).getDaysPassedFromInstant(new Instant()).intValue()) + " " + getActivity().getString(R.string.activity_charts_insufficient_data3);

                        new MyToast(getActivity(), t);

                    } else {
                        // no hay datos suficientes
                        String t = getActivity().getString(R.string.activity_charts_insufficient_data1) + " " + (levels.size() > 10 ? 0 : (10 - levels.size()));
                        t += " " + getActivity().getString(R.string.activity_charts_insufficient_data2) + " ";
                        t += "14 " + getActivity().getString(R.string.activity_charts_insufficient_data3);

                        new MyToast(getActivity(), t);
                    }

                }

            }
        }
    }

    //
    // Helper to load glucose tests from database by criteria, and set the result to the
    // target view chart.
    //
    private class LoadGlucoseTestByCriteria extends AsyncTask<Object, Void, Boolean> {
        private DataCollectionCriteria criteria;
        private ChartPageElementChartView targetView = null;
        private ArrayList<GlucoseTest> glucoseTests = null;

        protected Boolean doInBackground(Object... params){
            if(params[0] instanceof DataCollectionCriteria) {
                criteria = (DataCollectionCriteria) params[0];
            } else {
                return false;
            }

            if (params[1] instanceof ChartPageElementChartView) {
                targetView = (ChartPageElementChartView) params[1];
            } else {
                return false;
            }

            glucoseTests = getDatabase().getGlucoseTestsByCriteria(criteria);


            return true;
        }

        protected void onPostExecute(Boolean result){
            if(result){
                if(targetView != null){
                    targetView.setGlucoseTests(glucoseTests);

                    if(targetView.getChartPageElement().getType().equals(C.CHART_PAGE_ELEMENT_TYPE_GLUCOSE_CROSSED_DATES)) {

                        float xHigh = targetView.getXHigh();
                        float xLow = targetView.getXLow();

                        if(ChartPageFragment.this.xLow == 0.0f || ChartPageFragment.this.xLow > xLow){
                            ChartPageFragment.this.xLow = xLow;
                        }

                        if(ChartPageFragment.this.xHigh == 0.0f || ChartPageFragment.this.xHigh < xHigh){
                            ChartPageFragment.this.xHigh = xHigh;
                        }

                    }

                }

            }
        }
    }

    //
    // Helper to load glucose tests from database by criteria, and set the result to the
    // target view chart.
    //
    private class LoadGlucoseTestCrossedMealsByCriteria extends AsyncTask<Object, Void, Boolean> {
        private DataCollectionCriteria criteria;
        private ChartPageElementChartView targetView = null;
        private GlucoseTestsCrossedMeals testsCrossedMeals;

        protected Boolean doInBackground(Object... params){
            if(params[0] instanceof DataCollectionCriteria) {
                criteria = (DataCollectionCriteria) params[0];
            } else {
                return false;
            }

            if (params[1] instanceof ChartPageElementChartView) {
                targetView = (ChartPageElementChartView) params[1];
            } else {
                return false;
            }

            testsCrossedMeals = getDatabase().getGlucoseTestsCrossedMealsByCriteria(criteria);

            return true;
        }

        protected void onPostExecute(Boolean result){
            if(result){
                if(targetView != null){
                    targetView.setGlucoseTestsCrossedMeals(testsCrossedMeals);
                }
            }
        }
    }


    private DataCollectionCriteria mixCriteria(DataCollectionCriteria globalCriteria, DataCollectionCriteria specificCriteria) {

        // 1
        ArrayList<DataCollectionLabelRule> lr1;
        int st1 = C.DATA_COLLECTION_CRITERIA_INSTANT_TYPE_RELATIVE;
        long sl1 = 0;
        int ut1 = C.DATA_COLLECTION_CRITERIA_INSTANT_TYPE_RELATIVE;
        long ul1 = 0;

        int dayWeek1Activated = C.DATA_COLLECTION_CRITERIA_ACTIVATED;
        int dayWeek1 = 0;
        int times1Activated = C.DATA_COLLECTION_CRITERIA_ACTIVATED;
        int times1 = 0;

        // 2
        ArrayList<DataCollectionLabelRule> lr2;
        int st2 = C.DATA_COLLECTION_CRITERIA_INSTANT_TYPE_RELATIVE;
        long sl2 = 0;
        int ut2 = C.DATA_COLLECTION_CRITERIA_INSTANT_TYPE_RELATIVE;
        long ul2 = 0;

        int dayWeek2Activated = C.DATA_COLLECTION_CRITERIA_ACTIVATED;
        int dayWeek2 = 0;
        int times2Activated = C.DATA_COLLECTION_CRITERIA_ACTIVATED;
        int times2 = 0;


        // 1
        if(globalCriteria != null) {
            lr1 = globalCriteria.getLabelRules();
            st1 = globalCriteria.getSince().getType();
            sl1 = globalCriteria.getSince().getData();
            ut1 = globalCriteria.getUntil().getType();
            ul1 = globalCriteria.getUntil().getData();

            dayWeek1Activated = globalCriteria.getDayWeeksActivated();
            dayWeek1 = globalCriteria.getDayWeeksInteger();
            times1Activated = globalCriteria.getMealTimesActivated();
            times1 = globalCriteria.getMealTimeInteger();
        }

        // 2
        if(specificCriteria != null) {
            lr2 = specificCriteria.getLabelRules();
            st2 = specificCriteria.getSince().getType();
            sl2 = specificCriteria.getSince().getData();
            ut2 = specificCriteria.getUntil().getType();
            ul2 = specificCriteria.getUntil().getData();

            dayWeek2Activated = specificCriteria.getDayWeeksActivated();
            dayWeek2 = specificCriteria.getDayWeeksInteger();
            times2Activated = specificCriteria.getMealTimesActivated();
            times2 = specificCriteria.getMealTimeInteger();
        }

        if(globalCriteria == null) {
            if(specificCriteria == null) {
                return null;
            } else {
                return specificCriteria;
            }
        } else {
            if(specificCriteria == null) {
                return globalCriteria;
            } else {

                int sinceType;
                long sinceLong;
                int untilType;
                long untilLong;
                if(st1 != 0 || sl1 != 0){
                    sinceType = st1;
                    sinceLong = sl1;
                } else {
                    sinceType = st2;
                    sinceLong = sl2;
                }
                if(ut1 != 0 || ul1 != 0){
                    untilType = ut1;
                    untilLong = ul1;
                } else {
                    untilType = ut2;
                    untilLong = ul2;
                }

                DataCollectionCriteriaInstant sin = new DataCollectionCriteriaInstant(
                        getActivity(),
                        db,
                        sinceType,
                        sinceLong
                );

                DataCollectionCriteriaInstant unt = new DataCollectionCriteriaInstant(
                        getActivity(),
                        db,
                        untilType,
                        untilLong
                );

                DataCollectionCriteria result = new DataCollectionCriteria(
                        0,
                        sin,
                        unt,
                        dayWeek1Activated | dayWeek2Activated,
                        dayWeek1 | dayWeek2,
                        times1Activated | times2Activated,
                        times1 | times2
                );

                if(globalCriteria.getLabelRules() != null && globalCriteria.getLabelRules().size() > 0) {
                    result.setLabelRules(globalCriteria.getLabelRules());
                } else {
                    result.setLabelRules(specificCriteria.getLabelRules());
                }

                return result;
            }
        }

    }


    private ActivityBuildCharts getParent(){
        return (ActivityBuildCharts) getActivity();
    }

    private DataDatabase getDatabase(){
        if(db == null) {
            db = ((ActivityBuildCharts)getActivity()).getDatabase();
        }

        if(db == null) {
            db = new DataDatabase(getActivity());
            ((ActivityBuildCharts)getActivity()).setDatabase(db);
        }

        return db;
    }

}

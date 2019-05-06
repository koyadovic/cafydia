package org.cafydia.android.activities;

import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import org.cafydia.android.R;
import org.cafydia.android.adapters.AnnotationAdapter;
import org.cafydia.android.adapters.LabelAdapter;
import org.cafydia.android.chartobjects.ChartPage;
import org.cafydia.android.chartobjects.ChartPageElement;
import org.cafydia.android.chartobjects.DataCollectionCriteria;
import org.cafydia.android.chartobjects.DataCollectionCriteriaInstant;
import org.cafydia.android.chartobjects.Label;
import org.cafydia.android.chartobjects.StatisticalObject;
import org.cafydia.android.core.Annotation;
import org.cafydia.android.datadatabase.DataDatabase;
import org.cafydia.android.dialogfragments.CafydiaAlertDialog;
import org.cafydia.android.dialogfragments.DialogAddAnnotation;
import org.cafydia.android.dialogfragments.DialogAddLabel;
import org.cafydia.android.dialogfragments.DialogChartPageElementSelectChartType;
import org.cafydia.android.dialogfragments.DialogCriteriaEditor;
import org.cafydia.android.dialogfragments.DialogNewChartActivityElement;
import org.cafydia.android.fragments.ChartPageFragment;
import org.cafydia.android.genericdialogfragments.DialogConfirmation;
import org.cafydia.android.genericdialogfragments.DialogGetTextOrNumber;
import org.cafydia.android.tutorial.HelpFragmentBundle;
import org.cafydia.android.tutorial.Tutorial;
import org.cafydia.android.util.C;
import org.cafydia.android.util.MyToast;
import org.cafydia.android.util.OnSwipeTouchListener;
import org.cafydia.android.util.ViewUtil;

import java.util.ArrayList;

/**
 * Created by user on 23/11/14.
 */
public class ActivityBuildCharts extends FragmentActivity implements
        DialogAddAnnotation.OnAnnotationAddedListener,
        DialogAddLabel.OnAddLabelListener,
        DialogNewChartActivityElement.OnNewChartElementListener,
        DialogGetTextOrNumber.OnTextIntroducedListener,
        DialogConfirmation.OnConfirmListener {

    private ChartsPagerAdapter mPagerAdapter;
    private ViewPager mPager;
    private int mCurrentPagerScopePosition = 0;

    private ListView lvAnnotations, lvLabels;
    private AnnotationAdapter mAnnotationAdapter;
    private LabelAdapter mLabelAdapter;

    private static DataDatabase db;

    private ArrayList<Label> mLabels;

    private boolean mActivityActive = false;


    /////////////////////
    // for the panel
    /////////////////////
    private RelativeLayout rlPanel;
    private boolean mPanelVisible;
    private ImageView mIvToggleShow;
    private int mHeightPanel = -1;
    private int mHeightIvToggleShow = -1;
    private final int TIME_ANIMATION = C.PANEL_SPEED_OF_HIDE_AND_SHOW_OPERATIONS;


    private HelpFragmentBundle mHelpFragmentBundle;


    private void setOrientation(){
        if (getResources().getBoolean(R.bool.phone)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    public ArrayList<Annotation> getAnnotationsNumberedByXLowXHigh(int scope, Float xLow, Float xHigh){
        ArrayList<Annotation> annotations = getDatabase().getAnnotationsByOrderNumberScopeXLowAndXHigh(scope, xLow, xHigh);

        if(scope == mCurrentPagerScopePosition)
            mAnnotationAdapter.setAnnotations(annotations);

        return annotations;
    }

    private void recreateViewPager(){

        ///////////////////////////////////////////////////////////////////////////////
        //
        // Very important thing is that never never never the number order
        // have to be repeated.
        //
        // All the numbers needs to be consecutive and starting from one.
        //
        ///////////////////////////////////////////////////////////////////////////////

        if(mPager == null) {
            mPager = (ViewPager) findViewById(R.id.pager);
        }

        mPagerAdapter = new ChartsPagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mPager.setOffscreenPageLimit(2);
        mPager.setCurrentItem(0);
        mCurrentPagerScopePosition = 0;

        ChartPage hba1c;
        hba1c = new ChartPage(C.PAGER_SCOPE_HBA1C_POSITION, getString(R.string.activity_charts_0_tab_hba1c), null);
        ChartPageFragment f = ChartPageFragment.newInstance(hba1c);

        mPagerAdapter.addFragment(f, hba1c);

        ArrayList<ChartPage> pages = getDatabase().getChartPagesOrderedByScope();

        long delay = 0;

        for(ChartPage page : pages){
            delay += 50;
            final ChartPage p = page;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(mActivityActive) {
                        ChartPageFragment f = ChartPageFragment.newInstance(p);
                        mPagerAdapter.addFragment(f, p);
                    }
                }
            }, delay);
        }

        mPager.setOnPageChangeListener(pagerListener);

    }


    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_charts);

        mHelpFragmentBundle = new HelpFragmentBundle(this, R.id.help);

        mActivityActive = true;

        //getDatabase().cleanChartsTables();

        ///////////////////////
        // for the panel
        ///////////////////////

        mPanelVisible = false;

        rlPanel = (RelativeLayout) findViewById(R.id.rlPanel);

        mIvToggleShow = (ImageView) findViewById(R.id.ivToggleShow);
        mIvToggleShow.setOnTouchListener(new OnSwipeTouchListener(this) {

            @Override
            public void onSwipeTop(){
                showPanel();
            }

            @Override
            public void onSwipeBottom(){
                hidePanel();
            }

        });


        ////////////////
        // annotations
        ////////////////

        lvAnnotations = (ListView) findViewById(R.id.lvAnnotations);
        registerForContextMenu(lvAnnotations);

        ArrayList<Annotation> annotations = new ArrayList<>();

        mAnnotationAdapter = new AnnotationAdapter(this, annotations);
        lvAnnotations.setAdapter(mAnnotationAdapter);

        lvAnnotations.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DialogAddAnnotation
                        .newInstance(mCurrentPagerScopePosition, mAnnotationAdapter.getItem(position))
                        .show(getFragmentManager(), "dialog_add_annotation");
            }
        });


        ////////////////
        // Labels
        ////////////////

        lvLabels = (ListView) findViewById(R.id.lvLabels);
        registerForContextMenu(lvLabels);


        mLabels = getDatabase().getLabels();
        mLabelAdapter = new LabelAdapter(this, mLabels);
        lvLabels.setAdapter(mLabelAdapter);

        lvLabels.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Label l = mLabelAdapter.getItem(position);
                DialogAddLabel.newInstance(l.getId(), l.getTitle(), l.getColor()).show(getFragmentManager(), "dialog_add_label");

            }
        });


        if(getActionBar() != null)
            getActionBar().setDisplayHomeAsUpEnabled(true);


        Tutorial.BuildYourCharts.aboutChartsActivity(mHelpFragmentBundle);

    }

    @Override
    public void onResume() {
        super.onResume();

        // this code is to measure the width for show and hide operations
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        if ((getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK) ==
                Configuration.SCREENLAYOUT_SIZE_SMALL) {
            mHeightPanel = (int) (metrics.density * 300);
        } else {
            mHeightPanel = (int) (metrics.density * 350);
        }

        mHeightIvToggleShow = (int) (metrics.density * 24);


        // if is a phone, the panel start hidden
        if(!mPanelVisible) {

            final RelativeLayout.LayoutParams params = rlPanel != null ? (RelativeLayout.LayoutParams) rlPanel.getLayoutParams() : null;
            if (params != null) {
                params.topMargin = mHeightPanel - mHeightIvToggleShow;
                params.bottomMargin = -mHeightPanel + mHeightIvToggleShow;

                rlPanel.setLayoutParams(params);
                rlPanel.invalidate();
            }
        }


        if(mPagerAdapter == null) {
            recreateViewPager();
        }

    }

    @Override
    public void onStop(){
        super.onStop();
        mActivityActive = false;
    }

        // to save and restore the state
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////// TO MANAGE ACTIONBAR MENU //////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(mCurrentPagerScopePosition == C.PAGER_SCOPE_HBA1C_POSITION) {
            getMenuInflater().inflate(R.menu.activity_charts_menu_hba1c_position, menu);
        } else {
            getMenuInflater().inflate(R.menu.activity_charts_menu, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            case R.id.action_chart_page_set_criteria:
                final ChartPage page = mPagerAdapter.getItem(mCurrentPagerScopePosition).getPage();

                DialogCriteriaEditor.newInstance(
                        mAnnotationAdapter.getAnnotations(),
                        null,
                        page.getCriteria(),
                        null,
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
                                page.setCriteria(c);
                                page.save(getDatabase());

                                // el criterio global fue modificado por lo que hay que hacer refresh
                                // de todos los page elements que cuelguen de la página actual
                                getChild().reloadChartPageElements();
                            }
                        }

                ).show(getFragmentManager(), null);

                return true;

            case R.id.action_new_chart_element:

                if(mPanelVisible) {
                    hidePanel();
                }

                DialogNewChartActivityElement.newInstance(mCurrentPagerScopePosition).show(getFragmentManager(), "dialog_new_element");
                return true;

            case R.id.action_chart_page_delete:
                if(mCurrentPagerScopePosition == 0){
                    new MyToast(this, getString(R.string.activity_charts_cannot_change_or_delete_first_page));
                } else {
                    deleteCurrentPageDialog();
                }
                return true;

            case R.id.action_chart_move_to_left:
                mPagerAdapter.moveItemToLeft(mCurrentPagerScopePosition);

                return true;

            case R.id.action_chart_move_to_right:
                mPagerAdapter.moveItemToRight(mCurrentPagerScopePosition);
                return true;

            case R.id.action_chart_change_title:
                if(mCurrentPagerScopePosition == 0){
                    new MyToast(this, getString(R.string.activity_charts_cannot_change_or_delete_first_page));
                } else {
                    newPageDialog(mPagerAdapter.getItem(mCurrentPagerScopePosition).getPage());
                }
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    public void onNewChartActivityElementSelected(int selection){
        switch (selection){
            case C.CHART_ACTIVITY_ELEMENT_PAGE:
                newPageDialog(null);
                break;

            case C.CHART_ACTIVITY_ELEMENT_TEXT:
                DialogGetTextOrNumber
                        .newInstance(
                                "new_element_text",
                                getText(R.string.dialog_new_chart_element_text_title).toString(),
                                null,
                                this)
                        .show(getFragmentManager(), null);

                break;

            case C.CHART_ACTIVITY_ELEMENT_CHART:
                DialogChartPageElementSelectChartType.newInstance(mCurrentPagerScopePosition, new DialogChartPageElementSelectChartType.OnChartTypeSelectedListener() {
                    @Override
                    public void onChartTypeSelected(int type) {

                        ChartPage page = getChild().getPage();

                        if(page != null) {

                            ChartPageElement element = new ChartPageElement(0, type, page.getId(), "");
                            element.save(getDatabase());

                            // recogemos el elemento para conseguir su ID
                            element = getDatabase().getLastChartPageElementAdded();

                            // creamos el criterio por defecto y el objeto estadístico
                            DataCollectionCriteria criteria = new DataCollectionCriteria(ActivityBuildCharts.this);

                            // 0b1100000000 es glucosas y el grid, lo que se muestra por defecto
                            StatisticalObject statisticalObject = new StatisticalObject(element);


                            // los guardamos en la base de datos
                            criteria.save(getDatabase());
                            statisticalObject.save(getDatabase());

                            // los cargamos para conseguir su ID
                            criteria = getDatabase().getLastDataCollectionCriteriaAdded();
                            statisticalObject = getDatabase().getLastStatisticalObjectAdded();

                            // los asignamos al nuevo elemento y lo guardamos en la base de datos
                            element.setCriteria(criteria);
                            element.setStatisticalObject(statisticalObject);
                            element.save(getDatabase());

                            // añadimos el nuevo elemento en el fragment actual
                            getChild().addElement(element);

                            Tutorial.BuildYourCharts.aboutClickOrLongClickOnChart(mHelpFragmentBundle);

                            // Esto soluciona la carga de anotaciones al añadir una nueva gráfica
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    updateCurrentPagerChildIfItsNeededAndGetAnnotationsUpdated();
                                }
                            }, 1500);

                        } else {
                            new MyToast(ActivityBuildCharts.this, "HA SURGIDO UN PUTO ERROR");
                        }

                    }
                }).show(getFragmentManager(), null);
                break;

            case C.CHART_ACTIVITY_ELEMENT_ANNOTATION:
                DialogAddAnnotation.newInstance(mCurrentPagerScopePosition).show(getFragmentManager(), "dialog_add_annotation");
                break;

            case C.CHART_ACTIVITY_ELEMENT_LABEL:
                DialogAddLabel.newInstance().show(getFragmentManager(), "dialog_add_label");
                break;
        }
    }

    public void onTextIntroduced(String tag, String text, View targetView){
        if(tag.equals("new_element_text")){

            ChartPageFragment fragment = mPagerAdapter.getItem(mCurrentPagerScopePosition);

            ChartPageElement element = new ChartPageElement(0, C.CHART_PAGE_ELEMENT_TYPE_TEXT, fragment.getPage().getId(), text);
            element.save(getDatabase());

            element = getDatabase().getLastChartPageElementAdded();

            fragment.addElement(element);
        }
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////// Contextual menu /////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onContextMenuClosed(Menu menu) {
        super.onContextMenuClosed(menu);

        //
        // Esto es para asignar a currentContextualView el valor null cuando el menú contextual es
        // cerrado en todos los fragments hijos del pagerAdapter
        //
        for(int a = 0; a < mPagerAdapter.getCount(); a++) {
            mPagerAdapter.getItem(a).onContextMenuClosed(menu);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        if(v.getId() == R.id.lvAnnotations){
            inflater.inflate(R.menu.activity_charts_annotations_contextual_menu, menu);
        }
        else if(v.getId() == R.id.lvLabels){
            inflater.inflate(R.menu.activity_charts_labels_contextual_menu, menu);
        }

    }

    @Override
    public boolean onContextItemSelected(MenuItem item){
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        Annotation a;
        Label l;

        switch (item.getItemId()){
            case R.id.edit_annotation:
                a = mAnnotationAdapter.getItem(info.position);
                DialogAddAnnotation
                        .newInstance(mCurrentPagerScopePosition, a)
                        .show(getFragmentManager(), "dialog_add_annotation");
                return true;

            case R.id.delete_annotation:
                a = mAnnotationAdapter.getItem(info.position);
                DialogConfirmation
                        .newInstance("delete_annotation", this, R.string.dialog_confirmation_delete_annotation_title, R.string.dialog_confirmation_delete_annotation_message, a)
                        .show(getFragmentManager(), null);

                return true;

            case R.id.edit_label:
                l = mLabelAdapter.getItem(info.position);
                DialogAddLabel.newInstance(l.getId(), l.getTitle(), l.getColor()).show(getFragmentManager(), "dialog_add_label");
                return true;

            case R.id.delete_label:
                l = mLabelAdapter.getItem(info.position);
                DialogConfirmation
                        .newInstance("delete_label", this, R.string.dialog_confirmation_delete_label_title, R.string.dialog_confirmation_delete_label_message, l)
                        .show(getFragmentManager(), null);

                return true;
        }


        return super.onContextItemSelected(item);
    }

    public void onConfirmPerformed(String tag, boolean confirmation, Object object){
        if(confirmation && tag.equals("delete_annotation")) {
            final Annotation a = (Annotation) object;
            a.delete(getDatabase());
            mAnnotationAdapter.deleteAnnotation(a);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    checkPagerChildrenIfItsAffectedToAnnotationChange(a);
                    updateCurrentPagerChildIfItsNeededAndGetAnnotationsUpdated();
                }
            }, 300);

        }
        else if(tag.equals("delete_label")) {
            if(confirmation) {
                Label l = (Label) object;

                l.delete(getDatabase());

                onLabelAddedOrEdited(null, null);
            }
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

    // FOR ANNOTATIONS

    //
    //

    //
    //
    @Override
    public void onAnnotationAddedOrEdited(final Annotation a){
        checkPagerChildrenIfItsAffectedToAnnotationChange(a);
        updateCurrentPagerChildIfItsNeededAndGetAnnotationsUpdated();

    }

    private void checkPagerChildrenIfItsAffectedToAnnotationChange(Annotation a){
        for(int n=0; n<mPagerAdapter.getCount(); n++){
            ChartPageFragment f = mPagerAdapter.getItem(n);

            if(f != null && f.getPage() != null) {
                DataCollectionCriteria criteria = f.getPage().getCriteria();
                boolean criteriaChanged = false;

                if(criteria != null) {

                    if (criteria.getSinceInstant() == null) {
                        criteria.setSince(new DataCollectionCriteriaInstant(ActivityBuildCharts.this, getDatabase()));
                        criteria.save(getDatabase());
                        criteriaChanged = true;
                    }

                    if (criteria.getUntilInstant() == null) {
                        criteria.setUntil(new DataCollectionCriteriaInstant(ActivityBuildCharts.this, getDatabase()));
                        criteria.save(getDatabase());
                        criteriaChanged = true;
                    }

                    if(criteriaChanged) {
                        if (n == mCurrentPagerScopePosition) {
                            f.reloadChartPageElements();
                        } else {
                            f.setRecreate(true);
                        }
                    }


                }
                if(!criteriaChanged) {
                    if (isAnnotationUsedGloballyInChartPage(a, f)) {
                        if (n == mCurrentPagerScopePosition) {
                            f.reloadChartPageElements();
                        } else {
                            f.setRecreate(true);
                        }
                    } else {
                        f.addAnnotationIdPendingToReview(a.getId());

                        if (n == mCurrentPagerScopePosition) {
                            f.checkChildrenChartViewsIfItsAffectedToAnnotationsDeletedOrChanged();
                        }

                    }
                }
            }
        }
    }

    private boolean isAnnotationUsedGloballyInChartPage(Annotation a, ChartPageFragment f) {
        DataCollectionCriteria criteria = f.getPage().getCriteria();
        if(criteria != null) {
            if(criteria.getSinceInstant() instanceof Annotation && criteria.getSince().getData().intValue() == a.getId()){
                return true;
            }
            if(criteria.getUntilInstant() instanceof Annotation && criteria.getUntil().getData().intValue() == a.getId()){
                return true;
            }
        }
        return false;
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
    public void onLabelAddedOrEdited(Label oldLabel, Label newLabel){
        // update labels
        mLabels = getDatabase().getLabels();

        setLabelsOnViewPagerChildren(mLabels);

        mLabelAdapter.setLabels(mLabels);
    }

    private void setLabelsOnViewPagerChildren(ArrayList<Label> labels){
        for(int n=0; n<mPagerAdapter.getCount(); n++) {
            ChartPageFragment f = mPagerAdapter.getItem(n);

            if(f != null)
                f.setLabels(labels);
        }
    }


    private void deleteCurrentPageDialog(){
        CafydiaAlertDialog.Builder builder = new CafydiaAlertDialog.Builder(
                this,
                getResources().getColor(R.color.colorCafydiaDefault),
                null,
                R.string.dialog_delete_chart_page_title
        );
        builder.setMessage(R.string.dialog_delete_chart_page_message);
        builder.setNegativeButton(R.string.dialog_delete_chart_page_button_cancel, null);
        builder.setPositiveButton(R.string.dialog_delete_chart_page_button_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (getChild().getPage() == null) {

                    ArrayList<ChartPage> pages = getDatabase().getChartPagesOrderedByScope();
                    for (int a = 1; a < mPagerAdapter.getCount(); a++) {
                        for (ChartPage page : pages) {
                            if (page.getPagerScopePosition().equals(a)) {
                                mPagerAdapter.getItem(a).setPage(page);
                            }
                        }
                    }


                }

                ChartPage page = getChild().getPage();

                if (page != null) {
                    page.delete(getDatabase());
                }

                mPagerAdapter.removeItem(mCurrentPagerScopePosition);

            }
        });

        builder.show();
    }

    private void newPageDialog(ChartPage page){
        CafydiaAlertDialog.Builder builder = new CafydiaAlertDialog.Builder(
                this,
                getResources().getColor(R.color.colorCafydiaDefault),
                null,
                R.string.dialog_new_chart_page_title
        );

        final boolean newPage = page == null;

        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_new_page, null);
        final EditText etName = (EditText) view.findViewById(R.id.etName);

        ViewUtil.showKeyboard(this, etName);

        if (!newPage) {
            etName.setText(page.getTitle());

        } else {
            int number = mPagerAdapter.getCount();
            page = new ChartPage(number, etName.getText().toString(), null);
        }

        builder.setView(view);

        final ChartPage finalPage = page;

        // place the cursor at the end of the edittext
        etName.setSelection(etName.getText().length());

        builder.setPositiveButton(R.string.dialog_new_chart_page_button_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!etName.getText().toString().equals("")) {
                    finalPage.setTitle(etName.getText().toString());

                    // to save it to database
                    finalPage.save(getDatabase());

                    if (newPage) {
                        // to get the id from database
                        getDatabase().setChartPageDatabaseId(finalPage);

                        ChartPageFragment f = ChartPageFragment.newInstance(finalPage);

                        mPagerAdapter.addFragment(f, finalPage);

                    } else {
                        String newTitle = etName.getText().toString();

                        ChartPageFragment f = mPagerAdapter.getItem(finalPage.getPagerScopePosition());

                        f.getPage().setTitle(newTitle);
                        ChartPageFragment.setTitle(finalPage.getPagerScopePosition(), newTitle);
                        mPagerAdapter.notifyDataSetChanged();
                    }



                    if (newPage) {
                        new MyToast(ActivityBuildCharts.this, R.string.dialog_new_chart_element_page_added);
                    }

                }
                ViewUtil.hideKeyboard(ActivityBuildCharts.this, etName);
            }
        });

        builder.show();

    }


    /*
     * ANIMATIONS TO OPEN AND CLOSE THE PANEL
     */

    public void showPanel(){
        if(!mPanelVisible) {
            mPanelVisible = true;

            Tutorial.BuildYourCharts.aboutAnnotationsAndLabels(mHelpFragmentBundle);

            Animation showAnimation = new Animation() {

                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    super.applyTransformation(interpolatedTime, t);
                    RelativeLayout.LayoutParams params = rlPanel != null ? (RelativeLayout.LayoutParams) rlPanel.getLayoutParams() : null;
                    if (params != null) {
                        params.topMargin = (int) ((mHeightPanel - mHeightIvToggleShow) * (1.0 - interpolatedTime));
                        params.bottomMargin = (int) ((-mHeightPanel + mHeightIvToggleShow) * (1.0 - interpolatedTime));

                        rlPanel.setLayoutParams(params);
                        //rlPanel.bringToFront();
                    }
                }
            };

            RelativeLayout.LayoutParams params = rlPanel != null ? (RelativeLayout.LayoutParams) rlPanel.getLayoutParams() : null;
            if (params != null) {
                showAnimation.setDuration(TIME_ANIMATION);

                rlPanel.startAnimation(showAnimation);
            }
        }
    }

    public void hidePanel(){
        if(mPanelVisible) {
            mPanelVisible = false;

            Animation hideAnimation = new Animation() {
                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    super.applyTransformation(interpolatedTime, t);


                    RelativeLayout.LayoutParams params = rlPanel != null ? (RelativeLayout.LayoutParams) rlPanel.getLayoutParams() : null;
                    if (params != null) {


                        params.topMargin = (int) ((mHeightPanel - mHeightIvToggleShow) * interpolatedTime);
                        params.bottomMargin = (int) ((-mHeightPanel + mHeightIvToggleShow) * interpolatedTime);

                        rlPanel.setLayoutParams(params);
                    }
                }
            };

            RelativeLayout.LayoutParams params = rlPanel != null ? (RelativeLayout.LayoutParams) rlPanel.getLayoutParams() : null;
            if (params != null) {
                hideAnimation.setDuration(TIME_ANIMATION);
                rlPanel.startAnimation(hideAnimation);
            }
        }
    }


    ////////////////////////////////////////////////////////////////////
    ////////////////// LISTENER FOR PAGE VIEWER ////////////////////////
    ////////////////////////////////////////////////////////////////////

    private ViewPager.OnPageChangeListener pagerListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int i, float v, int i2) {

        }

        @Override
        public void onPageSelected(int i) {
            if(mPagerAdapter.getItem(mCurrentPagerScopePosition) != null) {
                mPagerAdapter.getItem(mCurrentPagerScopePosition).setScrollYToZero();
            }
            mCurrentPagerScopePosition = i;
        }

        @Override
        public void onPageScrollStateChanged(int i) {
            if(i == ViewPager.SCROLL_STATE_IDLE){
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        updateCurrentPagerChildIfItsNeededAndGetAnnotationsUpdated();

                        // this refresh chart page object from database
                        getChild().refreshPageFromDatabase();

                        // this update the labels
                        getChild().setLabels(mLabels);
                    }
                });

                if(mCurrentPagerScopePosition != 0) {
                    Tutorial.BuildYourCharts.aboutNewPageCreated(mHelpFragmentBundle);
                }
            }
        }
    };

    private void updateCurrentPagerChildIfItsNeededAndGetAnnotationsUpdated(){

        getChild().checkChildrenChartViewsIfItsAffectedToAnnotationsDeletedOrChanged();

        if(mCurrentPagerScopePosition == 0) {
            mAnnotationAdapter.setAnnotations(getDatabase().getAnnotationsByOrderNumberScopeXLowAndXHigh(mCurrentPagerScopePosition, -120, 0));

        } else {
            if (!getChild().recreateIfNeeded()) {
                // se mira si hay que recargar algún chartview
                mAnnotationAdapter.setAnnotations(getChild().updateCachedAnnotationsAndGetIt());
            }
        }

        invalidateOptionsMenu();

        getChild().setCachedAnnotationsInViews();
    }


    ///////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////
    /////////////////// CHART PAGE ADAPTER ////////////////////
    ///////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////

    private class ChartsPagerAdapter extends FragmentStatePagerAdapter {
        private ArrayList<ChartPageFragment> chartPageFragments;

        public ChartsPagerAdapter(FragmentManager fm){
            super(fm);
            onResumeFragments();
            chartPageFragments = new ArrayList<>();
        }

        public void addFragment(ChartPageFragment f, ChartPage page){

            if (chartPageFragments.size() == 0 || page.getPagerScopePosition() >= chartPageFragments.size()) {
                chartPageFragments.add(f);

            } else {
                chartPageFragments.add(page.getPagerScopePosition(), f);
            }

            notifyDataSetChanged();

        }

        private void recalculateChartPageOrderNumbers(){
            ArrayList<ChartPage> pages = getDatabase().getChartPagesOrderedByScope();

            int n = 1;

            for(ChartPage page : pages) {
                if (page.getPagerScopePosition() != n) {
                    page.setScopeOrderNumber(n);
                    page.save(getDatabase());
                }
                n++;
            }
        }


        public int getItemPosition(Object item) {
            ChartPageFragment fragment = (ChartPageFragment) item;

            if(!fragment.getPage().getPagerScopePosition().equals(chartPageFragments.indexOf(fragment))){
                return POSITION_NONE;
            }
            return chartPageFragments.indexOf(fragment);

        }

        @Override
        public ChartPageFragment getItem(int pos){
            if(pos < chartPageFragments.size()) {
                return chartPageFragments.get(pos);
            } else {
                return null;
            }
        }

        public void removeItem(int pos){

            // for annotations in the db and for titles
            int originalCount = getCount();

            getDatabase().deleteChartPageElementsByChartPageId(getItem(pos).getPage().getId());

            // fragments
            chartPageFragments.remove(pos);

            // refresh the order numbers
            recalculateChartPageOrderNumbers();

            // titles
            // if it is the last element
            if(pos == originalCount - 1){
                ChartPageFragment.setTitle(pos, "");
            } else {
                // if not
                for(int a = pos; a < originalCount; a++){
                    String nextTitle = ChartPageFragment.getTitle(a + 1);
                    ChartPageFragment.setTitle(a, nextTitle);
                }

            }

            notifyDataSetChanged();

            // annotations
            ArrayList<Annotation> annotations = getDatabase().getAllAnnotations();

            for(Annotation an : annotations){
                if(an.getOrderNumberScope().equals(pos)){
                    an.delete(getDatabase());
                }
                else if(an.getOrderNumberScope() > pos) {
                    an.setOrderNumberScope(an.getOrderNumberScope() - 1);
                    an.save(getDatabase());
                }
            }

            if(getCount() == 1){
                recreateViewPager();
            } else {
                getChild().refreshPageFromDatabase();
            }

            invalidateOptionsMenu();

        }

        public void moveItemToLeft(int pos){
            if(pos < getCount() && pos > 1){
                // titles first
                String currentTitle = ChartPageFragment.getTitle(pos);
                String leftTitle = ChartPageFragment.getTitle(pos - 1);
                ChartPageFragment.setTitle(pos, leftTitle);
                ChartPageFragment.setTitle(pos - 1, currentTitle);

                // fragments
                ChartPageFragment current = getItem(pos);
                ChartPageFragment left = getItem(pos - 1);

                chartPageFragments.set(pos, left);
                chartPageFragments.set(pos - 1, current);

                current.getPage().setScopeOrderNumber(pos - 1);
                current.getPage().save(getDatabase());

                left.getPage().setScopeOrderNumber(pos);
                left.getPage().save(getDatabase());



                // refresh the order numbers
                //recalculateChartPageOrderNumbers();

                notifyDataSetChanged();

                // annotations
                ArrayList<Annotation> currentAnnotations = getDatabase().getAllAnnotationsByScope(pos);
                ArrayList<Annotation> leftAnnotations = getDatabase().getAllAnnotationsByScope(pos - 1);

                for(Annotation currentAnnotation : currentAnnotations){
                    currentAnnotation.setOrderNumberScope(pos - 1);
                    currentAnnotation.save(getDatabase());
                }
                for(Annotation leftAnnotation : leftAnnotations){
                    leftAnnotation.setOrderNumberScope(pos);
                    leftAnnotation.save(getDatabase());
                }

                getChild().refreshPageFromDatabase();

            }
        }

        public void moveItemToRight(int pos){
            if(pos < getCount() - 1 && pos > 0){
                // titles first
                String currentTitle = ChartPageFragment.getTitle(pos);
                String rightTitle = ChartPageFragment.getTitle(pos + 1);
                ChartPageFragment.setTitle(pos, rightTitle);
                ChartPageFragment.setTitle(pos + 1, currentTitle);

                // fragments
                ChartPageFragment current = getItem(pos);
                ChartPageFragment right = getItem(pos + 1);

                chartPageFragments.set(pos, right);
                chartPageFragments.set(pos + 1, current);

                current.getPage().setScopeOrderNumber(pos + 1);
                current.getPage().save(getDatabase());

                right.getPage().setScopeOrderNumber(pos);
                right.getPage().save(getDatabase());

                // refresh the order numbers
                //recalculateChartPageOrderNumbers();

                notifyDataSetChanged();

                // annotations
                ArrayList<Annotation> currentAnnotations = getDatabase().getAllAnnotationsByScope(pos);
                ArrayList<Annotation> rightAnnotations = getDatabase().getAllAnnotationsByScope(pos + 1);

                for(Annotation currentAnnotation : currentAnnotations){
                    currentAnnotation.setOrderNumberScope(pos + 1);
                    currentAnnotation.save(getDatabase());
                }
                for(Annotation rightAnnotation : rightAnnotations){
                    rightAnnotation.setOrderNumberScope(pos);
                    rightAnnotation.save(getDatabase());
                }

                getChild().refreshPageFromDatabase();

            }
        }



        @Override
        public int getCount(){
            return chartPageFragments.size();
        }

        @Override
        public CharSequence getPageTitle (int position) {
            return ChartPageFragment.getTitle(position);
        }

    }

    public ChartPageFragment getChild(){
        return mPagerAdapter.getItem(mCurrentPagerScopePosition);
    }

    public DataDatabase getDatabase(){
        if(db == null) {
            db = new DataDatabase(this);

        }

        return db;
    }

    public void setDatabase(DataDatabase database) {
        db = database;
    }

}


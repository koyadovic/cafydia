package org.cafydia4.android.services;

import android.app.backup.BackupAgentHelper;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.app.backup.FileBackupHelper;
import android.app.backup.SharedPreferencesBackupHelper;
import android.os.ParcelFileDescriptor;

import org.cafydia4.android.activities.ActivityMealsSnacks;
import org.cafydia4.android.bedcasearch.BedcaLocalDatabase;
import org.cafydia4.android.bedcasearch.BedcaSearch;
import org.cafydia4.android.bedcasearch.SQLOpenHelperBedca;
import org.cafydia4.android.configdatabase.ConfigurationDatabase;
import org.cafydia4.android.configdatabase.SQLOpenHelperConfiguration;
import org.cafydia4.android.datadatabase.DataDatabase;
import org.cafydia4.android.datadatabase.SQLOpenHelperData;
import org.cafydia4.android.fragments.BaselineFragment;
import org.cafydia4.android.recommendations.BaselineBasal;
import org.cafydia4.android.recommendations.BaselinePreprandial;
import org.cafydia4.android.recommendations.MetabolicFrameworkState;
import org.cafydia4.android.tutorial.HelpFragment;
import org.cafydia4.android.util.HbA1cHelper;

import java.io.IOException;

/**
 * Created by user on 12/09/14.
 */
public class CafydiaAgentHelper extends BackupAgentHelper {

    private final String DEFAULT_SHARED_PREFERENCES = "org.cafydia.android" + "_preferences";

    private final String CONFIGURATION_DATABASES = "configuration_database";
    private final String CONFIGURATION_SHARED_PREFERENCES = "configuration_shared_preferences";

    @Override
    public void onCreate(){

        FileBackupHelper dbHelper = new FileBackupHelper(this,
                "../databases/" + SQLOpenHelperConfiguration.DATABASE_NAME,
                "../databases/" + SQLOpenHelperData.DATABASE_NAME,
                "../databases/" + SQLOpenHelperBedca.DATABASE_NAME);

        SharedPreferencesBackupHelper spHelper = new SharedPreferencesBackupHelper(this,
                DEFAULT_SHARED_PREFERENCES,
                BaselineBasal.SHARED_PREFERENCES_INSULIN_PATTERN,
                ActivityMealsSnacks.SHARED_PREFERENCES_SNACK_VALUES,
                BaselinePreprandial.SHARED_PREFERENCES_FUNCTION_KEY,
                BaselineFragment.MINIMUM_PREPRANDIAL_KEY,
                MetabolicFrameworkState.SHARED_PREFERENCES_METABOLIC_FRAMEWORK_STATE,
                HbA1cHelper.SHARED_PREFERENCES_HBA1C_KEY,
                HelpFragment.HELP_FRAGMENTS_TAG,
                BedcaSearch.SHARED_PREFERENCES_INITIALIZED,
                BaselinePreprandial.Fitter.BASELINE_PREPRANDIAL_FITTER_TAG,
                BaselinePreprandial.CalculateFromDatabase.CALCULATE_FROM_DATABASE_TAG
        );

        addHelper(CONFIGURATION_DATABASES, dbHelper);
        addHelper(CONFIGURATION_SHARED_PREFERENCES, spHelper);
    }

    @Override
    public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data, ParcelFileDescriptor newState) throws IOException {

        synchronized (ConfigurationDatabase.dbLock) {
            super.onBackup(oldState, data, newState);
        }

        synchronized (DataDatabase.dbLock) {
            super.onBackup(oldState, data, newState);
        }

        synchronized (BedcaLocalDatabase.dbLock) {
            super.onBackup(oldState, data, newState);
        }

    }
    @Override
    public void onRestore(BackupDataInput data, int appVersionCode,
                          ParcelFileDescriptor newState) throws IOException {

        synchronized (ConfigurationDatabase.dbLock){
            super.onRestore(data, appVersionCode, newState);
        }

        synchronized (DataDatabase.dbLock){
            super.onRestore(data, appVersionCode, newState);
        }

        synchronized (BedcaLocalDatabase.dbLock){
            super.onRestore(data, appVersionCode, newState);
        }

    }

}

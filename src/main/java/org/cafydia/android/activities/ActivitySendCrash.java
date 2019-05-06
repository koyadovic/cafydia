package org.cafydia.android.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;

import org.cafydia.android.R;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 *
 * thank you http://stackoverflow.com/a/19968400
 */
public class ActivitySendCrash extends Activity {
    private static boolean crashed = false;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // because without this, is opened three or more activities ActivitySendCrash
        if(crashed)
            finish();

        crashed = true;

        //requestWindowFeature(Window.FEATURE_NO_TITLE); // make a dialog without a titlebar
        setFinishOnTouchOutside(false); // prevent users from dismissing the dialog by tapping outside
        setContentView(R.layout.send_log);

        findViewById(R.id.bSend).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendLogFile();
            }
        });
    }

    @Override
    public void onStop(){
        super.onStop();
        crashed = false;
    }

    private void sendLogFile(){
        String fullName = extractLogToFile();

        if (fullName == null) {
            finish();
            return;
        }

        Intent intent = new Intent (Intent.ACTION_SEND);
        intent.setType ("plain/text"); // "message/rfc822"
        intent.putExtra (Intent.EXTRA_EMAIL, new String[] {"crashes@cafydia.org"});
        intent.putExtra (Intent.EXTRA_SUBJECT, "Cafydia crash report");
        intent.putExtra (Intent.EXTRA_STREAM, Uri.parse("file://" + fullName));
        intent.putExtra (Intent.EXTRA_TEXT, "Crash report attached."); // do this so some email clients don't complain about empty body.
        startActivity(intent);

        ActivitySendCrash.this.finish();
    }

    private String extractLogToFile(){
        // method as shown above
        PackageManager manager = this.getPackageManager();
        PackageInfo info = null;
        try {
            info = manager.getPackageInfo (this.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e2) {

        }
        String model = Build.MODEL;
        if (!model.startsWith(Build.MANUFACTURER))
            model = Build.MANUFACTURER + " " + model;

        // Make file name - file must be saved to external storage or it wont be readable by
        // the email app.
        String path = Environment.getExternalStorageDirectory() + "/";
        String fullName = path + "crash-report.txt";

        // Extract to file.
        File file = new File (fullName);
        InputStreamReader reader = null;
        FileWriter writer = null;
        try
        {
            // For Android 4.0 and earlier, you will get all app's log output, so filter it to
            // mostly limit it to your app's output.  In later versions, the filtering isn't needed.
            String cmd = "logcat -d -v time";

            // get input stream
            Process process = Runtime.getRuntime().exec(cmd);
            reader = new InputStreamReader (process.getInputStream());

            // write output stream
            writer = new FileWriter (file);
            String s = "***********************\n";
            s += "*** Cafydia Version ***\n";
            s += "***********************";
            s += "\nVersion name: " + (info != null ? info.packageName : "No name") + "\n\n";

            s += "**************\n";
            s += "*** Device ***\n";
            s += "**************";

            s += "\nOS Version: "      + System.getProperty("os.version")      + "(" + android.os.Build.VERSION.INCREMENTAL + ")";
            s += "\nOS API Level: "    + android.os.Build.VERSION.SDK_INT;
            s += "\nDevice: "          + android.os.Build.DEVICE;
            s += "\nModel (and Product): " + android.os.Build.MODEL            + " ("+ android.os.Build.PRODUCT + ")";

            s += "\nRELEASE: "         + android.os.Build.VERSION.RELEASE;
            s += "\nBRAND: "           + android.os.Build.BRAND;
            s += "\nDISPLAY: "         + android.os.Build.DISPLAY;
            //s += "\nCPU_ABI: "         + android.os.Build.CPU_ABI;
            //s += "\nCPU_ABI2: "        + android.os.Build.CPU_ABI2;
            s += "\nUNKNOWN: "         + android.os.Build.UNKNOWN;
            s += "\nHARDWARE: "        + android.os.Build.HARDWARE;
            s += "\nBuild ID: "        + android.os.Build.ID;
            s += "\nMANUFACTURER: "    + android.os.Build.MANUFACTURER;
            s += "\nSERIAL: "          + "No interesa"; //android.os.Build.SERIAL;
            s += "\nUSER: "            + android.os.Build.USER;
            s += "\nHOST: "            + android.os.Build.HOST;

            writer.write(s + "\n\n");
            writer.write("**************\n");
            writer.write("*** Logcat ***\n");
            writer.write("**************\n");

            char[] buffer = new char[10000];
            do
            {
                int n = reader.read (buffer, 0, buffer.length);
                if (n == -1)
                    break;
                writer.write (buffer, 0, n);
            } while (true);

            reader.close();
            writer.close();
        }
        catch (IOException e)
        {
            if (writer != null)
                try {
                    writer.close();
                } catch (IOException e1) {
                }
            if (reader != null)
                try {
                    reader.close();
                } catch (IOException e1) {
                }

            // You might want to write a failure message to the log here.
            return null;
        }

        String zipFilename = fullName + ".zip";

        zipFile(fullName, zipFilename);

        // delete the file not zipped.
        new File(fullName).delete();

        return zipFilename;
    }

    public void zipFile(String file, String zipFileName) {
        try {
            BufferedInputStream origin;
            FileOutputStream dest = new FileOutputStream(zipFileName);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(
                    dest));
            byte data[] = new byte[2048];

            FileInputStream fi = new FileInputStream(file);
            origin = new BufferedInputStream(fi, 2048);

            ZipEntry entry = new ZipEntry(file.substring(file.lastIndexOf("/") + 1));
            out.putNextEntry(entry);
            int count;

            while ((count = origin.read(data, 0, 2048)) != -1) {
                out.write(data, 0, count);
            }
            origin.close();

            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

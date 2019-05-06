package org.cafydia.android.util;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * Created by user on 28/04/15.
 */
public class CafydiaSDBackup {
    private static final String DST_DIRECTORY_NAME = "Cafydia-Backup";

    private static void doIt(Context c){
        File data = Environment.getDataDirectory();
        File sd = Environment.getExternalStorageDirectory();

        File dst = new File(sd, DST_DIRECTORY_NAME);
        if(!dst.exists())
            dst.mkdir();

        deleteDirectoryRecursively(dst);

        try {
            copyFolder(data, dst);
            new MyToast(c, "Done!");
        } catch (IOException e){
            new MyToast(c, "Failed!");
        }
    }

    private static void restoreData(Context c){
        File dst = Environment.getDataDirectory();
        File src = new File(Environment.getExternalStorageDirectory(), DST_DIRECTORY_NAME);

        deleteDirectoryRecursively(dst);

        try {
            copyFolder(src, dst);
            new MyToast(c, "Done!");
        } catch (IOException e){
            new MyToast(c, "Failed!");
        }

    }


    private static void deleteDirectoryRecursively(File directory){
        if (directory.exists() && directory.isDirectory()) {
            for(String ch : directory.list()){
                File child = new File(directory, ch);

                if(child.isDirectory()){
                    deleteDirectoryRecursively(child);
                }
                else if(child.isFile()){
                    deleteFile(child);
                }
            }
        }
    }

    private static void deleteFile(File file){
        if(file.exists())
            file.delete();
    }

    private static void deleteDirectoryContent(File directory){
        if (directory.exists() && directory.isDirectory()) {
            for (String file : directory.list()) {
                deleteFile(new File(directory, file));
            }
        }

    }


    private static void copyFolder(File src, File dest) throws IOException {
        if(src.isDirectory()){

            if(!dest.exists()){
                dest.mkdir();
            }

            String files[] = src.list();

            for (String file : files) {

                File srcFile = new File(src, file);
                File destFile = new File(dest, file);

                copyFolder(srcFile,destFile);
            }

        } else {

            FileInputStream in = new FileInputStream(src);
            FileOutputStream out = new FileOutputStream(dest);

            FileChannel iChannel = in.getChannel();
            FileChannel oChannel = out.getChannel();

            oChannel.transferFrom(iChannel, 0, iChannel.size());

            oChannel.close();
            iChannel.close();

            out.close();
            in.close();

        }
    }
}

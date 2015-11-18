package com.example.robert.bettersnapchat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * Created by Ryan on 10/28/2015.
 */
//Class for static methods that can be used in multiple places in the app
public class Utility {

    //Below variables and methods are a sloppy way of restoring background images on screen orientation change

    //utility in case files need to be cleared in app's main dir , can easily change path if need be
    public static void deleteFilesInAppDir(Context context)
    {
        try {
            String files[] = context.getFilesDir().list();
            for (int i = 0; i < files.length; ++i) {
                File f = new File(context.getFilesDir(), files[i]);
                f.delete();
            }
        }
        catch (Exception e) {
            generateAlertDialog(context, "Error", e.getMessage());
        }
    }

    //generate an alert dialog with specified title and message
    public static void generateAlertDialog(Context context, String title, String message)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message)
                .setTitle(title)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //do things
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    //get String representing file extension
    public static String getFileExt(String fileName) {
        try {
            return fileName.substring((fileName.lastIndexOf(".") + 1), fileName.length());
        }
        catch (Exception e)
        {
            return "";
        }
    }

    //copy file at source location to file at destination location
    public static void fileCopy(File src, File dst) throws IOException {
        FileInputStream inStream = new FileInputStream(src);
        FileOutputStream outStream = new FileOutputStream(dst);
        FileChannel inChannel = inStream.getChannel();
        FileChannel outChannel = outStream.getChannel();
        inChannel.transferTo(0, inChannel.size(), outChannel);
        inStream.close();
        outStream.close();
    }

    public static boolean isSupportedExtension(String ext, String[] comparisonArray)
    {
        for(int i = 0; i < comparisonArray.length; ++i)
        {
            if(ext.equals(comparisonArray[i]))
                return true;
        }
        return false;
    }
}

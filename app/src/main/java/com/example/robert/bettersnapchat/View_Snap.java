package com.example.robert.bettersnapchat;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.VideoView;

import java.io.File;
import java.io.IOException;

//This class is used for handling the 'View Snap' screen
public class View_Snap extends AppCompatActivity {

    Button saveBtn;
    Button deleteBtn;
    Button replyBtn;
    File currentSnap;
    String filePath;
    VideoView video_area;
    final static String EXT_STORAGE_DIR_NAME = "Test_SnapChat_Folder";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view__snap);

        //find the 3 buttons and video area needed for view snap screen
        saveBtn = (Button) findViewById(R.id.view_snap_save);
        deleteBtn = (Button) findViewById(R.id.view_snap_delete);
        replyBtn = (Button) findViewById(R.id.view_snap_reply);
        video_area = (VideoView) findViewById(R.id.view_snap_video_area);

        //bring each of these to front so that the video area does not cover them
        saveBtn.bringToFront();
        deleteBtn.bringToFront();
        replyBtn.bringToFront();

        //currently lists all files found in the app's default directory, searches for two different filenames and opens the first it finds (should only ever be one)
        //currently filename must be hardcoded as one of the options below, shouldn't be a problem even in final implementation
        try {
            String files[] = getFilesDir().list(); //list all files in app default directory, store in array
            for (int i = 0; i < files.length; ++i) { //loop through them (in case there are multiple) until a snap file is found
                if(files[i].equals("current_snap.png")) //picture file
                {
                    filePath = getFilesDir() + "/" + files[i];
                    loadPictureSnap(filePath);
                    currentSnap = new File(filePath);
                    break;
                }
                else if(files[i].equals("current_snap.mp4")) //video file
                {
                    filePath = getFilesDir() + "/" + files[i];
                    loadVideoSnap(filePath);
                    currentSnap = new File(filePath);
                    break;
                }

                //if we've gone through all the files and did not find the standard picture or video format, print error
                if((i+1) == files.length){
                    Utility.generateAlertDialog(this, "Error", "Did not find current_snap.png or current_snap.mp4");
                    currentSnap = null;
                }
            }
        }
        catch (Exception e) {
            Utility.generateAlertDialog(this, "Error", e.getMessage());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_view__snap, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //if snap is detected as a .png , set background as the png
    public void loadPictureSnap(String filePath)
    {
        Drawable new_bg = Drawable.createFromPath(filePath);
        final int sdk = android.os.Build.VERSION.SDK_INT;
        if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            video_area.setBackgroundDrawable(new_bg);
        }
        else {
            video_area.setBackground(new_bg);
        }
    }

    //if snap is detected as a .mp4 , play the video on loadup
    public void loadVideoSnap(String filePath){
        video_area.setVideoPath(filePath);
        video_area.start();
    }

    //save file to external storage location, which is determined by result of getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) from the saveToExternal method
    public void saveSnap(View view)
    {
        saveBtn.setText("Saving...");
        try {
            String newFileLocation = saveSnapToExternal(currentSnap); //save the file
            if (!newFileLocation.equals("")) { //if we get a response with a path to the new file
                saveBtn.setEnabled(false);
                saveBtn.setText("Saved");
                deleteBtn.setText("Close");
                Utility.generateAlertDialog(this, "Saved", "Snap saved at " + newFileLocation); //create popup to inform user where the file was saved to
            }
            else {
                Utility.generateAlertDialog(this, "Error", "Error saving file");
            }
        }
        catch(Exception e)
        {
            Utility.generateAlertDialog(this, "Error", "No current snap detected");
            saveBtn.setText("Error Saving");
            saveBtn.setEnabled(false);
        }
    }

    //currently deletes the copy of the test file in the app's file area, then kills the program
    //eventually should just delete and redirect back to home screen
    public void deleteSnap(View view)
    {
        deleteBtn.setText("Deleting...");
        currentSnap.delete();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    //currently copies the test file into the app's file area
    //eventually will set the snap sender as the receiver on a new snap, and redirect to take snap screen
    public void reply(View view) {
        replyBtn.setText("Replying...");
        currentSnap.delete();
        //somehow notify appropriate part of the program with the receiver for the new snap being sent as the reply

        //redirect to the take snap screen here
        startActivity(new Intent(View_Snap.this, Sample_TakeSnap.class));
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    //save the File associated to current_snap to the external public pictures directory, return absolute path of the directory where the file was saved
    private String saveSnapToExternal(File fileToSave) {
        Long tsLong = System.currentTimeMillis()/1000; //use timestamp to generate unique filename
        String ts = tsLong.toString();
        String fileExt = Utility.getFileExt(currentSnap.getName()); //get the file extension of the current snap

        File pathToNewFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "/" + EXT_STORAGE_DIR_NAME + "/" + ts + "." + fileExt); //will be the path to new file
        File externalDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/" + EXT_STORAGE_DIR_NAME); //get public external pictures directory
        if(!externalDir.exists()) //if external app directory does not already exist
        {
            externalDir.mkdirs(); //create external storage directory
        }
        try {
            Utility.fileCopy(fileToSave, pathToNewFile);
        }
        catch(IOException ioe){
            Utility.generateAlertDialog(this, "Error", ioe.getMessage());
            return "";
        }
        return externalDir.getAbsolutePath();
    }
}

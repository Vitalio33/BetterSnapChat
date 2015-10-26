package com.example.robert.bettersnapchat;

/**
 * Created by Robert on 10/24/2015.
 */


        import android.content.Intent;
        import android.graphics.Color;
        import android.graphics.drawable.Drawable;
        import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;
        import android.support.v7.app.NotificationCompat;
        import android.view.Menu;
        import android.view.MenuItem;
        import android.view.View;
        import android.widget.Button;
        import android.widget.RelativeLayout;
        import android.widget.TextView;
        import android.widget.VideoView;

        import java.io.BufferedReader;
        import java.io.File;
        import java.io.FileInputStream;
        import java.io.FileNotFoundException;
        import java.io.FileOutputStream;
        import java.io.FileReader;
        import java.io.IOException;
        import java.nio.channels.FileChannel;

//This class is used for handling the 'View Snap' screen
public class View_Snap extends AppCompatActivity {

    Button saveBtn;
    Button deleteBtn;
    Button replyBtn;
    //TextView logArea;
    File currentSnap;
    String filePath;
    VideoView video_area;

    //temporary method used to show error messages on screen
    public void logWrite(String string)
    {
        //logArea.append(string + "\n");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_snap);

        //just for debugging purposes , write output here with logWrite()
        //logArea = (TextView) findViewById(R.id.logArea);
        //logArea.setTextColor(Color.RED);

        //find the 3 buttons and video area needed for view snap screen
        saveBtn = (Button) findViewById(R.id.view_snap_save);
        deleteBtn = (Button) findViewById(R.id.view_snap_delete);
        replyBtn = (Button) findViewById(R.id.view_snap_reply);
        video_area = (VideoView) findViewById(R.id.view_snap_video_area);

        //bring each of these to front so that the video area does not cover them
        //logArea.bringToFront();
        saveBtn.bringToFront();
        deleteBtn.bringToFront();
        replyBtn.bringToFront();

        //currently lists all files found in the app's default directory, searches for two different filenames and opens the first it finds (should only ever be one)
        //currently filename must be hardcoded as one of the options below, shouldn't be a problem even in final implementation
        try {
            String files[] = getFilesDir().list(); //list all files in app default directory, store in array
            for (int i = 0; i < files.length; ++i) { //loop through them (in case there are multiple) until a snap file is found
                logWrite(getFilesDir() + "/" + files[i]);
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
                    logWrite("did not find current_snap.png or current_snap.mp4");
                    currentSnap = null;
                }
            }
        }
        catch (Exception e) {
            logWrite(e.getMessage());
        }

    }

    //utility in case files need to be cleared in app's main dir , can easily change path if need be
    public void deleteFilesInAppDir()
    {
        try {
            String files[] = getFilesDir().list();
            for (int i = 0; i < files.length; ++i) {
                logWrite(getFilesDir() + "/" + files[i]);
                currentSnap = new File(getFilesDir(), files[i]);
                currentSnap.delete();
                logWrite("deleted: " + files[i]);
            }
        }
        catch (Exception e) {
            logWrite(e.getMessage());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //will eventually copy file to permanent or semi-permanent storage
    public void saveSnap(View view)
    {
        saveBtn.setText("Saving...");
        //determine where to store the file and save a copy in that location

        saveBtn.setEnabled(false);
        saveBtn.setText("Saved");
        deleteBtn.setText("Close");
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
    public void reply(View view)
    {
        replyBtn.setText("Replying...");
        //somehow notify appropriate part of the program with the receiver for the new snap being sent as the reply

        //redirect to the take snap screen here
        startActivity(new Intent(View_Snap.this,Take_Snap.class));
    }

    //copy file at source location to file at destination location
    //keep this for the 'Save' feature
    public void fileCopy(File src, File dst) throws IOException {
        FileInputStream inStream = new FileInputStream(src);
        FileOutputStream outStream = new FileOutputStream(dst);
        FileChannel inChannel = inStream.getChannel();
        FileChannel outChannel = outStream.getChannel();
        inChannel.transferTo(0, inChannel.size(), outChannel);
        inStream.close();
        outStream.close();
    }

    //if snap is detected as a .mp4 , play the video on loadup
    public void loadVideoSnap(String filePath){
        video_area.setVideoPath(filePath);
        video_area.start();
    }


}

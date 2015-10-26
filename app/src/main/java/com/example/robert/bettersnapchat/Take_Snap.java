package com.example.robert.bettersnapchat;

/**
 * Created by Robert on 10/25/2015.
 */
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.File;

public class Take_Snap extends AppCompatActivity {

    Button importBtn;
    Button exitButton;
    static final int FILE_RECEIVE_CODE = 4321;
    VideoView video_area;
    TextView logArea;

    public void logWrite(String string)
    {
        logArea.append(string + "\n");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_snap);

        logArea = (TextView) findViewById(R.id.take_snap_log_area);
        logArea.setTextColor(Color.RED);

        importBtn = (Button) findViewById(R.id.import_button);
        exitButton = (Button) findViewById(R.id.take_snap_exit);
        video_area = (VideoView) findViewById(R.id.take_snap_video_view);

        importBtn.bringToFront();
        exitButton.bringToFront();
        logArea.bringToFront();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        logWrite("response received, requestCode = " + requestCode + " resultCode = " + resultCode + " data = " + data.getData());
        if (requestCode == FILE_RECEIVE_CODE || resultCode == FILE_RECEIVE_CODE) {
            //set selected file as background
            logWrite("code recognized");
            Uri selectedFile = data.getData();
            //File myFile = new File(selectedFile.toString());
            //String path = myFile.getAbsolutePath();
            String path = data.getData().getPath();

            final int sdk = android.os.Build.VERSION.SDK_INT;
            logWrite("path is " + path);
            Drawable new_bg = Drawable.createFromPath(path);

            if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                video_area.setBackgroundDrawable(new_bg);
            }
            else {
                video_area.setBackground(new_bg);
            }
        }
    }

    public void takeSnapExit(View view)
    {
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    public void importSnap(View view)
    {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        //intent.addCategory("com.estrongs.action.PICK_FILE");
        intent.setType("file/*");
        startActivityForResult(intent, FILE_RECEIVE_CODE);

    }
}

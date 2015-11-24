package com.example.robert.bettersnapchat;

/**
 * Created by Robert on 10/25/2015.
 */
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class Take_Snap extends AppCompatActivity {

    Button importBtn;
    Button exitButton;
    Button testSendButton;
    static final int FILE_RECEIVE_CODE = 4321;
    boolean fileIsImported = false;
    VideoView video_area;
    TextView logArea;
    Uri snapToSend;

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
        testSendButton = (Button) findViewById(R.id.take_snap_send);
        video_area = (VideoView) findViewById(R.id.take_snap_video_view);

        importBtn.bringToFront();
        exitButton.bringToFront();
        testSendButton.bringToFront();
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

            //Uri selectedFile = data.getData();
            snapToSend = data.getData();
            final int sdk = android.os.Build.VERSION.SDK_INT;

            try {
                //InputStream inStream = getContentResolver().openInputStream(selectedFile);
                InputStream inStream = getContentResolver().openInputStream(snapToSend);
                Bitmap bitmap = BitmapFactory.decodeStream(inStream);
                BitmapDrawable bitmapDrawable = new BitmapDrawable(bitmap);

                if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    video_area.setBackgroundDrawable(bitmapDrawable);

                } else {
                    video_area.setBackground(bitmapDrawable);
                }
                if(Utility.getFileExt(snapToSend.getPath()).equals("mp4")) {
                    video_area.setVideoPath(snapToSend.toString());
                    video_area.start(); //used so that there will be a 'thumbnail' preview if the user attaches a video
                }
            }
            catch(FileNotFoundException fnf)
            {
                logWrite("file not found");
            }
        }
        importBtn.setText("Remove File");
        fileIsImported = true;
    }

    public void takeSnapExit(View view)
    {
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    public void importSnap(View view)
    {
        if(fileIsImported){
            video_area.pause();
            //set the screen view back to the camera here

            fileIsImported = false;
            importBtn.setText("Import File");
        }
        else {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            startActivityForResult(intent, FILE_RECEIVE_CODE);
        }
    }

    public void sendTestMessage(View view) {
        testSendButton.setEnabled(false);
        //figure out how to send a message

        String phoneNumber = "9999999999";
        String smsBody = "This is an SMS!";

        String SMS_SENT = "SMS_SENT";
        String SMS_DELIVERED = "SMS_DELIVERED";

        PendingIntent sentPendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(SMS_SENT), 0);
        PendingIntent deliveredPendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(SMS_DELIVERED), 0);

        // For when the SMS has been sent
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(context, "SMS sent successfully", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(context, "Generic failure cause", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(context, "Service is currently unavailable", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(context, "No pdu provided", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(context, "Radio was explicitly turned off", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(SMS_SENT));

        // For when the SMS has been delivered
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS delivered", Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(getBaseContext(), "SMS not delivered", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(SMS_DELIVERED));

        // Get the default instance of SmsManager
        SmsManager smsManager = SmsManager.getDefault();
        // Send a text based SMS
        smsManager.sendTextMessage(phoneNumber, null, smsBody, sentPendingIntent, deliveredPendingIntent);
     }
    }
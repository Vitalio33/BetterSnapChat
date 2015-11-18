package com.example.robert.bettersnapchat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.VideoView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

public class Sample_TakeSnap extends AppCompatActivity {

    static final int FILE_RECEIVE_CODE = 4321; //expected return code when file has been selected for import
    public static final String[] VALID_IMAGE_FORMATS = {"jpg","png","gif","bmp","webp","jpeg"};
    public static final String[] VALID_VIDEO_FORMATS = {"mp4","3gp","webm","mkv","ts"};
    public static final String PICTURE_SNAP_FILENAME = "/current_snap.jpeg";
    public static final String VIDEO_SNAP_FILENAME = "/current_snap.mp4";
    public static final int MAX_VIDEO_LENGTH_MS = 10000;
    final int sdk = android.os.Build.VERSION.SDK_INT;

    static BitmapDrawable savedTakeSnapBackgroundImage; //allows us to 'cache' background image in the case of screen orientation change
    static String savedTakeSnapBackgroundVideoPath; //allows us to 'cache' background video path in the case of screen orientation change
    public static String currentUserTextValue; //save any 'add text' message the user has typed

    MediaRecorder.OnInfoListener infoListener;

    Context context;
    Camera camera;
    SurfaceHolder surfaceHolder;
    SurfaceView surfaceView;
    Camera.PictureCallback jpeg;
    MediaRecorder mediaRecorder;
    VideoView video_area;

    ProgressBar progressBar;

    Button takesnap;
    Button clear;
    Button attach;
    Button draw;
    Button addtext;
    ToggleButton choice;
    EditText userText;

    boolean previewing;
    boolean recording;
    boolean stat;
    boolean fileIsImported = false;

    /* Button onClick methods below */

    public void toggleCameraVideo(View view)
    {
        //
    }

    public void addText(View view)
    {
        addtext.setEnabled(false);
        userText.setVisibility(View.VISIBLE);
        userText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(userText, InputMethodManager.SHOW_IMPLICIT);
    }

    public void addDrawing(View view)
    {
        Utility.generateAlertDialog(this, "Add Drawing", "You clicked the pen tool");
    }

    public void clearScreen(View view)
    {
        try {
            camera.stopPreview();
            userText.setText("");
            takesnap.setText("");
            userText.setVisibility(View.GONE);
            video_area.setVisibility(View.GONE);
            deleteCurrentSnapFile();
            if(fileIsImported)
                importSnap(null); //clear button will remove any attachments present
            else {
                takesnap.setEnabled(true);
                choice.setEnabled(true);
                addtext.setEnabled(true);
            }
        }
        catch (NullPointerException e) {
            Utility.generateAlertDialog(this,"Error","Error clearing screen");
        }
        previewing = true;
        createPreview(camera, surfaceHolder);
    }

    public void importSnap(View view)
    {
        if(fileIsImported){
            //set the screen view back to the camera here
            fileIsImported = false;
            enableButtonsVideoImportVersion(); //re-enable all buttons
            savedTakeSnapBackgroundImage = null;
            savedTakeSnapBackgroundVideoPath = null;
            video_area.setVisibility(View.GONE);
            clearScreen(view);
        }
        else {
            //start an intent for a file selection application
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            startActivityForResult(intent, FILE_RECEIVE_CODE);
        }
    }

    public void takeSnap(View view)
    {
        if (previewing) {
            if (choice.getText().equals("Picture")) {
                camera.takePicture(null, null, jpeg);
            }
            else {
                if (!recording) {
                    //While the video is recording, most buttons should be disabled
                    choice.setEnabled(false);
                    draw.setEnabled(false);
                    attach.setEnabled(false);
                    addtext.setEnabled(false);
                    clear.setEnabled(false);

                    String filePath = context.getFilesDir() + VIDEO_SNAP_FILENAME; //Define the storage location for the video

                    //Below calls do configuration for the mediaRecorder
                    mediaRecorder = new MediaRecorder();
                    camera.unlock();
                    mediaRecorder.setOnInfoListener(infoListener);
                    mediaRecorder.setCamera(camera);
                    mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
                    mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
                    mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
                    mediaRecorder.setOutputFile(filePath);
                    mediaRecorder.setPreviewDisplay(surfaceHolder.getSurface());
                    mediaRecorder.setMaxDuration(MAX_VIDEO_LENGTH_MS);

                    //Prepare and start the video using the media recorder
                    try {
                        mediaRecorder.prepare();
                        stat = true;
                    } catch (IllegalStateException e) {
                        releaseMediaRecorder(mediaRecorder, camera);
                        Toast.makeText(context, "IllegalState", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        releaseMediaRecorder(mediaRecorder, camera);
                        Toast.makeText(context, "IOEXCEPTION", Toast.LENGTH_SHORT).show();
                    } catch (RuntimeException e) {
                        stat = false;
                        Toast.makeText(context, "" + e, Toast.LENGTH_SHORT).show(); //MediaRecorder.start() causes this exception
                    }
                    catch (Exception e)
                    {
                        Utility.generateAlertDialog(context,"Media Failure",e.toString());
                    }
                    progressBar = startProgressBarTimer();
                    //progressBar.setProgress(0);
                    mediaRecorder.start();
                    recording = true;
                }
                else {
                    if (mediaRecorder != null && stat == true) {
                        mediaRecorder.stop();
                    }
                    try {
                        progressBar.setVisibility(View.GONE);
                        //progressBar.setProgress(0);
                        releaseMediaRecorder(mediaRecorder, camera);
                        camera.reconnect();
                        camera.stopPreview();
                        recording = false;
                        previewing = false;
                        clear.setEnabled(true);
                        //choice.setEnabled(true);
                        draw.setEnabled(true);
                        attach.setEnabled(true);
                        addtext.setEnabled(true);
                        takesnap.setText("Playback");
                        savedTakeSnapBackgroundVideoPath = context.getFilesDir() + VIDEO_SNAP_FILENAME;
                    }
                    catch (Exception e)
                    {
                        Utility.generateAlertDialog(this,"ERROR","Error restoring the camera");
                    }
                }
            }
        }
        else if(takesnap.getText().equals("Playback"))
        {
            takesnap.setText("Stop");
            video_area.setVideoPath(savedTakeSnapBackgroundVideoPath);
            video_area.setVisibility(View.VISIBLE);
            video_area.start();
        }
        else if(takesnap.getText().equals("Stop"))
        {
            video_area.stopPlayback();
            takesnap.setText("Playback");
        }
    }

    /* End of Button onClick methods */
    /* Methods automatically invoked by Android below */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Utility.generateAlertDialog(this,"OC","onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample__take_snap);

        context = this;
        camera = null;

        takesnap = (Button) findViewById(R.id.takesnap);
        clear = (Button) findViewById(R.id.btnClear);
        attach = (Button) findViewById(R.id.btnAttach);
        draw = (Button) findViewById(R.id.btnDraw);
        addtext = (Button) findViewById(R.id.btnAddText);
        choice = (ToggleButton) findViewById(R.id.choose);

        userText = (EditText) findViewById(R.id.editTextField);
        userText.setTextColor(Color.WHITE);
        userText.setVisibility(View.GONE);

        video_area = (VideoView) findViewById(R.id.take_snap_video_area);
        surfaceView = (SurfaceView) findViewById(R.id.surfview);
        surfaceHolder = surfaceView.getHolder();
        recording = false;
        previewing = true;
        stat = false;

        takesnap.bringToFront();
        clear.bringToFront();
        attach.bringToFront();
        draw.bringToFront();
        addtext.bringToFront();
        choice.bringToFront();

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    //check if there was a previously saved background image or video, which implies that the activity was reloaded due to screen orientation change
                    if (currentUserTextValue != null && !currentUserTextValue.equals("")) {
                        addtext.setEnabled(false);
                        userText.setVisibility(View.VISIBLE);
                        userText.setText(currentUserTextValue);
                    }
                    if (savedTakeSnapBackgroundVideoPath != null) { //reload previously attached video
                        disableButtonsVideoImportVersion();
                        fileIsImported = true;
                        previewing = false;
                        takesnap.setText("Playback");
                        video_area.setVideoPath(savedTakeSnapBackgroundVideoPath);
                        video_area.start();
                    } else if (savedTakeSnapBackgroundImage != null) { //reload previously attached image
                        disableButtonsImageImportVersion();
                        fileIsImported = true;
                        previewing = false;
                        setScreenBackground(savedTakeSnapBackgroundImage);
                    } else { //if no cached snap, load camera preview
                        camera.setPreviewDisplay(surfaceHolder);
                        camera.startPreview();
                    }
                } catch (IOException e) {
                }
            }

            public void surfaceDestroyed(SurfaceHolder holder) {
                //Do nothing when the surface is destroyed
            }

            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                //If the surface is changed try stopping the current preview and setting/starting a new one
            }
        });

        jpeg = new Camera.PictureCallback() {
            public void onPictureTaken(byte[] data, Camera cam)
            {
                try {
                    FileOutputStream fos = new FileOutputStream(context.getFilesDir() + PICTURE_SNAP_FILENAME);
                    fos.write(data);
                    fos.close();
                    Bitmap bitmap = BitmapFactory.decodeFile(context.getFilesDir() + PICTURE_SNAP_FILENAME); //create a bitmap from the input stream
                    BitmapDrawable bitmapDrawable = new BitmapDrawable(bitmap);
                    savedTakeSnapBackgroundImage = bitmapDrawable; //'cache' the background image in case user changes screen orientation
                    fileIsImported = true;
                    previewing = false;
                    disableButtonsImageImportVersion();
                    savedTakeSnapBackgroundVideoPath = null;
                    //File source = new File(context.getFilesDir() + "/current_snap.jpeg");
                    //File dest = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "/" + View_Snap.EXT_STORAGE_DIR_NAME + "/TEST.jpeg");
                    //Utility.fileCopy(source, dest);
                }
                catch(Exception e)
                {
                    Utility.generateAlertDialog(context,"Picture Error","Could not save picture to app dir: " + e.toString());
                }
            }
        };

        infoListener = new MediaRecorder.OnInfoListener() {
            @Override
            public void onInfo(MediaRecorder mr, int code, int extra) {
                if(code == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED)
                    takesnap.performClick();
            }
        };
    }

    @Override
    public void onResume() {
        //Utility.generateAlertDialog(this,"OR","onResume");
        try {
            super.onResume();
            camera = null;
            try {
                camera = Camera.open();
            } catch (NullPointerException e) {
            }
            if (camera != null) {
                createPreview(camera, surfaceHolder);
            }
        }
        catch(Exception e)
        {
            Utility.generateAlertDialog(this,"Failure",e.toString());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (camera != null) {
            camera.stopPreview();
            camera.release();
        }
        if(!userText.getText().equals(""))
            currentUserTextValue = userText.getText().toString();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == FILE_RECEIVE_CODE) { //if this is the response to a request for a file import
            try {
                Uri imported = data.getData(); //get the content uri from the file selection app

                String fileString[] = getContentResolver().getType(imported).split("/",2); //split imported file into category and extension
                String fileType = fileString[1]; //get the extension of the file

                //if this is not a supported image or video file extension
                if(!(Utility.isSupportedExtension(fileType,VALID_IMAGE_FORMATS) || Utility.isSupportedExtension(fileType,VALID_VIDEO_FORMATS)))
                {
                    Utility.generateAlertDialog(this,"File Type Error","Unsupported file type");
                    return;
                }

                InputStream inStream = getContentResolver().openInputStream(imported); //create an input stream
                Bitmap bitmap = BitmapFactory.decodeStream(inStream); //create a bitmap from the input stream
                BitmapDrawable bitmapDrawable = new BitmapDrawable(bitmap);

                //Set bitmap as background image of screen
                video_area.setVisibility(View.VISIBLE);
                setScreenBackground(bitmapDrawable);
                savedTakeSnapBackgroundImage = bitmapDrawable; //'cache' the background image in case user changes screen orientation
                fileIsImported = true;
                previewing = false;

                //if this is a video file, play it once imported
                if(Utility.isSupportedExtension(fileType, VALID_VIDEO_FORMATS)) {
                    disableButtonsVideoImportVersion();
                    savedTakeSnapBackgroundVideoPath = imported.toString(); //'cache' the video url in case user changes screen orientation
                    savedTakeSnapBackgroundImage = null;
                    video_area.setVideoPath(savedTakeSnapBackgroundVideoPath);
                    video_area.start();
                }
                else
                {
                    disableButtonsImageImportVersion();
                    savedTakeSnapBackgroundVideoPath = null;
                }
            }
            catch(FileNotFoundException fnf)
            {
                Utility.generateAlertDialog(this, "File not found", "file not found error");
            }
            catch (Exception e)
            {
                Utility.generateAlertDialog(this,"Return Error","Couldn't get current snap data");
                return;
            }

        }
        //other receive codes can be checked below
    }

    /* End of methods automatically invoked by Android */
    /* Helper methods below */

    public void disableButtonsVideoImportVersion()
    {
        attach.setEnabled(false);
        choice.setEnabled(false);
    }

    public void enableButtonsVideoImportVersion()
    {
        enableButtonsImageImportVersion();
        draw.setEnabled(true);
        addtext.setEnabled(true);
    }

    public void disableButtonsImageImportVersion()
    {
        attach.setEnabled(false);
        choice.setEnabled(false);
        takesnap.setEnabled(false);
    }

    public void enableButtonsImageImportVersion()
    {
        attach.setEnabled(true);
        choice.setEnabled(true);
        takesnap.setEnabled(true);
    }

    public void createPreview(Camera cam, SurfaceHolder surfhold) {
        try {
            cam.setPreviewDisplay(surfhold);
            cam.startPreview();
        } catch (IOException e) {
        }
    }

    public void releaseMediaRecorder(MediaRecorder mr, Camera cam) {
        if (mr != null) {
            mr.reset();
            mr.release();
            mr = null;
            cam.lock();
        }
    }

    public void setScreenBackground(BitmapDrawable bd)
    {
        //Set bitmap as background image of screen
        if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            video_area.setBackgroundDrawable(bd);
        } else {
            video_area.setBackground(bd);
        }
    }

    public void deleteCurrentSnapFile()
    {
        File pictureFile = new File(context.getFilesDir() + PICTURE_SNAP_FILENAME);
        File videoFile = new File(context.getFilesDir() + VIDEO_SNAP_FILENAME);
        if(videoFile.exists())
            videoFile.delete();
        if(pictureFile.exists())
            pictureFile.delete();
    }

    public ProgressBar startProgressBarTimer()
    {
        final int TIMER_FREQ = MAX_VIDEO_LENGTH_MS / 10;

        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        //progressBar.setProgress(0);
        progressBar.setMax(MAX_VIDEO_LENGTH_MS);

        Timer progressBarAdvancer = new Timer();
        progressBarAdvancer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                progressBar.setProgress(progressBar.getProgress() + TIMER_FREQ);
            }
        }, 0, TIMER_FREQ);

        progressBar.setVisibility(View.VISIBLE);
        return progressBar;
    }

    /* End of helper methods */
}


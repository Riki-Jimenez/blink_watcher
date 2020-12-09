package com.example.blink_watcher_app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    ConstraintLayout background;
    TextView user_message;

    boolean flag = false;
    CameraSource cameraSource;

    Button awake_button;
    MediaPlayer mp;
    PowerManager.WakeLock wakeLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PowerManager powerManager = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "blink_watcher:wakelock");

        awake_button = (Button) findViewById(R.id.awake_button);
        awake_button.setOnClickListener(v -> wake_alarm_stop());

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, 1);
            Toast.makeText(this, "Permission not granted!\n Grant permission and restart app", Toast.LENGTH_SHORT).show();
        }else{
            init();
        }
    }

    private void init() {
        background = findViewById(R.id.background);
        user_message = findViewById(R.id.user_text);
        flag = true;
        mp = MediaPlayer.create(this, R.raw.wakeup_alarm);

        initCameraSource();
    }

    //method to create camera source from faceFactoryDaemon class
    private void initCameraSource() {
        FaceDetector detector = new FaceDetector.Builder(this)
                .setTrackingEnabled(true)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .setMode(FaceDetector.FAST_MODE)
                .build();
        detector.setProcessor(new MultiProcessor.Builder(new FaceTrackerDaemon(MainActivity.this)).build());

        cameraSource = new CameraSource.Builder(this, detector)
                .setRequestedPreviewSize(1024, 768)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setRequestedFps(10.0f)
                .build();

        try {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            cameraSource.start();
        }
        catch (IOException e) {
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (cameraSource != null) {
            try {
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                wakeLock.acquire();
                cameraSource.start();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (cameraSource!=null) {
            cameraSource.stop();
        }

        setBackgroundGrey();
        wakeLock.release();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraSource!=null) {
            cameraSource.release();
        }
        wakeLock.release();
    }

    //update view
    public void updateMainView(Condition condition){
        switch (condition){
            case USER_EYES_OPEN:
                setBackgroundGreen();
                user_message.setText("Open eyes detected");
                break;
            case USER_EYES_CLOSED:
                setBackgroundOrange();
                user_message.setText("Close eyes detected");
                break;
            case FACE_NOT_FOUND:
                setBackgroundRed();
                user_message.setText("User not found");
                break;
            case USER_EMERGENCY:
                setBackgroundEmergency();
                user_message.setText("EMERGENCY CONDITION FOUND!");
                wake_alarm_start();

            default:
                setBackgroundGrey();
                user_message.setText("Please after permissions are granted restart the App");
        }
    }

    //set background Grey
    private void setBackgroundGrey() {
        if(background != null)
            background.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
    }

    //set background Green
    private void setBackgroundGreen() {
        if(background != null){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    background.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
                }
            });
        }
    }

    //set background Orange
    private void setBackgroundOrange() {
        if(background != null){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    background.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_dark));
                }
            });
        }
    }

    //set background Red
    private void setBackgroundRed() {
        if(background != null){
            runOnUiThread(new Runnable() {
                    @Override
                public void run() {
                    background.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
                }
            });
        }
    }

    //set background Emergency
    private void    setBackgroundEmergency() {
        if(background != null){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    background.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_light));
                }
            });
        }
    }

    // Start Alarm
    public void wake_alarm_start(){
        mp.start();
        awake_button.setVisibility(View.VISIBLE);
        cameraSource.stop();
    }

    //Stop alarm
    public void wake_alarm_stop(){
        mp.stop();
        awake_button.setVisibility(View.INVISIBLE);

       init();
    }

}

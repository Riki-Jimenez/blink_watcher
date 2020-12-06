package com.example.blink_watcher_app;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;

import static androidx.constraintlayout.widget.Constraints.TAG;


public class EyesTracker extends Tracker<Face> {
    // Set confidence threshold
    private final float THRESHOLD = 0.75f;
    // Seconds with eye close to trigger the alarm
    private static final int BLINK_LIMIT = 15;
    // Minimum freq ration of blinking
    private static final float FREQ_LIMIT = 0.4f;
    // Interval of freq measure
    private static final int AVERAGE_INTERVAL = 30;
    // Counter of events
    private int Counter = 0;
    private int BlinkCounter = 0;
    private int CloseCounter = 0;
    private float freq;

    private Context context;


    public EyesTracker(Context context) {
        this.context = context;
    }
    @Override
    public void onUpdate(Detector.Detections<Face> detections, Face face) {
        // Counter increments every time a face is recognized
        Counter++;
        if (face.getIsLeftEyeOpenProbability() > THRESHOLD || face.getIsRightEyeOpenProbability() > THRESHOLD) {
            Log.i(TAG, "onUpdate: Open Eyes Detected");
            ((MainActivity)context).updateMainView(Condition.USER_EYES_OPEN);
            CloseCounter = 0;

        }else {
            Log.i(TAG, "onUpdate: Close Eyes Detected");
            ((MainActivity)context).updateMainView(Condition.USER_EYES_CLOSED);
            BlinkCounter++;
            CloseCounter++;
            if (CloseCounter>=BLINK_LIMIT){
                Log.i(TAG, "ALARM: WAKE UP!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                ((MainActivity)context).updateMainView(Condition.USER_EMERGENCY);
            }

        }

        if (Counter==AVERAGE_INTERVAL){
            freq = (float)(BlinkCounter)/(float)(Counter);
            if (freq>=FREQ_LIMIT){
                Log.i(TAG, "ALARM: YOU ARE TIRED!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                ((MainActivity)context).updateMainView(Condition.USER_EMERGENCY);
            }
            Counter = 0;
            BlinkCounter = 0;

        }

    }
    @Override
    public void onMissing(Detector.Detections<Face> detections) {
        super.onMissing(detections);
        Log.i(TAG, "onUpdate: Face Not Detected yet!");
        BlinkCounter = 0;
        Counter = 0;
        ((MainActivity)context).updateMainView(Condition.FACE_NOT_FOUND);
    }

    @Override
    public void onDone() {
        super.onDone();
    }
}

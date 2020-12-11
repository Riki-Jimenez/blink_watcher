package com.example.blink_watcher_app;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;

import java.nio.channels.ClosedSelectorException;

import static androidx.constraintlayout.widget.Constraints.TAG;


public class EyesTracker extends Tracker<Face> {
    // Set confidence threshold
    private final float THRESHOLD = 0.75f;
    // Seconds with eye close to trigger the alarm
    private static final int BLINK_LIMIT = 15;
    private int CloseCounter = 0;

    private Context context;


    public EyesTracker(Context context) {
        this.context = context;
    }
    @Override
    public void onUpdate(Detector.Detections<Face> detections, Face face) {
        if (face.getIsLeftEyeOpenProbability() > THRESHOLD || face.getIsRightEyeOpenProbability() > THRESHOLD) {
            Log.i(TAG, "onUpdate: Open Eyes Detected");
            CloseCounter = 0;
            ((MainActivity)context).updateMainView(Condition.USER_EYES_OPEN);


        }else {
            Log.i(TAG, "onUpdate: Close Eyes Detected");
            CloseCounter++;
            if (CloseCounter>=BLINK_LIMIT){
                Log.i(TAG, "ALARM: WAKE UP!");
                ((MainActivity)context).updateMainView(Condition.USER_EMERGENCY);
            }
            ((MainActivity)context).updateMainView(Condition.USER_EYES_CLOSED);
        }

        if(CloseCounter==Integer.MAX_VALUE-1){
            //Reset counters to avoid overflow
            CloseCounter = 0;
        }
    }
    @Override
    public void onMissing(Detector.Detections<Face> detections) {
        super.onMissing(detections);
        Log.i(TAG, "onUpdate: Face Not Detected yet!");
        CloseCounter = 0;
        ((MainActivity)context).updateMainView(Condition.FACE_NOT_FOUND);
    }

    @Override
    public void onDone() {
        super.onDone();
    }
}

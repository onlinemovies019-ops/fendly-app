package com.example.fendly;

import android.content.pm.ActivityInfo;
import android.os.Bundle;

import com.journeyapps.barcodescanner.CaptureActivity;

public class PortraitCaptureActivity extends CaptureActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.zxing_custom_capture);
    }
}

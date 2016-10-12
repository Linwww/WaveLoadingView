package com.example.administrator.waveloadingview;

import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class DemoActivity extends AppCompatActivity {
    private WaveLoadingView mWaveLoadingView;
    private boolean mIsStop = false;
    private String[] mColor = {"#678966", "#57bc51", "#303F9F", "#FF4081","#CA0612"};
    private int mIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        mWaveLoadingView = (WaveLoadingView) findViewById(R.id.loadingView);
        mWaveLoadingView.setOriginalImage(R.mipmap.bga_refresh_moooc);
        mWaveLoadingView.setWaveColor(ContextCompat.getColor(this, R.color.colorWava));
    }

    public void click(View v) {
        if ((((TextView) v).getText()).equals("stop")) {
            ((TextView) v).setText("start");
        } else {
            ((TextView) v).setText("stop");
        }
        mIsStop = !mIsStop;
        mWaveLoadingView.setmStopInvalidate(mIsStop);
    }

    public void changeColor(View v) {
        mWaveLoadingView.setWaveColor(Color.parseColor(mColor[mIndex % 5]));
        mIndex++;
    }
}

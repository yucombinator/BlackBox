package icechen1.com.blackbox.activities;

import android.os.Build;
import android.os.Bundle;

import com.github.paolorotolo.appintro.AppIntro2;
import com.github.paolorotolo.appintro.AppIntroFragment;

import icechen1.com.blackbox.R;

/**
 * Created by yuchen on 2/6/16.
 */
public class IntroActivity extends AppIntro2 {

    // Please DO NOT override onCreate. Use init
    @Override
    public void init(Bundle savedInstanceState) {

        addSlide(AppIntroFragment.newInstance("Welcome to Rewind", "Rewind is a reverse voice recorder.", R.drawable.web_hi_res_512, getResources().getColor(R.color.primary_dark)));
        addSlide(AppIntroFragment.newInstance("Reverse voice recorder?", "Rewind allows you to passively record audio from your phone, and allows you to recall everything that's recorded in the last few 1 to 30 minutes.", R.drawable.ic_device_access_time, getResources().getColor(R.color.primary)));
        addSlide(AppIntroFragment.newInstance("Get started", "Start your first recording by pressing the record button on the bottom right!", R.drawable.ic_av_mic, getResources().getColor(R.color.accent)));
        showStatusBar(true);

        // Edit the color of the nav bar on Lollipop+ devices
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(getResources().getColor(R.color.primary_dark));
        }
        setVibrate(true);
        setVibrateIntensity(30);
        //setSlideOverAnimation(); // OR
        //setFadeAnimation();
        // Permissions -- takes a permission and slide number
        //askForPermissions(new String[]{Manifest.permission.CAMERA}, 3);
    }

    @Override
    public void onNextPressed() {
        // Do something when users tap on Next button.
    }

    @Override
    public void onDonePressed() {
        // Do something when users tap on Done button.
        finish();
    }

    @Override
    public void onSlideChanged() {
        // Do something when slide is changed
    }
}
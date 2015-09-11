package icechen1.com.blackbox.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.github.jorgecastilloprz.FABProgressCircle;
import com.nineoldandroids.animation.ValueAnimator;

import de.greenrobot.event.EventBus;
import icechen1.com.blackbox.R;
import icechen1.com.blackbox.activities.RecordActivity;
import icechen1.com.blackbox.audio.AudioBufferManager;
import icechen1.com.blackbox.audio.RecordingSampler;
import icechen1.com.blackbox.messages.AudioBufferMessage;
import icechen1.com.blackbox.messages.RecordStatusMessage;
import icechen1.com.blackbox.messages.RecordingSavedMessage;
import icechen1.com.blackbox.provider.recording.RecordingContentValues;
import icechen1.com.blackbox.services.AudioRecordService;
import icechen1.com.blackbox.views.VisualizerView;

import static humanize.Humanize.duration;

public class RecordActivityFragment extends Fragment implements RecordingSampler.CalculateVolumeListener {

    private View mRoot;
    private FABProgressCircle mFab;
    private boolean mRecording = false;
    private View mCtlPanel;
    private RecordingSampler mRecordingSampler;
    private CardView mListeningCard;
    private CardView mStartListeningCard;
    private VisualizerView mVisualizerView;
    private CardView mSaveListeningCard;
    private RadioButton mCheckedRadioButton;
    private int mTime;
    private TextView mDuration;

    public RecordActivityFragment() {
    }

    @Override
    public void onCreate(Bundle b){
        EventBus.getDefault().register(this);
        super.onCreate(b);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.fragment_record, container, false);
        mCtlPanel = mRoot.findViewById(R.id.control_panel);
        mFab = (com.github.jorgecastilloprz.FABProgressCircle) mRoot.findViewById(R.id.fabProgressCircle);
        mRoot.findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mRecording) {
                    startRecording();
                } else {
                    stopRecording();
                }
            }
        });

        //cards
        mListeningCard = (CardView) mRoot.findViewById(R.id.card_listening);
        mStartListeningCard = (CardView) mRoot.findViewById(R.id.card_start_listening);
        mSaveListeningCard = (CardView) mRoot.findViewById(R.id.card_save_recording);
        mDuration = (TextView) mRoot.findViewById(R.id.duration);

        //visualizer
        mVisualizerView = (VisualizerView) mRoot.findViewById(R.id.visualizer);
        mRecordingSampler = new RecordingSampler();
        mRecordingSampler.setVolumeListener(this);  // for custom implements
        mRecordingSampler.link(mVisualizerView);     // link to visualizer

        // This will get the radiogroup
        RadioGroup rGroup = (RadioGroup)mRoot.findViewById(R.id.radio_group_time);
        // This will get the radiobutton in the radiogroup that is checked
        mCheckedRadioButton = (RadioButton)rGroup.findViewById(rGroup.getCheckedRadioButtonId());
        getTime(mCheckedRadioButton);

        // This overrides the radiogroup onCheckListener
        rGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup rGroup, int checkedId) {
                // This will get the radiobutton that has changed in its check state
                mCheckedRadioButton = (RadioButton) rGroup.findViewById(checkedId);
                // This puts the value (true/false) into the variable
                boolean isChecked = mCheckedRadioButton.isChecked();
                // If the radiobutton that has changed in check state is now checked...
                if (isChecked) {
                    getTime(mCheckedRadioButton);
                }
            }
        });

        if(AudioBufferManager.getInstanceIfExisting() != null){
            mTime = AudioBufferManager.getInstanceIfExisting().getDuration();
            tintSystemBarsForStartRecord();
            mCtlPanel.setVisibility(View.VISIBLE);
            mStartListeningCard.setVisibility(View.GONE);
            mListeningCard.setVisibility(View.VISIBLE);
            mFab.bringToFront();
            //mFab.show();
            mRecording = true;
        }

        return mRoot;
    }

    private void getTime(View v){
        switch(v.getId()){
            case R.id.time_30sec :
                mTime = 30;
                break;
            case R.id.time_1min :
                mTime = 60;
                break;
            case R.id.time_5min :
                mTime = 5 * 60;
                break;
            case R.id.time_10min :
                mTime = 10 * 60;
                break;
        }
    }

    private void revealControlPanel(){
        // previously invisible view

    // get the center for the clipping circle
        int cx = (int) mFab.getX() + mFab.getWidth()  / 2;
        int cy = (int) mFab.getY() + mFab.getHeight()  / 2;
    // get the final radius for the clipping circle
        int finalRadius = Math.max(mCtlPanel.getWidth(), mCtlPanel.getHeight());

    // create the animator for this view (the start radius is zero)
        Animator anim =
                ViewAnimationUtils.createCircularReveal(mCtlPanel, cx, cy, 0, finalRadius);

    // make the view visible and start the animation
        mCtlPanel.setVisibility(View.VISIBLE);
        anim.start();
        mFab.bringToFront();
        mFab.show();
        mDuration.setText(duration(mTime));

    }

    private void hideControlPanel(){

        // get the center for the clipping circle
        int cx = (int) mFab.getX() + mFab.getWidth()  / 2;
        int cy = (int) mFab.getY() + mFab.getHeight()  / 2;

        // get the initial radius for the clipping circle
        int initialRadius = mCtlPanel.getWidth();

        // create the animation (the final radius is zero)
        Animator anim =
                ViewAnimationUtils.createCircularReveal(mCtlPanel, cx, cy, initialRadius, 0);

        // make the view invisible when the animation is done
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mCtlPanel.setVisibility(View.INVISIBLE);
            }
        });

        // start the animation
        anim.start();
        mFab.beginFinalAnimation();
    }

    public void setUpVisual(){
        mRecordingSampler.startRecording();
    }

    public void stopVisual(){
        mRecordingSampler.stopRecording();
    }

    @Override
    public void onResume() {
        setUpVisual();
        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onPause() {
        if(mRecording)
            stopVisual();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if(mRecording)
            mRecordingSampler.release();
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    private void startRecording(){
        Intent i = new Intent(getActivity(), AudioRecordService.class);
        i.putExtra("mode", AudioRecordService.MODE_START);
        i.putExtra("length", mTime);
        getActivity().startService(i);
        //startRecordingUI();
    }

    private void stopRecording(){
        Intent i = new Intent(getActivity(), AudioRecordService.class);
        i.putExtra("mode", AudioRecordService.MODE_STOP);
        getActivity().startService(i);
        //stopRecordingUI();
    }

    private void stopRecordingUI(){
        hideControlPanel();
        tintSystemBarsForEndRecord();
        mRecording = false;
        mListeningCard.setVisibility(View.GONE);
        mStartListeningCard.setVisibility(View.VISIBLE);
        //stopVisual();
    }

    private void startRecordingUI(){
        revealControlPanel();
        tintSystemBarsForStartRecord();
        //setUpVisual();
        mStartListeningCard.setVisibility(View.GONE);
        mListeningCard.setVisibility(View.VISIBLE);
        mRecording = true;
    }

    @Override
    public void onCalculateVolume(int i) {

    }

    public void onEvent(final RecordStatusMessage event) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(event.status == RecordStatusMessage.STOPPED){
                    stopRecordingUI();
                }else{
                    startRecordingUI();
                }
            }
        });
    }

    public void onEvent(final AudioBufferMessage event) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mRecordingSampler.process(event.bytes);
            }
        });
    }

    public void onEvent(final RecordingSavedMessage event) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                addSavedCard(event.obj);
            }
        });
    }

    private void addSavedCard(RecordingContentValues saved) {
        //first remove any previously existing contentcard
        //mSaveListeningCard.setVisibility(View.VISIBLE);
    }

    private void tintSystemBarsForStartRecord(){
        // Initial colors of each system bar.
        final int statusBarColor = getResources().getColor(R.color.primary_dark);
        final int toolbarColor = getResources().getColor(R.color.primary);

        // Desired final colors of each bar.
        final int statusBarToColor = getResources().getColor(R.color.accent_dark);
        final int toolbarToColor = getResources().getColor(R.color.accent);

        tintSystemBars(statusBarColor, statusBarToColor, toolbarColor, toolbarToColor);
    }

    private void tintSystemBarsForEndRecord(){
        // Initial colors of each system bar.
        final int statusBarToColor = getResources().getColor(R.color.primary_dark);
        final int toolbarToColor = getResources().getColor(R.color.primary);

        // Desired final colors of each bar.
        final int statusBarColor = getResources().getColor(R.color.accent_dark);
        final int toolbarColor = getResources().getColor(R.color.accent);

        tintSystemBars(statusBarColor, statusBarToColor, toolbarColor, toolbarToColor);
    }

    private void tintSystemBars(final int statusBarColor, final int statusBarToColor, final int toolbarColor, final int toolbarToColor) {
        ValueAnimator anim = ValueAnimator.ofFloat(0, 1);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                // Use animation position to blend colors.
                float position = animation.getAnimatedFraction();

                // Apply blended color to the status bar.
                int blended = blendColors(statusBarColor, statusBarToColor, position);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    getActivity().getWindow().setStatusBarColor(blended);
                }

                // Apply blended color to the ActionBar.
                blended = blendColors(toolbarColor, toolbarToColor, position);
                ColorDrawable background = new ColorDrawable(blended);
                ((RecordActivity)getActivity()).getSupportActionBar().setBackgroundDrawable(background);
            }
        });

        anim.setDuration(500).start();
    }

    private int blendColors(int from, int to, float ratio) {
        final float inverseRatio = 1f - ratio;

        final float r = Color.red(to) * ratio + Color.red(from) * inverseRatio;
        final float g = Color.green(to) * ratio + Color.green(from) * inverseRatio;
        final float b = Color.blue(to) * ratio + Color.blue(from) * inverseRatio;

        return Color.rgb((int) r, (int) g, (int) b);
    }

}

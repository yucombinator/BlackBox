package icechen1.com.blackbox.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.nineoldandroids.animation.ValueAnimator;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import icechen1.com.blackbox.R;
import icechen1.com.blackbox.activities.PremiumActivity;
import icechen1.com.blackbox.activities.RecordActivity;
import icechen1.com.blackbox.audio.RecordingSampler;
import icechen1.com.blackbox.common.AppUtils;
import icechen1.com.blackbox.messages.AudioBufferMessage;
import icechen1.com.blackbox.messages.GetRecordingStatusMessage;
import icechen1.com.blackbox.messages.RecordStatusMessage;
import icechen1.com.blackbox.messages.RecordingSavedMessage;
import icechen1.com.blackbox.provider.recording.RecordingContentValues;
import icechen1.com.blackbox.services.AudioRecordService;
import icechen1.com.blackbox.views.ResizeWidthAnimation;
import icechen1.com.blackbox.views.VisualizerView;
import ru.dimorinny.floatingtextbutton.FloatingTextButton;

import static humanize.Humanize.duration;

public class RecordActivityFragment extends Fragment implements RecordingSampler.CalculateVolumeListener {

    private View mRoot;
    private FloatingTextButton mSaveBtn;
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
    private RadioGroup mRGroup;
    private LinearLayout mBtnsView;
    private FloatingTextButton mCancelBtn;
    private View mCoordinatorLayout;

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
        mCoordinatorLayout = mRoot.findViewById(R.id.container);
        mCtlPanel = mRoot.findViewById(R.id.control_panel);
        mSaveBtn = (FloatingTextButton) mRoot.findViewById(R.id.action_save_button);
        mCancelBtn = (FloatingTextButton) mRoot.findViewById(R.id.action_cancel_button);
        mBtnsView = (LinearLayout) mRoot.findViewById(R.id.action_buttons_view);

        final Vibrator vib = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);

        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mRecording) {
                    startRecording();
                } else {
                    stopRecording(true);
                }
                vib.vibrate(25);
            }
        });

        mCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mRecording) {
                    // confirm
                    Snackbar snackbar = Snackbar
                            .make(mCoordinatorLayout, getResources().getText(R.string.are_you_sure).toString(), Snackbar.LENGTH_LONG)
                            .setAction(getResources().getText(R.string.yes).toString(), new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    stopRecording(false);
                                }
                            });
                    snackbar.show();
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

        final SharedPreferences getPrefs = PreferenceManager
                .getDefaultSharedPreferences(getContext());
        mTime = Integer.valueOf(getPrefs.getString("default_length", "300"));

        // This will get the radiogroup
        mRGroup = (RadioGroup)mRoot.findViewById(R.id.radio_group_time);
        // This will get the radiobutton in the radiogroup that is checked
        mCheckedRadioButton = (RadioButton)mRGroup.findViewById(mRGroup.getCheckedRadioButtonId());
        setDefaultChecked();

        // This overrides the radiogroup onCheckListener
        mRGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup rGroup, int checkedId) {
            // This will get the radiobutton that has changed in its check state
            mCheckedRadioButton = (RadioButton) rGroup.findViewById(checkedId);
            // This puts the value (true/false) into the variable
            boolean isChecked = mCheckedRadioButton.isChecked();
            // If the radiobutton that has changed in check state is now checked...
            if (isChecked) {
                getTime(mCheckedRadioButton);
                if(mTime > 1800 && !AppUtils.isPremium(getContext())) {
                    //Needs premium
                    startActivity(new Intent(getContext(), PremiumActivity.class));
                    mTime = Integer.valueOf(getPrefs.getString("default_length", "300"));
                    setDefaultChecked();
                }
            }
            }
        });

        return mRoot;
    }

    private void setDefaultChecked(){
        switch (mTime){
            case 60:
                mRGroup.check(R.id.time_1_minute);
                break;
            default:
            case 5 * 60:
                mRGroup.check(R.id.time_5_minutes);
                break;
            case 10 * 60:
                mRGroup.check(R.id.time_10_minutes);
                break;
            case 30 * 60:
                mRGroup.check(R.id.time_30_minutes);
                break;
            case 60 * 60:
                mRGroup.check(R.id.time_60_minutes);
                break;
            case 3 * 60 * 60:
                mRGroup.check(R.id.time_180_minutes);
                break;
        }
    }

    private void getTime(View v){
        switch(v.getId()){
            case R.id.time_1_minute :
                mTime = 60;
                break;
            case R.id.time_5_minutes :
                mTime = 5 * 60;
                break;
            case R.id.time_10_minutes :
                mTime = 10 * 60;
                break;
            case R.id.time_30_minutes :
                mTime = 30 * 60;
                break;
            case R.id.time_60_minutes :
                mTime = 60 * 60;
                break;
            case R.id.time_180_minutes :
                mTime = 3 * 60 * 60;
                break;
        }
    }

    private void revealControlPanel(boolean shouldAnimate){
        if (shouldAnimate && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            // previously invisible view

            // get the center for the clipping circle
            int cx = (int) mBtnsView.getX() + mBtnsView.getWidth()  / 2;
            int cy = (int) mBtnsView.getY() + mBtnsView.getHeight()  / 2;
            // get the final radius for the clipping circle
            int finalRadius = Math.max(mCtlPanel.getWidth(), mCtlPanel.getHeight());

            // create the animator for this view (the start radius is zero)
            Animator anim = null;
            anim = ViewAnimationUtils.createCircularReveal(mCtlPanel, cx, cy, 0, finalRadius);
            anim.start();
        }

        // make the view visible and start the animation
        mCtlPanel.setVisibility(View.VISIBLE);
        //mBtnsView.bringToFront();
        mDuration.setText(duration(mTime));
    }

    private void hideControlPanel(boolean shouldAnimate){
        if(shouldAnimate && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP){
            // get the center for the clipping circle
            int cx = (int) mBtnsView.getX() + mBtnsView.getWidth()  / 2;
            int cy = (int) mBtnsView.getY() + mBtnsView.getHeight()  / 2;

            // get the initial radius for the clipping circle
            int initialRadius = mCtlPanel.getWidth();

            // create the animation (the final radius is zero)
            Animator anim = ViewAnimationUtils.createCircularReveal(mCtlPanel, cx, cy, initialRadius, 0);

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
        } else {
            mCtlPanel.setVisibility(View.INVISIBLE);
        }
    }

    public void setUpVisual(){
        mRecordingSampler.startRecording();
    }

    public void stopVisual(){
        mRecordingSampler.stopRecording();
    }

    @Override
    public void onResume() {
        super.onResume();
        //emit message
        EventBus.getDefault().post(new GetRecordingStatusMessage());
        setUpVisual();
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

    private void stopRecording(boolean saving){
        Intent i = new Intent(getActivity(), AudioRecordService.class);
        if(saving)
            i.putExtra("mode", AudioRecordService.MODE_STOP);
        else
            i.putExtra("mode", AudioRecordService.MODE_STOP_NO_SAVE);

        getActivity().startService(i);
        //stopRecordingUI();
    }

    private void stopRecordingUI(boolean shouldAnimate){
        hideControlPanel(shouldAnimate);
        tintSystemBarsForEndRecord();
        mRecording = false;
        mListeningCard.setVisibility(View.GONE);
        mStartListeningCard.setVisibility(View.VISIBLE);
        // bit of a hack to set mCancelBtn to be gone (since GONE does not set radius properly)
        // mCancelBtn.getLayoutParams().width = 0;
        ResizeWidthAnimation anim = new ResizeWidthAnimation(mCancelBtn, 0);
        anim.setDuration(500);
        mCancelBtn.startAnimation(anim);

        mStartListeningCard.bringToFront();
        mSaveBtn.setTitle(getResources().getText(R.string.action_start_title).toString());
        mSaveBtn.setIconDrawable(getResources().getDrawable(R.drawable.ic_mic_white_24dp));
        mBtnsView.bringToFront();
    }

    private void startRecordingUI(boolean shouldAnimate){
        revealControlPanel(shouldAnimate);
        tintSystemBarsForStartRecord();
        mStartListeningCard.setVisibility(View.GONE);
        mListeningCard.setVisibility(View.VISIBLE);
        // bit of a hack to set mCancelBtn to be visible (since GONE does not set radius properly)
        // mCancelBtn.getLayoutParams().width = LinearLayout.LayoutParams.WRAP_CONTENT;
        ResizeWidthAnimation anim = new ResizeWidthAnimation(mCancelBtn, LinearLayout.LayoutParams.WRAP_CONTENT);
        anim.setDuration(500);
        mCancelBtn.startAnimation(anim);

        mCancelBtn.setVisibility(View.VISIBLE);
        mListeningCard.bringToFront();
        mSaveBtn.setTitle(getResources().getText(R.string.action_save_title).toString());
        mSaveBtn.setIconDrawable(getResources().getDrawable(R.drawable.ic_stop_white_24dp));
        mBtnsView.bringToFront();
        mRecording = true;
    }

    @Override
    public void onCalculateVolume(int i) {

    }

    @Subscribe
    public void onEvent(final RecordStatusMessage event) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
            switch (event.status) {
                case RecordStatusMessage.JUST_STOPPED:
                    stopRecordingUI(true);
                    break;
                case RecordStatusMessage.STOPPED:
                    stopRecordingUI(false);
                    break;
                case RecordStatusMessage.JUST_STARTED:
                    setUpVisualizer(event.bufferSize);
                    startRecordingUI(true);
                    break;
                case RecordStatusMessage.STARTED:
                    setUpVisualizer(event.bufferSize);
                    startRecordingUI(false);
                    break;
            }
            }
        });
    }

    public void setUpVisualizer(int bufsize){
        mRecordingSampler.initAudioRecord(bufsize);
    }

    @Subscribe
    public void onEvent(final AudioBufferMessage event) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mRecordingSampler.isReady()) {
                    mRecordingSampler.process(event.bytes);
                }
            }
        });
    }

    @Subscribe
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

                if(getActivity() == null){
                    return;
                }

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

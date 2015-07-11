package icechen1.com.blackbox.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;

import com.github.jorgecastilloprz.FABProgressCircle;

import de.greenrobot.event.EventBus;
import icechen1.com.blackbox.R;
import icechen1.com.blackbox.audio.RecordingSampler;
import icechen1.com.blackbox.messages.AudioBufferMessage;
import icechen1.com.blackbox.messages.RecordStatusMessage;
import icechen1.com.blackbox.services.AudioRecordService;
import icechen1.com.blackbox.views.VisualizerView;

public class RecordActivityFragment extends Fragment implements RecordingSampler.CalculateVolumeListener {

    private View mRoot;
    private FABProgressCircle mFab;
    private boolean mRecording = false;
    private View mCtlPanel;
    private RecordingSampler mRecordingSampler;
    private CardView mListeningCard;
    private CardView mStartListeningCard;
    private VisualizerView mVisualizerView;

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

        //visualizer
        mVisualizerView = (VisualizerView) mRoot.findViewById(R.id.visualizer);
        mRecordingSampler = new RecordingSampler();
        mRecordingSampler.setVolumeListener(this);  // for custom implements
        mRecordingSampler.link(mVisualizerView);     // link to visualizer

        return mRoot;
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
        mRecording = false;
        mListeningCard.setVisibility(View.GONE);
        mStartListeningCard.setVisibility(View.VISIBLE);
        //stopVisual();
    }

    private void startRecordingUI(){
        revealControlPanel();
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

}

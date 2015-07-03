package icechen1.com.blackbox.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;

import com.github.jorgecastilloprz.FABProgressCircle;

import icechen1.com.blackbox.R;
import icechen1.com.blackbox.services.AudioRecordService;

/**
 * A placeholder fragment containing a simple view.
 */
public class RecordActivityFragment extends Fragment {

    private View mRoot;
    private View mCtlPanel;
    private FABProgressCircle mFab;
    private boolean mRecording = false;

    public RecordActivityFragment() {
    }

    @Override
    public void onCreate(Bundle b){
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
        return mRoot;
    }

    private void revealControlPanel(){
        // previously invisible view

    // get the center for the clipping circle
        int cx = (mCtlPanel.getLeft() + mCtlPanel.getRight()) / 2;
        int cy = (mCtlPanel.getTop() + mCtlPanel.getBottom()) / 2;

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
        int cx = (mCtlPanel.getLeft() + mCtlPanel.getRight()) / 2;
        int cy = (mCtlPanel.getTop() + mCtlPanel.getBottom()) / 2;

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

    private void startRecording(){
        Intent i = new Intent(getActivity(), AudioRecordService.class);
        i.putExtra("mode", AudioRecordService.MODE_START);
        getActivity().startService(i);
        revealControlPanel();
        mRecording = true;
    }

    private void stopRecording(){
        Intent i = new Intent(getActivity(), AudioRecordService.class);
        i.putExtra("mode", AudioRecordService.MODE_STOP);
        getActivity().startService(i);
        hideControlPanel();
        mRecording = false;
    }
}

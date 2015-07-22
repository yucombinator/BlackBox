package icechen1.com.blackbox.fragments;

import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.avast.android.dialogs.core.BaseDialogFragment;

import java.io.File;

import icechen1.com.blackbox.R;
import icechen1.com.blackbox.provider.recording.RecordingCursor;
import nl.changer.audiowife.AudioWife;


/**
 * A placeholder fragment containing a simple view.
 */
public class PlayerDialogFragment extends BaseDialogFragment {

    private String name;
    private String path;
    private long timestamp;
    private long duration;
    private View mRoot;
    private SeekBar mMediaSeekBar;
    private TextView mRunTime;
    private TextView mTotalTime;
    private View mPlayMedia;
    private View mPauseMedia;

    public PlayerDialogFragment() {
    }

    public static void show(FragmentActivity activity, RecordingCursor mResults, int item) {
        FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
        ft.addToBackStack(null);
        new PlayerDialogFragment().setItem(mResults,item).show(ft, "PlayerDialogFragment");
    }

    public PlayerDialogFragment setItem(RecordingCursor mResults, int item){
        if(mResults.moveToPosition(item)){
            name = mResults.getName();
            path = mResults.getFilename();
            timestamp = mResults.getTimestamp();
            duration = mResults.getDuration();
        }
        return this;
    }

    @Override
    public void onShow(DialogInterface i){
        super.onShow(i);
        setUpAudioControls();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        getDialog().setCanceledOnTouchOutside(false);
        return super.onCreateView(inflater, container, savedInstanceState);
    }


    @Override
    public BaseDialogFragment.Builder build(BaseDialogFragment.Builder builder) {
        mRoot = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_player, null);
        builder.setTitle(name);
        builder.setView(mRoot);
        builder.setPositiveButton(getString(R.string.close), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // when done playing, release the resources
                AudioWife.getInstance().release();
                dismiss();
            }
        });
        setCancelable(true);
        return builder;
    }

    void setUpAudioControls(){
        // initialize the player controls
        mPlayMedia = mRoot.findViewById(R.id.play);
        mPauseMedia = mRoot.findViewById(R.id.pause);
        mMediaSeekBar = (SeekBar) mRoot.findViewById(R.id.seekBar);
        mRunTime = (TextView) mRoot.findViewById(R.id.currentTime);
        mTotalTime = (TextView) mRoot.findViewById(R.id.totalTime);

        // AudioWife takes care of click handler for play/pause button
        AudioWife.getInstance()
                .init(getActivity(), Uri.fromFile(new File(path)))
                .setPlayView(mPlayMedia)
                .setPauseView(mPauseMedia)
                .setSeekBar(mMediaSeekBar)
                .setRuntimeView(mRunTime)
                .setTotalTimeView(mTotalTime);

        // to explicitly pause
        AudioWife.getInstance().pause();
    }
}

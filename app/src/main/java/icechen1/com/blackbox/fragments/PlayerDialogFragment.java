package icechen1.com.blackbox.fragments;

import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.avast.android.dialogs.core.BaseDialogFragment;

import java.io.File;

import icechen1.com.blackbox.R;
import icechen1.com.blackbox.activities.MainActivity;
import icechen1.com.blackbox.common.DatabaseHelper;
import icechen1.com.blackbox.provider.recording.RecordingCursor;
import nl.changer.audiowife.AudioWife;


/**
 * A placeholder fragment containing a simple view.
 */
public class PlayerDialogFragment extends BaseDialogFragment {

    private DialogInterface.OnDismissListener onDismissListener;
    public void setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
    }

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
    private RecordingCursor mCursor;
    private int mItem;
    private ImageView mSetFavoriteBtn;
    private ImageView mUnSetFavoriteBtn;

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

            //save the cursor and item
            mCursor = mResults;
            mItem = item;
        }
        return this;
    }

    @Override
    public void onShow(DialogInterface i){
        super.onShow(i);
        setUpAudioControls();
        setUpFavoriteState();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        getDialog().setCanceledOnTouchOutside(false);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    void setUpFavoriteState(){
        boolean isFavorite = false;
        mCursor.moveToPosition(mItem);
        isFavorite = mCursor.getFavorite();

        if(isFavorite){
            mUnSetFavoriteBtn.setVisibility(View.VISIBLE);
            mSetFavoriteBtn.setVisibility(View.GONE);
        }else{
            mSetFavoriteBtn.setVisibility(View.VISIBLE);
            mUnSetFavoriteBtn.setVisibility(View.GONE);
        }

        mSetFavoriteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseHelper.editFavoriteforId(getActivity(), mCursor.getId(), true);
                mUnSetFavoriteBtn.setVisibility(View.VISIBLE);
                mSetFavoriteBtn.setVisibility(View.GONE);
            }
        });

        mUnSetFavoriteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseHelper.editFavoriteforId(getActivity(), mCursor.getId(), false);
                mSetFavoriteBtn.setVisibility(View.VISIBLE);
                mUnSetFavoriteBtn.setVisibility(View.GONE);
            }
        });
    }


    @Override
    public BaseDialogFragment.Builder build(BaseDialogFragment.Builder builder) {
        mRoot = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_player, null);

        mUnSetFavoriteBtn = (ImageView) mRoot.findViewById(R.id.unset_fav_btn);
        mSetFavoriteBtn = (ImageView) mRoot.findViewById(R.id.set_fav_btn);

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

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (onDismissListener != null) {
            onDismissListener.onDismiss(dialog);
        }
    }
}

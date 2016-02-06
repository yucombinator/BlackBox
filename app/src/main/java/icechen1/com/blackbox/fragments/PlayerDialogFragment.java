package icechen1.com.blackbox.fragments;

import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.Toast;

import com.avast.android.dialogs.core.BaseDialogFragment;
import com.gregacucnik.EditTextView;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

import icechen1.com.blackbox.R;
import icechen1.com.blackbox.common.DatabaseHelper;
import icechen1.com.blackbox.messages.DatabaseUpdatedMessage;
import icechen1.com.blackbox.provider.recording.RecordingCursor;
import nl.changer.audiowife.AudioWife;


/**
 * A placeholder fragment containing a simple view.
 */
public class PlayerDialogFragment extends BaseDialogFragment {

    private DialogInterface.OnDismissListener onDismissListener;
    private EditTextView mTitle;
    private boolean mHasChanged = false;
    private ImageView mDeleteBtn;
    private ImageView mShareBtn;

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
        setUpButtonActions();
    }

    public void setUpButtonActions(){
        final View options = mRoot.findViewById(R.id.player_options);
        final View confirm = mRoot.findViewById(R.id.delete_confirm);

        mDeleteBtn = (ImageView) mRoot.findViewById(R.id.delete_btn);
        mDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                options.setVisibility(View.GONE);
                confirm.setVisibility(View.VISIBLE);
            }
        });

        final View delete_yes = mRoot.findViewById(R.id.delete_confirm_yes);
        final View delete_no = mRoot.findViewById(R.id.delete_confirm_no);
        delete_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirm.setVisibility(View.GONE);
                options.setVisibility(View.VISIBLE);
            }
        });

        delete_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseHelper.deleteForId(getActivity(), mCursor.getId());
                //delete file
                File file = new File(path);
                boolean deleted = file.delete();
                if(!deleted){
                    Toast.makeText(getContext(), getString(R.string.delete_failed), Toast.LENGTH_LONG).show();
                }
                mHasChanged = true;
                dismiss();
            }
        });

        mShareBtn = (ImageView) mRoot.findViewById(R.id.share_btn);
        mShareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.fromFile(new File(path));
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("audio/*");
                share.putExtra(Intent.EXTRA_STREAM, uri);
                startActivity(Intent.createChooser(share, getString(R.string.share_sound)));
            }
        });
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
                mHasChanged = true;
            }
        });

        mUnSetFavoriteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseHelper.editFavoriteforId(getActivity(), mCursor.getId(), false);
                mSetFavoriteBtn.setVisibility(View.VISIBLE);
                mUnSetFavoriteBtn.setVisibility(View.GONE);
                mHasChanged = true;
            }
        });
    }


    @Override
    public BaseDialogFragment.Builder build(BaseDialogFragment.Builder builder) {
        mRoot = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_player, null);

        //set up so we can leave edittext by clicking elsewhere
        mRoot.setClickable(true);
        mRoot.setFocusable(true);
        mTitle = (EditTextView) mRoot.findViewById(R.id.title);
        mTitle.setText(name);

        mRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTitle.setText(name); //restore old name
                mRoot.requestFocus();
            }
        });

        mTitle.setEditTextViewListener(new EditTextView.EditTextViewListener() {
            @Override
            public void onEditTextViewEditModeStart() {

            }

            @Override
            public void onEditTextViewEditModeFinish(String text) {
                DatabaseHelper.editTitleforId(getActivity(), mCursor.getId(), text);
                mHasChanged = true;
            }
        });

        mUnSetFavoriteBtn = (ImageView) mRoot.findViewById(R.id.unset_fav_btn);
        mSetFavoriteBtn = (ImageView) mRoot.findViewById(R.id.set_fav_btn);

        //builder.setTitle(name);
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
        if(mHasChanged){
            EventBus.getDefault().post(new DatabaseUpdatedMessage());
        }
        if (onDismissListener != null) {
            onDismissListener.onDismiss(dialog);
        }
    }
}

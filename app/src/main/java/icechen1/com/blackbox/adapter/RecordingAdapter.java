package icechen1.com.blackbox.adapter;

import android.content.Context;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import icechen1.com.blackbox.R;
import icechen1.com.blackbox.fragments.PlayerDialogFragment;
import icechen1.com.blackbox.provider.recording.RecordingCursor;
import icechen1.com.blackbox.provider.recording.RecordingSelection;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.tonicartos.superslim.GridSLM;
import com.tonicartos.superslim.LinearSLM;

import java.util.Date;
import java.util.List;

import static humanize.Humanize.duration;
import static humanize.Humanize.naturalTime;

public class RecordingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public class ViewHolder extends RecyclerView.ViewHolder {

        public final TextView mTitle;
        public final TextView mLength;
        private final TextView mDate;

        public ViewHolder(View itemView) {
            super(itemView);

            mTitle = (TextView) itemView.findViewById(R.id.title);
            mLength = (TextView) itemView.findViewById(R.id.length);
            mDate = (TextView) itemView.findViewById(R.id.date);
        }
    }

    private final FragmentActivity mContext;
    private final RecordingCursor mResults;


    public RecordingAdapter(FragmentActivity c){
        mContext = c;

        RecordingSelection where = new RecordingSelection();
        mResults = where.query(mContext.getContentResolver());
        Log.d("icechen1", "count: " + mResults.getCount());
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_recording_item, parent, false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            view.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    ((RippleDrawable) view.getBackground()).setHotspot(event.getX(), event.getY());
                    return false;

                }
            });
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (mResults.moveToPosition(position)) {
            final ViewHolder vh = (ViewHolder) holder;
            vh.mTitle.setText(mResults.getName());
            vh.mLength.setText(String.valueOf(duration(mResults.getDuration() / 1000)));
            vh.mDate.setText(String.valueOf(naturalTime(new Date(mResults.getTimestamp()))));
            vh.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PlayerDialogFragment.show(mContext, mResults, position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        if (mResults != null) {
            return mResults.getCount();
        }
        return 0;
    }

}
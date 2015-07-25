package icechen1.com.blackbox.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import icechen1.com.blackbox.R;
import icechen1.com.blackbox.common.CursorRecyclerViewAdapter;
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

public class RecordingAdapter extends CursorRecyclerViewAdapter<RecyclerView.ViewHolder, RecordingCursor> {

    private final FragmentActivity mContext;

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


    public RecordingAdapter(FragmentActivity c){
        this(c, new RecordingSelection().query(c.getContentResolver()));
    }

    public RecordingAdapter(FragmentActivity context,RecordingCursor cursor){
        super(context, cursor);
        mContext = context;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final RecordingCursor cursor) {
        final ViewHolder vh = (ViewHolder) holder;
        final int position = cursor.getPosition();
        vh.mTitle.setText(cursor.getName());
        vh.mLength.setText(String.valueOf(duration(cursor.getDuration() / 1000)));
        vh.mDate.setText(String.valueOf(naturalTime(new Date(cursor.getTimestamp()))));
        vh.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayerDialogFragment.show(mContext, cursor, position);
            }
        });
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

}
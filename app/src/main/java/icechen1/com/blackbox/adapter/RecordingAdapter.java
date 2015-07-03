package icechen1.com.blackbox.adapter;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import icechen1.com.blackbox.R;
import icechen1.com.blackbox.provider.recording.RecordingCursor;
import icechen1.com.blackbox.provider.recording.RecordingSelection;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.widget.TextView;

import java.util.List;

public class RecordingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public class ViewHolder extends RecyclerView.ViewHolder {

        public final TextView mTitle;
        public final TextView mLength;

        public ViewHolder(View itemView) {
            super(itemView);

            mTitle = (TextView) itemView.findViewById(R.id.title);
            mLength = (TextView) itemView.findViewById(R.id.length);
        }
    }

    private final Context mContext;
    private final RecordingCursor mResults;


    public RecordingAdapter(Context c){
        mContext = c;

        RecordingSelection where = new RecordingSelection();
        mResults = where.query(mContext.getContentResolver());
        Log.d("icechen1", "count: " + mResults.getCount());
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_recording_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (mResults.moveToPosition(position)) {
            ViewHolder vh = (ViewHolder) holder;
            vh.mTitle.setText(mResults.getName());
            vh.mLength.setText(String.valueOf(mResults.getDuration()));
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
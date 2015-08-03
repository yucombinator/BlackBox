package icechen1.com.blackbox.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.malinskiy.superrecyclerview.SuperRecyclerView;
import com.melnykov.fab.FloatingActionButton;
import icechen1.com.blackbox.R;
import icechen1.com.blackbox.activities.RecordActivity;
import icechen1.com.blackbox.adapter.RecordingAdapter;
import icechen1.com.blackbox.services.AudioRecordService;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {


    private SuperRecyclerView mRecyclerView = null;

    private RecordingAdapter mAdapter= null;

    public static MainActivityFragment newInstance(boolean favorite){
        MainActivityFragment f =  new MainActivityFragment();
        Bundle args = new Bundle();
        args.putBoolean("favorite", favorite);
        f.setArguments(args);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        Bundle args = getArguments();
        boolean isFavorite = args.getBoolean("favorite", false);

        View view = inflater.inflate(R.layout.fragment_list, container, false);

        int mScrollPosition = 0;
        if (savedInstanceState != null) {
            //restore state
            //list = savedInstanceState.getParcelableArrayList("users");
            mScrollPosition = savedInstanceState.getInt("currentIndex");
            //hideProgressBar();
        } else {
            //list = new ArrayList<>();
        }

        mRecyclerView = (SuperRecyclerView) view.findViewById(R.id.recyclerView);
        mRecyclerView.getRecyclerView().setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = setRecyclerViewLayoutManager(mRecyclerView.getRecyclerView());
        mAdapter = new RecordingAdapter(getActivity(), isFavorite);
        mRecyclerView.setAdapter(mAdapter);
        //mAdapter.setDataSet(list);

        //scroll to saved position
        int count = mLayoutManager.getChildCount();
        if (mScrollPosition != RecyclerView.NO_POSITION && mScrollPosition < count) {
            mLayoutManager.scrollToPosition(mScrollPosition);
        }
        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.attachToRecyclerView(mRecyclerView.getRecyclerView());
        fab.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent recordActivity = new Intent(getActivity(),RecordActivity.class);
                getActivity().startActivity(recordActivity);
            }
        });
        return view;
        }
    /**
     * Set RecyclerView's LayoutManager
     */
    public LinearLayoutManager setRecyclerViewLayoutManager(RecyclerView recyclerView){
        int scrollPosition = 0;

        // If a layout manager has already been set, get current scroll position.
        if (recyclerView.getLayoutManager() != null) {
            scrollPosition = ((LinearLayoutManager)recyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
        }

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.scrollToPosition(scrollPosition);
        return linearLayoutManager;
    }

    @Override
    public void onResume(){
        super.onResume();
        mAdapter.refreshCursor();
        mAdapter.notifyDataSetChanged();
    }
}
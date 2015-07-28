package icechen1.com.blackbox.fragments

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.malinskiy.superrecyclerview.SuperRecyclerView
import com.melnykov.fab.FloatingActionButton
import icechen1.com.blackbox.R
import icechen1.com.blackbox.activities.RecordActivity
import icechen1.com.blackbox.adapter.RecordingAdapter
import icechen1.com.blackbox.services.AudioRecordService

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment : Fragment() {


    private var mRecyclerView: SuperRecyclerView? = null;

    private var mAdapter: RecordingAdapter? = null;

    fun newInstance(favorite: Boolean?): MainActivityFragment{
        var f =  MainActivityFragment()
        var args = Bundle()
        args.putBoolean("favorite", favorite!!);
        f.setArguments(args);
        return f
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        var args = getArguments();
        var isFavorite = args.getBoolean("favorite", false);

        var view = inflater!!.inflate(R.layout.fragment_list, container, false)

        var mScrollPosition = 0
        if (savedInstanceState != null) {
            //restore state
            //list = savedInstanceState.getParcelableArrayList("users");
            mScrollPosition = savedInstanceState.getInt("currentIndex")
            //hideProgressBar();
        } else {
            //list = new ArrayList<>();
        }

        mRecyclerView = view.findViewById(R.id.recyclerView) as SuperRecyclerView
        mRecyclerView!!.getRecyclerView().setHasFixedSize(true)
        val mLayoutManager = setRecyclerViewLayoutManager(mRecyclerView!!.getRecyclerView())
        mAdapter = RecordingAdapter(getActivity(), isFavorite);
        mRecyclerView!!.setAdapter(mAdapter);
        //mAdapter.setDataSet(list);

        //scroll to saved position
        val count = mLayoutManager.getChildCount()
        if (mScrollPosition != RecyclerView.NO_POSITION && mScrollPosition < count) {
            mLayoutManager.scrollToPosition(mScrollPosition)
        }
        val fab = view.findViewById(R.id.fab) as FloatingActionButton
        fab.attachToRecyclerView(mRecyclerView!!.getRecyclerView())
        fab.setOnClickListener(View.OnClickListener(){
            var recordActivity = Intent(getActivity(),javaClass<RecordActivity>())
            getActivity().startActivity(recordActivity)
        })
        return view
    }
    /**
     * Set RecyclerView's LayoutManager
     */
    public fun setRecyclerViewLayoutManager(recyclerView: RecyclerView): LinearLayoutManager {
        var scrollPosition = 0

        // If a layout manager has already been set, get current scroll position.
        if (recyclerView.getLayoutManager() != null) {
            scrollPosition = (recyclerView.getLayoutManager() as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()
        }

        val linearLayoutManager = LinearLayoutManager(getActivity())

        recyclerView.setLayoutManager(linearLayoutManager)
        recyclerView.scrollToPosition(scrollPosition)
        return linearLayoutManager
    }

    public override fun onResume(){
        super.onResume()
        mAdapter?.refreshCursor()
        mAdapter?.notifyDataSetChanged()
    }
}
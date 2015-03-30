package icechen1.com.blackbox

import android.support.v4.app.Fragment
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.melnykov.fab.FloatingActionButton


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment : Fragment() {

    private var mSwipeRefreshLayout: SwipeRefreshLayout? = null;

    private var mRecyclerView: RecyclerView? = null;

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
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
        mSwipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout) as SwipeRefreshLayout
        mSwipeRefreshLayout!!.setColorSchemeResources(R.color.primary, R.color.primary_dark, R.color.accent)
        mSwipeRefreshLayout!!.setOnRefreshListener(object : SwipeRefreshLayout.OnRefreshListener {
            override fun onRefresh() {
                //refreshContent();
            }
        })
        mRecyclerView = view.findViewById(R.id.recyclerView) as RecyclerView
        mRecyclerView!!.setHasFixedSize(true)
        val mLayoutManager = setRecyclerViewLayoutManager(mRecyclerView!!)
        //mAdapter = new UserDataSetAdapter(this);
        //mRecyclerView.setAdapter(mAdapter);
        //mAdapter.setDataSet(list);

        //scroll to saved position
        val count = mLayoutManager.getChildCount()
        if (mScrollPosition != RecyclerView.NO_POSITION && mScrollPosition < count) {
            mLayoutManager.scrollToPosition(mScrollPosition)
        }
        val fab = view.findViewById(R.id.fab) as FloatingActionButton
        fab.attachToRecyclerView(mRecyclerView)

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
}

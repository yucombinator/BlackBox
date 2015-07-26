package icechen1.com.blackbox.activities;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;

import com.github.ppamorim.cult.CultView;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

import icechen1.com.blackbox.R;
import icechen1.com.blackbox.common.NavigationDrawerUtil;
import icechen1.com.blackbox.fragments.MainActivityFragment;
import icechen1.com.blackbox.views.SearchView;

/**
 * This is the base activity of the application, here
 * are injected the view and configured the drawerLayout
 *
 * @author Pedro Paulo Amorim
 *
 */
public class MainActivity extends AppCompatActivity {

    private ActionBarDrawerToggle mDrawerToggle;
    private SearchView searchView;
    private CultView cultView;
    private DrawerLayout drawerLayout;
    private FragmentPagerItemAdapter adapter;
    private ViewPager viewPager;
    private SmartTabLayout smartTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cultView = (CultView) findViewById(R.id.cult_view);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_left);
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        smartTabLayout = (SmartTabLayout) findViewById(R.id.smart_tab_layout);

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        configToolbar(this, cultView.getInnerToolbar());
        mDrawerToggle = NavigationDrawerUtil
                .configNavigationDrawer(this, drawerLayout, null);
        initializeViewPager();
        configCultView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mDrawerToggle != null) {
            mDrawerToggle.syncState();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mDrawerToggle != null) {
            mDrawerToggle.onConfigurationChanged(newConfig);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                return mDrawerToggle != null && mDrawerToggle.onOptionsItemSelected(item);
            case R.id.action_search:
                cultView.showSlide();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initializeViewPager() {
        adapter = new FragmentPagerItemAdapter(
                getSupportFragmentManager(), FragmentPagerItems.with(this)
                .add(R.string.recent, MainActivityFragment.class)
                .add(R.string.favorites, MainActivityFragment.class)
                .create());
        if (viewPager == null || smartTabLayout == null) {
            return;
        }
        viewPager.setAdapter(adapter);
        smartTabLayout.setViewPager(viewPager);
    }


    public static void configToolbar(AppCompatActivity appCompatActivity, Toolbar toolbar) {
        if (toolbar == null || appCompatActivity == null) {
            throw new IllegalArgumentException("toolbar or appCompatActivity is null");
        }
        appCompatActivity.setSupportActionBar(toolbar);
        ActionBar actionBar = appCompatActivity.getSupportActionBar();
        if (actionBar == null) {
            return;
        }
        actionBar.setTitle(appCompatActivity.getResources().getString(R.string.app_name));
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onBackPressed() {
        if (cultView.isSecondViewAdded()) {
            cultView.hideSlideTop();
            return;
        }
        super.onBackPressed();
    }

    private void configCultView() {
        searchView = new SearchView();
        cultView.setOutToolbarLayout(searchView.getView(
                LayoutInflater.from(this).inflate(R.layout.layout_search, null), searchViewCallback));
        cultView.setBackgroundColor(getResources().getColor(R.color.primary));
        cultView.getInnerToolbar().setBackgroundColor(getResources().getColor(R.color.primary_dark));
        cultView.getInnerToolbar().setTitleTextColor(getResources().getColor(android.R.color.black));
        cultView.getInnerToolbar().setBackgroundColor(getResources().getColor(R.color.white));
        cultView.getOutToolbar().setBackgroundColor(getResources().getColor(R.color.primary));
        //cultView.setOutContentLayout(R.layout.fragment_list);
    }

    private SearchView.SearchViewCallback searchViewCallback =
            new SearchView.SearchViewCallback() {
                @Override
                public void onCancelClick() {
                    hideKeyboard();
                    onBackPressed();
                }
            };

    private void hideKeyboard() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (getCurrentFocus() != null) {
                    ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                            .hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                                    InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }
        });
    }
}


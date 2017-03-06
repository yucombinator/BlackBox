package icechen1.com.blackbox.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.github.ppamorim.cult.CultView;
import com.malinskiy.superrecyclerview.SuperRecyclerView;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

import angtrim.com.fivestarslibrary.FiveStarsDialog;
import icechen1.com.blackbox.AppConstants;
import icechen1.com.blackbox.R;
import icechen1.com.blackbox.adapter.RecordingAdapter;
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
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private ActionBarDrawerToggle mDrawerToggle;
    private SearchView searchView;
    private CultView cultView;
    private DrawerLayout drawerLayout;
    private FragmentPagerItemAdapter adapter;
    private ViewPager viewPager;
    private SmartTabLayout smartTabLayout;
    private NavigationView mNavigationMenu;
    private CoordinatorLayout mCoordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.clayout);
        cultView = (CultView) findViewById(R.id.cult_view);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_left);
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        smartTabLayout = (SmartTabLayout) findViewById(R.id.smart_tab_layout);

        mNavigationMenu = (NavigationView) findViewById(R.id.vNavigation);
        mNavigationMenu.setNavigationItemSelectedListener(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        //fab.attachToRecyclerView(mRecyclerView.getRecyclerView());
        fab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent recordActivity = new Intent(MainActivity.this, RecordActivity.class);
                startActivity(recordActivity);
            }
        });

        // Edit the color of the nav bar on Lollipop+ devices
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(getResources().getColor(R.color.primary_dark));
        }

        runAppIntro();
        showAppRate();
    }

    private void showAppRate() {
        FiveStarsDialog fiveStarsDialog = new FiveStarsDialog(this,"me+rewind@yuchenhou.com");
        fiveStarsDialog.setRateText(getString(R.string.rate_app))
                .setTitle(getString(R.string.rate_app_title))
                .setForceMode(false)
                .setUpperBound(4) // Market opened if a rating >= 4 is selected
                .showAfter(4);
        /* if(!AppUtils.shouldLaunchAppRater(this)){
            return;
        }
        Snackbar.make(mCoordinatorLayout, getString(R.string.rate_app), Snackbar.LENGTH_LONG)
            .setAction(getString(R.string.rate_app_action), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=icechen1.com.blackbox")));
                }
            }).show();
        */
    }

    private void runAppIntro() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences getPrefs = PreferenceManager
                        .getDefaultSharedPreferences(getBaseContext());

                boolean isFirstStart = getPrefs.getBoolean("firstStart", true);

                if (isFirstStart) {
                    //  Launch app intro
                    Intent i = new Intent(MainActivity.this, IntroActivity.class);
                    startActivity(i);

                    SharedPreferences.Editor e = getPrefs.edit();
                    e.putBoolean("firstStart", false);
                    e.apply();
                }
            }
        });
        t.start();
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
        Bundle args = new Bundle();
        args.putBoolean("favorite", true);
        adapter = new FragmentPagerItemAdapter(
                getSupportFragmentManager(), FragmentPagerItems.with(this)
                .add(R.string.recent, MainActivityFragment.class)
                .add(R.string.favorites, MainActivityFragment.class,args)
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
        cultView.getInnerToolbar().setTitleTextColor(getResources().getColor(R.color.darker_gray));
        cultView.getInnerToolbar().setBackgroundColor(getResources().getColor(R.color.white));
        cultView.getOutToolbar().setBackgroundColor(getResources().getColor(R.color.primary));
        cultView.setOutContentLayout(R.layout.fragment_search_list);


        SuperRecyclerView searchRecyclerView = ((SuperRecyclerView) findViewById(R.id.searchRecyclerView));

        setRecyclerViewLayoutManager(searchRecyclerView.getRecyclerView());
        final RecordingAdapter adapter = new RecordingAdapter(this, false);
        searchRecyclerView.setAdapter(adapter);

        ((EditText)findViewById(R.id.search_edit_text)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                adapter.getFilter().filter(editable.toString());
            }
        });
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

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.scrollToPosition(scrollPosition);
        return linearLayoutManager;
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

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        switch(menuItem.getItemId()){
            case R.id.menu_list:
                return true;
            case R.id.menu_record:
                Intent record = new Intent(this, RecordActivity.class);
                startActivity(record);
                return true;
            case R.id.menu_settings:
                Intent pref = new Intent(this, PreferenceActivity.class);
                startActivity(pref);
                return true;
            case R.id.menu_app_upgrade:
                Intent upg = new Intent(this, PremiumActivity.class);
                startActivity(upg);
                return true;
            case R.id.menu_suggestions:
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse("mailto: me+rewind@yuchenhou.com"));
                startActivity(Intent.createChooser(emailIntent, getResources().getString(R.string.feature_recommendations)));
                return true;
            case R.id.menu_app_rate:
                String packageName = getPackageName();
                Uri uri = Uri.parse("market://details?id=" + packageName);
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
                return true;
        }
        return false;
    }

    @Override
    protected void onStart()
    {
        super.onStart();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
    }

    @Override
    protected void onDestroy()
    {

        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
    }

    /*
    @Override
    public void onRedeemAutomaticOffer(Offer offer)
    {
        for(Feature feature : offer.getFeatures())
        {
            String featureRef = feature.getReference();
            String value = feature.getValue();

            // Provide the feature defined in the campaign to the user.
            if(featureRef.toLowerCase().equals("premium")) {
                // removes ads
                final SharedPreferences.Editor e = PreferenceManager.getDefaultSharedPreferences(this).edit();
                e.putBoolean(AppConstants.IAP_PREF,true).apply();
                new AlertDialog.Builder(this)
                        .setTitle("Rewind")
                        .setMessage(offer.getOfferAdditionalParameters().get("reward_message"))
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .setIcon(R.mipmap.ic_launcher)
                        .show();
            }
        }
    }
    */
}


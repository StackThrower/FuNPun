package funpun.org;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.*;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        FunImageTaskProcessing {

    RecyclerView recyclerView;
    FunImageTask funImageTask;
    int page = 0;
    int imagesSize = 0;
    FunImageRecyclerViewAdapter adapter;

    private static boolean IS_AD_ENABLED = false;
    private static boolean IS_STAT_ENABLED = false;
    private static boolean IS_ENABLED = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initRemoteConfig();

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle("");

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        recyclerView = findViewById(R.id.images_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        recyclerView.addItemDecoration(new DividerItemDecoration(this,
//                DividerItemDecoration.VERTICAL));

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (!recyclerView.canScrollVertically(1)) {

                    funImageTask = new FunImageTask(FunImageTask.FunImageMode.GET_LIST);
                    funImageTask.page = ++page;
                    funImageTask.setProcessing(MainActivity.this);
                    funImageTask.execute((Void) null);
                }
            }
        });

        adapter = new FunImageRecyclerViewAdapter(MainActivity.this);
        recyclerView.setAdapter(adapter);
    }


    public static synchronized boolean isAdEnabled() {
        return IS_AD_ENABLED;
    }

    public static synchronized boolean isStatEnabled() {
        return IS_STAT_ENABLED;
    }


    private synchronized void initRemoteConfig() {

        final FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        mFirebaseRemoteConfig.setConfigSettings(configSettings);
        mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);

        long cacheExpiration = 3600; // 1 hour in seconds.
        // If your app is using developer mode, cacheExpiration is set to 0, so each fetch will
        // retrieve values from the service.
        if (mFirebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
            cacheExpiration = 0;
        }

        try {
            mFirebaseRemoteConfig.fetch(cacheExpiration)
                    .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

//                                MobileAds.initialize(MainActivity.this, "ca-app-pub-3579118192425679~7722662605");


                                // After config data is successfully fetched, it must be activated before newly fetched
                                // values are returned.
                                mFirebaseRemoteConfig.activateFetched();

                                String versionKey = BuildConfig.VERSION_NAME.replaceAll("\\.", "_");

                                IS_AD_ENABLED = (mFirebaseRemoteConfig.getBoolean(String.format("v%s_ad_enabled", versionKey)));
                                IS_STAT_ENABLED = (mFirebaseRemoteConfig.getBoolean(String.format("v%s_stat_enabled", versionKey)));
                                IS_ENABLED = (mFirebaseRemoteConfig.getBoolean(String.format("v%s_enabled", versionKey)));

                                setStateForFirebase(IS_STAT_ENABLED);

                                if (IS_ENABLED) {
                                    funImageTask = new FunImageTask(FunImageTask.FunImageMode.GET_LIST);
                                    funImageTask.setProcessing(MainActivity.this);
                                    funImageTask.execute((Void) null);

                                }
                            }
                        }
                    });
        } catch (Exception e) {

        }
    }

    public void setStateForFirebase(boolean enabled) {

        FirebaseAnalytics mFirebaseAnalytics =
                FirebaseAnalytics.getInstance(MainActivity.this);

        mFirebaseAnalytics.setAnalyticsCollectionEnabled(enabled);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.add_content) {

            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);

            AlertDialog dialog = new AlertDialog.Builder(MainActivity.this).setMessage(R.string.add_url_to_content)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {


                            FunImageTask funImageTask = new FunImageTask(FunImageTask.FunImageMode.ADD);
                            try {
                                funImageTask.url = URLEncoder.encode(input.getText().toString(), "UTF-8");
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                            ;
                            funImageTask.execute((Void) null);

                            Toast.makeText(getApplicationContext(), R.string.after_verification_it_will_be_available, Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            // Do stuff if user accepts
                        }
                    }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            // Do stuff when user neglects.
                        }
                    }).setView(input)
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {

                        @Override
                        public void onCancel(DialogInterface dialog) {
                            dialog.dismiss();
                            // Do stuff when cancelled
                        }
                    }).create();
            dialog.show();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onProcessFunImagesResponse(List<FunImage> list) {
        adapter.addImages(list);
        imagesSize += list != null ? list.size(): 0;
        adapter.notifyItemInserted(imagesSize);
    }
}

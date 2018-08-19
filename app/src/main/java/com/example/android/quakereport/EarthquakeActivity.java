/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.quakereport;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.List;

public class EarthquakeActivity extends AppCompatActivity implements LoaderCallbacks<List<Earthquake>> {

    public static final String LOG_TAG = EarthquakeActivity.class.getName();
    private static final String USGS_REQUES_URL = "https://earthquake.usgs.gov/fdsnws/event/1/query";
    private EarthquakeAdapter mAdapter;
    private static final int EARTHQUAKE_LOADER_ID = 0;
    // Find a reference to the {@link ListView} in the layout
    ListView mEarthquakeListView;
    ProgressBar mProgressBar;
    TextView mEmptyView;
    TextView mNoConnection;
    SwipeRefreshLayout mSwipeRefreshLayout;
    boolean isConnected;
    NetworkReceiver mReceiver = new NetworkReceiver();
    Snackbar mNoConnectionSnackBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.earthquake_activity);


        //Register BroadCast Receiver
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        mReceiver = new NetworkReceiver();
        registerReceiver(mReceiver, intentFilter);

        mNoConnectionSnackBar = Snackbar.make(findViewById(R.id.coordinator_layout),
                R.string.lost_connection, Snackbar.LENGTH_INDEFINITE);
        mNoConnectionSnackBar.setAction(R.string.retry, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadLoader();
            }
        });

        mEarthquakeListView = (ListView) findViewById(R.id.list);


        // Create a new adapter that takes the list of earthquakes as input
        mAdapter = new EarthquakeAdapter(this, new ArrayList<Earthquake>());

        // Set the adapter on the {@link ListView}
        // so the list can be populated in the user interface
        mEarthquakeListView.setAdapter(mAdapter);

        mEmptyView = (TextView) findViewById(R.id.empty);
        mProgressBar = (ProgressBar) findViewById(R.id.progress);
        mEarthquakeListView.setEmptyView(mEmptyView);

        loadLoader();

        //Set onClick Listener
        mEarthquakeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent web = new Intent(Intent.ACTION_VIEW);
                String url = mAdapter.getItem(i).getmUrl();
                web.setData(Uri.parse(url));
                startActivity(web);


            }
        });

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);

        //Set onRefresh Listener
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                                                     @Override
                                                     public void onRefresh() {
                                                         loadLoader();
                                                         mSwipeRefreshLayout.setRefreshing(false);
                                                     }
                                                 }
        );

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    private void loadLoader() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        isConnected = activeNetwork != null &&
                activeNetwork.isConnected();
        mNoConnection = (TextView) findViewById(R.id.no_network);


        if (isConnected) {
            mNoConnection.setVisibility(View.GONE);
            mNoConnectionSnackBar.dismiss();
            getLoaderManager().initLoader(EARTHQUAKE_LOADER_ID, null, this);
        } else {
            if (mEarthquakeListView.getVisibility() != View.VISIBLE) {
                mProgressBar.setVisibility(View.GONE);
                mNoConnection.setText(R.string.no_internet);
                mNoConnection.setVisibility(View.VISIBLE);
                mNoConnectionSnackBar.dismiss();
            } else {
                mNoConnectionSnackBar = Snackbar.make(findViewById(R.id.coordinator_layout),
                        R.string.lost_connection, Snackbar.LENGTH_INDEFINITE);
                mNoConnectionSnackBar.setAction(R.string.retry, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        loadLoader();
                    }
                });
                mNoConnectionSnackBar.show();
            }

        }
    }

    @Override
    public Loader<List<Earthquake>> onCreateLoader(int id, Bundle args) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String minMagnitude = sharedPreferences.getString(getString(R.string.settings_min_magnitude_key),
                getString(R.string.settings_min_magnitude_default));
        String limit = sharedPreferences.getString(getString(R.string.settings_limit_key),
                getString(R.string.settings_limit_default));
        String maxMagnitude = sharedPreferences.getString(getString(R.string.settings_max_magnitude_key),
                getString(R.string.settings_max_magnitude_default));
        String orderby = sharedPreferences.getString(getString(R.string.settings_orderby_key),
                getString(R.string.settings_orderby_default));

        Uri baseUri = Uri.parse(USGS_REQUES_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();
        uriBuilder.appendQueryParameter("format", "geojson");
        uriBuilder.appendQueryParameter("limit", limit);
        uriBuilder.appendQueryParameter("minmag", minMagnitude);
        uriBuilder.appendQueryParameter("maxmagnitude", maxMagnitude);
        uriBuilder.appendQueryParameter("orderby", orderby);

        return new EarthquakeLoader(this, uriBuilder.toString());
    }

    @Override
    public void onLoadFinished(Loader<List<Earthquake>> loader, List<Earthquake> earthquakes) {
        mProgressBar.setVisibility(View.GONE);
        mAdapter.clear();
        if (earthquakes != null && !earthquakes.isEmpty()) {
            mAdapter.addAll(earthquakes);
        }
        if (isConnected) {
            mEmptyView.setText(R.string.no_earthquakes_found);
        }

    }


    @Override
    public void onLoaderReset(Loader<List<Earthquake>> loader) {
        mAdapter.clear();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;

            case R.id.menu_refresh:
                mSwipeRefreshLayout.setRefreshing(true);
                loadLoader();
                mSwipeRefreshLayout.setRefreshing(false);
                return true;

        }

        return super.onOptionsItemSelected(item);

    }

    public class NetworkReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            loadLoader();
        }
    }
}

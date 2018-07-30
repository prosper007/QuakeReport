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

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;


import java.util.ArrayList;
import java.util.List;

public class EarthquakeActivity extends AppCompatActivity implements android.support.v4.app.LoaderManager.LoaderCallbacks<List<Earthquake>> {

    public static final String LOG_TAG = EarthquakeActivity.class.getName();
    private static final String USGS_REQUES_URL = "https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&orderby=time&minmag=5&limit=10";
    private EarthquakeAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.earthquake_activity);
        /*EarthquakesTask earthquakesTask = new EarthquakesTask();
        earthquakesTask.execute(USGS_REQUES_URL);
*/
        // Find a reference to the {@link ListView} in the layout
        ListView mEarthquakeListView = (ListView) findViewById(R.id.list);

        // Create a new adapter that takes the list of earthquakes as input
        mAdapter = new EarthquakeAdapter(this, new ArrayList<Earthquake>());

        // Set the adapter on the {@link ListView}
        // so the list can be populated in the user interface
        mEarthquakeListView.setAdapter(mAdapter);
        getSupportLoaderManager().initLoader(0, null, this).forceLoad();

        //Set on Click Listener
        mEarthquakeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent web = new Intent(Intent.ACTION_VIEW);
                String url = mAdapter.getItem(i).getmUrl();
                web.setData(Uri.parse(url));
                startActivity(web);


            }
        });


    }

    @Override
    public android.support.v4.content.Loader<List<Earthquake>> onCreateLoader(int id, Bundle args) {
        return new EarthquakeLoader(this, USGS_REQUES_URL);
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<List<Earthquake>> loader, List<Earthquake> earthquakes) {
        mAdapter.clear();
        if(earthquakes != null && !earthquakes.isEmpty()){
            mAdapter.addAll(earthquakes);
        }
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<List<Earthquake>> loader) {
        mAdapter.clear();
    }

   /*private class EarthquakesTask extends AsyncTask<String, Void, List<Earthquake>> {
        @Override
        protected List<Earthquake> doInBackground(String... urls) {
            if (urls.length < 1 || urls[0] == null) {
                return null;
            }

            //get earthquakesss
            return QueryUtils.fetchEarthquakeData(urls[0]);
        }

        @Override
        protected void onPostExecute(List<Earthquake> earthquakes) {
            mAdapter.clear();
            if(earthquakes != null && !earthquakes.isEmpty()){
                mAdapter.addAll(earthquakes);
            }
        }
    }*/
}

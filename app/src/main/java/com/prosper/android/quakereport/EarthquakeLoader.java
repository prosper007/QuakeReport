package com.prosper.android.quakereport;

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.util.List;

public class EarthquakeLoader extends AsyncTaskLoader<List<Earthquake>>{
    private String[] mURLs;

    public EarthquakeLoader(Context context, String... urls){
        super(context);
        mURLs = urls;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public List<Earthquake> loadInBackground() {
        if (mURLs.length < 1 || mURLs[0] == null) {
            return null;
        }

        //get earthquakesss
        return QueryUtils.fetchEarthquakeData(mURLs[0]);

    }
}

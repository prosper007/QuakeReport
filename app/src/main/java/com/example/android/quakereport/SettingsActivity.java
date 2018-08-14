package com.example.android.quakereport;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.ListIterator;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
    }

    public static class EarthquakePreferenceFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {
        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings_main);

            Preference minMagnitude = findPreference(getString(R.string.settings_min_magnitude_key));
            bindPreferenceSummaryToValue(minMagnitude);
            Preference maxMagnitude = findPreference(getString(R.string.settings_max_magnitude_key));
            bindPreferenceSummaryToValue(maxMagnitude);
            Preference limit = findPreference(getString(R.string.settings_limit_key));
            bindPreferenceSummaryToValue(limit);
            Preference orderBy = findPreference(getString(R.string.settings_orderby_key));
            bindPreferenceSummaryToValue(orderBy);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object o) {
            String stringValue = o.toString();
            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                int prefIndex = listPreference.findIndexOfValue(stringValue);
                if (prefIndex >= 0) {
                    CharSequence[] labels = listPreference.getEntries();
                    preference.setSummary(labels[prefIndex]);
                }
            } else {
                preference.setSummary(stringValue);
            }
            if (stringValue.equals("")) {
                stringValue = "No preference set";
                preference.setSummary(stringValue);
            }


            return true;
        }

        private void bindPreferenceSummaryToValue(Preference preference) {
                preference.setOnPreferenceChangeListener(this);
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(preference.getContext());
                String stringValue = sharedPreferences.getString(preference.getKey(), "");

                onPreferenceChange(preference, stringValue);
        }

    }
}

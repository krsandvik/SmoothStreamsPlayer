package com.iosharp.android.ssplayer;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;

public class SettingsActivity extends PreferenceActivity implements
        OnSharedPreferenceChangeListener {

    private static final String TAG = SettingsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceManager.setDefaultValues(getBaseContext(), R.xml.preferences,
                false);

        addPreferencesFromResource(R.xml.preferences);
        // show the current value in the settings screen
        for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
            initSummary(getPreferenceScreen().getPreference(i));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        updatePreferenceSummary(findPreference(key));

        // Check only if one of the three below keys is changed to call method
        // to get service id and password
        if (key.equals(getString(R.string.pref_service_key))
                || key.equals(getString(R.string.pref_service_username_key))
                || key.equals(getString(R.string.pref_service_password_key))) {

            if (hasSetServiceDetails()) {

                FetchLoginInfoTask fetchLoginInfoTask = new FetchLoginInfoTask(getApplicationContext());
                fetchLoginInfoTask.execute();
            }

        }

    }

    private void initSummary(Preference p) {
        if (p instanceof PreferenceCategory) {
            PreferenceCategory cat = (PreferenceCategory) p;
            for (int i = 0; i < cat.getPreferenceCount(); i++) {
                initSummary(cat.getPreference(i));
            }
        } else {
            updatePreferenceSummary(p);
        }
    }

    private void updatePreferenceSummary(Preference p) {
        if (p instanceof ListPreference) {
            ListPreference listPref = (ListPreference) p;
            p.setSummary(listPref.getEntry());
        }
        if (p instanceof EditTextPreference) {
            EditTextPreference editTextPref = (EditTextPreference) p;
            if (p.getTitle().toString().contains("assword")) {
                if (editTextPref.getText() == null) {
                    p.setSummary("");
                } else {
                    p.setSummary(editTextPref.getText().replaceAll(".", "*"));
                }
            } else {
                p.setSummary(editTextPref.getText());
            }
        }
        if (p instanceof MultiSelectListPreference) {
            EditTextPreference editTextPref = (EditTextPreference) p;
            p.setSummary(editTextPref.getText());
        }
    }

    /**
     * Checks to see if all needed fields for retrieving service id and username
     * are filled out.
     *
     * @return boolean
     */
    private boolean hasSetServiceDetails() {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(this);

        String username = prefs.getString(getString(R.string.pref_service_username_key), null);
        String password = prefs.getString(getString(R.string.pref_service_password_key), null);
        String service = prefs.getString(getString(R.string.pref_service_key), null);

        if (username == null || password == null || service == null) {
            return false;
        } else {
            return true;
        }
    }
}
package fr.valentinporchet.romeo;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.WindowManager;

/**
 * Created by Valentin on 19/01/2016.
 */
public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // remove the status bar and title
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // changes of preferences are linked to the listener
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);

        // main method
        addPreferencesFromResource(R.xml.preferences);

        // update the summary of IP edit text
        Preference preferenceServerIP = findPreference("preference_penpal_IP");
        EditTextPreference textPreferenceServerIP = (EditTextPreference) preferenceServerIP;
        preferenceServerIP.setSummary(textPreferenceServerIP.getText());
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.i("SettingsActivity", "Property changed : " + key + " => " + sharedPreferences.getString(key, "null"));

        // if this is an edit text, edit the summary with the new value
        Preference pref = findPreference(key);

        if (pref instanceof EditTextPreference) {
            EditTextPreference editPref = (EditTextPreference) pref;
            pref.setSummary(editPref.getText());
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }
}
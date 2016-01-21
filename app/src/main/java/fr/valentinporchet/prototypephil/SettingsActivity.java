package fr.valentinporchet.prototypephil;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by Valentin on 19/01/2016.
 */
public class SettingsActivity extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
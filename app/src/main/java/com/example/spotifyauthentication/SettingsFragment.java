package com.example.spotifyauthentication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.PreferenceFragmentCompat;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.prefs);
        EditTextPreference pref = (EditTextPreference) findPreference("key_numsongs");
        pref.setSummary(pref.getText());
        pref = (EditTextPreference) findPreference("key_popu");
        pref.setSummary(pref.getText());
        pref = (EditTextPreference) findPreference("key_name");
        pref.setSummary(pref.getText());
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.equals("key_numsongs") ){
            EditTextPreference pref = (EditTextPreference) findPreference(s);
            pref.setSummary(pref.getText());
        }
        if (s.equals("key_popu") ){
            EditTextPreference pref = (EditTextPreference) findPreference(s);
            pref.setSummary(pref.getText());
        }
        if (s.equals("key_name") ){
            EditTextPreference pref = (EditTextPreference) findPreference(s);
            pref.setSummary(pref.getText());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);

    }
}

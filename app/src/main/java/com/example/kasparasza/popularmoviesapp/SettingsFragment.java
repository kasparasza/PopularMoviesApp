package com.example.kasparasza.popularmoviesapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.widget.Toast;

/**
 * Preference Fragment with Preferences that are used with discover/movie endpoint
 */

public class SettingsFragment extends PreferenceFragmentCompat implements
        Preference.OnPreferenceChangeListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private static final int EARLIEST_YEAR = 1800;
    private static final int LATEST_YEAR = 2100;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.all_movies_preferences);

        PreferenceScreen prefScreen = getPreferenceScreen();
        SharedPreferences sharedPreferences = prefScreen.getSharedPreferences();
        int count = prefScreen.getPreferenceCount();

        // Go through all of the preferences, and set up their preference summary.
        // Note: this is needed only for EditTextPreference, since other preferences
        // have their summary set up in the xml
        for (int i = 0; i < count; i++) {
            Preference p = prefScreen.getPreference(i);
            if (p instanceof EditTextPreference) {
                String value = sharedPreferences.getString(p.getKey(), "");
                p.setSummary(value);
            }
        }

        // set preference change listener for EditTextPreferences, since this type of
        // preferences requires input validation
        findPreference(getString(R.string.pref_earliest_year_key)).setOnPreferenceChangeListener(this);
        findPreference(getString(R.string.pref_latest_year_key)).setOnPreferenceChangeListener(this);
    }

    /**
    * method required by OnPreferenceChangeListener,
    * performs input validation for EditTextPreferences
     * */
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {

        // check that the preference is the one of our EditTextPreferences
        if (preference.getKey().equals(getString(R.string.pref_earliest_year_key)) ||
                preference.getKey().equals(getString(R.string.pref_latest_year_key))) {
            String stringYear = (String) newValue;

            boolean isValid = yearIsValid(stringYear);
            // if user input is valid, check whether the setting of the other EditTextPreference
            // is consistent with this new setting
            if(isValid){
                checkYearsForConsistency(preference, stringYear);
            }
            return isValid;
        }
        return false;
    }

    /**
    * checks whether year entered is valid
    * @param yearString year entered in EditTextPreference
    * @return true if year is valid
    * */
    private boolean yearIsValid(String yearString){

        Toast error = Toast.makeText(getContext(), R.string.pref_year_check_toast, Toast.LENGTH_SHORT);

        try {
            int year = Integer.parseInt(yearString);
            // If the number is outside of the acceptable range, show an error.
            if (year > LATEST_YEAR || year < EARLIEST_YEAR) {
                error.show();
                return false;
            }
        } catch (NumberFormatException nfe) {
            // If user input can not be parsed to a number, show an error
            error.show();
            return false;
        }
        return true;
    }

    /**
    * checks whether the earliest year is not actually greater than the latest,
    * if years are inconsistent - one of them is reset; namely we reset value of the
    * preference other than the preference which was just changed
    * @param preferenceJustChanged preference which was just changed
    * */
    private void checkYearsForConsistency(Preference preferenceJustChanged, String newValue){

        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        String keyEarliestYearPref = getString(R.string.pref_earliest_year_key);
        String keyLatestYearPref = getString(R.string.pref_latest_year_key);

        Preference startYearPreference = findPreference(keyEarliestYearPref);
        int startYear = Integer.parseInt(startYearPreference.getSummary().toString());
        Preference endYearPreference = findPreference(keyLatestYearPref);
        int endYear = Integer.parseInt(endYearPreference.getSummary().toString());

        if(preferenceJustChanged.getKey().equals(keyEarliestYearPref)
                && Integer.parseInt(newValue) > endYear){
            sharedPreferences.edit().putString(keyLatestYearPref, newValue).apply();
        }
        if(preferenceJustChanged.getKey().equals(keyLatestYearPref)
                && startYear > Integer.parseInt(newValue)){
            sharedPreferences.edit().putString(keyEarliestYearPref, newValue).apply();
        }
    }

    /**
     * method required by OnSharedPreferenceChangeListener,
     * performs preference summary update for EditTextPreferences
     *
     * Note: this is needed only for EditTextPreference, since other preferences
     * have their summary set up in the xml
     * */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference preference = findPreference(key);
        if (preference != null) {
            if (preference instanceof EditTextPreference) {
                String value = sharedPreferences.getString(preference.getKey(), "");
                preference.setSummary(value);
            }
        }
    }

    // Note: for the OnSharedPreferenceChangeListener to work, we need to register it
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

}

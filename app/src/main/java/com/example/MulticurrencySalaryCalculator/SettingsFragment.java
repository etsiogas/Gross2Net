package com.example.MulticurrencySalaryCalculator;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.preference.MultiSelectListPreference;
import androidx.preference.Preference;

import com.takisoft.preferencex.PreferenceFragmentCompat;

import java.util.ArrayList;
import java.util.HashSet;

public class SettingsFragment extends PreferenceFragmentCompat
{
	static final String TAG = SettingsFragment.class.getSimpleName();
	ArrayList<String> currencyCodes;
	
	public SettingsFragment(ArrayList<String> arrayList)
	{
		currencyCodes = arrayList;
	}
	
	@Override
	public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState, String rootKey)
	{
		setPreferencesFromResource(R.xml.preferences, rootKey);
		MultiSelectListPreference preference = findPreference("currencies_preference");
		
		if(preference != null)
		{
			String[] codesArray = new String[currencyCodes.size()];
			preference.setEntryValues(currencyCodes.toArray(codesArray));
			
			preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
			{
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue)
				{
					HashSet<?> currentCurrencies = (HashSet<?>) newValue;
					return !currentCurrencies.isEmpty();
				}
			});
		}
	}
}

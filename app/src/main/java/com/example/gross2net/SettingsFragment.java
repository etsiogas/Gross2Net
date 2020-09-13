package com.example.gross2net;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.preference.MultiSelectListPreference;
import androidx.preference.Preference;

import com.takisoft.preferencex.PreferenceFragmentCompat;

import java.util.HashSet;

public class SettingsFragment extends PreferenceFragmentCompat
{
	static final String TAG = SettingsFragment.class.getSimpleName();
	
	@Override
	public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState, String rootKey)
	{
		setPreferencesFromResource(R.xml.preferences, rootKey);
		MultiSelectListPreference preference = findPreference("currencies_preference");
		
		if(preference != null)
		{
			// List<String> currencyNames = Arrays.asList(getResources().getStringArray( R.array.currencies) );
			// preference.setValues( new HashSet<>( currencyNames.subList(0, 3) ) );
			
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

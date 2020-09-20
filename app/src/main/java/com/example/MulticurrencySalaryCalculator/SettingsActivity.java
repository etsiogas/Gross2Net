package com.example.MulticurrencySalaryCalculator;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class SettingsActivity extends AppCompatActivity
{
	static final String TAG = SettingsActivity.class.getSimpleName();
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Bundle bundle = getIntent().getExtras();
		
		if(bundle != null)
			getSupportFragmentManager().beginTransaction().replace(
							android.R.id.content, new SettingsFragment(bundle.getStringArrayList("currency_codes")) ).commit();
	}
}

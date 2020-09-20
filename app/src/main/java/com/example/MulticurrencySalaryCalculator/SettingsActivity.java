package com.example.gross2net;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;

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

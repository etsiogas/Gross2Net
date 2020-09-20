package com.example.MulticurrencySalaryCalculator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

// String lineNumber = Integer.toString( Thread.currentThread().getStackTrace()[2].getLineNumber() );

public class MainActivity extends AppCompatActivity
{
	RadioButton monthGross, months12Gross, months14Gross, monthNet, months12Net, months14Net;
	RadioGroup grossGroup, netGroup;
	Button convert, clearGross, clearNet;
	CheckBox incomeTax, insuranceTax, otherTax, vat;
	Spinner spinnerGross, spinnerNet;
	SpinnerAdapter spinnerAdapter;
	DecimalFormat twoDecimals;
	EditText net, gross;
	String lastFocus = null;
	Set<String> currenciesSet;
	Map<String, Double> symbol2Rate;
	ArrayList<String> currenciesList;
	SharedPreferences sharedPreferences;
	SharedPreferences.OnSharedPreferenceChangeListener listener;
	double incomeTaxRate, insuranceTaxRate, otherTaxRate, VATRate;
	
	static final String TAG = MainActivity.class.getSimpleName();
	
	Map<Integer, Integer> monthsMap = new HashMap<Integer, Integer>()
	{{
		put(0, 1); put(1, 12); put(2, 14);
	}};
	
	Map<String, String> Code2Symbol = new HashMap<String, String>()
	{{
		put("EUR", "€");
		put("USD", "$");
		put("GBP", "£");
		put("HRK", "kn");
		put("AUD", "A$");
		put("PLN", "zł");
		put("INR", "₹");
		put("IDR", "Rp");
		put("JPY", "JPY");
		put("BGN", "лв");
		put("ILS", "₪");
		put("SGD", "S$");
		put("PHP", "₱");
		put("NZD", "NZ$");
		put("DKK", "DKK");
		put("CZK", "Kč");
		put("CNY", "CNY");
		put("ZAR", "R");
		put("HUF", "Ft");
		put("ISK", "ISK");
		put("TRY", "₺");
		put("RUB", "₽");
		put("KRW", "₩");
		put("NOK", "NOK");
		put("BRL", "R$");
		put("CHF", "Fr");
		put("MXN", "Mex$");
		put("HKD", "HK$");
		put("RON", "lei");
		put("SEK", "SEK");
		put("CAD", "C$");
		put("MYR", "RM");
		put("THB", "฿");
	}};
	
	@Override
  protected void onCreate(Bundle savedInstanceState)
	{
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
		
		symbol2Rate = new HashMap<String, Double>() { { put("€", 1.0); } };
		twoDecimals = new DecimalFormat("##.##");
		
		monthGross = findViewById(R.id.month_gross);
    months12Gross = findViewById(R.id.year_gross);
    months14Gross = findViewById(R.id.year_gross_2);
    monthNet = findViewById(R.id.month_net);
    months12Net = findViewById(R.id.year_net);
    months14Net = findViewById(R.id.year_net_2);
		incomeTax = findViewById(R.id.income_tax);
		insuranceTax = findViewById(R.id.insurance_tax);
		otherTax = findViewById(R.id.other_tax);
		vat = findViewById(R.id.vat);
    net = findViewById(R.id.net);
    gross = findViewById(R.id.gross);
    convert = findViewById(R.id.convert_btn);
    clearGross = findViewById(R.id.clear_gross);
    clearNet = findViewById(R.id.clear_net);
    spinnerGross = findViewById(R.id.currencies_gross);
    spinnerNet = findViewById(R.id.currencies_net);
		grossGroup = findViewById(R.id.radios_gross);
		netGroup = findViewById(R.id.radios_net);
		
		// ActionBar actionBar = getSupportActionBar();
		// actionBar.setTitle("Main");
		
		setListeners();
		
		RequestQueue queue = Volley.newRequestQueue(this);
		String url = "https://api.exchangeratesapi.io/latest";
		
		StringRequest stringRequest = new StringRequest(
			Request.Method.GET, url, new Response.Listener<String>()
			{
				@Override
				public void onResponse(String response)
				{
					checkResponse(response);
					Log.i(TAG, "Got updated currency rates");
				}
			},
			new Response.ErrorListener()
			{
				@Override
				public void onErrorResponse(VolleyError error)
				{
					Log.e(TAG, "Failed to get currency rates");
				}
			}
		);
		
		queue.add(stringRequest);
		
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		updateCurrencies();
		
		listener = new SharedPreferences.OnSharedPreferenceChangeListener()
		{
			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s)
			{
				updateCurrencies();
			}
		};
		
		sharedPreferences.registerOnSharedPreferenceChangeListener(listener);
		
		String[] currenciesArray = getResources().getStringArray( R.array.currencies);
		currenciesSet = new HashSet<>();
		currenciesList = new ArrayList<>();
		
		for(String string : currenciesArray)
		{
			String currencyCode = string.substring(string.indexOf("(") + 1);
			currencyCode = currencyCode.substring( 0, currencyCode.indexOf("/") );
			currenciesSet.add(currencyCode);
			currenciesList.add(currencyCode);
		}
	}
	
	public void updateCurrencies()
	{
		for( Map.Entry<String, ?> entry : sharedPreferences.getAll().entrySet() )
		{
			Object O = entry.getValue();
			
			switch( entry.getKey() )
			{
				case "currencies_preference":
					ArrayList<String> currentSymbols = new ArrayList<>();
					HashSet<?> savedCurrencies = (HashSet<?>) O;
					
					for(Object currencyCode: savedCurrencies)
						if(currencyCode instanceof String)
							currentSymbols.add( Code2Symbol.get(currencyCode) );
						
					spinnerAdapter = new ArrayAdapter<> (this, android.R.layout.simple_spinner_item, currentSymbols);
					spinnerGross.setAdapter(spinnerAdapter);
					spinnerNet.setAdapter(spinnerAdapter);
					
					break;
					
				case "income_tax_preference":
					if(O instanceof String)
					{
						incomeTaxRate = Double.parseDouble( String.valueOf(O) );
						incomeTaxRate /= 100;
						String rate = "Income tax (" + twoDecimals.format(incomeTaxRate * 100) + "%)";
						incomeTax.setText(rate);
					}
					
					break;
					
				case "insurance_tax_preference":
					if(O instanceof String)
					{
						insuranceTaxRate = Double.parseDouble( String.valueOf(O) );
						insuranceTaxRate /= 100;
						String rate = "Insurance tax (" + twoDecimals.format(insuranceTaxRate * 100) + "%)";
						insuranceTax.setText(rate);
					}
					
					break;
					
				case "other_tax_preference":
					if(O instanceof String)
					{
						otherTaxRate = Double.parseDouble( String.valueOf(O) );
						otherTaxRate /= 100;
						String rate = "Other tax (" + twoDecimals.format(otherTaxRate * 100) + "%)";
						otherTax.setText(rate);
					}
					
					break;
					
				case "vat_preference":
					if(O instanceof String)
					{
						VATRate = Double.parseDouble( String.valueOf(O) );
						VATRate /= 100;
						String rate = "VAT (" + twoDecimals.format(VATRate * 100) + "%)";
						vat.setText(rate);
					}
			}
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item)
	{
		if(item.getItemId() == R.id.settings)
		{		
			Intent intent = new Intent(this, SettingsActivity.class);
			intent.putStringArrayListExtra("currency_codes", currenciesList);
			startActivity(intent);
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	private void checkResponse(String response)
	{
		try
		{
			JSONObject obj = new JSONObject(response);
			Iterator<String> keys = obj.keys();
			
			while( keys.hasNext() )
			{
				String key = keys.next();
				
				if(obj.get(key) instanceof JSONObject)
				{
					JSONObject obj2 = (JSONObject) obj.get(key);
					Iterator<String> rates = obj2.keys();
					
					while( rates.hasNext() )
					{
						String currencyCode = rates.next();
						
						if( !currenciesSet.contains(currencyCode) )
							throw new AssertionError("Currency code not included in currency array");
						
						symbol2Rate.put( Code2Symbol.get(currencyCode), obj2.optDouble(currencyCode) );
					}
				}
			}
		}
		catch (Throwable t)
		{
			Log.e(TAG, "Could not parse malformed JSON: \"" + response + "\"");
		}
	}
	
	public void setListeners()
	{
		convert.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if(lastFocus == null)
					return;
				
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				
				if(imm == null)
					return;
				
				imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
				
				final double CurrentIncomeTax = incomeTax.isChecked() ? incomeTaxRate : 0.0,
										 CurrentInsuranceTax = insuranceTax.isChecked() ? insuranceTaxRate : 0.0,
										 CurrentOtherTax = otherTax.isChecked() ? otherTaxRate : 0.0;
				
				final double DEDUCTION_RATE = (1.0 - CurrentIncomeTax - CurrentInsuranceTax - CurrentOtherTax) /
																			(1.0 + (vat.isChecked() ? VATRate : 0.0) );
				
				int radioButtonID = grossGroup.getCheckedRadioButtonId();
				View radioButton = grossGroup.findViewById(radioButtonID);
				int index_gross = grossGroup.indexOfChild(radioButton);
				radioButtonID = netGroup.getCheckedRadioButtonId();
				radioButton = netGroup.findViewById(radioButtonID);
				int index_net = netGroup.indexOfChild(radioButton);
				
				Integer monthsNet = monthsMap.get(index_net), monthsGross = monthsMap.get(index_gross);
				double monthFactor = 1.0;
				
				if(monthsNet != null && monthsGross != null)
					monthFactor = (double) monthsNet / monthsGross;
				
				double currenciesFactor = 1.0;
				Double currencyNet = symbol2Rate.get( spinnerNet.getSelectedItem().toString() ),
							 currencyGross = symbol2Rate.get( spinnerGross.getSelectedItem().toString() );
				
				if(currencyNet != null && currencyGross != null)
					currenciesFactor = currencyNet / currencyGross;
				
				double netAmount, grossAmount;
				
				if( lastFocus.equals("gross") && !gross.getText().toString().isEmpty() )
				{
					grossAmount = Double.parseDouble( gross.getText().toString() );
					netAmount = monthFactor * currenciesFactor * grossAmount * DEDUCTION_RATE;
					net.setText( twoDecimals.format(netAmount) );
				}
				else if( lastFocus.equals("net") && !net.getText().toString().isEmpty() )
				{
					netAmount = Double.parseDouble( net.getText().toString() );
					grossAmount = netAmount / (DEDUCTION_RATE * monthFactor * currenciesFactor);
					gross.setText( twoDecimals.format(grossAmount) );
				}
				else
					Log.w(TAG, "No amount to convert");
			}
		});
		
		gross.setOnFocusChangeListener(new View.OnFocusChangeListener()
		{
			@Override
			public void onFocusChange(View v, boolean hasFocus)
			{
				if(hasFocus)
					lastFocus = "gross";
			}
		});
		
		net.setOnFocusChangeListener(new View.OnFocusChangeListener()
		{
			@Override
			public void onFocusChange(View v, boolean hasFocus)
			{
				if(hasFocus)
					lastFocus = "net";
			}
		});
		
		clearGross.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				gross.setText("");
			}
		});
		
		clearNet.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				net.setText("");
			}
		});
		
		incomeTax.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				convert.performClick();
			}
		});
		
		insuranceTax.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				convert.performClick();
			}
		});
		
		otherTax.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				convert.performClick();
			}
		});
		
		vat.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				convert.performClick();
			}
		});
		
		spinnerGross.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
		{
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
			{
				if( !gross.getText().toString().isEmpty() )
					convert.performClick();
			}
			
			@Override
			public void onNothingSelected(AdapterView<?> parent)
			{
			}
		});
		
		spinnerNet.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
		{
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
			{
				if( !net.getText().toString().isEmpty() )
					convert.performClick();
			}
			
			@Override
			public void onNothingSelected(AdapterView<?> parent)
			{
			}
		});
		
		monthGross.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				convert.performClick();
			}
		});
		
		months12Gross.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				convert.performClick();
			}
		});
		
		months14Gross.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				convert.performClick();
			}
		});
		
		monthNet.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				convert.performClick();
			}
		});
		
		months12Net.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				convert.performClick();
			}
		});
		
		months14Net.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				convert.performClick();
			}
		});
	}
}

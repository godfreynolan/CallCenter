package com.riis.callcenter;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.riis.callcenter.broadsoftrequest.BroadsoftRequestRunner;
import com.riis.callcenter.R;

public class SettingsActivity extends Activity {
	public static final String LAST_USERNAME_KEY = "lastUsername";
	public static final String LAST_URL_KEY = "lastURL";
	public static final String DATE_FORMAT_KEY = "dateFormat";
	public static final String LAST_UPDATED_KEY = "lastUpdated";
	public static final String SHARED_PREF_NAME = "mySharedPrefs";
	
	private Button logoutButton;
	private TextView usernameView;
	private TextView urlView;
	private CheckBox dateFormatCheckBox;
	
	private SharedPreferences sharedPrefs;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
    	setTheme(R.style.CustomTheme);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.settings_screen);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_titlebar_with_logout);
		((TextView)findViewById(R.id.title)).setText("Settings");

		sharedPrefs = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);

		usernameView = (TextView) findViewById(R.id.usernameField);
		urlView = (TextView) findViewById(R.id.urlField);
		dateFormatCheckBox = (CheckBox) findViewById(R.id.dateFormat);
		
		usernameView.setText(sharedPrefs.getString(LAST_USERNAME_KEY, ""));
		urlView.setText(sharedPrefs.getString(LAST_URL_KEY, ""));
		dateFormatCheckBox.setChecked(sharedPrefs.getBoolean(DATE_FORMAT_KEY, false));
		
		setOnChangeListeners();
		setUpTheLogOutButton();
	}

	private void setOnChangeListeners() {
		usernameView.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				String username = usernameView.getText().toString();
				SharedPreferences.Editor editor = sharedPrefs.edit();
				editor.putString(LAST_USERNAME_KEY, username);
				editor.commit();
				
				BroadsoftRequestRunner.setCredentials(username, null, null);
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
		});
		
		urlView.addTextChangedListener(new TextWatcher() { 
			@Override
			public void afterTextChanged(Editable s) {
				String url = urlView.getText().toString();
				SharedPreferences.Editor editor = sharedPrefs.edit();
				editor.putString(LAST_URL_KEY, url);
				editor.commit();
				
				BroadsoftRequestRunner.setCredentials(null, null, url);
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
		});
		
		dateFormatCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				SharedPreferences.Editor editor = sharedPrefs.edit();
				editor.putBoolean(DATE_FORMAT_KEY, isChecked);
				editor.commit();
			}
		});
	}
	
	private void setUpTheLogOutButton() {
		logoutButton = (Button) findViewById(R.id.logout);
		logoutButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
				startActivity(intent);
			}
		});
	}

}

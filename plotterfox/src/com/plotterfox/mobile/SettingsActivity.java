package com.plotterfox.mobile;
import android.app.ActionBar;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;

import android.view.Menu;
import android.view.MenuItem;

public class SettingsActivity extends PreferenceActivity implements
OnSharedPreferenceChangeListener{

    private Preference pref;
	    @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        ActionBar actionBar = getActionBar();
	        actionBar.setHomeButtonEnabled(true);
	        actionBar.setDisplayHomeAsUpEnabled(true);
	        
	        addPreferencesFromResource(R.xml.settings);
	        
			SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(this);
			
	    	EditTextPreference etp = (EditTextPreference) findPreference("postLimit");
	    	String prefString = shared.getString("postLimit", "");
	    	etp.setSummary("Your post limit is '" + prefString + "'");
		    }
		@Override
		public boolean onCreateOptionsMenu(Menu menu) {
			// Inflate the menu; this adds items to the action bar if it is present.
			ActionBar actionBar = getActionBar();
			actionBar.setTitle("Settings");
			 
			return true;
		}
	    @Override
	    public boolean onOptionsItemSelected(MenuItem item) {
		    switch (item.getItemId()) {
		        case android.R.id.home:
		            NavUtils.navigateUpFromSameTask(this);
		            return true;
		        default:
		            return super.onOptionsItemSelected(item);
		    }
	    }
	    @Override     
	    protected void onResume() {
	        super.onResume();
	        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);     
	    } //end onResume

	    @Override     
	    protected void onPause() {         
	        super.onPause();
	        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	    } //end onPause
	    
		@Override
		public void onSharedPreferenceChanged(
				SharedPreferences sharedPreferences, String key) {


				SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(this);
				
		    	EditTextPreference etp = (EditTextPreference) findPreference("postLimit");
		    	String prefString = shared.getString("postLimit", "");
		    	etp.setSummary("Your post limit is '" + prefString + "'");

		}
}


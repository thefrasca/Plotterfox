package com.plotterfox.mobile;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

public class LoginActivity extends Activity {
	

	 EditText passText  ;
	 EditText userText;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.loginlayout);

		 userText = new EditText(this);
		 userText = (EditText) findViewById(R.id.user);
		 passText = new EditText(this);
		 passText = (EditText) findViewById(R.id.password);

/*
		 final Button button = (Button) findViewById(R.id.button_id);
         button.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 // Perform action on click
             }
         });

		      SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		      Editor ed = prefs.edit();
		      ed.putString(KEY_USERNAME, user );
		      ed.putString(KEY_PASSWORD, pass);
		      ed.commit();
*/
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	public void login(View v)
	{
		 String user = null;
		 String pass = null;
		 user = userText.getText().toString();
		 pass = passText.getText().toString();
		 String hashedPassword = getMD5_Hash(pass);
	     SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
	     Editor ed = prefs.edit();
	     ed.putString("username", user );
	     Log.e("hash",hashedPassword);
	     ed.putString("password", hashedPassword);
	     ed.commit();
	     
			Intent goToNextActivity = new Intent(this, MainActivity.class);
			startActivity(goToNextActivity);
	     
	}
	public static String getMD5_Hash(String s) { MessageDigest m = null;

    try {
            m = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
    }

    m.update(s.getBytes(),0,s.length());
    String hash = new BigInteger(1, m.digest()).toString(16);
    return hash;
	}

}

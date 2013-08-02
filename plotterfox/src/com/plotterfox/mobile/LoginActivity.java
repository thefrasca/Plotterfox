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
import android.view.View;
import android.widget.EditText;

public class LoginActivity extends Activity {
	
	 EditText passText  ;
	 EditText userText;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		//A user never starts here.  They are redirected from main activity only if failing validation checks.
		super.onCreate(savedInstanceState);
		setContentView(R.layout.loginlayout);

		 userText = new EditText(this);
		 userText = (EditText) findViewById(R.id.user);
		 passText = new EditText(this);
		 passText = (EditText) findViewById(R.id.password);
	}

	//This is run when a user clicks the onclick button.  It does not actualy validate a user, but sets the preferences and then redirects to
	//MainActivity.  It is on MainActivity that validation is performed.  If validation check fails, they are redirected back to login.	
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
	//This method hashes the password via MD5. 
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

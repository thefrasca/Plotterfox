package com.plotterfox.mobile;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;


public class PostActivity extends Activity{
	String username = null;
	String postBody = null;
	String cTopic = null;
	int targetIndex;
	EditText postText;
	protected void onCreate(Bundle savedInstanceState) {
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
		    username = extras.getString("username");
		    cTopic = extras.getString("cTopic");
		    targetIndex = extras.getInt("targetIndex");
		}
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.post_layout);
        ActionBar actionBar = getActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		postText = new EditText(this);
		postText = (EditText) findViewById(R.id.postText);
        
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
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		ActionBar actionBar = getActionBar();
		actionBar.setTitle("Make a post");
		return true;
	}
	public void post(View v)
	{
		    postBody = postText.getText().toString();

			postBody = postBody.trim();
			if (!postBody.isEmpty())
			{
				SetPost setPost = new SetPost();
				setPost.execute();
			}
			else
			{
			    Toast.makeText(this, "Post contains no data.", Toast.LENGTH_SHORT)
		          .show();
			}			     
	}
	private class SetPost extends AsyncTask<Void, Void, Boolean> {

	      @Override
	      protected Boolean doInBackground(Void... params) {
	    	  	boolean result = false;
	    	  	HttpClient httpclient = new DefaultHttpClient();
	    	    HttpPost httppost = new HttpPost(getBaseContext().getString(R.string.set_post_url));
	    	    
	    	    InputStream is = null; 	        
	    	    String postResponse = "0";
	    	    // Add your data
  	        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
  	        nameValuePairs.add(new BasicNameValuePair("user", username));  
  	        nameValuePairs.add(new BasicNameValuePair("cTopic", cTopic));  
  	        nameValuePairs.add(new BasicNameValuePair("postbody", postBody));   
  	        
  	       try {
					httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
					HttpResponse response = httpclient.execute(httppost);
					HttpEntity entity = response.getEntity();
		    		is = entity.getContent();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    		//convert response to string
	    		try{
	    			BufferedReader reader = new BufferedReader(new InputStreamReader(is,"iso-8859-1"),8);
	    			StringBuilder sb = new StringBuilder();
	    			String line = null;
	    			while ((line = reader.readLine()) != null) {
	    				sb.append(line + "\n");
	    			}
	    			is.close();
	    			postResponse = sb.toString();
	    			
	
	    		}catch(Exception e){
	    			Log.e("log", "Error converting result "+e.toString());
	    		}  		
	    		postResponse = postResponse.trim();


	    		if (postResponse.equals("1"))
	    			{
	    				result = true;
	    				Intent goToNextActivity = new Intent(getBaseContext(), MainActivity.class);
	    				startActivity(goToNextActivity);
	    			}
	    				
				return result; 
	      }      

	      @Override
	      protected void onPostExecute(Boolean result) {       
	
	      } 

	      @Override
	      protected void onPreExecute() {
	      }

	      @Override
	      protected void onProgressUpdate(Void... values) {
	      }
	}
}

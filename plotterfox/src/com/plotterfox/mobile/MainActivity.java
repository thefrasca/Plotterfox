package com.plotterfox.mobile;

import java.io.BufferedReader;

import java.io.InputStream;
import java.io.InputStreamReader;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends FragmentActivity implements
		ActionBar.OnNavigationListener {

	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * current dropdown position.
	 */
	private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
	private ArrayList<Plots> plotList = new ArrayList<Plots>();
	private ArrayList<Posts> postList = new ArrayList<Posts>();
	private ArrayList<String> plotNames = new ArrayList<String>();
	String username = null;
	String password = null;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		boolean validated = false;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		
		if(getPreferences())		//Attempts to load preferences
		{
			ValidateTask validate = new ValidateTask();
			try {

				Boolean validateResult = validate.execute().get();
				if (validateResult == true)
				{
					validated = true;
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
		if (validated != true)
		{
			Intent goToNextActivity = new Intent(this, LoginActivity.class);
			startActivity(goToNextActivity);
			
		}	
		else
		{

		GetPlots getPlots = new GetPlots();
		GetPosts getPosts = new GetPosts();
		try {
			plotList = getPlots.execute().get();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(!plotList.isEmpty())
		{
		
			getPosts.execute(plotList.get(0).getId()); // Makes sure to load posts for the selected plot first
		}
		setSpinnerAdapter(plotList);
		}

	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		// Restore the previously serialized current dropdown position.
		if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
			getActionBar().setSelectedNavigationItem(
					savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// Serialize the current dropdown position.
		outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getActionBar()
				.getSelectedNavigationIndex());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onNavigationItemSelected(int position, long id) {
		if (!plotList.isEmpty()){
			GetPosts getPosts = new GetPosts();
			getPosts.execute(plotList.get(position).getId());
		}
		return true;
	}
	public void displayPosts(ArrayList<Posts> postList)
	{

		float wPixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200, getResources().getDisplayMetrics());
		setContentView(R.layout.activity_main);
		NowLayout nowLayout = (NowLayout) findViewById(R.id.nowlayout);
		
		for(int i=0;i < postList.size();i++){	
			LinearLayout linLayout = new LinearLayout(this);
			linLayout.setMinimumWidth((int) wPixels);
			linLayout.setBackgroundResource(R.drawable.search_bg_shadow);
			linLayout.setOrientation(LinearLayout.VERTICAL);
			
			TextView textAuthor = new TextView(this);
			textAuthor.setTextSize(15);
			textAuthor.setTextColor(Color.parseColor("#FF0000"));
			textAuthor.setTypeface(null, Typeface.BOLD);
			textAuthor.setText(Html.fromHtml(postList.get(i).getPostAuthor()));
			
			TextView textDate = new TextView(this);
			textDate.setTextSize(8);
			textDate.setPadding(0, -5, 0, 0);
			textDate.setText(Html.fromHtml(postList.get(i).getPostDate()));
			
			TextView textBody = new TextView(this);
			textBody.setPadding(0, 5, 0, 0);
			textBody.setText(Html.fromHtml(postList.get(i).getPostBody()));
			textBody.setMovementMethod(LinkMovementMethod.getInstance());
					
			nowLayout.addView(linLayout);
			
			linLayout.addView(textAuthor);
			linLayout.addView(textDate);
			linLayout.addView(textBody);
			
		}
	}
	public void setSpinnerAdapter(ArrayList<Plots> plotList)
	{
		for(int i=0;i < plotList.size();i++){			
			plotNames.add(plotList.get(i).getName());
		}
		final ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

		// Set up the dropdown list navigation in the action bar.
		ArrayAdapter<String> spinnerMenu = new ArrayAdapter<String>(
	            actionBar.getThemedContext(),
	            android.R.layout.simple_list_item_1, plotNames);
	
		actionBar.setListNavigationCallbacks(spinnerMenu,this);
		
	}
	private boolean getPreferences()
	{
		boolean result = false;
		SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(this);
		username = (shared.getString("username", null));
		password = (shared.getString("password", null));

		if(username != null && password != null)
		{
			result = true;
		}
		return result;
	}
	
	/**
	 * A dummy fragment representing a section of the app, but that simply
	 * displays dummy text.
	 */
	public static class DummySectionFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		public static final String ARG_SECTION_NUMBER = "section_number";

		public DummySectionFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main_dummy,
					container, false);
			TextView dummyTextView = (TextView) rootView
					.findViewById(R.id.section_label);
			dummyTextView.setText(Integer.toString(getArguments().getInt(
					ARG_SECTION_NUMBER)));
			return rootView;
		}
	}
	private class ValidateTask extends AsyncTask<Void, Void, Boolean> {

	      @Override
	      protected Boolean doInBackground(Void... params) {
	    	  	boolean result = false;
	    	  	HttpClient httpclient = new DefaultHttpClient();
	    	    HttpPost httppost = new HttpPost("http://dev.ninespace.net/working/validate.php");
	    	    
	    	    InputStream is = null; 	        
	    	    String validateResponse = "0";
	    	    // Add your data
    	        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
    	        nameValuePairs.add(new BasicNameValuePair("user", username));  //insert user name here
    	        nameValuePairs.add(new BasicNameValuePair("pass", password));  //insert password here
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
	    			validateResponse = sb.toString();
	    			
	
	    		}catch(Exception e){
	    			Log.e("log", "Error converting result "+e.toString());
	    		}  		
	    		validateResponse = validateResponse.trim();
	    		if (validateResponse.equals("1"))
	    			{
	    				result = true;
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
	private class GetPosts extends AsyncTask<String, Void, ArrayList<Posts>> {

	      @Override
	      protected ArrayList<Posts> doInBackground(String... params) {
	    	  	String cTopic = params[0];
	    	  	HttpClient httpclient = new DefaultHttpClient();
	    	    HttpPost httppost = new HttpPost("http://dev.ninespace.net/working/getPosts.php");
 	    	    postList = new ArrayList<Posts>();
	    	    
  	        InputStream is = null; 	        
  	        JSONObject json = new JSONObject();
  	        JSONArray postInfo = new JSONArray();
  	        
	    	    // Add your data
	    	        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
	    	        nameValuePairs.add(new BasicNameValuePair("user", username));  //insert user name here
	    	        nameValuePairs.add(new BasicNameValuePair("pass", password));  //insert password here
	    	        nameValuePairs.add(new BasicNameValuePair("cTopic", cTopic));
	    	        nameValuePairs.add(new BasicNameValuePair("pLimit", "5")); // post limit
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
    	
	    			json = new JSONObject(sb.toString());
	    		}catch(Exception e){
	    			Log.e("log", "Error converting result "+e.toString());
	    		}
	    		try {
					postInfo = json.getJSONArray("postInfo");
			
		    		for(int i=0;i < postInfo.length();i++){			
		    			JSONObject e = postInfo.getJSONObject(i);
		         		Posts postHolder = new Posts();
		         		
		         	//	Date date = new Date(location.getTime());
		         	//	DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getApplicationContext());
		         	//	mTimeText.setText("Time: " + dateFormat.format(date));
		         		
		         		String mytime = e.getString("postDate");
		         	    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		         	    java.util.Date myDate = null;
		         	    myDate = dateFormat.parse(mytime);


		         	    SimpleDateFormat timeFormat = new SimpleDateFormat("MM.dd.yyyy HH:mm");
		         	    String finalDate = timeFormat.format(myDate);
		         	    
		         		postHolder.setPostID(e.getString("postID"));
		         		postHolder.setPostAuthor(e.getString("postAuthor"));
		         		postHolder.setPostBody(e.getString("postBody"));
		         		postHolder.setPostDate(finalDate);
		            	postList.add(postHolder);
		    		} 
	    		}catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    		
				return postList; 
	      }      

	      @Override
	      protected void onPostExecute(ArrayList<Posts> result) {       
		  		displayPosts(result);
	      } 

	      @Override
	      protected void onPreExecute() {
	      }

	      @Override
	      protected void onProgressUpdate(Void... values) {
	      }
	}
	private class GetPlots extends AsyncTask<Void, Void, ArrayList<Plots>> {

		     @Override
	      protected ArrayList<Plots> doInBackground(Void... params) {
		    	InputStream is = null;
    	        JSONObject json = new JSONObject();
    	        JSONArray plotInfo = new JSONArray();
    	        
	    	    // Add your data
	
	    	        try {
	    	    	  	HttpClient httpclient = new DefaultHttpClient();
			    	    HttpPost httppost = new HttpPost("http://dev.ninespace.net/working/getPlotsAndroid.php");
		    	        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		    	        nameValuePairs.add(new BasicNameValuePair("user", username)); //username
		    	        nameValuePairs.add(new BasicNameValuePair("pass", password)); //password
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
	    	    		json = new JSONObject(sb.toString());
	    	    	}catch(Exception e){
	    	    		Log.e("log_tag", "Error converting result "+e.toString());
	    	    	}
	    	       
	    		try {
					plotInfo = json.getJSONArray("plotInfo");
			
		    		for(int i=0;i < plotInfo.length();i++){						
		    			JSONObject e = plotInfo.getJSONObject(i);
		         		Plots plotHolder = new Plots();
		         		plotHolder.setId(e.getString("plotID"));
		         		plotHolder.setName(e.getString("plotName"));
		            	plotList.add(plotHolder);
		    		} 
	    		}catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    		
				return plotList; 
	      }      

	      @Override
	      protected void onPostExecute(ArrayList<Plots> result) {

	      }

	      @Override
	      protected void onPreExecute() {
	      }

	      @Override
	      protected void onProgressUpdate(Void... values) {
	      }
	}
}

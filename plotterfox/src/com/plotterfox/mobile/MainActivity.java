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
import org.json.JSONArray;
import org.json.JSONObject;
import android.app.ActionBar;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

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
		float hPixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics());
		setContentView(R.layout.activity_main);
		NowLayout nowLayout = (NowLayout) findViewById(R.id.nowlayout);
		
		for(int i=0;i < postList.size();i++){	

			TextView text = new TextView(this);
			text.setMinHeight((int)hPixels);
			text.setMinWidth((int) wPixels);
			text.setBackgroundResource(R.drawable.search_bg_shadow);
			text.setText(Html.fromHtml(postList.get(i).getPostAuthor() + "<BR>"
					+ postList.get(i).getPostDate() + "<BR>"
					+ postList.get(i).getPostBody() + "<BR>"));
			nowLayout.addView(text);
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
	    	        nameValuePairs.add(new BasicNameValuePair("user", ""));  //insert user name here
	    	        nameValuePairs.add(new BasicNameValuePair("pass", ""));  //insert password here
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
		         		postHolder.setPostID(e.getString("postID"));
		         		postHolder.setPostAuthor(e.getString("postAuthor"));
		         		postHolder.setPostDate(e.getString("postDate"));
		         		postHolder.setPostBody(e.getString("postBody"));
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
		    	        nameValuePairs.add(new BasicNameValuePair("user", "")); //username
		    	        nameValuePairs.add(new BasicNameValuePair("pass", "")); //password
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

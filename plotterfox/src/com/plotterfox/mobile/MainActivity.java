package com.plotterfox.mobile;

import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;

import java.security.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;



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

import com.google.android.gcm.GCMRegistrar;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.ActionBar;
import android.app.ProgressDialog;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.graphics.Typeface;
import android.provider.Settings;
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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends FragmentActivity implements
	ActionBar.OnNavigationListener {
    
	//Activity level variables for use in managing plots and authentication credentials.  Username and password are only read once.
	private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
	private ArrayList<Plots> plotList = new ArrayList<Plots>();
	private ArrayList<Posts> postList = new ArrayList<Posts>();
	private ArrayList<String> plotNames = new ArrayList<String>();
	String username = null;
	String password = null;
	String postLimit = null;
	String cTopic = "0";
	int targetIndex = 0;
	String androidID = null;
	ProgressDialog progressDialog;
	public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final String PROPERTY_ON_SERVER_EXPIRATION_TIME =
            "onServerExpirationTimeMs";
    /**
     * Default lifespan (7 days) of a reservation until it is considered expired.
     */
    public static final long REGISTRATION_EXPIRY_TIME_MS = 1000 * 3600 * 24 * 7;

    /**
     * Substitute you own sender ID here.
     */
    String SENDER_ID = "590906407653";

    /**
     * Tag used on log messages.
     */
    static final String TAG = "PFOX";

    GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    SharedPreferences prefs;
    Context context;
	boolean validated = false;

    String regid;
    public void onNewIntent(Intent intent){
    	int navItem = -1;
    	int iterator = 0;
		Bundle extras = intent.getExtras();
		if(extras !=null) {
		    String plotID = extras.getString("plotID");
		    	while (navItem == -1)
		    	{
		    		if (plotID.equals(plotList.get(iterator).getId()))
		    		{
		    			navItem = iterator;
		   		     SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
				     Editor ed = prefs.edit();
					 postLimit = (prefs.getString("postLimit", "10"));
						
				     ed.putInt("targetIndex", navItem );
				     ed.commit();
				     getActionBar().setSelectedNavigationItem(navItem);
		    		}
		    		iterator++;
		    	}
		}
    }
    
    @Override
    public void onResume(){
    	super.onResume();

	}

    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
  
		//On create of the main activity.  All users start here unvalidated!
	
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	
		if(getPreferences())		//Attempts to load preferences
		{
	    
			//This validates the user by using stored credentials.
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
		//User is validated and can proceed to see plots.
		else
		{
	    	
			//Registers the device for notifications.

			//Sets permanent menu key on the action bar.  This is disabled normally for people who have a menu button.  Pressing physical
			//menu button does the same as pressing the options indicator in the upper right.
			try {
		        ViewConfiguration config = ViewConfiguration.get(this);
		        Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
		        if(menuKeyField != null) {
		            menuKeyField.setAccessible(true);
		            menuKeyField.setBoolean(config, false);
		        }
		    } catch (Exception ex) {
		        // Ignore
		    }
			
			//Begin to populate plots drop down and get posts.
			GetPlots getPlots = new GetPlots();
			GetPosts getPosts = new GetPosts();
			
			try {
				//Gets all active plots for the user.
				plotList = getPlots.execute().get();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			setSpinnerAdapter(plotList);

			context = getApplicationContext();
	        regid = getRegistrationId(context);
			 androidID = Settings.Secure.getString(getContentResolver(),  
	                Settings.Secure.ANDROID_ID);	
			
	        if (regid.length() == 0) {
				
	            registerBackground();
	        }
	        gcm = GoogleCloudMessaging.getInstance(this);

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

	//Refresh posts if plot select changes via the drop down.
	@Override
	public boolean onNavigationItemSelected(int position, long id) {
		if (!plotList.isEmpty()){
		     SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
				postLimit = (prefs.getString("postLimit", "10"));
	GetPosts getPosts = new GetPosts();
			getPosts.execute(plotList.get(position).getId());
	     Editor ed = prefs.edit();
		     ed.putInt("targetIndex", position );
		     ed.commit();
		
			     
		}
		return true;
	}
	
	//Non drop down action bar item actions here
	@Override
	  public boolean onOptionsItemSelected(MenuItem item) {
	    if (item.getItemId() == R.id.action_clearPref) {
	    
	    //If selecting clear login, this resets the login information stored locally on the device.  This will force a login when app
	    //is restarted.  User can continue to use app normally until the app is closed and reopened. 
	        final SharedPreferences prefsGCM = getGCMPreferences(context);
	        SharedPreferences.Editor editor = prefsGCM.edit();
	        editor.putString(PROPERTY_REG_ID, "");
	        editor.commit();
	    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		     Editor ed = prefs.edit();
		     ed.putString("username", null );
		     ed.putString("password", null );
		     ed.commit();
		   String registrationId = prefsGCM.getString(PROPERTY_REG_ID, "");
	      Toast.makeText(this, "Login Information has been cleared.", Toast.LENGTH_SHORT)
	          .show();
	    }
	    
	    else if ( item.getItemId() == R.id.action_settings){

	    	Intent settingsActivity = new Intent(this, SettingsActivity.class);
	            startActivity(settingsActivity);
	    }
	    else if (item.getItemId() == R.id.action_post){
	    	 Intent postActivity = new Intent(this, PostActivity.class);
	    	 postActivity.putExtra("username",username);
	    	 postActivity.putExtra("cTopic",cTopic);
	    	 startActivity(postActivity);
	    }
	    else
	    {}
	  
	    return true;
	  }

    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.length() == 0) {
            Log.v(TAG, "Registration not found.");
            return "";
        }
        // check if app was updated; if so, it must clear registration id to
        // avoid a race condition if GCM sends a message
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion || isRegistrationExpired()) {
            Log.v(TAG, "App version changed or registration expired.");
            return "";
        }
        return registrationId;
    }
    private SharedPreferences getGCMPreferences(Context context) {
        return getSharedPreferences(MainActivity.class.getSimpleName(), 
                Context.MODE_PRIVATE);
    }
    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * Checks if the registration has expired.
     *
     * <p>To avoid the scenario where the device sends the registration to the
     * server but the server loses it, the app developer may choose to re-register
     * after REGISTRATION_EXPIRY_TIME_MS.
     *
     * @return true if the registration has expired.
     */
    private boolean isRegistrationExpired() {
        final SharedPreferences prefs = getGCMPreferences(context);
        // checks if the information is not stale
        long expirationTime =
                prefs.getLong(PROPERTY_ON_SERVER_EXPIRATION_TIME, -1);
        return System.currentTimeMillis() > expirationTime;
    }
    
    /**
     * Stores the registration id, app versionCode, and expiration time in the
     * application's {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId registration id
     */
    private void setRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.v(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        long expirationTime = System.currentTimeMillis() + REGISTRATION_EXPIRY_TIME_MS;

        editor.putLong(PROPERTY_ON_SERVER_EXPIRATION_TIME, expirationTime);
        editor.commit();
    }
    
	//This method physically displays the posts once they are retrieved.  The input arraylist contains all the posts and post data.
	public void displayPosts(ArrayList<Posts> postList)
	{
		float wPixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200, getResources().getDisplayMetrics());
		setContentView(R.layout.activity_main);
		LinearLayout lLayout = (LinearLayout) findViewById(R.id.nowlayout);

		//NowLayout nowLayout = (NowLayout) findViewById(R.id.nowlayout);
		//NowLayout nowLayout = new NowLayout(this);
		
		for(int i=0;i < postList.size();i++){	
			LinearLayout linLayout = new LinearLayout(this);
			linLayout.setMinimumWidth((int) wPixels);
			//linLayout.setBackgroundResource(R.drawable.search_bg_shadow);
			linLayout.setBackgroundResource(R.drawable.customshape);
			linLayout.setOrientation(LinearLayout.VERTICAL);
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
				     LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			layoutParams.setMargins(20, 20, 20, 0);
			linLayout.setPadding(5, 5, 5, 5);
			
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
					
			lLayout.addView(linLayout,layoutParams);
			
			linLayout.addView(textAuthor);
			linLayout.addView(textDate);
			linLayout.addView(textBody);	
		}
	}
	
	//Sets the plots in the drop down.
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
		int selectedIndex = actionBar.getSelectedNavigationIndex();
		if (selectedIndex != targetIndex) {
		    actionBar.setSelectedNavigationItem(targetIndex);
		}
	}
	
	//Gets existing login preferences.  If it cannot find them, it returns null in both username and password variables.  
	private boolean getPreferences()
	{
    
		boolean result = false;
		SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(this);
		username = (shared.getString("username", null));
		password = (shared.getString("password", null));
		postLimit = (shared.getString("postLimit", "10"));
		targetIndex = (shared.getInt("targetIndex",0));

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
	
	//This asynctask validates users against the PHP service.  A '1' means you are accepted.  A '0' means rejected.  
	private class ValidateTask extends AsyncTask<Void, Void, Boolean> {

	      @Override
	      protected Boolean doInBackground(Void... params) {
	    	  	boolean result = false;
	    	  	HttpClient httpclient = new DefaultHttpClient();
	    	    HttpPost httppost = new HttpPost(getBaseContext().getString(R.string.validate_url));
	    	    
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
	
	private void registerBackground() {
	    new AsyncTask<Object, Object, Object>() {
	  		@Override
			protected Object doInBackground(Object... params) {
				 String msg = "";
		        
		            try {
		                if (gcm == null) {
		                    gcm = GoogleCloudMessaging.getInstance(context);
		    		        
		                }
		                regid = gcm.register(SENDER_ID);
		                // You should send the registration ID to your server over HTTP,
		                // so it can use GCM/HTTP or CCS to send messages to your app.

		                // For this demo: we don't need to send it because the device
		                // will send upstream messages to a server that echo back the message
		                // using the 'from' address in the message.

		                // Save the regid - no need to register again.
		                setRegistrationId(context, regid);
		                
			    	  	HttpClient httpclient = new DefaultHttpClient();
			    	    HttpPost httppost = new HttpPost(getBaseContext().getString(R.string.set_register_url));
			    	    
			    	    InputStream is = null; 	        
			   	    // Add your data

		  	        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		  	        SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(context);
		  	        username = (shared.getString("username", null));
		  	        password = (shared.getString("password", null));
		  	        nameValuePairs.add(new BasicNameValuePair("user", username));  
		  	        nameValuePairs.add(new BasicNameValuePair("pass", password));  		  	        
		  	        nameValuePairs.add(new BasicNameValuePair("regID", regid));  
		  	        nameValuePairs.add(new BasicNameValuePair("androidID", androidID));   
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
			
			    		}catch(Exception e){
			    			Log.e("log", "Error converting result "+e.toString());
			    		}  		
	                
		            } catch (IOException ex) {
		                msg = "Error :" + ex.getMessage();
		            }
		            return msg;
			}
	    }.execute(null, null, null);
	}
	
	//This asynctask gets all posts for a given plot.  Post limit is hard coded to 5 currently.  This needs to be changed and allow a user
	//to input mobile only preferences for display.
	private class GetPosts extends AsyncTask<String, Void, ArrayList<Posts>> {

	      @Override
	      protected ArrayList<Posts> doInBackground(String... params) {
	    	  	cTopic = params[0];
	    	  	HttpClient httpclient = new DefaultHttpClient();
	    	    HttpPost httppost = new HttpPost(getBaseContext().getString(R.string.get_posts_url));
 	    	    postList = new ArrayList<Posts>();

  	        InputStream is = null; 	        
  	        JSONObject json = new JSONObject();
  	        JSONArray postInfo = new JSONArray();
	    	    // Add your data
	    	        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
	    	        nameValuePairs.add(new BasicNameValuePair("user", username));  //insert user name here
	    	        nameValuePairs.add(new BasicNameValuePair("pass", password));  //insert password here
	    	        nameValuePairs.add(new BasicNameValuePair("cTopic", cTopic));
	    	        nameValuePairs.add(new BasicNameValuePair("pLimit", postLimit)); // post limit
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
	  		progressDialog.dismiss();
		      
	    	  displayPosts(result);
		  } 

	      @Override
	      protected void onPreExecute() {
	          progressDialog  = new ProgressDialog (MainActivity.this);
	          progressDialog.setIndeterminate(true);
	      	progressDialog.setTitle("Loading Plot...");
	      	progressDialog.setMessage("Please wait.");
	          
	          progressDialog.show();
	      }

	      @Override
	      protected void onProgressUpdate(Void... values) {
	      }
	}
	
	//This asynctask gets all existing plots that a given user belongs to.  
	private class GetPlots extends AsyncTask<Void, Void, ArrayList<Plots>> {

		     @Override
	      protected ArrayList<Plots> doInBackground(Void... params) {
		    	InputStream is = null;
    	        JSONObject json = new JSONObject();
    	        JSONArray plotInfo = new JSONArray();
    	        
	    	    // Add your data
	
	    	        try {
	    	    	  	HttpClient httpclient = new DefaultHttpClient();
			    	    HttpPost httppost = new HttpPost(getBaseContext().getString(R.string.get_plots_url));
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

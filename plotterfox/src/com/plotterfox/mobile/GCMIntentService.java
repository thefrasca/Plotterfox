package com.plotterfox.mobile;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.google.android.gcm.GCMBaseIntentService;

public class GCMIntentService extends GCMBaseIntentService {

	@Override
	protected void onError(Context arg0, String arg1) {
		Log.e("Registration", "Got an error!");
		Log.e("Registration", arg0.toString() + arg1.toString());
	}

	@Override
	protected void onMessage(Context context, Intent intent) {
		Log.i("Registration", "Got a message!");
	       String message = intent.getExtras().getString("message");
	         
	//        displayMessage(context, message);
	        // notifies user
	        generateNotification(context, message);}

	@Override
	protected void onRegistered(Context arg0, String arg1) {
		Log.i("Registration", "Just registered!");
		Log.i("Registration", arg0.toString() + arg1.toString());	
		// This is where you need to call your server to record the device toekn and registration id.
	}

	@Override
	protected void onUnregistered(Context arg0, String arg1) {
	}
	   private static void generateNotification(Context context, String message) {
	        int icon = R.drawable.ic_launcher_pfox;
	        long when = System.currentTimeMillis();
	        NotificationManager notificationManager = (NotificationManager)
	                context.getSystemService(Context.NOTIFICATION_SERVICE);
	        Notification notification = new Notification(icon, message, when);
	         
	        String title = context.getString(R.string.app_name);
	         
	        Intent notificationIntent = new Intent(context, MainActivity.class);
	        // set intent so it does not start a new activity
	        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
	                Intent.FLAG_ACTIVITY_SINGLE_TOP);
	        PendingIntent intent =
	                PendingIntent.getActivity(context, 0, notificationIntent, 0);
	        notification.setLatestEventInfo(context, title, message, intent);
	        notification.flags |= Notification.FLAG_AUTO_CANCEL;
	         
	        // Play default notification sound
	        notification.defaults |= Notification.DEFAULT_SOUND;
	         
	        // Vibrate if vibrate is enabled
	        notification.defaults |= Notification.DEFAULT_VIBRATE;
	        notificationManager.notify(0, notification);      
	 
	    }
}


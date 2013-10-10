package com.plotterfox.mobile;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.google.android.gcm.GCMBaseIntentService;

public class GCMIntentService extends GCMBaseIntentService {
	static int numMessages =0;
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
	generateNotification(context, message);
	}

	@Override
	protected void onRegistered(Context arg0, String arg1) {
		Log.i("Registration", "Just registered!");
		Log.i("Registration", arg0.toString() + arg1.toString());	
		// This is where you need to call your server to record the device token and registration id.
	}

	@Override
	protected void onUnregistered(Context arg0, String arg1) {
	}
	   
	private static void generateNotification(Context context, String message) {
	
		numMessages++;
		NotificationCompat.Builder mBuilder =
		        new NotificationCompat.Builder(context)
		        .setSmallIcon(R.drawable.ic_launcher_pfox)
		        .setContentTitle("Plotterfox")
		        .setContentText("New Post!")
		        .setSmallIcon(R.drawable.ic_launcher_pfox)
		        .setNumber(numMessages);
			
		// Creates an explicit intent for an Activity in your app
		Intent resultIntent = new Intent(context, MainActivity.class);
		resultIntent.putExtra("plotID", message);
		// The stack builder object will contain an artificial back stack for the
		// started Activity.
		// This ensures that navigating backward from the Activity leads out of
		// your application to the Home screen.
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
		// Adds the back stack for the Intent (but not the Intent itself)
		stackBuilder.addParentStack(MainActivity.class);
		// Adds the Intent that starts the Activity to the top of the stack

		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent =
		        stackBuilder.getPendingIntent(
		            0,
		            PendingIntent.FLAG_UPDATE_CURRENT
		        );
		mBuilder.setContentIntent(resultPendingIntent);
		mBuilder.setAutoCancel(true);
		NotificationManager mNotificationManager =
		    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	
		Notification noti = mBuilder.build();

	    // Play default notification sound
		noti.defaults |= Notification.DEFAULT_SOUND;
	         
	    // Vibrate if vibrate is enabled
	    noti.defaults |= Notification.DEFAULT_VIBRATE;

	    // Because the ID remains unchanged, the existing notification is
	    // updated.
	    mNotificationManager.notify(1,noti);
	 }
}


package tracker.service;

import tracker.springversion1.Common;
import tracker.springversion1.R;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;


public class ReminderService extends IntentService {
	public int mId = Common.ReminderNotificationId;
    public ReminderService() {
       super("ReminderService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
       // Do the task here

    	NotificationCompat.Builder mBuilder =
    	        new NotificationCompat.Builder(this)
    	        .setSmallIcon(R.drawable.ic_launcher)
    	        .setContentTitle("Mobile study")
    	        .setContentText("Reminder: You now can continue the experiment.");

    	Intent resultIntent = new Intent(this, tracker.UI.activity.MainActivity.class);
    	
//    	TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
//    	stackBuilder.addParentStack(MainActivity.class);
//    	stackBuilder.addNextIntent(resultIntent);
//    	PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
    	
    	
    	
    	PendingIntent resultPendingIntent = PendingIntent.getActivity(
    			this, 0, resultIntent, PendingIntent.FLAG_CANCEL_CURRENT);
    	mBuilder.setContentIntent(resultPendingIntent);
    	NotificationManager mNotificationManager =
    		    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    		// mId allows you to update the notification later on.
    		mNotificationManager.notify(mId, mBuilder.getNotification());
    }
}

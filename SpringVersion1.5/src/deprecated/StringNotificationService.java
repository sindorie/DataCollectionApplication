package deprecated;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Log;


public class StringNotificationService extends IntentService{
	public final static String DATAKEY = "key";
	private NotificationManager mNotificationManager;
	private int notifyID = 1;
	
	private String textContent = "no content";
	
	public StringNotificationService() {
		super("StringNotificationService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.v("mark","StringNotificationService onHandleIntent");
		Bundle bundle = intent.getExtras();
		
		if(bundle!=null){
			if(bundle.containsKey(DATAKEY)){
				textContent = bundle.getString(DATAKEY);
			}
		}
		showNotification();
	}
	
	private void showNotification(){
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		
		Builder mNotifyBuilder = new NotificationCompat.Builder(this)
	    .setContentTitle("New Message")
	    .setContentText(textContent)
	    .setSmallIcon(tracker.springversion1.R.drawable.ic_launcher);

		mNotificationManager.notify(
	            notifyID,
	            mNotifyBuilder.getNotification());

	}
}

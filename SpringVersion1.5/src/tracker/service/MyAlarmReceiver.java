package tracker.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class MyAlarmReceiver extends BroadcastReceiver {

	public static final int REQUEST_CODE = 12345;
	public static final String ACTION = "com.codepath.example.servicesdemo.alarm";
	public static final String SERVICE_TAG = "tag";
	public static final String REMINDER = "reminder";
	public static final String UPLOADER = "uploader";
	
	// Triggered by the Alarm periodically (starts the service to run task)
	@Override
	public void onReceive(Context context, Intent intent) {
		String msg = intent.getStringExtra(SERVICE_TAG);
		Log.v("mark","MyAlarmReceiver has the msg:"+msg);
		if(msg!=null){
			if(msg.equals(REMINDER)){
				Intent i = new Intent(context, tracker.service.ReminderService.class);
				context.startService(i);
			}else if(msg.equals(UPLOADER)){
				// a static flag should be set when all task is finished
				Intent i = new Intent(context, tracker.service.FileUploader.class);
				context.startService(i);
			}
		}
	}
}
package tracker.service;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;
import java.util.Stack;
import tracker.springversion1.Common;
import tracker.springversion1.MobileStudyApplication;
import tracker.springversion1.R;
import tracker.utility.SSLCommunication;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class FileUploader extends IntentService {
	// assume the service will not be killed by the user
	public final static String MULTIFILE = "mkey";
	private final static String FILENAME = "FileRecord";
	
	public FileUploader() {
		super("SupportingService");
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void onCreate() {
		super.onCreate();
	}
	
	@Override
	public int onStartCommand (Intent intent, int flags, int startId){
		return super.onStartCommand(intent, flags, startId);
	}
	@Override
	protected void onHandleIntent(Intent intent) {
		Log.v("mark","FileUploader onHandleIntent");
		//open file which should store the filename to upload
		Queue<String> filequeue = new LinkedList<String>();
		try {
			FileInputStream fis = this.openFileInput(FILENAME);
			Scanner sc = new Scanner(fis);
			while(sc.hasNext()){
				String line = sc.nextLine();
				String first = line.split(" ")[0].trim();
				filequeue.add(first);
			}
			sc.close();fis.close();
		} catch (FileNotFoundException e1) {} catch (IOException e) {}
		
		String[] fileNames = intent.getStringArrayExtra(MULTIFILE);
		if(fileNames != null){
			for(String name : fileNames){
				if (name != null && !filequeue.contains(name)) { filequeue.add(name); }
			}
		}
		
		// check Internet availability
		if (!isNetworkConnected()) { 
			saveFile(filequeue);
			return; 	
		}
		// for each filename in the queue
		String workingOn = null;
		SSLCommunication ssl = new SSLCommunication(this);
		while (!filequeue.isEmpty()) {
			workingOn = filequeue.peek();
			Log.v("mark","SupportingService onHandleIntent:"+workingOn);
			try {
				final File myFile = new File(this.getFilesDir(), workingOn);
				ssl.sendFile(myFile);
			} catch (Exception e) {
				Log.v("mark","onHandleIntent:"+e.getClass()+" "+e.getMessage()); break;
			} catch (Error err) {
				Log.v("mark","onHandleIntent:"+err.getClass()+" "+err.getMessage()); break; }
			//successful
			Log.v("mark","successful to sending file");
			workingOn = filequeue.poll();
		}
		ssl.shutdown();
		
		if(checkAllFinished(filequeue)){
			Log.v("mark","FileUploader all finished");
			//set flag	
//			MobileStudyApplication.alldatauploaded = true;
			
			((MobileStudyApplication)this.getApplication()).setUserInfomraiton(null, null, null, null, null, 1);
			
			
			//Notify the user 
	    	NotificationCompat.Builder mBuilder =
	    	        new NotificationCompat.Builder(this)
	    	        .setSmallIcon(R.drawable.ic_launcher)
	    	        .setContentTitle("Mobile study")
	    	        .setContentText("All data log has been uploaded. You can now delete the app");

	    	Intent resultIntent = new Intent(this, tracker.UI.activity.PaymentActivity.class);
	    	
//	    	TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
//	    	stackBuilder.addParentStack(MainActivity.class);
//	    	stackBuilder.addNextIntent(resultIntent);
//	    	PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
	    	
	    	PendingIntent resultPendingIntent = PendingIntent.getActivity(
	    			this, 0, resultIntent, PendingIntent.FLAG_CANCEL_CURRENT);
	    	mBuilder.setContentIntent(resultPendingIntent);
	    	NotificationManager mNotificationManager =
	    		    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
	    		// mId allows you to update the notification later on.
	    	mNotificationManager.notify(Common.AllFinishedNoticationId, mBuilder.getNotification());
			
	    		
	    	//cancel clarm
    		Intent canIntent = new Intent(getApplicationContext(), MyAlarmReceiver.class);
    		final PendingIntent pIntent = PendingIntent.getBroadcast(this, MyAlarmReceiver.REQUEST_CODE, canIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    		AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
    		alarm.cancel(pIntent);
    		
		}else{
			saveFile(filequeue);
		}
	}
	
	private boolean checkAllFinished(Queue<String>  filequeue){
		if(((MobileStudyApplication)this.getApplication()).getUserInfo().allTaskFinished>=1){
			return filequeue.isEmpty();
		}
		return false;
	}
	
	private void saveFile(Queue<String>  filequeue){
		try {
			FileOutputStream fos = this.openFileOutput(FILENAME, Context.MODE_PRIVATE);
			if(filequeue.isEmpty()){
				
			}else{
				for(String record:filequeue){
					fos.write((record+"\n").getBytes() );
				}
			}
			fos.close();
		} catch (FileNotFoundException e) { } catch (IOException e) { }
	}

	private boolean isNetworkConnected() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		if (ni == null) { return false;
		} else return true;
	}
}

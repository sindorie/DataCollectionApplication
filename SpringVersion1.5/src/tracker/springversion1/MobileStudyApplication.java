package tracker.springversion1;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner; 
import java.util.Set;
import tracker.UI.activity.MainActivity;
import tracker.UI.activity.FrameActivity;
import tracker.UI.components.MyArrayListAdapter;
import tracker.service.FileUploader;
import tracker.springversion1.TaskManager.Instruction;
import tracker.utility.DBOpenerHelper;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

/**
 * This class intends to declare some variables which are used across
 * Activities.
 * 
 * @author Sindorie
 * 
 */
public class MobileStudyApplication extends Application implements ProjectInterface.ApplicationInterface {
//	public static boolean allTaskFinished = false;
//	public static boolean alldatauploaded = false;
	
	public static long lastTimeSkip = -1;
	
	private static String dbName = "trackerDatabase";
	private DBOpenerHelper helper;
	private SQLiteDatabase db;
	private Activity currentActivity;
	private UserInfo userinfo;
	private TaskManager taskmanager;
	private int totalInstructionCount = 0;
	private long wakeUpTime = -1;
	private int duration = Common.durationBetweenSegment; //1min
	// private String defaultPhotoName = "user.jpg";
	@Override
	public void onCreate() {
		super.onCreate();
		helper = new DBOpenerHelper(this.getApplicationContext(), dbName, null, 1);
		db = helper.getReadableDatabase();
//		taskmanager = new TaskManager(this);
	}

	public void onTerminate() {
		this.closeAll();
		super.onTerminate();
	}
	
	private String timeFile = "time.txt";
	public void setWakeUpTime(long time){
		Log.v("mark","setWakeUpTime:"+time);
		try {
			FileOutputStream fout = this.openFileOutput(timeFile, Context.MODE_PRIVATE);
			fout.write((time+"").getBytes());
		} catch (FileNotFoundException e) {
			Log.v("mark","onChangeSegment cannot save time information FileNotFoundException");
		} catch (IOException e) {
			Log.v("mark","onChangeSegment cannot save time information IOException");
		}
		this.wakeUpTime = time;
	}
	
	public long getWakeUpTime(){
		Log.v("mark","getWakeUpTime");
		File myFile = new File(this.getFilesDir(), timeFile);
		if(myFile.exists()){
			try {
				Scanner sc = new Scanner(myFile);
				if(sc.hasNext()){
					this.wakeUpTime = sc.nextLong();
					return wakeUpTime;
				}
			} catch (FileNotFoundException e) { }
			return -1;
		}else{
			return -1;
		}
	}
	
	public void initTaskManager(){
		taskmanager = new TaskManager(this);
	}

	public int getCurrentInstructionId() {
		return taskmanager.getInstructionReference()._id;
	}

	public void setInstructionCount(int amount) {
		this.totalInstructionCount = amount;
	}

	public int getInstructionCount() {
		return this.totalInstructionCount;
	}

	private void internalCheck() {
		// check table
		String[] tables = Common.Tables.TABLENAMES;
		SQLiteDatabase db = getDB();
		for (String table : tables) {
			Cursor cur = db.query(table, null, null, null, null, null, null);
			Log.v("mark", table + " has " + cur.getCount() + " entry with " + cur.getColumnCount() + " columns");
		}
	}

	public void resetDatabase() {
		if (db != null) {
			helper.reset(db);
		}
	}

	public SQLiteDatabase getDB() {
		if (db == null) {
			db = new DBOpenerHelper(this.getApplicationContext(), dbName, null, 1).getReadableDatabase();
		}
		if (!db.isOpen()) {
			Log.v("mark", "database not open yet");
		}
		return db;
	}

	public void setCurrentActivity(Activity act) {
		this.currentActivity = act;
	}

	public Activity getCurrentActivity() {
		return currentActivity;
	}

	public void requestVerification(int event_obj, String event) {
		this.taskmanager.onVerification(event_obj, event);
	}

	public void requestVerification(int event_obj, String event, String additionalInfo) {
		this.taskmanager.setAdditionalInformation(additionalInfo);
		this.taskmanager.onVerification(event_obj, event);
	}

	public boolean requestAction(String tag) {
//		 int intended_id = Common.Task.taskNameToId.get(tag);
//		 if(intended_id == this.taskmanager.current.task_id){
//		 return true;
//		 }else{
//		 return false;
//		 }
		//
		return true;
	}

	public UserInfo getUserInfo() {
		SQLiteDatabase db = this.getDB();
		db.beginTransaction();
		Cursor cur = db.query(Common.Tables.USERINFO, null, null, null, null, null, null);
		if (cur.moveToFirst()){
			this.userinfo = new UserInfo(cur);
//			Log.v("mark","getUserInfo:"+userinfo);
		}else Log.v("mark", "Cannot find userinfo!!");
		cur.close();
		db.setTransactionSuccessful();
		db.endTransaction();
		return userinfo;
	}
	
	public void setUserInfomraiton(String username, Integer inst_id, Integer taskId, Integer segId){
		setUserInfomraiton(username,inst_id,taskId, segId,null,null);
	}
	
	public void setUserInfomraiton(String username, Integer inst_id, Integer taskId, Integer segId, Integer finished, Integer uploaded){
		SQLiteDatabase db = this.getDB();
		db.beginTransaction();
		ContentValues values = new ContentValues();
		if(username != null)values.put(Common.Tables.UserInfo._id, username);
		if(inst_id!=null)values.put(Common.Tables.UserInfo.inst_id, inst_id);
		if(taskId!=null)values.put(Common.Tables.UserInfo.task_id, taskId);
		if(segId!=null)values.put(Common.Tables.UserInfo.seg_id, segId);
		if(finished!=null) values.put(Common.Tables.UserInfo.finished, finished);
		if(uploaded!=null) values.put(Common.Tables.UserInfo.uploaded, uploaded);
		long result = db.update(Common.Tables.USERINFO, values, null, null);
		Log.v("mark","setUserInfomraiton:"+username+"|"+inst_id+"|"+taskId+"|"+segId+" with result:"+result);
		db.setTransactionSuccessful();
		db.endTransaction();
	}
	
	
	public void onChangeSegment(){
		//store information in a file
//		Log.v("mark","onChangeSegment");
//		
//		
//		//start the alarm service
//		Calendar cal = Calendar.getInstance();
//		Intent serviceIntent = new Intent(this, tracker.utility.StringNotificationService.class);
//		PendingIntent pintent = PendingIntent.getService(this, 1, serviceIntent, 0);
//		Bundle arg = new Bundle();
//		arg.putString(tracker.utility.StringNotificationService.DATAKEY, "a message from main");
//		serviceIntent.putExtras(arg);
//		AlarmManager alarm = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
//		alarm.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis()+this.duration, pintent);
		
		this.setWakeUpTime(System.currentTimeMillis()+this.duration);
		if(this.currentActivity instanceof FrameActivity){
			((FrameActivity)this.currentActivity).onChangeSegment();
		}
	}

	public void onAllTaskFinished(){
		Log.v("mark","onAllTaskFinished");
		if(this.currentActivity instanceof FrameActivity){
			((FrameActivity)this.currentActivity).onAllTaskFinished();
		}
	}

	public Instruction getInstruction() {
		return this.taskmanager.getCurrentInstruction();
	}

	public void skipInstruction() {
		this.taskmanager.nextInstruction(false);
		Activity act = this.getCurrentActivity();
		if (act instanceof FrameActivity) {
			((FrameActivity) act).closeRightDrawer();
			((FrameActivity) act).openLeftDrawer();
		}
	}
	
	private String[] filenames = new String[4];
	public synchronized void enterNewTask(int newtask_id) {
		Log.v("mark", "enterNewTask:" + newtask_id);
		// deal with original file
		if (streams.isEmpty()) {// on start up
			Log.v("mark", "enterNewTask: bos is null");
			// do nothing
		} else {
			String[] fileNames = getStreamName();
			this.closeAll();
			// try to upload file
			Log.v("mark", "enterNewTask starting service");
			startUploadService(fileNames);
		}
		// open file if valid task_id
		if (newtask_id >= 0) {
			UserInfo info = this.getUserInfo();
			String prefix = info.username + "_" + info.task_id;
			filenames[0] = prefix+"_sensor";
			filenames[1] = prefix+"_motion";
			filenames[2] = prefix+"_key";
			filenames[3] = prefix+"_view";
			this.createFileStreams(filenames);
		} else {
			closeAll();
			filenames[0] = null;
			filenames[1] = null;
			filenames[2] = null;
			filenames[3] = null;
			// do something?
		}
		// check if segment changes
		// TODO
	}
	int counter = 0;

	public void startUploadService(String... filenames){
		Intent intent = new Intent(this.getApplicationContext(), FileUploader.class);
		intent.putExtra(FileUploader.MULTIFILE, filenames);
		startService(intent);
	}
	
	public void gatherDeviceInformation(Activity act){
		String textToStore = "";

		// Screen Info
		DisplayMetrics metrics = new DisplayMetrics();
		act.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		textToStore +=       Float.toString(metrics.density);
		textToStore += "," + Integer.toString(metrics.densityDpi);
		textToStore += "," + Integer.toString(metrics.heightPixels);
		textToStore += "," + Float.toString(metrics.scaledDensity);
		textToStore += "," + Integer.toString(metrics.widthPixels);
		textToStore += "," + Float.toString(metrics.xdpi);
		textToStore += "," + Float.toString(metrics.ydpi);

		// SW Info
		textToStore += "," + System.getProperty("os.version");
		textToStore += "," + android.os.Build.VERSION.CODENAME;
		textToStore += "," + android.os.Build.VERSION.INCREMENTAL;
		textToStore += "," + android.os.Build.VERSION.RELEASE;
		textToStore += "," + android.os.Build.VERSION.SDK_INT;
		textToStore += "," + Integer.valueOf(android.os.Build.VERSION.SDK);;

		// HW Info
		textToStore += "," + android.os.Build.DEVICE;
		textToStore += "," + android.os.Build.MODEL;
		textToStore += "," + android.os.Build.PRODUCT;
		textToStore += "," + android.os.Build.BOOTLOADER;
		textToStore += "," + android.os.Build.BRAND;
		textToStore += "," + android.os.Build.BOARD;
		textToStore += "," + android.os.Build.CPU_ABI;
		textToStore += "," + android.os.Build.CPU_ABI2;
		textToStore += "," + android.os.Build.DISPLAY;
		textToStore += "," + android.os.Build.FINGERPRINT;
		textToStore += "," + android.os.Build.HARDWARE;
		textToStore += "," + android.os.Build.HOST;
		textToStore += "," + android.os.Build.ID;
		textToStore += "," + android.os.Build.MANUFACTURER;
		textToStore += "," + android.os.Build.SERIAL;
		textToStore += "," + android.os.Build.TAGS;
		textToStore += "," + android.os.Build.TIME;
		textToStore += "," + android.os.Build.TYPE;
		textToStore += "," + android.os.Build.USER;

		// Display - Another Method
		Display display = act.getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		textToStore += "," + size.x;
		textToStore += "," + size.y;

		String filename = this.getUserInfo().username+"_deviceinfo";
		this.createFileStream(filename);
		writeToStream(filename,textToStore);
		closeStream(filename);
		
		//start intent service
		startUploadService(filename);
	}
	
	
	/*************File writer Stream Starts*************/
	private final Map<String,BufferedOutputStream> streams = new HashMap<String,BufferedOutputStream>();
	
	public String[] getStreamName(){
		String[] result;
		synchronized(streams){
			result = streams.keySet().toArray(new String[0]);
		}
		return result;
	}
	
	public void closeAll(){
		synchronized(streams){
			String[] arr = streams.keySet().toArray(new String[0]);
			for(String key : arr){
				BufferedOutputStream out = streams.remove(key);
				try { out.close();
				} catch (IOException e) {  }
			}
		}
	}
	public void closeStream(String name){
		synchronized(streams){
			BufferedOutputStream bout = streams.get(name);
			if(bout != null) try { bout.close();
			} catch (IOException e) { }
		}
	}
	public void flushAll(){
		synchronized(streams){
			String[] arr = streams.keySet().toArray(new String[0]);
			for(String key : arr){
				BufferedOutputStream out = streams.remove(key);
				try { out.flush();
				} catch (IOException e) {  }
			}
		}
	}
	public void flushStream(String name){
		synchronized(streams){
			BufferedOutputStream bout = streams.get(name);
			if(bout != null) try { bout.flush();
			} catch (IOException e) { }
		}
	}
	public void createFileStream(String name){
		synchronized(streams){
			BufferedOutputStream bout = streams.get(name);
			if(bout == null){
				FileOutputStream fout;
				try {
					fout = this.openFileOutput(name, Context.MODE_APPEND);
					bout = new BufferedOutputStream(fout);
					streams.put(name, bout);
				} catch (FileNotFoundException e) {
					Log.v("mark","FileCreationFailure:"+name);
				}
			}
		}
	}
	public void createFileStreams(String... names){
		for(String name:names){
			createFileStream(name);
		}
	}
	public void writeToStream(String name, String data){
		if(name == null) return;
		synchronized(streams){
			BufferedOutputStream bout = streams.get(name);
			if(bout != null){
				try { bout.write(data.getBytes());
				} catch (IOException e) {}
			}
		}
	}
	public void writeToStream(int index, String data){
		String name = this.filenames[index];
		this.writeToStream(name, data);
	}
	/*************File writer Stream Ends*************/
	
	
	public static class UserInfo {

		public int inst_id, task_id, seg_id;
		public String username;
		public int allTaskFinished, allDataUploaded;
		
		public UserInfo(String username, int inst_id, int task_id, int seg_id, int taskAllFinished, int uploaded) {
			this.username = username;
			this.inst_id = inst_id;
			this.task_id = task_id;
			this.seg_id = seg_id;
			allTaskFinished = taskAllFinished;
			allDataUploaded = uploaded;
		}

		public UserInfo(Cursor cur) {
			this(cur.getString(cur.getColumnIndex(Common.Tables.UserInfo._id)),
					cur.getInt(cur.getColumnIndex(Common.Tables.UserInfo.inst_id)), 
					cur.getInt(cur.getColumnIndex(Common.Tables.UserInfo.task_id)), 
					cur.getInt(cur.getColumnIndex(Common.Tables.UserInfo.seg_id)),
					cur.getInt(cur.getColumnIndex(Common.Tables.UserInfo.finished)),
					cur.getInt(cur.getColumnIndex(Common.Tables.UserInfo.uploaded))
					);
		}

		public String toString() {
			return username + "|" + inst_id+ "|"+ task_id + "|" + seg_id;
		}
	}
	
	

	
}

package tracker.UI.activity;

import java.util.Arrays;
import tracker.UI.components.MyKeyboard;
import tracker.UI.components.MyKeyboardView;
import tracker.UI.fragment.FileWriterFragment;
import tracker.service.MyAlarmReceiver;
import tracker.springversion1.Common;
import tracker.springversion1.MobileStudyApplication;
import tracker.springversion1.ProjectInterface;
import tracker.springversion1.R;
import tracker.springversion1.TaskManager;
import tracker.springversion1.TaskManager.Instruction;
import tracker.utility.Utility;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public abstract class FrameActivity extends Activity implements ProjectInterface.ActivityInterface {

	private DrawerLayout drawerlayout;
	private DrawerLayout.DrawerListener drawerListener;
	private SQLiteDatabase db;
	protected MobileStudyApplication app;
	protected MyKeyboard innerkeyboard;
	private LeftInstructionFragment leftFragement;
	private RightQuestionFragment rightFragment;
	private Sensor mAccelerometer, mMagneticSensor, mGyroscope;
	private long sensorCollectDuration = 20;//20ms
	
	/**********Sensor Data Variable Starts**********************/
	private SensorEventListener mListener = new SensorEventListener() {

		@Override
		public void onAccuracyChanged(Sensor arg0, int arg1) {}
		@Override
		public void onSensorChanged(SensorEvent event) {
			recordSensorData(event);
		}
	};
	private static final int PERIOD = 2000;
	private static final int SENSOR_DURATION = 600;
	private Handler handler;
	private SensorManager mSensorManager;
	private final Runnable processSensors = new Runnable() {

		@Override
		public void run() {
			mSensorManager.registerListener(mListener, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
			mSensorManager.registerListener(mListener, mMagneticSensor, SensorManager.SENSOR_DELAY_NORMAL);
			mSensorManager.registerListener(mListener, mGyroscope, SensorManager.SENSOR_DELAY_NORMAL);
			handler.postDelayed(this, PERIOD);
		}
	};
	private final Runnable postProcess = new Runnable() {

		@Override
		public void run() {
			mSensorManager.unregisterListener(mListener);
			handler.postDelayed(this, PERIOD);
		}
	};

	private long lastRecorded_acc = -1, lastRecorded_gyro = -1, lastRecorded_magn = -1;
	
	private SensorEventListener sensorListener = new SensorEventListener(){
		@Override
		public void onAccuracyChanged(Sensor event, int arg1) { }
		@Override
		public void onSensorChanged(SensorEvent event) {
			long current = System.currentTimeMillis();
			switch(event.sensor.getType()){
			case Sensor.TYPE_ACCELEROMETER:{
				if(current - lastRecorded_acc > sensorCollectDuration){
					recordSensorData(event);
					lastRecorded_acc = current; 
				}
			}break;
			case Sensor.TYPE_GYROSCOPE:{
				if(current - lastRecorded_gyro > sensorCollectDuration){
					recordSensorData(event);
					lastRecorded_gyro = current; 
				}
			}break;
			case Sensor.TYPE_MAGNETIC_FIELD:{
				if(current - lastRecorded_magn > sensorCollectDuration){
					recordSensorData(event);
					lastRecorded_magn = current; 
				}
			}break;
			}
		}
	};
	
	
	
	
	
	/**********Sensor Data Variable Ends**********************/
	
	/*********Activity Functions Starts************/
	@Override
	protected void onResume() {
		super.onResume();
		app.setCurrentActivity(this);
//		handler.post(processSensors);
//		handler.postDelayed(postProcess, SENSOR_DURATION);
		mSensorManager.registerListener(sensorListener, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
		mSensorManager.registerListener(sensorListener, mMagneticSensor, SensorManager.SENSOR_DELAY_NORMAL);
		mSensorManager.registerListener(sensorListener, mGyroscope, SensorManager.SENSOR_DELAY_NORMAL);
		this.cancelReminderAlarmAndNotification();
	}

	@Override
	public void onPause() {
		// deal with sensor
//		handler.removeCallbacks(processSensors);
//		handler.removeCallbacks(postProcess);
//		mSensorManager.unregisterListener(mListener);
		mSensorManager.unregisterListener(sensorListener);
		super.onPause();
	}
	
	@Override
	public void onBackPressed() {
		if (this.innerkeyboard.isCustomKeyboardVisible()) {
			innerkeyboard.hideCustomKeyboard();
//			View v = this.getCurrentFocus();
//			app.recordEvent("onBackPressed: innerkeyboard.hideCustomKeyboard");
		} else {
			FragmentManager manager = this.getFragmentManager();
			int count = manager.getBackStackEntryCount();
			switch (count) {
			case 0:
				super.onBackPressed();
				break;
			case 1: {//taskpanel is currently add to the stack with tag.
				String topname = manager.getBackStackEntryAt(count - 1).getName();
				Fragment topFrag = manager.findFragmentByTag(topname);
				if (topFrag instanceof ProjectInterface.FragmentInterface) {
					if (!((ProjectInterface.FragmentInterface) topFrag).actionOnBackPressed()) {
						manager.popBackStack();
						super.onBackPressed();
					}
				} else {
					manager.popBackStack();
					super.onBackPressed();
				}
			}break;
			default: {
				// find the last fragment
				String topname = manager.getBackStackEntryAt(count - 1).getName();
				Fragment topFrag = manager.findFragmentByTag(topname);
				if (topFrag instanceof ProjectInterface.FragmentInterface) {
					if (!((ProjectInterface.FragmentInterface) topFrag).actionOnBackPressed()) {
						super.onBackPressed();
//						this.recreate();
					}
				} else { super.onBackPressed(); 
//				this.recreate();
				}
				recordProcedureData(this.getLatestFragment().toString());
			}break;
			}
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_drawer);
		app = (MobileStudyApplication) this.getApplication(); 
		
		//setup sensor collection
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mMagneticSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		handler = new Handler();
		
		//setup UI
		initVariables();
		setupActionBar();
		innerkeyboard = new MyKeyboard(this, R.id.keyboardview, R.xml.qwer);
		leftFragement = new LeftInstructionFragment();
		rightFragment = new RightQuestionFragment();
		getFragmentManager().beginTransaction().replace(R.id.left, leftFragement).replace(R.id.right, rightFragment).commit();
		setCenterContent(savedInstanceState);
		
		//setup file writer fragment
//		streams = FileWriterFragment.findOrCreateRetainFragment(this, "streams");
	}

	private void setupActionBar() {
		// http://stackoverflow.com/questions/15518414/how-can-i-implement-custom-action-bar-with-custom-buttons-in-android
		final ViewGroup actionBarLayout = (ViewGroup) getLayoutInflater().inflate(R.layout.action_bar, null);
		final ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setCustomView(actionBarLayout);
		View showInstruction = actionBarLayout.findViewById(R.id.actionbar_instruction);
		showInstruction.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				closeRightDrawer();
				openLeftDrawer();
			}
		});
		View showInputField = actionBarLayout.findViewById(R.id.actionbar_inputfield);
		showInputField.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				closeLeftDrawer();
				openRightDrawer();
			}
		});
	}
	
	/*********Activity Functions Ends************/

	/*********Operation On Right&Left Panel Starts**********/
	
	public void clearRightPanel() {
		TextView tv = (TextView) this.findViewById(R.id.right_ans_inputfield);
		tv.setText("");
	}

	public void openRightDrawer() {
		drawerlayout.openDrawer(Gravity.RIGHT);
	}

	public void openLeftDrawer() {
		drawerlayout.openDrawer(Gravity.LEFT);
	}

	public void closeRightDrawer() {
		this.innerkeyboard.hideCustomKeyboard();
		drawerlayout.closeDrawer(Gravity.RIGHT);
	}

	public void closeLeftDrawer() {
		drawerlayout.closeDrawer(Gravity.LEFT);
	}

	public String retrieveAnswerOnRightPanel() {
		if (this.rightFragment != null) { return rightFragment.getSubmittion(); }
		return null;
	}

	/*********Operation On Right&Left Panel Ends**********/
	
	
	/*********Operation On Keyboard Starts***************/
	public void closeKeyBoardCallBack() {
		View v = this.findViewById(R.id.drawer_layout);
		v.requestFocus();
//		if (needResizeOnKeyboardShowUp()) {}
		recordProcedureData("closekeyboard");
	}

	public void openKeyBoardCallBack() {
		View top = this.findViewById(R.id.drawer_layout);
		RelativeLayout.LayoutParams llp = (android.widget.RelativeLayout.LayoutParams) top.getLayoutParams();
		if (needResizeOnKeyboardShowUp()) { llp.addRule(RelativeLayout.ABOVE, R.id.keyboardview);
		} else { llp.addRule(RelativeLayout.ABOVE, -1); }
		recordProcedureData("openkeyboard");
	}
	
	public void closeKeyBoard() {
		this.innerkeyboard.hideCustomKeyboard();
	}
	
	public boolean needResizeOnKeyboardShowUp() {
		return true;
	}
	/*********Operation On Keyboard Ends***************/
	
	
	
	/*********Various Operation Starts***************/
	public MobileStudyApplication getMobileStudyApplication() {
		return (MobileStudyApplication) this.getApplication();
	}

	public void replaceTheCenterView(Fragment frag, String tag) {
		recordProcedureData(frag.toString());
		if (tag == null) {
			this.getFragmentManager().beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).replace(this.getCenterViewId(), frag).commit();
		} else {
			this.getFragmentManager().beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).replace(this.getCenterViewId(), frag, tag).addToBackStack(tag).commit();
		}
	}

	public int getCenterViewId() {
		return R.id.toreplace;
	}
	
	private void initVariables() {
		drawerlayout = (DrawerLayout) this.findViewById(R.id.drawer_layout);
		drawerListener = new DrawerListener() {
			@Override
			public void onDrawerClosed(View arg0) {}
			@Override
			public void onDrawerOpened(View arg0) {
				int id = arg0.getId();
				switch (id) {
				case R.id.left: { leftFragement.update(); } break;
				case R.id.right: { innerkeyboard.hideCustomKeyboard(); } break;
				default: Log.v("mark", "onDrawerOpened: unidentified id "); }
			}
			@Override
			public void onDrawerSlide(View arg0, float arg1) {}
			@Override
			public void onDrawerStateChanged(int arg0) {}
		};
		drawerlayout.setDrawerListener(drawerListener);
	}
	
	public SQLiteDatabase getDatabaseReferenceFromActivity() {
		if (db == null) {
			if (this.getApplication() instanceof MobileStudyApplication) {
				db = ((MobileStudyApplication) this.getApplication()).getDB();
			}
		}
		return db;
	}

	public void functionUnavailable(View v) {
		Toast.makeText(getApplicationContext(), "Function unavailable", Toast.LENGTH_SHORT).show();
	}

	public void registerEditText(EditText text) {
		innerkeyboard.registerEditText(text);
	}
	
	public abstract void setCenterContent(Bundle savedInstanceState);

	public boolean onTouchEvent(MotionEvent event) {
		if (this.innerkeyboard.isCustomKeyboardVisible()) {
			innerkeyboard.hideCustomKeyboard();
			return true;
		} else {
			this.closeRightDrawer();
			this.closeLeftDrawer();
			return super.onTouchEvent(event);
		}
	}
	
	public Fragment getLatestFragment(){
		FragmentManager manager = this.getFragmentManager();
		int count = manager.getBackStackEntryCount();
		String fragLabel= manager.getBackStackEntryAt(count-1).getName();
	    Fragment fragment=getFragmentManager().findFragmentByTag(fragLabel);
	    return fragment;
	}
	/*********Various Operation Ends***************/
	


	/*********Task Operation Starts***********/
	
	public void onChangeSegment() {
		// start the screen that tell the user to wait
		Intent intent = new Intent(this, WaitingActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		this.startActivity(intent);
		
		//start alarm service
		scheduleReminderAlarm();
	}
	
	public void onAllTaskFinished(){
		Intent intent = new Intent(this, PaymentActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		this.startActivity(intent);
		
//		MobileStudyApplication.allTaskFinished = true;
		
		this.app.setUserInfomraiton(null, null, null, null,1,null);
		
		scheduleFinalUploaderAlarm();
	}
	
	/*********Task Manager Ends***********/
	

	
	/********* Data Collection Starts***********/
	private static String dataSeparater = ",";
	private static String dataSegment = ";";
	private static String dataLineEnd = "\n";
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		super.dispatchTouchEvent(ev);
		this.recordMotinoData(ev);
		return false;
	}
	
	public void recordRawData(int channel, String data){
		if(!data.endsWith(dataLineEnd)){
			data += dataLineEnd;
		}
		this.getMobileStudyApplication().writeToStream(channel, data);
	}
	
	public void recordSensorData(SensorEvent event) {
		String torecord = buildSensorData(event);
		if(torecord == null) return;
		if(!torecord.endsWith(dataLineEnd)){
			torecord += dataLineEnd;
		}
		this.getMobileStudyApplication().writeToStream(Common.StreamChannel.SENSOR, torecord);
	}

	public void recordMotinoData(MotionEvent event) {
		String torecord = buildMotionData(event);
		if(!torecord.endsWith(dataLineEnd)){
			torecord += dataLineEnd;
		}
		this.getMobileStudyApplication().writeToStream(Common.StreamChannel.MOTION, torecord);
	}

	public void recordKeyboardEvent(MotionEvent event, MyKeyboardView board) {
		String torecord = buildKeyboardData(event, board);
		if(!torecord.endsWith(dataLineEnd)){
			torecord += dataLineEnd;
		}
		this.getMobileStudyApplication().writeToStream(Common.StreamChannel.KEY, torecord);
	}
	
	public void recordViewTouchEvent(MotionEvent event, View view){
		String torecord = buildViewTouchData(event, view);
		if(!torecord.endsWith(dataLineEnd)){
			torecord += dataLineEnd;
		}
		this.getMobileStudyApplication().writeToStream(Common.StreamChannel.VIEW, torecord);
	}
	
	public void recordProcedureData(String procedure){
		if(!procedure.endsWith(dataLineEnd)){
			procedure += dataLineEnd;
		}
		this.recordRawData(Common.StreamChannel.MOTION, "Procedure,"+SystemClock.uptimeMillis()+","+procedure);
	}
	
	public String buildSensorData(SensorEvent event) {
		int type = event.sensor.getType();
		switch (type) {
		case Sensor.TYPE_ACCELEROMETER: {
			return Utility.connectAsString(dataSeparater, SystemClock.uptimeMillis(), type,
					event.values[0], event.values[1], event.values[2])+dataLineEnd;
		}
		case Sensor.TYPE_GYROSCOPE: {
			return Utility.connectAsString(dataSeparater, SystemClock.uptimeMillis(), type,
					event.values[0], event.values[1], event.values[2])+dataLineEnd;	
		}
		case Sensor.TYPE_MAGNETIC_FIELD: {
			return Utility.connectAsString(dataSeparater, SystemClock.uptimeMillis(), type,
					event.values[0], event.values[1], event.values[2])+dataLineEnd;
		}
		default: {
			Log.v("mark", "Unexpected:" + type);
			return "Unexpected sensor type" + type;
		}
		}
	}

	private MotionEvent.PointerCoords cor = new MotionEvent.PointerCoords();
	public String buildMotionData(MotionEvent event) {
		String instructionInformation = ""+getMobileStudyApplication().getInstruction()._id;
		String currentFragment = getLatestFragment().toString();
	    int count = event.getPointerCount();
	    long eventTime = event.getEventTime();
	    long downTime = event.getDownTime();
	    int actionIndex = event.getActionIndex();
	    int actionMasked = event.getActionMasked();
	    int screenOri = this.getWindowManager().getDefaultDisplay().getOrientation();
	    StringBuilder sb = new StringBuilder();
	    
	    sb.append(eventTime).append(dataSeparater);
	    sb.append(downTime).append(dataSeparater);
	    sb.append(screenOri).append(dataSeparater);
	    sb.append(instructionInformation).append(dataSeparater);
	    sb.append(currentFragment).append(dataSeparater);
	    sb.append(actionIndex).append(dataSeparater);
	    sb.append(actionMasked).append(dataSeparater);
	    sb.append(event.getEdgeFlags()).append(dataSeparater);
	    sb.append(event.getFlags()).append(dataSeparater);
	    sb.append(count).append(dataSegment);
	    
	    for(int index = 0;index<count;index++){
	    	sb.append(index).append(dataSeparater);
	    	sb.append(event.getPointerId(index)).append(dataSeparater);
	    	event.getPointerCoords(index, cor);
	    	sb.append(cor.pressure).append(dataSeparater);
	    	sb.append(cor.size).append(dataSeparater);
	    	sb.append(cor.orientation).append(dataSeparater);
	    	sb.append(cor.x).append(dataSeparater);
	    	sb.append(cor.y).append(dataSeparater);
	    	sb.append(dataSegment);
	    }
	    
	    sb.append(dataLineEnd);
//	    for(int index =0; index<count;index ++){
//	    	//Motion identifier information
//	    	sb.append(eventTime).append(dataSeparater);//uptimeMillis()  based
//	    	sb.append(event.getPointerId(index)).append(dataSeparater);
//		    //Current instruction information
//		    sb.append(instructionInformation).append(dataSeparater);
//		    //Current Fragment information
//		    sb.append(currentFragment).append(dataSeparater);
//		    //Pointer information
//		    sb.append(count).append(dataSeparater);
////		    sb.append(event.getRawX()).append(dataSeparater);
////		    sb.append(event.getRawY()).append(dataSeparater);
//		    sb.append(actionMasked).append(dataSeparater);
//		    sb.append(downTime).append(dataSeparater);
//		    sb.append(event.getPressure(index)).append(dataSeparater);
//		    sb.append(event.getSize(index)).append(dataSeparater);
//		    sb.append(event.getOrientation(index)).append(dataSeparater);
//		    sb.append(screenOri).append(dataLineEnd);
//	    }
		return sb.toString();
	}

	public String buildKeyboardData(MotionEvent event, MyKeyboardView board) {
		StringBuilder sb = new StringBuilder();
		int count = event.getPointerCount();
		long eventTime = event.getEventTime();
		
		sb.append(eventTime).append(dataSegment);
		
		for(int index = 0; index<count;index++){
			sb.append(index).append(dataSeparater);
			sb.append(event.getPointerId(index)).append(dataSeparater);
			
			float x = event.getX(index);
			float y = event.getY(index);
			
			String keys = board.whichKey(x, y);
			sb.append(keys).append(dataSegment);
		}
		sb.append(dataLineEnd);
//		long eventTime = event.getEventTime();
//		for(int index = 0; index < count; index ++){
//			//Motion identifier information
//	    	sb.append(eventTime).append(dataSeparater);//uptimeMillis()  based
//	    	sb.append(event.getPointerId(index)).append(dataSeparater);
//	    	sb.append(key).append(dataLineEnd);
//		}
		return sb.toString();
	}
	
	public String buildViewTouchData(MotionEvent event, View view){
		StringBuilder sb = new StringBuilder();
		int count = event.getPointerCount();
		long eventTime = event.getEventTime();
		for(int index = 0; index < count; index ++){
			//Motion identifier information
	    	sb.append(eventTime).append(dataSeparater);//uptimeMillis()  based
	    	sb.append(event.getPointerId(index)).append(dataSeparater);
	    	//view information
	    	sb.append(view.getId()).append(dataSeparater);
	    	sb.append(view.getTag()).append(dataLineEnd);
		}
		return sb.toString();
	}
	
	public View.OnTouchListener getTouchListener(){
		return viewTouchListener;
	}
	
	private View.OnTouchListener viewTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			recordViewTouchEvent(event,v);
			return false;
		}
	};
	
	/***********Data collection ends******************/

	/***********Static Fragment class Starts****************/
	public static class LeftInstructionFragment extends Fragment {
		private View ground;
		private static String instructionfolder = "instruction/";
		private Instruction current;

		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			ground = inflater.inflate(R.layout.frag_left, container, false);
			ground.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					Activity act = getActivity();
					((FrameActivity) act).closeLeftDrawer();
				}
			});
			View action = ground.findViewById(R.id.left_open_right);
			if (action != null) {
				action.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						Activity act = LeftInstructionFragment.this.getActivity();
						((FrameActivity) act).closeLeftDrawer();
						((FrameActivity) act).openRightDrawer();
					}
				});
			}
			return ground;
		}

		public void onStart() {
			update();
			super.onStart();
		}

		public void update() {
			MobileStudyApplication app = (MobileStudyApplication) this.getActivity().getApplication();
			Instruction inst = app.getInstruction();
			if (current == inst) return;
			if (inst != null) {
				TextView text = (TextView) ground.findViewById(R.id.left_text);
				text.setText(inst.instruction.replace("\\n", "\n"));
				ImageView imageview = (ImageView) ground.findViewById(R.id.left_image);
				if (!Utility.setImage(instructionfolder + inst.imagename, this.getActivity().getAssets(), imageview)) {
					imageview.setVisibility(View.INVISIBLE);
				} else {
					imageview.setVisibility(View.VISIBLE);
				}
				TextView step = (TextView) ground.findViewById(R.id.left_step);
				step.setText(inst._id + "/" + app.getInstructionCount());
				current = inst;
				TextView hint = (TextView) ground.findViewById(R.id.left_hint);
				hint.setText(inst.hint.replace("\\n", "\n"));
			} else {
				// TODO
			}
		}
	}
	public static class RightQuestionFragment extends Fragment {

		private View ground;
		
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			ground = inflater.inflate(R.layout.frag_right, container, false);
			View skipview = ground.findViewById(R.id.right_instruction_skip);
			TextView submit = (TextView) ground.findViewById(R.id.right_ans_submit);
			submit.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					TextView tv = (TextView) ground.findViewById(R.id.right_ans_inputfield);
				}
			});
			if (skipview != null) {
				skipview.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						long currentTime = System.currentTimeMillis();
						long interval = currentTime - MobileStudyApplication.lastTimeSkip ;
						if(interval < Common.duratinoBetweenSkip){

							Toast.makeText(getActivity(), "Skip button is coolling down!", Toast.LENGTH_SHORT).show();
						}else{
							MobileStudyApplication app = (MobileStudyApplication) getActivity().getApplication();
							app.skipInstruction();
							TextView tv = (TextView) ground.findViewById(R.id.right_ans_inputfield);
							tv.setText("");
							
							MobileStudyApplication.lastTimeSkip = System.currentTimeMillis();
						}
					}
				});
			}
			ground.findViewById(R.id.right_clear).setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					TextView tv = (TextView) ground.findViewById(R.id.right_ans_inputfield);
					tv.setText("");
				}
			});
			// View openLeft = ground.findViewById(R.id.right_open_left);
			// if(openLeft!=null){
			// openLeft.setOnClickListener(new View.OnClickListener() {
			// @Override
			// public void onClick(View v) {
			// Activity act = RightQuestionFragment.this.getActivity();
			// ((DrawerActivity)act).closeRightDrawer();
			// ((DrawerActivity)act).openLeftDrawer();
			// TextView tv =
			// (TextView)ground.findViewById(R.id.right_ans_inputfield);
			// tv.setText("");
			// }
			// });
			// }
			return ground;
		}
		
		public void update() {
			MobileStudyApplication app = (MobileStudyApplication) this.getActivity().getApplication();
			Instruction inst = app.getInstruction();
			if (inst.verif_src == 2) { // if this panel is needed, which the
										// number 2 represents
				if (ground != null) {
					ground.findViewById(R.id.right_answer_field).setVisibility(View.VISIBLE);
					ground.findViewById(R.id.right_message).setVisibility(View.INVISIBLE);
				}
			} else {
				if (ground != null) {
					ground.findViewById(R.id.right_answer_field).setVisibility(View.INVISIBLE);
					ground.findViewById(R.id.right_message).setVisibility(View.VISIBLE);
				}
			}
		}

		public String getSubmittion() {
			TextView tv = (TextView) ground.findViewById(R.id.right_ans_inputfield);
			return tv.getText().toString().trim();
		}
	}

	/***********Alarm setup****************/
	public void scheduleFinalUploaderAlarm(){
		Intent intent = new Intent(getApplicationContext(), MyAlarmReceiver.class);
		intent.putExtra(MyAlarmReceiver.SERVICE_TAG, MyAlarmReceiver.UPLOADER);
		
		// Create a PendingIntent to be triggered when the alarm goes off
		final PendingIntent pIntent = PendingIntent.getBroadcast(this, MyAlarmReceiver.REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		int intervalMillis = Common.durationBetweenFinalUploading; 
		long firstMillis = System.currentTimeMillis() + intervalMillis; 
		AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
		alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis, intervalMillis, pIntent);
	}
	
	public void scheduleReminderAlarm() {
		// Construct an intent that will execute the AlarmReceiver
		Intent intent = new Intent(getApplicationContext(), MyAlarmReceiver.class);
		intent.putExtra(MyAlarmReceiver.SERVICE_TAG, MyAlarmReceiver.REMINDER);
		
		// Create a PendingIntent to be triggered when the alarm goes off
		final PendingIntent pIntent = PendingIntent.getBroadcast(this, MyAlarmReceiver.REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		
		int intervalMillis = Common.durationBetweenSegment; 
		long firstMillis = System.currentTimeMillis() + intervalMillis; 
		AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
		alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis, intervalMillis, pIntent);
	}
	
	public void cancelReminderAlarmAndNotification() {
		Intent intent = new Intent(getApplicationContext(), MyAlarmReceiver.class);
		final PendingIntent pIntent = PendingIntent.getBroadcast(this, MyAlarmReceiver.REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
		alarm.cancel(pIntent);
		
    	NotificationManager mNotificationManager =
    		    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    	mNotificationManager.cancel(Common.ReminderNotificationId);
	}
}



//if (gyro_timestamp != 0) {
//	final float dT = (event.timestamp - gyro_timestamp) * NS2S;
//	// Axis of the rotation sample, not normalized yet.
//	float axisX = event.values[0];
//	float axisY = event.values[1];
//	float axisZ = event.values[2];
//	// Calculate the angular speed of the sample
//	float omegaMagnitude = (float) Math.sqrt(axisX * axisX + axisY * axisY + axisZ * axisZ);
//	// Normalize the rotation vector if it's big enough to get the
//	// axis
//	// if (omegaMagnitude > EPSILON) {
//	// axisX /= omegaMagnitude;
//	// axisY /= omegaMagnitude;
//	// axisZ /= omegaMagnitude;
//	// }
//	// Integrate around this axis with the angular speed by the
//	// timestep
//	// in order to get a delta rotation from this sample over the
//	// timestep
//	// We will convert this axis-angle representation of the delta
//	// rotation
//	// into a quaternion before turning it into the rotation matrix.
//	float thetaOverTwo = omegaMagnitude * dT / 2.0f;
//	float sinThetaOverTwo = (float) Math.sin(thetaOverTwo);
//	float cosThetaOverTwo = (float) Math.cos(thetaOverTwo);
//	deltaRotationVector[0] = sinThetaOverTwo * axisX;
//	deltaRotationVector[1] = sinThetaOverTwo * axisY;
//	deltaRotationVector[2] = sinThetaOverTwo * axisZ;
//	deltaRotationVector[3] = cosThetaOverTwo;
//}
//gyro_timestamp = event.timestamp;
//float[] deltaRotationMatrix = new float[9];
//SensorManager.getRotationMatrixFromVector(deltaRotationMatrix, deltaRotationVector);
//// User code should concatenate the delta rotation we computed with
//// the current rotation
//// in order to get the updated rotation.
//// rotationCurrent = rotationCurrent * deltaRotationMatrix;
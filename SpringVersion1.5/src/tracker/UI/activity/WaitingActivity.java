package tracker.UI.activity;

import tracker.UI.fragment.TaskPanel;
import tracker.springversion1.MobileStudyApplication;
import tracker.springversion1.R;
import tracker.springversion1.R.id;
import tracker.springversion1.R.layout;
import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


public class WaitingActivity extends Activity {
	
	long deadline = -1;
	private Handler handler;
	public static final String deadlineKey = "deadline";
	private long HANDLER_DELAY = 1000*60*5;//1 minutes
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.waiting_act);
		
		handler = new Handler();
		Application app = this.getApplication();
		if(app instanceof MobileStudyApplication){
			deadline = ((MobileStudyApplication)app).getWakeUpTime();
		}
	}
	@Override
	protected void onResume (){
		super.onResume();
		this.handler.post(new TimeProcess()); 
	}
	@Override
	protected void onPause (){
		super.onPause();
		handler.removeCallbacks(new TimeProcess());
	}
	
	private class TimeProcess implements Runnable{
		@Override
		public void run() { 
			checkTime();
			handler.postDelayed(this, HANDLER_DELAY);
		} 
	}
	
	private boolean checkTime(){
		long current = System.currentTimeMillis();
		long diff = deadline - current; 
		if(diff>0){
			long min = diff/1000/60;
			TextView label = (TextView)this.findViewById(R.id.wait_act_label);
			if(min<1){
				label.setText("Please wait for less than 1 min");
			}else{
				label.setText("Please wait for around "+min+" mins");
			}
			return false;
		}else{ 
			TextView label = (TextView)this.findViewById(R.id.wait_act_label);
			label.setText("You can preceed. Please press Continue");
			this.findViewById(R.id.wait_act_back).setVisibility(View.VISIBLE);
			return true;
		}
	}
	
	public void clickme(View v){
		Intent intent = new Intent(this,ExperimentActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		this.startActivity(intent);
		this.finish();
	}
}

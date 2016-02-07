package tracker.UI.activity;

import tracker.springversion1.Common;
import tracker.springversion1.MobileStudyApplication;
import tracker.springversion1.R;
import tracker.springversion1.R.layout;
import android.os.Bundle;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.view.Menu;
import android.widget.TextView;

public class PaymentActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_payment);
		
		if(((MobileStudyApplication)this.getApplication()).getUserInfo().allDataUploaded >= 1){
			TextView tv = (TextView)this.findViewById(R.id.payment_message);
			tv.setText("All tasks have been finished.\nAll data haven been uploaded.\nYou can now delete the application.\nThanks join this experiment.");
		}
	}

	@Override
	protected void onResume(){
		super.onResume();
    	NotificationManager mNotificationManager =
    		    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    	mNotificationManager.cancel(Common.AllFinishedNoticationId);
	}
}

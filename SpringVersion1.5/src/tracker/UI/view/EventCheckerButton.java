package tracker.UI.view;

import tracker.UI.activity.FrameActivity;
import tracker.springversion1.MobileStudyApplication;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Button;

public class EventCheckerButton extends EventRecordedButton {

	public EventCheckerButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	public EventCheckerButton(Context context, AttributeSet attrs ) {
		super(context, attrs);
	}
	public EventCheckerButton(Context context ) {
		super(context);
	}
	
	public boolean performClick (){
		boolean result = super.performClick();
		if(this.getContext() instanceof FrameActivity){
			MobileStudyApplication app = (MobileStudyApplication)((FrameActivity)this.getContext()).getApplication();
			app.requestVerification(this.getId(), "onclicked");
		}else{
			Log.v("mark","context not DrawerActivity:"+getContext().toString());
		}
		return result;
	}
}

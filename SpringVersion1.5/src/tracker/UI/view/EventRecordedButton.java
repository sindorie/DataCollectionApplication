package tracker.UI.view;

import tracker.UI.activity.FrameActivity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Button;


public class EventRecordedButton extends Button {
	public EventRecordedButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	public EventRecordedButton(Context context, AttributeSet attrs ) {
		super(context, attrs);
	}
	public EventRecordedButton(Context context ) {
		super(context);
	}
	@Override
	public boolean onTouchEvent (MotionEvent event){
		Context con = this.getContext();
		if(con instanceof FrameActivity){
			((FrameActivity)con).recordViewTouchEvent(event, this);
		}else{
			Log.v("mark","EventRecordedButton context not frameActivity");
		}
		return super.onTouchEvent(event);
	}
}

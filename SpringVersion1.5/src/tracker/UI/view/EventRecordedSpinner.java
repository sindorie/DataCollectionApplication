package tracker.UI.view;

import tracker.UI.activity.FrameActivity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Spinner;

public class EventRecordedSpinner extends Spinner {

	public EventRecordedSpinner(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public EventRecordedSpinner(Context context, AttributeSet attrs, int defStyle, int mode) {
		super(context, attrs, defStyle, mode);
		// TODO Auto-generated constructor stub
	}

	public EventRecordedSpinner(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public EventRecordedSpinner(Context context) {
		super(context);
	}

	public EventRecordedSpinner(Context context, int mode) {
		super(context, mode);
	}
	
	@Override
	public boolean onTouchEvent (MotionEvent event){
		Context con = this.getContext();
		if(con instanceof FrameActivity){
			((FrameActivity)con).recordViewTouchEvent(event, this);
		}
		return super.onTouchEvent(event);
	}
}

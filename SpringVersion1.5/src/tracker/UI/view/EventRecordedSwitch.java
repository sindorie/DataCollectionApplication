package tracker.UI.view;

import tracker.UI.activity.FrameActivity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Switch;

public class EventRecordedSwitch extends Switch {

	public EventRecordedSwitch(Context context) {
		super(context);
	}

	public EventRecordedSwitch(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public EventRecordedSwitch(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
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
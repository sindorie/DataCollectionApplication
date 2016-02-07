package tracker.UI.view;

import tracker.UI.activity.FrameActivity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.EditText;

public class EventRecordedEditText extends EditText {
	public EventRecordedEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public EventRecordedEditText(Context context) {
		super(context);
	}

	public EventRecordedEditText(Context context, AttributeSet attrs, int arg3) {
		super(context,attrs,arg3);
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

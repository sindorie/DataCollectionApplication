package tracker.UI.view;

import tracker.UI.activity.FrameActivity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

public class EventRecordedImageView extends ImageView {

	public EventRecordedImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public EventRecordedImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public EventRecordedImageView(Context context) {
		super(context);
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

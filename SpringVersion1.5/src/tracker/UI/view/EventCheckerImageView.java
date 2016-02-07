package tracker.UI.view;

import tracker.UI.activity.FrameActivity;
import tracker.springversion1.Common;
import tracker.springversion1.MobileStudyApplication;
import tracker.springversion1.Common.RequestIdMultiCast;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

public class EventCheckerImageView extends EventRecordedImageView {

	public EventCheckerImageView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);  
	}

	public EventCheckerImageView(Context context, AttributeSet attrs ) {
		super(context, attrs ); 
	}
	
	public EventCheckerImageView(Context context ) {
		super(context ); 
	}
	
	public boolean performClick (){
		if(this.getContext() instanceof FrameActivity){
			MobileStudyApplication app = (MobileStudyApplication)((FrameActivity)this.getContext()).getApplication();
			String tag = this.getTag().toString();
			String parts[] = tag.split("/");
			app.requestVerification(Common.RequestIdMultiCast.TOUCHIMAGE, "onclicked",parts[parts.length-1]);
		}else if (this.getContext() instanceof MobileStudyApplication){
			MobileStudyApplication app = (MobileStudyApplication)this.getContext();
			String tag = this.getTag().toString();
			String parts[] = tag.split("/");
			app.requestVerification(Common.RequestIdMultiCast.TOUCHIMAGE, "onclicked",parts[parts.length-1]);
		}else{
			Log.v("mark","context:"+getContext().toString());
		}
		return super.performClick();
	}
}

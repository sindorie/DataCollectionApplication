package tracker.UI.view;

import tracker.springversion1.ProjectInterface;
import tracker.springversion1.ProjectInterface.ActivityInterface;
import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.EditText;

public class RegisteredEditText extends EventRecordedEditText {

	public RegisteredEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		if(context instanceof ProjectInterface.ActivityInterface){
			int id = this.getId();
			if(id>=0){
				((ProjectInterface.ActivityInterface)context).registerEditText(this);
			}
		}
	}

	public RegisteredEditText(Context context) {
		super(context);
		if(context instanceof ProjectInterface.ActivityInterface){
			int id = this.getId();
			if(id>=0){
				((ProjectInterface.ActivityInterface)context).registerEditText(this);
			}
		}
	}

	public RegisteredEditText(Context context, AttributeSet attrs, int arg3) {
		super(context,attrs,arg3);
		if(context instanceof ProjectInterface.ActivityInterface){
			int id = this.getId();
			if(id>=0){
				((ProjectInterface.ActivityInterface)context).registerEditText(this);
			}
		}
	}
}

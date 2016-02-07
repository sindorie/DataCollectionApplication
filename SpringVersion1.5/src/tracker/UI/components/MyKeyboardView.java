package tracker.UI.components;

import java.util.Arrays;
import java.util.List;
import tracker.UI.activity.ExperimentActivity;
import android.content.Context;
import android.inputmethodservice.Keyboard.Key;
import android.inputmethodservice.KeyboardView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;

public class MyKeyboardView extends KeyboardView {
	ExperimentActivity act;
	public MyKeyboardView(Context context, AttributeSet attrs) {
		super(context, attrs);
		if(context instanceof ExperimentActivity){
			act = (ExperimentActivity)context;
		}
	}
	public MyKeyboardView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		if(context instanceof ExperimentActivity){
			act = (ExperimentActivity)context;
		}
	}
	public boolean dispatchTouchEvent (MotionEvent event){
		act.recordKeyboardEvent(event, this);
		
//		int index = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
//	    float x = event.getX(index);
//	    float y = event.getY(index);
//
//	    List<Key> keys = this.getKeyboard().getKeys();
//	    String keycodes = whichKey(keys,x,y);
//	    if(keycodes != null) act.recordKeyboardEvent(event, keycodes);
	    
	    return super.dispatchTouchEvent(event);
	}
	public String whichKey(float x, float y){
		for(Key k : this.getKeyboard().getKeys()){
			if(k.isInside((int)x, (int)y)){
				return Arrays.toString(k.codes);
			}
		}
		return null;
	}
}

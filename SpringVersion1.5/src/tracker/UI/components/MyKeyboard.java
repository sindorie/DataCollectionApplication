package tracker.UI.components;
 
 
import java.util.Arrays;
import tracker.springversion1.ProjectInterface;
import tracker.springversion1.R;
import tracker.springversion1.ProjectInterface.ActivityInterface;
import tracker.springversion1.R.integer;
import android.app.Activity;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.os.SystemClock;
import android.text.Editable;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class MyKeyboard {
	private KeyboardView board;
	private Activity act;
	Keyboard normal_keyboard;
	Keyboard capital_keyboard;
	Keyboard capital_once_keyboard ;
	Keyboard symbol_keyboard ;
	
	private OnKeyboardActionListener mOnKeyboardActionListener = new OnKeyboardActionListener() {

		public final static int CodeDelete = -5; // Keyboard.KEYCODE_DELETE
		public final static int CodeCancel = -3; // Keyboard.KEYCODE_CANCEL

		public final static int TO_ALPH_NORMAL = -11;
		public final static int TO_ALPH_CAP_once = -12;
		public final static int TO_ALPH_CAP = -13;
		public final static int TO_ALPH_NEED_DET = -14;
		
		public final static int TO_SYMBOL = -16;
		
		private int aplh_state = R.integer.keyboard_normal;

		private boolean isAlpah = true;
		
		@Override
		public void onKey(int primaryCode, int[] keyCodes) {
			Log.v("mark","onKey:"+primaryCode+"  "+Arrays.toString(keyCodes));
			
			View focusCurrent = act.getWindow().getCurrentFocus();
			if (focusCurrent == null||!(focusCurrent instanceof  EditText)){
				return;
			}
				
			EditText edittext = (EditText) focusCurrent;
			Editable editable = edittext.getText();
			int start = edittext.getSelectionEnd(); 
			Log.v("mark","primaryCode:"+primaryCode);
			// Apply the key to the edittext
			if (primaryCode == CodeCancel) {
				hideCustomKeyboard();
			} else if (primaryCode == CodeDelete) {
				Log.v("mark","deleting with primaryCode:"+primaryCode+" with start:"+start);
				if (editable != null && start > 0){
					editable.delete(start - 1, start);
				}
			} else if(primaryCode == 10){
				hideCustomKeyboard();
			} else { // insert character
				Log.v("mark","inserting with primaryCode:"+primaryCode);
				editable.insert(start, Character.toString((char) primaryCode));
			}
			
			
			View fv= act.getCurrentFocus();
			if(fv == null){
				Log.v("mark","no focused");
			}else{
				Log.v("mark",""+fv.getId());
			}
		} 
		private long pre_press_time = -1;
		
		@Override public void onPress(int primaryCode) { 
			Log.v("mark","onPress with "+primaryCode+" at "+SystemClock.uptimeMillis());
			
		} 
		@Override public void onRelease(int primaryCode) { 
			Log.v("mark","onRelease with "+primaryCode+" at "+SystemClock.uptimeMillis());
//			if(isAlpah && this.aplh_state == R.integer.keyboard_capital_once){
//				board.setKeyboard(normal_keyboard);
//				aplh_state = R.integer.keyboard_normal;
//			}else if(this.aplh_state == R.integer.keyboard_capital_once){
//				aplh_state = R.integer.keyboard_normal;
//			}
			
			
			
			
			long current_time = System.currentTimeMillis();
			boolean pressTwice = (current_time - pre_press_time < 200 );
			switch(primaryCode){
			case TO_ALPH_NORMAL:{	
				board.setKeyboard(normal_keyboard);
				aplh_state = R.integer.keyboard_normal;
				isAlpah = true;
			}break;
			case TO_ALPH_CAP_once:{
				board.setKeyboard(capital_once_keyboard);
				aplh_state = R.integer.keyboard_capital_once;
				isAlpah = true;
			}break;
			case TO_ALPH_CAP:{

			}break;
			case TO_ALPH_NEED_DET:{
				if(pressTwice){ board.setKeyboard(capital_keyboard);
					aplh_state = R.integer.keyboard_capital;
				}else{
					board.setKeyboard(normal_keyboard);
					aplh_state = R.integer.keyboard_normal;
				}
				isAlpah = true;
			}break;
			case TO_SYMBOL:{
				board.setKeyboard(symbol_keyboard);
				isAlpah = false;
			}break;
			}
			
			pre_press_time = current_time; //could move to onRelease?
		}
		@Override public void onText(CharSequence text) { } 
		@Override public void swipeDown() { } 
		@Override public void swipeLeft() { } 
		@Override public void swipeRight() { } 
		@Override public void swipeUp() { }

	};

	public MyKeyboard(Activity host, int viewid, int layoutid) {
		act = host;
		board = (KeyboardView) act.findViewById(viewid); 
		normal_keyboard = new Keyboard(act, layoutid,R.integer.keyboard_normal);
		capital_keyboard = new Keyboard(act, layoutid,R.integer.keyboard_capital);
		capital_once_keyboard = new Keyboard(act, layoutid,R.integer.keyboard_capital_once);
		symbol_keyboard = new Keyboard(act, layoutid,R.integer.keyboard_symbol);
	

		
//		board.setOnKeyListener(new View.OnKeyListener() {
//			@Override
//			public boolean onKey(View v, int keyCode, KeyEvent event) {
//				Log.v("mark","MyKeyboard OnKeyListener:"+event);
//				return false;
//			}
//		});
//		board.setOnClickListener(new View.OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				Log.v("mark","MyKeyboard OnClickListener:");
//			}
//		});
//		
//		board.setOnTouchListener(new View.OnTouchListener() {
//			@Override
//			public boolean onTouch(View v, MotionEvent event) {
//				int index = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
//				Log.v("mark","MyKeyboard onTouch:"+event+","+event.getPressure()
//						+","+event.getOrientation(index));
//				return false;
//			}
//		});
		

		
		board.setKeyboard(normal_keyboard);
		board.setPreviewEnabled(false); 
		board.setOnKeyboardActionListener(mOnKeyboardActionListener);
		act.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	}

	public boolean isCustomKeyboardVisible() {
		return board.getVisibility() == View.VISIBLE;
	}
	public void showCustomKeyboard(View v) { 
//		if(act instanceof ProjectInterface.ActivityInterface){
//			((ProjectInterface.ActivityInterface)act).arrangeKeyboardSpace(true);
//		}
		if(act instanceof ProjectInterface.ActivityInterface){
			((ProjectInterface.ActivityInterface)act).openKeyBoardCallBack();
		}
		board.setVisibility(View.VISIBLE);
		board.setEnabled(true);
		if (v != null)
			((InputMethodManager) act
					.getSystemService(Activity.INPUT_METHOD_SERVICE))
					.hideSoftInputFromWindow(v.getWindowToken(), 0);
	} 
	public void hideCustomKeyboard() { 
//		if(act instanceof ProjectInterface.ActivityInterface){
//			((ProjectInterface.ActivityInterface)act).arrangeKeyboardSpace(false);
//		}
		if(this.isCustomKeyboardVisible()){
			if(act instanceof ProjectInterface.ActivityInterface){
				((ProjectInterface.ActivityInterface)act).closeKeyBoardCallBack();
			}
			board.setVisibility(View.GONE);
			board.setEnabled(false);
		}
	}


	public void registerEditText(final EditText text) {
		text.setOnFocusChangeListener(new OnFocusChangeListener() { 
			@Override
			public void onFocusChange(View v, boolean hasFocus) { 
				if (hasFocus){
					showCustomKeyboard(v);
					text.setSelection(text.getText().length());
					text.getText();
				}
				else hideCustomKeyboard();
			}
		});
		text.setOnClickListener(new OnClickListener() { 
			@Override
			public void onClick(View v) {
				showCustomKeyboard(v);
				text.setSelection(text.getText().length());
				text.getText();
			}
		}); 
		text.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
//				Log.v("mark","onTouch");
//				EditText edittext = (EditText) v;
//				int pos = edittext.getSelectionStart();
				int inType = text.getInputType(); // Backup the input type
				text.setInputType(InputType.TYPE_NULL); // Disable standard
															// keyboard

				
				text.onTouchEvent(event); // Call native handler
				text.setInputType(inType); // Restore input type
//				edittext.setSelection(pos);
				return true; // Consume touch event
			}
		});
		
		
		// Disable spell check (hex strings look like words to Android)
//		text.setInputType(text.getInputType()
//				| InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
	}
	
	private KeyboardView getView(){
		return this.board;
	}
}

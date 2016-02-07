package tracker.springversion1;

import tracker.UI.components.MyArrayListAdapter;
import tracker.UI.fragment.Bank.BankAccount;
import tracker.UI.fragment.Contact.ContactInfomation;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;

public interface ProjectInterface {
	public interface ContactInterface{
		public void contactListItemOnClickCallBack(AdapterView<?> arg0, View arg1, int position,
				long arg3);
		public void contactDetailSubmitCallback(View v, int _id, String first,
				String last,String address, String phone,String email);
	
		public void contactAddContactOnClickCallBack();
	}
	
	public interface GalleryInterface {
		public void galleryListItemClickCallBack(String target);
		public void galleryWaterFallItemClickCallBack(String path, String name);
	}

	public interface FoodieInterface{
		public void foodieFrontListItemOnClickCallBack(AdapterView<?> arg0, View arg1, int arg2,long arg3);
		public void foodieDetailButtonOnClickCallBack(String message, int rate, int rest_id);
	}
	
	public interface BankInterface{
//		public MyArrayListAdapter<BankAccount> bankGetAccountAdapter();
		
		
		
		
//		public void bankFrontListItemOnClickCallBack(AdapterView<?> arg0, View arg1, int arg2,long arg3);
	}
	
	public interface SocialMediaInterface{
		public void socialGridOnItemClickCallBack(int id);
		public void socialNewsItemOnClickCallBack(int id, int liked);
		public void socialNewsGoCommentCallBack(int _id);
		public void socialNewsCommentLikeCallBack(int _id, int liked);
		public void socialNewsCommentSubmitCallBack(int news_id, String first, String second, String photo,String comment,int amount);
		public void socialNewsAddStatus(String message);
		public void socialNewsOnStatusClickedCallBack();
		public void socialFriendAddCallBack(int position,int _id, int isfriend);
		public void socialMessageSendCallBack(int conversation_id, String type);
		public void socialMessageRecordCallBack(int conversation_id, String message, long time);
		public int socialConversationCreationCallBack(int friend_id, String message, long time);
		public void socialPhotoCommentSubmitCallBack(int _id,String message);
		public void socialPhotoOnClickCallBack(AdapterView<?> arg0, View arg1,int arg2, long arg3);
	}
	
	public interface NewsInterface{
		public void newsFrontElementOnClickedCallBack(int news_id);
		public void newsDetailCommentSubmitCallBack(int _id, String message);
	}
	
	public interface FragmentInterface{
		public boolean actionOnBackPressed();
	}
	
//	public interface FragmentRecordInterface{
//		public String getMajorViewString();
//	}
	
	public interface ApplicationInterface{
//		public Point getWindowDimension();
//		public void setWindowDimension(int h, int w);
	}
	
	public interface ActivityInterface{
		public void openKeyBoardCallBack();
		public void closeKeyBoardCallBack();
		public SQLiteDatabase getDatabaseReferenceFromActivity();
//		public void arrangeKeyboardSpace(boolean toshowKeyboard);
		public void registerEditText(EditText text);
	}
}

package tracker.UI.activity;

import tracker.UI.fragment.Foodie;
import tracker.UI.fragment.Gallery;
import tracker.UI.fragment.News;
import tracker.UI.fragment.SocialMedia;
import tracker.UI.fragment.TaskPanel;
import tracker.UI.fragment.Contact.ContactDetail;
import tracker.UI.fragment.Contact.ContactInfomation;
import tracker.UI.fragment.Foodie.FoodieRestaurant;
import tracker.UI.fragment.Foodie.FoodieRestaurantDetail;
import tracker.UI.fragment.Gallery.GalleryDemoFragment;
import tracker.UI.fragment.Gallery.GalleryWaterFallFragment;
import tracker.UI.fragment.News.NewsDetail;
import tracker.UI.fragment.SocialMedia.SocialFriendFragment;
import tracker.UI.fragment.SocialMedia.SocialMessageFragment;
import tracker.UI.fragment.SocialMedia.SocialMessageSendingFragment;
import tracker.UI.fragment.SocialMedia.SocialNewsAddStatus;
import tracker.UI.fragment.SocialMedia.SocialNewsFeedDetail;
import tracker.UI.fragment.SocialMedia.SocialNewsFeedListFragment;
import tracker.UI.fragment.SocialMedia.SocialPhotoFragment;
import tracker.UI.fragment.SocialMedia.SocialPhotoPageFragment;
import tracker.UI.fragment.TaskPanel.TaskPanelFragment;
import tracker.springversion1.Common;
import tracker.springversion1.ProjectInterface;
import tracker.springversion1.R;
import tracker.springversion1.Common.Tables;
import tracker.springversion1.Common.Tables.Contact;
import tracker.springversion1.Common.Tables.Conversation;
import tracker.springversion1.Common.Tables.ConversationDetail;
import tracker.springversion1.Common.Tables.Friend;
import tracker.springversion1.Common.Tables.NewsArticle;
import tracker.springversion1.Common.Tables.NewsComment;
import tracker.springversion1.Common.Tables.NewsFeed;
import tracker.springversion1.Common.Tables.Review;
import tracker.springversion1.Common.Tables.SocialPhoto;
import tracker.springversion1.ProjectInterface.BankInterface;
import tracker.springversion1.ProjectInterface.ContactInterface;
import tracker.springversion1.ProjectInterface.FoodieInterface;
import tracker.springversion1.ProjectInterface.GalleryInterface;
import tracker.springversion1.ProjectInterface.NewsInterface;
import tracker.springversion1.ProjectInterface.SocialMediaInterface;
import tracker.springversion1.R.drawable;
import tracker.utility.Utility;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

public class ExperimentActivity extends FrameActivity implements
									ProjectInterface.ContactInterface,
									ProjectInterface.FoodieInterface,
									ProjectInterface.GalleryInterface,
									ProjectInterface.BankInterface,
									ProjectInterface.SocialMediaInterface,
									ProjectInterface.NewsInterface{

	private FragmentManager manager;
	public final static String needShowLeftPanel = "nslp";
	
	@Override
	public void setCenterContent(Bundle savedInstanceState) {
		manager = getFragmentManager();
		if (savedInstanceState == null) {
			Fragment fragment = new TaskPanel.TaskPanelFragment();
			this.replaceTheCenterView(fragment,"TaskPanelFragment");
			Bundle extras = getIntent().getExtras();
			if (extras != null) {
				boolean toshowLeft = extras.getBoolean(needShowLeftPanel, false);
				if (toshowLeft) {this.openLeftDrawer();}
			}
		}
	}
	
	public boolean needResizeOnKeyboardShowUp() {
		int count = manager.getBackStackEntryCount();
		return count > 1;//only the first(Taskpanel) do not resize
	}

	public String getTaskIconPattern() {
		Fragment tofind = manager.findFragmentByTag("TaskPanelFragment");
		if (tofind == null) return null;
		return ((TaskPanel.TaskPanelFragment) tofind).getTaskIconMap();
	}

	/**********Interface Starts************/
	
	/** ContactInterface  **/
	@Override
	public void contactListItemOnClickCallBack(AdapterView<?> arg0, View arg1,
			int position, long arg3) {
		Object o = arg0.getItemAtPosition(position);
		if (o instanceof ContactInfomation) {
			this.innerkeyboard.hideCustomKeyboard();
			ContactInfomation reference = (ContactInfomation) o;
			tracker.UI.fragment.Contact.ContactDetail detail = new tracker.UI.fragment.Contact.ContactDetail(); 
			Bundle arg = new Bundle();
			arg.putInt(tracker.UI.fragment.Contact.ContactDetail.KEYID,reference._id);
			detail.setArguments(arg);
			this.replaceTheCenterView(detail, "level1"); 
		} else {
			Log.v("mark", "contact item call back, item not ContactInfomation:"
					+ o.getClass());
		}
	}
	@Override
	public void contactDetailSubmitCallback(View v, int _id, String first, String last,
			String address, String phone,String email) {
		this.innerkeyboard.hideCustomKeyboard();
		SQLiteDatabase db = this.getDatabaseReferenceFromActivity();
		
		db.beginTransaction();
		ContentValues values = new ContentValues();
		values.put(Common.Tables.Contact.first, first);
		values.put(Common.Tables.Contact.last, last);
		values.put(Common.Tables.Contact.address, address);
		values.put(Common.Tables.Contact.phone, phone);
		values.put(Common.Tables.Contact.email, email);
		if (_id >= 0) {
			String whereClause = Common.Tables.Contact._id + " = ?";
			String[] whereArgs = { _id + "" };
			values.put("new", 1);
			db.update(Common.Tables.CONTACT, values, whereClause, whereArgs);
		} else {
			//check existance first
			String selection = Common.Tables.Contact.first + " = ? AND "+Common.Tables.Contact.last+" = ?";
			String[] selectionArgs = new String[]{first,last};
			Cursor query = db.query(Common.Tables.CONTACT, null, selection, selectionArgs, null, null, null);
			
			if(query.moveToFirst()){
				int index = query.getInt(query.getColumnIndex(Common.Tables.Contact._id));
				query.close();
				//update 
				values.put("new",1);
				long result = db.update(Common.Tables.CONTACT, values, Common.Tables.Contact._id +"=?", new String[]{index+""});
				if(result < 0){
					Log.v("mark","contactDetailSubmitCallback add new contact result < 0");
				}
			}else{
				query.close();
				values.put(Common.Tables.Contact.photo, " ");

				long index = db.insert(Common.Tables.CONTACT, null, values);
				Log.v("mark","contactDetailSubmitCallback insert with id:"+index);
			}
		}
		db.setTransactionSuccessful();
		db.endTransaction();
		this.closeKeyBoard();
		this.onBackPressed();
	}

	@Override
	public void contactAddContactOnClickCallBack() {
		tracker.UI.fragment.Contact.ContactDetail detail = new tracker.UI.fragment.Contact.ContactDetail();
		this.replaceTheCenterView(detail, "level1");
	}
	
	/** Foodie **/
	@Override
	public void foodieFrontListItemOnClickCallBack(AdapterView<?> adapterView,
			View arg1, int position, long id) {
		Foodie.FoodieRestaurantDetail fragment = new Foodie.FoodieRestaurantDetail();
		Bundle arg = new Bundle();
		Object o = adapterView.getAdapter().getItem(position);
		if (o instanceof Foodie.FoodieRestaurant) {
			int rest_id = ((Foodie.FoodieRestaurant) o).id;
			Log.v("mark","rest_id:"+rest_id+"  position:"+position);
			arg.putInt(Foodie.FoodieRestaurantDetail.REST_ID, rest_id);
		} else { Log.v("mark", "not FoodieRestaurant class"); }
		fragment.setArguments(arg);
		this.replaceTheCenterView(fragment,"level1");
	}

	@Override
	public void foodieDetailButtonOnClickCallBack(String message, int rate,
			int rest_id) {
		// TODO Auto-generated method stub
		ContentValues values = new ContentValues();
		values.put(Common.Tables.Review.username, Foodie.userName);
		values.put(Common.Tables.Review.rest_id, rest_id);
		values.put(Common.Tables.Review.stars, rate);
		values.put(Common.Tables.Review.photo, Foodie.userPhoto);
		values.put(Common.Tables.Review.comment, message);
		this.getDatabaseReferenceFromActivity().insert(Common.Tables.REVIEW,
				null, values);
	}
	
	
	/***Gallery***/
	@Override
	public void galleryListItemClickCallBack(String target) {
		String targetFolder = "tn_gallery" + "/" + target;
		Bundle argument = new Bundle();
		argument.putString(Gallery.GalleryWaterFallFragment.PATHKEY, targetFolder);
		Gallery.GalleryWaterFallFragment fragment = new Gallery.GalleryWaterFallFragment();
		fragment.setArguments(argument);
		this.replaceTheCenterView(fragment, "level1");
	}

	@Override
	public void galleryWaterFallItemClickCallBack(String path, String name) {
		Gallery.GalleryDemoFragment fragment = new Gallery.GalleryDemoFragment();
		Bundle bundle = new Bundle();
		bundle.putString(Gallery.GalleryDemoFragment.FOLDERKEY, path);
		bundle.putString(Gallery.GalleryDemoFragment.SELECTEDKEY, name);
		fragment.setArguments(bundle);
		this.replaceTheCenterView(fragment, "level2");
	}

	
	/** Social media**/
	@Override
	public void socialGridOnItemClickCallBack(int id) {
		switch(id){
		case R.drawable.icon_news:{
			SocialNewsFeedListFragment fragment = new SocialNewsFeedListFragment();
			this.replaceTheCenterView(fragment, "level1");
		}break;
		case R.drawable.icon_friends:{
			SocialFriendFragment fragment = new SocialFriendFragment();
			this.replaceTheCenterView(fragment, "level1");
		}break;
		case R.drawable.icon_message:{
			SocialMessageFragment fragment = new SocialMessageFragment();
			this.replaceTheCenterView(fragment, "level1");
		}break;
		case R.drawable.icon_photo:{
			SocialPhotoFragment fragment = new SocialPhotoFragment();
			this.replaceTheCenterView(fragment, "level1");
		}break;
		}
	}
	@Override
	public void socialNewsItemOnClickCallBack(int id, int liked) {
		SQLiteDatabase db = this.getDatabaseReferenceFromActivity();
		db.beginTransaction();
		ContentValues content = new ContentValues();
		content.put(Common.Tables.NewsFeed.liked, liked);
		content.put("new", 1);
		String whereClause = Common.Tables.NewsFeed._id + " = ?";
		String[] whereArgs = new String[]{id+""};
		long result = db.update(Common.Tables.NEWSFEED, content, whereClause, whereArgs);
		Log.v("mark","socialNewsItemOnClickCallBack result:"+result);
		db.setTransactionSuccessful();
		db.endTransaction();
	}
	@Override
	public void socialNewsCommentLikeCallBack(int _id,int liked) {
		SQLiteDatabase db = this.getDatabaseReferenceFromActivity();
		db.beginTransaction();
		ContentValues content = new ContentValues();
		content.put(Common.Tables.NewsComment.liked, liked);
		content.put("new", 1);
		String whereClause = Common.Tables.NewsComment._id + " = ?";
		String[] whereArgs = new String[]{_id+""};
		long result = db.update(Common.Tables.NEWSCOMMENT, content, whereClause, whereArgs);
		Log.v("mark","socialNewsCommentLikeCallBack result:"+result);
		db.setTransactionSuccessful();
		db.endTransaction();
	}
	@Override
	public void socialNewsGoCommentCallBack(int _id) {
		SocialNewsFeedDetail fragment = new SocialNewsFeedDetail();
		Bundle arg = new Bundle();
		arg.putInt(SocialNewsFeedDetail.NEWS_ID,_id);
		fragment.setArguments(arg);
		this.replaceTheCenterView(fragment, "level2");
	}

	@Override
	public void socialNewsCommentSubmitCallBack(int news_id, String first,
			String second, String photo,String comment, int amount) {
		
		SQLiteDatabase db = this.getDatabaseReferenceFromActivity();
		db.beginTransaction();
		ContentValues values = new ContentValues();
		values.put(Common.Tables.NewsComment.comment, comment);
		values.put(Common.Tables.NewsComment.first, first);
		values.put(Common.Tables.NewsComment.last, second);
		values.put(Common.Tables.NewsComment.photo, photo);
		values.put(Common.Tables.NewsComment.news_id, news_id);
		values.put(Common.Tables.NewsComment.isuers, 1);
		
		long result = db.insert(Common.Tables.NEWSCOMMENT, null, values);
		Utility.log("socialNewsCommentSubmitCallBack insert:"+result);
		if(result>=0){
			ContentValues content = new ContentValues();
			content.put(Common.Tables.NewsFeed.comment,amount);
			String selection = Common.Tables.NewsFeed._id+" = ?";
			String[] where = new String[]{news_id+""};
			content.put("new", 1);
			long result1 = db.update(Common.Tables.NEWSFEED, content, selection, where);
			Utility.log("socialNewsCommentSubmitCallBack update:"+result1);
		}
		db.setTransactionSuccessful();
		db.endTransaction();
	}
	
	@Override
	public void socialNewsAddStatus(String message) {
		if(message == null){//which means cancel

		}else{
			SQLiteDatabase db = this.getDatabaseReferenceFromActivity();
			db.beginTransaction();
			ContentValues values = new ContentValues();
			values.put(Common.Tables.NewsFeed.isusers, 1);
			values.put(Common.Tables.NewsFeed.first, SocialMedia.userfirst);
			values.put(Common.Tables.NewsFeed.last, SocialMedia.userlast);
			values.put(Common.Tables.NewsFeed.photo, SocialMedia.userphoto);
			values.put(Common.Tables.NewsFeed.status, message);
			long result = db.insert(Common.Tables.NEWSFEED, null, values);
			if(result <=0){
				Log.v("mark","socialNewsAddStatus failure");
			}
			db.setTransactionSuccessful();
			db.endTransaction();
		}
		this.closeKeyBoard();
		this.onBackPressed();
	}
	@Override
	public void socialNewsOnStatusClickedCallBack() {
		SocialNewsAddStatus fragment = new SocialNewsAddStatus();
		this.replaceTheCenterView(fragment, "level2");
	}
	@Override
	public void socialFriendAddCallBack(int position, int _id, int isfriend) {
		// TODO Auto-generated method stub
		//show confirmation
		
		
		//modify database
		SQLiteDatabase db = this.getDatabaseReferenceFromActivity();
		db.beginTransaction();
		String where = Common.Tables.Friend._id+" = ?";
		String[] whereargs = new String[]{_id+""};
		ContentValues values = new ContentValues();
		values.put(Common.Tables.Friend.isfriend, isfriend);
		values.put("new", 1);
		long result = db.update(Common.Tables.FRIEND, values, where, whereargs);
		Utility.log("socialFriendAddCallBack result:"+result);
		db.setTransactionSuccessful();
		db.endTransaction();
	}
	@Override
	public int socialConversationCreationCallBack(int friend_id,
			String message, long time) {
		Log.v("mark","socialConversationCreationCallBack with "+friend_id+", "+message+", "+time);
		SQLiteDatabase db = this.getDatabaseReferenceFromActivity();
		db.beginTransaction();
		int conversation_id = -1;
		//seems to be unnecessary to check if the conversation exists
		//as it should be checked before 
		//create new record
		CONVERSATION_TABLE:{
			ContentValues values = new ContentValues();
			values.put(Common.Tables.Conversation.friend_id, friend_id);
			values.put(Common.Tables.Conversation.hasRead, 1);
			values.put(Common.Tables.Conversation.message, message);
			values.put(Common.Tables.Conversation.time, time);
			long result = db.insert(Common.Tables.CONVERSATION, null, values);
			if(result<=0){
				Log.v("mark","socialConversationCreationCallBack CONVERSATION_TABLE failure");
			}else{
				String selection = Common.Tables.Conversation.friend_id + " = ?";
				String[] selectionArgs = new String[]{""+friend_id};
				Cursor cur_con = db.query(Common.Tables.CONVERSATION, null, selection, selectionArgs, null, null, null);
				if(cur_con.moveToFirst()){
					conversation_id = cur_con.getInt(cur_con.getColumnIndex(Common.Tables.Conversation._id));
					cur_con.close();
				}
			}
		}
		
		if(conversation_id < 0) return -1;
		
		//insert the conversation detail table
		CONVERSATION_DETAIL_TABLE:{
			ContentValues values = new ContentValues();
			values.put(Common.Tables.ConversationDetail.conversation_id, conversation_id);
			values.put(Common.Tables.ConversationDetail.friend_id, -1);//user himself
			values.put(Common.Tables.ConversationDetail.message, message);
			values.put(Common.Tables.ConversationDetail.time, time);
			long result = db.insert(Common.Tables.CONVERSATION_DETAIL, null, values);
			if(result<=0){
				Log.v("mark","socialConversationCreationCallBack CONVERSATION_DETAIL_TABLE failure");
			}
		}
		db.setTransactionSuccessful();
		db.endTransaction();
		Log.v("mark","socialConversationCreationCallBack return "+conversation_id);
		return conversation_id;
	}
	@Override
	public void socialMessageSendCallBack(int id, String type) {
		SocialMessageSendingFragment fragment = new SocialMessageSendingFragment();
		Bundle arg = new Bundle();
		arg.putInt(type,id);
		fragment.setArguments(arg);
		this.replaceTheCenterView(fragment, "level2");
	}
	@Override
	public void socialMessageRecordCallBack(int conversation_id, String message, long time) {
		Log.v("mark","socialMessageRecordCallBack with "+conversation_id+", "+message+", time");
		SQLiteDatabase db = this.getDatabaseReferenceFromActivity();
		db.beginTransaction();
		//update content in the conversation detail
		CONVERSATION_DETAIL_TABLE:{
			ContentValues values = new ContentValues();
			values.put(Common.Tables.ConversationDetail.message, message);
			values.put(Common.Tables.ConversationDetail.conversation_id, conversation_id);
			values.put(Common.Tables.ConversationDetail.time, time);
			values.put(Common.Tables.ConversationDetail.friend_id, -1);
			long result = db.insert(Common.Tables.CONVERSATION_DETAIL,null, values);
			if(result<=0){
				Log.v("mark","socialMessageRecordCallBack CONVERSATION_DETAIL_TABLE failure");
			}
		}
		
		CONVERSATION_TABLE:{
			ContentValues values = new ContentValues();
			values.put(Common.Tables.Conversation.message, message);
			values.put(Common.Tables.Conversation.hasRead, 1);
			values.put(Common.Tables.Conversation.time, time);
			values.put("new", 1);
			String whereClause = Common.Tables.Conversation._id +" = ?";
			String[] whereArgs = new String[]{conversation_id+""};
			long result = db.update(Common.Tables.CONVERSATION, values, whereClause, whereArgs);
			if(result<=0){
				Log.v("mark","socialMessageRecordCallBack CONVERSATION_TABLE failure");
			}
		}
		db.setTransactionSuccessful();
		db.endTransaction();
	}
	@Override
	public void socialPhotoCommentSubmitCallBack(int _id, String message) {
		SQLiteDatabase db = this.getDatabaseReferenceFromActivity();
		db.beginTransaction();
		ContentValues values = new ContentValues();
		values.put(Common.Tables.SocialPhoto.caption, message);
		String whereClause = Common.Tables.SocialPhoto._id + " = ?";
		String[] whereArgs = new String[]{_id+""};
		values.put("new", 1);
		long result = db.update(Common.Tables.SOCIALPHOTO, values, whereClause, whereArgs);
		Log.v("mark","socialPhotoCommentSubmitCallBack result:"+result);
		db.setTransactionSuccessful();
		db.endTransaction();
	}
	@Override
	public void socialPhotoOnClickCallBack(AdapterView<?> arg0, View arg1,
			int position, long arg3) {
		SocialPhotoPageFragment fragment = new SocialPhotoPageFragment();
		Bundle arg = new Bundle();
		arg.putInt(SocialPhotoPageFragment.SELECTED	, position);
		fragment.setArguments(arg);
		this.replaceTheCenterView(fragment, "level3");
	}
	
	/***News***/
	@Override
	public void newsFrontElementOnClickedCallBack(int news_id) {
		NewsDetail fragment = new NewsDetail();
		Bundle arg = new Bundle();
		arg.putInt(NewsDetail.ITEM_ID,news_id);
		fragment.setArguments(arg);
		this.replaceTheCenterView(fragment, "level1");
}
	@Override
	public void newsDetailCommentSubmitCallBack(int _id, String message) {
		SQLiteDatabase db = this.getDatabaseReferenceFromActivity();
		db.beginTransaction();
		ContentValues values = new ContentValues();
		values.put("new", 1);
		values.put(Common.Tables.NewsArticle.comment, message);
		String where = Common.Tables.NewsArticle._id + " = ?";
		String[] arg = new String[]{_id+""};
		long result = db.update(Common.Tables.NEWSARTICLE, values, where, arg);
		if(result < 0){ Log.v("mark","newsDetailCommentSubmitCallBack with id:"+_id+"  attects 0 row"); }
		db.setTransactionSuccessful();
		db.endTransaction();
	}
	
	
}
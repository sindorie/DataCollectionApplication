package tracker.UI.fragment;

import tracker.UI.activity.FrameActivity;
import tracker.UI.components.MyArrayListAdapter;
import tracker.UI.components.MyPageAdapter;
import tracker.springversion1.Common;
import tracker.springversion1.ProjectInterface;
import tracker.springversion1.R;
import tracker.springversion1.Common.Tables;
import tracker.springversion1.Common.Tables.Conversation;
import tracker.springversion1.Common.Tables.ConversationDetail;
import tracker.springversion1.Common.Tables.Friend;
import tracker.springversion1.Common.Tables.NewsComment;
import tracker.springversion1.Common.Tables.NewsFeed;
import tracker.springversion1.Common.Tables.SocialPhoto;
import tracker.springversion1.ProjectInterface.ActivityInterface;
import tracker.springversion1.ProjectInterface.SocialMediaInterface;
import tracker.springversion1.R.array;
import tracker.springversion1.R.drawable;
import tracker.springversion1.R.id;
import tracker.springversion1.R.layout;
import tracker.utility.Utility;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.os.Bundle;
import android.app.FragmentManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.support.v4.view.PagerAdapter;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class SocialMedia{

	private static String imagefolderPath = "figure/";
	private static String socialPhotoFolder = "social/photo/";
	public static String userfirst = "you", userlast=" ",userphoto = "user.jpg";
	
	public static class SocialMediaFragment extends Fragment {
		private String[] titles; 
		private static int[] gridImageId = { R.drawable.icon_news,
			R.drawable.icon_friends, R.drawable.icon_message,
			R.drawable.icon_map, R.drawable.icon_group, R.drawable.icon_events,
			R.drawable.icon_photo, R.drawable.icon_chat, R.drawable.icon_note };
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View ground = inflater.inflate(R.layout.frag_socialgrid, container,false);
			GridView grid = (GridView) ground.findViewById(R.id.social_grid);
			titles = this.getResources().getStringArray(R.array.social_titles);
			PanelItem[] panelItems = new PanelItem[9];
			for (int i = 0; i < 9; i++) {
				String title = titles[i];
				int imageId = gridImageId[i];
				panelItems[i] = new PanelItem(imageId, title);
			}
			MyArrayListAdapter<PanelItem> adapter = new MyArrayListAdapter<PanelItem>(
					this.getActivity(), panelItems);
			grid.setAdapter(adapter);
			grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					Object clicked = arg0.getItemAtPosition(arg2);
					if(clicked instanceof PanelItem){
						PanelItem item = (PanelItem)clicked;
						Activity act = getActivity();
						if(act instanceof ProjectInterface.SocialMediaInterface){
							((ProjectInterface.SocialMediaInterface)act)
								.socialGridOnItemClickCallBack(item.imageId);
						}
					}
				}
			});
			Utility.fitViewToWindow(ground, getActivity());
			return ground;
		}

		@Override
		public String toString(){
			return "SocialMediaFragment";
		}
		
		public class PanelItem implements MyArrayListAdapter.GenerateView {
			int imageId; String imageTitle;
			public PanelItem(int id, String text) {
				imageId = id; imageTitle = text;
			}
			@Override
			public View getView(Activity act, int position, View convertView,
					ViewGroup parent) {
				LayoutInflater inflater = act.getLayoutInflater();
				View ground = inflater.inflate(R.layout.view_social_gridelement,
						parent, false);
				ImageView image = (ImageView) ground .findViewById(R.id.social_grid_image);
				image.setImageResource(imageId);
				TextView text = (TextView) ground .findViewById(R.id.social_grid_text);
				text.setText(imageTitle);
				return ground;
			}
			@Override
			public View getDropDownView(Activity act, int position,
					View convertView, ViewGroup parent) {
				return null;
			}
		}
	}

	public static class SocialNewsFeedListFragment extends Fragment{
		private MyArrayListAdapter<SocialNewsFeedElement> adatper;
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
		}
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			adatper = readNewsFeed();
			View ground = inflater.inflate(R.layout.view_social_newfeedlist, container,false);
			View statusbutton = ground.findViewById(R.id.social_newsfeed_status);
			ListView listview = (ListView)ground.findViewById(R.id.social_newfeed_listcontent); 
			listview.setAdapter(adatper);
			statusbutton.setOnClickListener(onStatusViewClicked);
			Utility.fitViewToWindow(ground, getActivity());
			return ground;
		}
		
		@Override
		public String toString(){
			return "SocialNewsFeedListFragment";
		}

		private MyArrayListAdapter<SocialNewsFeedElement> readNewsFeed(){
			Activity act = this.getActivity();
			if(act instanceof ProjectInterface.ActivityInterface){
				SQLiteDatabase db = ((ProjectInterface.ActivityInterface)act).getDatabaseReferenceFromActivity();
				db.beginTransaction();
				Cursor cur = db.query(Common.Tables.NEWSFEED, null, null, null, null, null, null);
				SocialNewsFeedElement[] newsfeed = new SocialNewsFeedElement[cur.getCount()];
				if(cur.moveToLast()){
					int index = 0;
					do{newsfeed[index] = new SocialNewsFeedElement(cur); index += 1;
					}while(cur.moveToPrevious() );
				}
				db.setTransactionSuccessful();
				db.endTransaction();
				return new MyArrayListAdapter<SocialNewsFeedElement>(act,newsfeed);
			}
			return new MyArrayListAdapter<SocialNewsFeedElement>(act,new SocialNewsFeedElement[0]);
		}
		private View.OnClickListener onStatusViewClicked = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.v("mark","status button clicked");
				Activity act = getActivity();
				if(act instanceof ProjectInterface.SocialMediaInterface){
					((ProjectInterface.SocialMediaInterface)act)
						.socialNewsOnStatusClickedCallBack();
				}
			}
		};
	}
	
	public static class SocialNewsFeedElement implements MyArrayListAdapter.GenerateView{
		int _id, likes,comments,liked,isusers;
		String imageName,status,first,last;
		boolean showComment = true;
		
		public SocialNewsFeedElement(Cursor cur){
			this(
			cur.getInt(cur.getColumnIndex(Common.Tables.NewsFeed._id)),
			cur.getInt(cur.getColumnIndex(Common.Tables.NewsComment.isuers)),
			cur.getString(cur.getColumnIndex(Common.Tables.NewsFeed.first)),
			cur.getString(cur.getColumnIndex(Common.Tables.NewsFeed.last)),
			cur.getString(cur.getColumnIndex(Common.Tables.NewsFeed.photo)),
			cur.getInt(cur.getColumnIndex(Common.Tables.NewsFeed.comment)),
			cur.getInt(cur.getColumnIndex(Common.Tables.NewsFeed.likes)),
			cur.getInt(cur.getColumnIndex(Common.Tables.NewsFeed.liked)),
			cur.getString(cur.getColumnIndex(Common.Tables.NewsFeed.status)));
		}
		public SocialNewsFeedElement(Cursor cur,boolean showComment){
			this(cur);this.showComment = showComment;
		}
		public SocialNewsFeedElement(int id,int isusers, String first, String last, String imageName, int comments,
				int likes, int liked, String status){
			this._id = id;this.isusers=isusers;this.first = first;this.last = last;this.imageName= imageName; 
			this.comments=comments; this.likes=likes;this.liked = liked;this.status=status;
		}
		public SocialNewsFeedElement(int id,int isusers, String first, String last, String imageName, int comments,
				int likes, int liked, String status,boolean showComment){
			this(id,isusers, first, last, imageName, comments, likes, liked, status);
			this.showComment = showComment;
		}
		
		public void populateView(Activity act, View ground){
			TextView nameview = (TextView)ground.findViewById(R.id.social_newscomment_name);
			TextView statusview = (TextView)ground.findViewById(R.id.social_newscomment_status);
			ImageView photoview = (ImageView)ground.findViewById(R.id.social_newscomment_image);
			
			TextView commentAmount = (TextView)ground.findViewById(R.id.social_newsfeed_element_comment);
			TextView upAmount = (TextView)ground.findViewById(R.id.social_newscomment_up);
		
			nameview.setText(first+" "+last);
			statusview.setText(status);
			if(showComment){ commentAmount.setText(comments+"");
			}else{ commentAmount.setVisibility(View.INVISIBLE); }
			upAmount.setText((likes+liked)+"");
			if(Utility.setImage(imagefolderPath+imageName, act.getAssets(), photoview) == false){
				photoview.setImageResource(R.drawable.defaultaccount);
			}
		}
		public void addListener(final Activity act, final View ground){
			TextView upAmount = (TextView)ground.findViewById(R.id.social_newscomment_up);
//			TextView statusview = (TextView)ground.findViewById(R.id.social_newscomment_status);
			TextView commentAmount = (TextView)ground.findViewById(R.id.social_newsfeed_element_comment);
			View.OnClickListener goComment = new View.OnClickListener(){
				@Override
				public void onClick(View v) {
					if(act instanceof ProjectInterface.ActivityInterface){
						((ProjectInterface.SocialMediaInterface)act)
						.socialNewsGoCommentCallBack(_id);
					}
				}
			};
			ground.setOnClickListener(goComment);
			commentAmount.setOnClickListener(goComment);
			upAmount.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					TextView upAmount = (TextView)ground.findViewById(R.id.social_newscomment_up);
					liked = (liked == 0)? 1:0;					
					upAmount.setText((likes+liked)+"");
					if(act instanceof ProjectInterface.ActivityInterface){
						((ProjectInterface.SocialMediaInterface)act).socialNewsItemOnClickCallBack(_id,liked);
					}
				}
			});
		}
		
		@Override
		public View getView(final Activity act, int position, View convertView,
				ViewGroup parent) {
			View ground = act.getLayoutInflater().inflate(R.layout.view_social_newsfeedelement, parent,false);
			populateView(act, ground);
			addListener(act,ground);
			return ground;
		}

		@Override
		public View getDropDownView(Activity act, int position,
				View convertView, ViewGroup parent) {
			return null;
		}
				
	}

	public static class SocialNewsFeedComment implements MyArrayListAdapter.GenerateView{
		int _id, news_id, likes, liked, isusers; 
		String first, last, photo, comment;
		public SocialNewsFeedComment(int _id, int news_id, int isusers, String first, String last, String photo, int likes, int liked, String comment){
			this._id=_id;this.news_id=news_id;this.isusers = isusers;this.first=first;this.last=last;this.photo=photo;this.likes=likes;this.liked=liked;this.comment=comment;
		}
		public SocialNewsFeedComment(int news_id, int isusers, String first, String last,String photo, int likes, int liked, String comment ){
			this(-1,news_id,isusers,first,last,photo,likes,liked,comment);
		}
		
		public SocialNewsFeedComment(Cursor cur){
			this(
			cur.getInt(cur.getColumnIndex(Common.Tables.NewsComment._id)),
			cur.getInt(cur.getColumnIndex(Common.Tables.NewsComment.news_id)),
			cur.getInt(cur.getColumnIndex(Common.Tables.NewsComment.isuers)),
			cur.getString(cur.getColumnIndex(Common.Tables.NewsComment.first)),
			cur.getString(cur.getColumnIndex(Common.Tables.NewsComment.last)),
			cur.getString(cur.getColumnIndex(Common.Tables.NewsComment.photo)),
			cur.getInt(cur.getColumnIndex(Common.Tables.NewsComment.likes)),
			cur.getInt(cur.getColumnIndex(Common.Tables.NewsComment.liked)),
			cur.getString(cur.getColumnIndex(Common.Tables.NewsComment.comment)));
		}
		@Override
		public View getView(final Activity act, int position, View convertView,
				ViewGroup parent) {
			View ground = act.getLayoutInflater().inflate(R.layout.view_social_newscommentelement, parent,false);
			TextView nameview = (TextView)ground.findViewById(R.id.social_newscomment_name);
			TextView commentview = (TextView)ground.findViewById(R.id.social_newscomment_status);
			ImageView photoview = (ImageView)ground.findViewById(R.id.social_newscomment_image);
			TextView upAmount = (TextView)ground.findViewById(R.id.social_newscomment_up);
			
			nameview.setText(first+" "+last);
			upAmount.setText(""+(likes+liked));
			commentview.setText(comment+"");
			if(Utility.setImage(imagefolderPath+photo, act.getAssets(), photoview) == false){
				photoview.setImageResource(R.drawable.defaultaccount);
			}
			
			upAmount.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					TextView upAmount = (TextView)v.findViewById(R.id.social_newscomment_up);
					liked = (liked == 0)? 1:0;
					upAmount.setText(""+(likes+liked));
					if(act instanceof ProjectInterface.ActivityInterface){
						Log.v("mark","comment id:"+_id+" with liked:"+liked);
						((ProjectInterface.SocialMediaInterface)act).socialNewsCommentLikeCallBack(_id,liked);
					}
				}
			});
			return ground;
		}

		@Override
		public View getDropDownView(Activity act, int position,
				View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	
	public static class SocialNewsFeedDetail extends Fragment{
		private SocialNewsFeedElement element;
		private MyArrayListAdapter<SocialNewsFeedComment> adapter;
		public static String NEWS_ID = "news_id";
		private View ground;
		private ScrollView scrollview;
		private LinearLayout linear;
		private LinearLayout.LayoutParams linear_param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		
		@Override
		public void onCreate(Bundle savedInstanceState){
			super.onCreate(savedInstanceState);
			Activity act = this.getActivity();
			Bundle arg = this.getArguments();
			if(arg!=null && arg.containsKey(NEWS_ID)){
				int news_id = arg.getInt(NEWS_ID);
				if(act instanceof ProjectInterface.ActivityInterface){
					SQLiteDatabase db =((ProjectInterface.ActivityInterface)act).getDatabaseReferenceFromActivity();
					db.beginTransaction();
					initNewsFeedElement(db,news_id);initAdapater(db,news_id);
					db.setTransactionSuccessful();
					db.endTransaction();
				}
			}
		}
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			ground = inflater.inflate(R.layout.view_social_newscommentlist, container,false);
			scrollview = (ScrollView) ground.findViewById(R.id.social_newscommentlist_wrapper);
			
			View topground = ground.findViewById(R.id.social_newscommentlist_top_element);
			if(topground != null){
				element.populateView(this.getActivity(), topground);
				topground.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {}
				});
				
				TextView upview = (TextView)topground.findViewById(R.id.social_newscomment_up);
				upview.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						View top = ground.findViewById(R.id.social_newscommentlist_top_element);
						TextView upview = (TextView)top.findViewById(R.id.social_newscomment_up);
						element.liked = (element.liked==0)? 1:0;
						upview.setText(""+(element.likes+element.liked));
						Activity act = getActivity();
						if(act instanceof ProjectInterface.SocialMediaInterface){
							((ProjectInterface.SocialMediaInterface)act).
							socialNewsItemOnClickCallBack(element._id,element.liked);
						}
					}
				});
			}
			
			linear = (LinearLayout)ground.findViewById(R.id.social_newscommentlist_top_linear);
			if(adapter!=null){
				for(int index = 0; index < adapter.getCount();index+= 1){
					linear.addView(adapter.getView(index, null, linear),linear_param);
				}
			}
			View submiteView = ground.findViewById(R.id.social_news_comment_bottom_button);
			submiteView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					//retrieve information
					EditText field = (EditText)ground.findViewById(R.id.social_news_comment_bottom_comment);
					String message = field.getText().toString().trim();
					field.setText("");
					Utility.cloasKeyBoard(getActivity(), field);
					//update adapter
					SocialNewsFeedComment comment = new SocialNewsFeedComment(element._id,
							1,userfirst,userlast,userphoto,0,0,message);
					adapter.addElement(comment);
					
					//update linear view
					View generated = comment.getView(getActivity(), -1, null, linear);
					linear.addView(generated, linear_param);
					scrollview.post(new Runnable() {            
					    @Override
					    public void run() {
					    	scrollview.fullScroll(View.FOCUS_DOWN);              
					    }
					});
					element.comments += 1;
					View topelement = ground.findViewById(R.id.social_newscommentlist_top_element);
					TextView commentAmount = (TextView)topelement.findViewById(R.id.social_newsfeed_element_comment);
					commentAmount.setText(element.comments+"");
					
					//update database
					Activity act = getActivity();
					if(act instanceof ProjectInterface.SocialMediaInterface){
						((ProjectInterface.SocialMediaInterface)act)
						.socialNewsCommentSubmitCallBack(element._id, userfirst, userlast, userphoto,message,element.comments);
					}
				}
			});
			Utility.fitViewToWindow(ground, getActivity());
			return ground;
		}
		@Override
		public String toString(){
			return "SocialNewsFeedDetail";
		}
		
		private void initNewsFeedElement(SQLiteDatabase db, int news_id){
			Cursor cur = Utility.getDataById(db,Common.Tables.NEWSFEED,news_id);
			if(cur.moveToFirst()){ element = new SocialNewsFeedElement(cur);
			}else{ Log.v("mark","SocialNewsFeedDetail: element does not exist in db -- "+news_id);}
		}
		private void initAdapater(SQLiteDatabase db, int news_id){
			Cursor cur = Utility.getDataBySingleSecltion(db,Common.Tables.NEWSCOMMENT,Common.Tables.NewsComment.news_id,news_id);
			SocialNewsFeedComment[] list = new SocialNewsFeedComment[cur.getCount()];
			if(cur.moveToFirst()){
				int index = 0;
				do{ list[index] = new SocialNewsFeedComment(cur);
					index += 1;
				}while(cur.moveToNext());
				adapter = new MyArrayListAdapter<SocialNewsFeedComment>(this.getActivity(),list);
			}else{
				adapter = new MyArrayListAdapter<SocialNewsFeedComment>(this.getActivity(),new SocialNewsFeedComment[0]);
			}
		}
	}

	public static class SocialNewsAddStatus extends Fragment{
		View ground;
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			ground = inflater.inflate(R.layout.frag_social_addstatus, container,false);
			View submiteview = ground.findViewById(R.id.social_news_addstatus_submit);
			View cancel = ground.findViewById(R.id.social_news_addstatus_cancel);
			submiteview.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					EditText field = (EditText)ground.findViewById(R.id.social_news_addstatus_text);
					String message = field.getText().toString().trim();
					Activity act = getActivity();
					if(act instanceof ProjectInterface.SocialMediaInterface){
						((ProjectInterface.SocialMediaInterface)act).socialNewsAddStatus(message);
					}
				}
			});
			cancel.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Activity act = getActivity();
					if(act instanceof ProjectInterface.SocialMediaInterface){
						((ProjectInterface.SocialMediaInterface)act).socialNewsAddStatus(null);
					}
				}
			});
			return ground;
		}
		
		@Override
		public String toString(){
			return "SocialNewsAddStatus";
		}
	}

	public static class SocialFriendFragment extends Fragment{
		private MyArrayListAdapter<SocialFriend> adapter;
		private View ground;
		public void onCreate (Bundle savedInstanceState){
			super.onCreate(savedInstanceState);	
		}
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			ground = inflater.inflate(R.layout.frag_social_friend, container,false);
			EditText searchInput = (EditText)ground.findViewById(R.id.social_friend_search);
			searchInput.addTextChangedListener(watcher);
			updateList(null);
			return ground;
		}
		
		@Override
		public String toString(){
			return "SocialFriendFragment";
		}
		
		private void updateList(String message){
			if(message==null || message.equals("")){
				adapter = getFriend(null,null);
			}else{
				String selection = Common.Tables.Friend.first + " LIKE ?";
				adapter = getFriend(selection,new String[]{message+"%"});
			}
		
			LinearLayout layout = (LinearLayout)ground.findViewById(R.id.social_friendlist_linear);
			layout.removeAllViews();
			LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.MATCH_PARENT,
					LinearLayout.LayoutParams.WRAP_CONTENT
					);
			for(int i=0;i<adapter.getCount();i++){
				View generated = adapter.getView(i, null, null);
				layout.addView(generated,param);
			}
		}
		
		private TextWatcher watcher = new TextWatcher(){
			@Override
			public void afterTextChanged(Editable arg0) {
				String message = arg0.toString().trim();
				Log.v("mark", "message is :" + message);
				updateList(message);
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {}

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {}
		};
		
		private MyArrayListAdapter<SocialFriend> getFriend(String selection, String[] argument){
			Activity act = this.getActivity();
			if(act instanceof ProjectInterface.ActivityInterface){
				SQLiteDatabase db =((ProjectInterface.ActivityInterface)act).getDatabaseReferenceFromActivity();
				db.beginTransaction();
				//Common.Tables.Friend.isfriend
				Cursor cur = db.query(Common.Tables.FRIEND, null, selection, argument, null, null, null);
				SocialFriend[] list = new SocialFriend[cur.getCount()];
				if(cur.moveToFirst()){
					int index = 0;
					do{list[index] = new SocialFriend(cur);
						index += 1;
					}while(cur.moveToNext());
				}
				db.setTransactionSuccessful();
				db.endTransaction();
				return new MyArrayListAdapter<SocialFriend>(this.getActivity(),list);
			}
			return new MyArrayListAdapter<SocialFriend>(this.getActivity(),new SocialFriend[0]);
		}
	}
	
	public static class SocialFriend implements MyArrayListAdapter.GenerateView{
		int _id,isfriend;
		String first,last,photo;
		View ground;
		public SocialFriend(int _id, String first, String last,int isfriend,String photo){
			this._id = _id; this.first = first; this.last=last;this.isfriend=isfriend;this.photo = photo;
		}
		public SocialFriend(Cursor cur){
			this(cur.getInt(cur.getColumnIndex(Common.Tables.Friend._id)),
					cur.getString(cur.getColumnIndex(Common.Tables.Friend.first)),
					cur.getString(cur.getColumnIndex(Common.Tables.Friend.last)),
					cur.getInt(cur.getColumnIndex(Common.Tables.Friend.isfriend)),
					cur.getString(cur.getColumnIndex(Common.Tables.Friend.photo)));
		}
		
		@Override
		public View getView(final Activity act,final int position, View convertView,ViewGroup parent) {
			ground = act.getLayoutInflater().inflate(R.layout.view_social_friend, parent,false);
			ImageView photoview = (ImageView)ground.findViewById(R.id.social_friend_photo);
			TextView firstview = (TextView)ground.findViewById(R.id.social_friend_first);
			TextView lastview = (TextView)ground.findViewById(R.id.social_friend_last);
			View switchcontainer = ground.findViewById(R.id.social_friend_addswitch_container);
			final View notfriendView = switchcontainer.findViewById(R.id.social_friend_addswitch_notfriend);
			View isfriendView = switchcontainer.findViewById(R.id.social_friend_addswitch_isfriend);
			firstview.setText(this.first);
			lastview.setText(this.last);
			if(!Utility.setImage(imagefolderPath+"photo", act.getAssets(),photoview)){
				photoview.setImageResource(R.drawable.defaultaccount);
			}
			ground.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) { 
					if(act instanceof ProjectInterface.SocialMediaInterface){
						((ProjectInterface.SocialMediaInterface)act)
						.socialMessageSendCallBack(_id,SocialMessageSendingFragment.FRIEND_ID);
					}
				}
			});
			if(isfriend == 0){//not friend yet
				notfriendView.setVisibility(View.VISIBLE);
				isfriendView.setVisibility(View.INVISIBLE);
				notfriendView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Log.v("mark","add friend on clicked");
						targetPosition = position;
						targetId = _id;
						tmpact = act;
//						addToFriend();
						AlertDialog dialog = new AlertDialog.Builder(act).setMessage("Add as a friend?")
						        .setCancelable(false)
						        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
						            public void onClick(DialogInterface dialog, int id) {
						            	addToFriend();
						            }
						        })
						        .setNegativeButton("No", new DialogInterface.OnClickListener() {
						            public void onClick(DialogInterface dialog, int id) {
						            	return;
						            }
						        })
						        .show();
						
						View transparent = new View(act);
						transparent.setBackgroundColor(Color.parseColor("#00000000"));
						transparent.setOnTouchListener(new View.OnTouchListener() {
							@Override
							public boolean onTouch(View v, MotionEvent event) {
								Context context = notfriendView.getContext();
								if(context instanceof FrameActivity){
									((FrameActivity)context).recordMotinoData(event);
								}
								return false;
							}
						});
						dialog.addContentView(transparent, new RelativeLayout.LayoutParams(
								ViewGroup.LayoutParams.MATCH_PARENT
								,ViewGroup.LayoutParams.MATCH_PARENT));
					}
				});
			}else{
				notfriendView.setVisibility(View.INVISIBLE);
				isfriendView.setVisibility(View.VISIBLE);
			}
			return ground;
		}
		int targetPosition = -1;
		int targetId = -1;
		Activity tmpact;
		public void addToFriend(){
			isfriend = 1;
			View switchcontainer = ground.findViewById(R.id.social_friend_addswitch_container);
			View notfriendView = switchcontainer.findViewById(R.id.social_friend_addswitch_notfriend);
			View isfriendView = switchcontainer.findViewById(R.id.social_friend_addswitch_isfriend);
			notfriendView.setVisibility(View.INVISIBLE);
			isfriendView.setVisibility(View.VISIBLE);
			if(tmpact instanceof ProjectInterface.SocialMediaInterface){
				((ProjectInterface.SocialMediaInterface)tmpact)
						.socialFriendAddCallBack(targetPosition,targetId, 1);
			}
		}

		@Override
		public View getDropDownView(Activity act, int position,
				View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			return null;
		}
		
	}

	public static class SocialMessageFragment extends Fragment{
		private View ground;
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			ground = inflater.inflate(R.layout.frag_social_messagefront, container,false);
			updateList(); 
			return ground;
		}
		
		@Override
		public String toString(){
			return "SocialMessageFragment";
		}
		
		private void updateList(){
			ListView conversationlist = (ListView)ground.findViewById(R.id.social_messagefront_list);
			MyArrayListAdapter<SocialMessageConversation> adapter = getAdapter();
			conversationlist.setAdapter(adapter);
		}
		private MyArrayListAdapter<SocialMessageConversation> getAdapter(){	
			Activity act = this.getActivity();
			if(act instanceof ProjectInterface.ActivityInterface){
				SQLiteDatabase db = ((ProjectInterface.ActivityInterface)act).getDatabaseReferenceFromActivity();
				db.beginTransaction();
				Cursor cur = db.query(Common.Tables.CONVERSATION, null, null, null, null, null, Common.Tables.Conversation.hasRead);
				SocialMessageConversation[] list = new SocialMessageConversation[cur.getCount()];
				if(cur.moveToFirst()){
					int index = 0;
					do{ list[index] = new SocialMessageConversation(cur);
						index += 1;
					}while(cur.moveToNext());
				}
				db.setTransactionSuccessful();
				db.endTransaction();
				return new MyArrayListAdapter<SocialMessageConversation>(act,list);
				
			}
			return new MyArrayListAdapter<SocialMessageConversation>(act,new SocialMessageConversation[0]);
		}
	}
	
	public static class SocialMessageConversation implements MyArrayListAdapter.GenerateView{
		int _id, friend_id,hasread;
		long time; String message;
		
		public SocialMessageConversation(int _id, int friend_id, int hasRead,long time,String message){
			this._id = _id; this.friend_id = friend_id; this.hasread = hasRead;
			this.time = time; this.message = message;
		}

		public SocialMessageConversation(Cursor cur){
			this(cur.getInt(cur.getColumnIndex(Common.Tables.Conversation._id)),
					cur.getInt(cur.getColumnIndex(Common.Tables.Conversation.friend_id)),
					cur.getInt(cur.getColumnIndex(Common.Tables.Conversation.hasRead)),
					cur.getLong(cur.getColumnIndex(Common.Tables.Conversation.time)),
					cur.getString(cur.getColumnIndex(Common.Tables.Conversation.message)));
		}
		
		@Override
		public View getView(final Activity act, int position, View convertView,
				ViewGroup parent) {
			View ground = act.getLayoutInflater().inflate(R.layout.view_social_messagefrontelement, parent,false);
			
			ImageView imageview = (ImageView)ground.findViewById(R.id.social_messagefrontelement_image);
			TextView nameview = (TextView)ground.findViewById(R.id.social_message_frontelement_name);
			View checkview = ground.findViewById(R.id.social_message_frontelement_check);
			TextView timeview = (TextView)ground.findViewById(R.id.social_message_frontelement_time);
			TextView messageview = (TextView)ground.findViewById(R.id.social_messagefrontelement_message);
			
			if(friend_id>=0){
				SocialFriend friend = getFriendById(friend_id,act);
				if(friend != null){
					nameview.setText(friend.first + " "+friend.last);
					checkview.setVisibility(hasread==1?View.VISIBLE:View.INVISIBLE);
					timeview.setText(Utility.longToTime(time));
					messageview.setText(message);
					if(!Utility.setImage(imagefolderPath+friend.photo, act.getAssets(), imageview)){
						imageview.setImageResource(R.drawable.defaultaccount);
					}
					
					ground.setOnClickListener( new View.OnClickListener(){
						@Override
						public void onClick(View v) {
							if(act instanceof ProjectInterface.SocialMediaInterface){
								((ProjectInterface.SocialMediaInterface)act).socialMessageSendCallBack(_id,SocialMessageSendingFragment.CONVER_ID);
							}
						}
					});
				}
			}
			return ground;
		}
		@Override
		public View getDropDownView(Activity act, int position,
				View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			return null;
		}
		
		private SocialFriend getFriendById(int _id, final Activity act){
			if(act instanceof ProjectInterface.ActivityInterface){
				SQLiteDatabase db =((ProjectInterface.ActivityInterface)act).getDatabaseReferenceFromActivity();				
				db.beginTransaction();
				Cursor cur = Utility.getDataById(db, Common.Tables.FRIEND,_id);
				db.setTransactionSuccessful();
				db.endTransaction();
				if(cur.moveToFirst()){ return new SocialFriend(cur);
				}else{ Utility.log("should not be"); }
			}
			return null;
		}
	}
	
	
	public static class SocialMessageSendingFragment extends Fragment{
		public final static String CONVER_ID = "id";
		public final static String FRIEND_ID = "fid";
		private int conversation_id;
		private int friend_id;
		private boolean isConversationStarted = true;
		private SocialMessageConversationDetail lasttime = null;
		View ground;
		@Override
		public void onCreate (Bundle savedInstanceState){
			super.onCreate(savedInstanceState);
			Bundle arg = this.getArguments();
			if(arg != null){
				if(arg.containsKey(CONVER_ID)){
					conversation_id = arg.getInt(CONVER_ID);
					Log.v("mark","SocialMessageSendingFragment conversation_id:"+conversation_id);
				}else if(arg.containsKey(FRIEND_ID)){
					friend_id = arg.getInt(FRIEND_ID);
					Log.v("mark","SocialMessageSendingFragment friend_id:"+friend_id);
					Activity act = this.getActivity();
					if(act instanceof FrameActivity){
						SQLiteDatabase db = ((FrameActivity)act).getDatabaseReferenceFromActivity();
						db.beginTransaction();
						String selection = Common.Tables.Conversation.friend_id+" = ?";
						String[] selectionArgs = new String[]{""+friend_id};
						Cursor cur = db.query(Common.Tables.CONVERSATION, null, selection,selectionArgs, null, null, null);
						if(cur.moveToFirst()){
							conversation_id = cur.getInt(cur.getColumnIndex(Common.Tables.Conversation._id));
							Log.v("mark","SocialMessageSendingFragment return with: "+conversation_id);
						}else{
							Log.v("mark","SocialMessageSendingFragment cur is empty");
							//conversation not started yet
							isConversationStarted = false;
						}
						cur.close();
						db.setTransactionSuccessful();
						db.endTransaction();
					}
					
				}
			}
		}
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			ground = inflater.inflate(R.layout.frag_social_messagesend, container,false);
			if(isConversationStarted) updatelist();
			View submitbutton = ground.findViewById(R.id.social_messagesend_submit);
			submitbutton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					TextView input = (TextView)ground.findViewById(R.id.social_messagesend_messageinput);
					String message = input.getText().toString().trim();
					long time = System.currentTimeMillis();
					SocialMessageConversationDetail newMessage = new SocialMessageConversationDetail(-1,conversation_id,-1,time,message);
					newMessage.setPrevious(lasttime);//TODO
					//update layout
					
					final ScrollView scroll = (ScrollView)ground.findViewById(R.id.social_messagesend_scroll);
					LinearLayout layout = (LinearLayout)ground.findViewById(R.id.social_messagesend_list);
					View generated = newMessage.getView(getActivity(), -1, null, null);
					LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
							LinearLayout.LayoutParams.MATCH_PARENT,
							LinearLayout.LayoutParams.WRAP_CONTENT);
					layout.addView(generated,param);
					
					scroll.post(new Runnable(){
						@Override
						public void run() {
							scroll.fullScroll(View.FOCUS_DOWN);
						}
					});
					
					Activity act = getActivity();
					if(act instanceof ProjectInterface.SocialMediaInterface){
						if(isConversationStarted){
							((ProjectInterface.SocialMediaInterface)act).socialMessageRecordCallBack(conversation_id,message,time);
						}else{
							conversation_id = ((ProjectInterface.SocialMediaInterface)act).socialConversationCreationCallBack(friend_id, message, time);
							if(conversation_id >= 0){
								isConversationStarted = true;
							}else{
								Log.v("mark","conversation_id is -1");
							}
						}
					}
					input.setText("");
				}
			});
			return ground;
		}
		
		@Override
		public String toString(){
			return "SocialMessageSendingFragment";
		};
		
		private void updatelist(){
			MyArrayListAdapter<SocialMessageConversationDetail> adapter = getAdapter(conversation_id);
			if(adapter == null || adapter.getCount() <= 0) return;
			
			LinearLayout layout = (LinearLayout)ground.findViewById(R.id.social_messagesend_list);
			LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.MATCH_PARENT,
					LinearLayout.LayoutParams.WRAP_CONTENT
					);
			for(int i =0; i<adapter.getCount(); i++){
				View generated = adapter.getView(i, null, null);
				layout.addView(generated,param);
			}
			lasttime = (SocialMessageConversationDetail)adapter.getItem(adapter.getCount()-1);
		}
		private MyArrayListAdapter<SocialMessageConversationDetail> getAdapter(int conversation_id){
			Activity act = this.getActivity();
			if(act instanceof ProjectInterface.ActivityInterface){
				SQLiteDatabase db = ((ProjectInterface.ActivityInterface)act).getDatabaseReferenceFromActivity();
				db.beginTransaction();
				String selection = Common.Tables.ConversationDetail.conversation_id + " = ?";
				String[] arg = new String[]{conversation_id+""};
				String orderby = Common.Tables.ConversationDetail.time;
				Cursor cur = db.query(Common.Tables.CONVERSATION_DETAIL, null, selection, arg, null, null, orderby);
				SocialMessageConversationDetail[] list = new SocialMessageConversationDetail[cur.getCount()];
				if(cur.moveToFirst()){
					int index = 0;
					do{ list[index] = new SocialMessageConversationDetail(cur);
						if(index > 0){ list[index].setPrevious(list[index-1]); }
						index += 1;
					}while(cur.moveToNext());
				}
				db.setTransactionSuccessful();
				db.endTransaction();
				return new MyArrayListAdapter<SocialMessageConversationDetail>(act,list);
			}
			return new MyArrayListAdapter<SocialMessageConversationDetail>(act,new SocialMessageConversationDetail[0]);
		}
	}

	public static class SocialMessageConversationDetail implements MyArrayListAdapter.GenerateView{
		private static long DIFFERENCE = 60000*5;//5 minutes
		private SocialFriend friend;
		int _id, conversation_id,friend_id;
		long time; String message;
		SocialMessageConversationDetail previous; 
		
		public SocialMessageConversationDetail(int _id, int con_id, int fri_id, long time, String message){
			this._id = _id; this.conversation_id = con_id; this.friend_id = fri_id;this.time = time;
			this.message = message;
			
		}
		
		public void setPrevious(SocialMessageConversationDetail previous){
			this.previous = previous;
		}
		
		public SocialMessageConversationDetail(Cursor cur){
			this(
					cur.getInt(cur.getColumnIndex(Common.Tables.ConversationDetail._id)),
					cur.getInt(cur.getColumnIndex(Common.Tables.ConversationDetail.conversation_id)),
					cur.getInt(cur.getColumnIndex(Common.Tables.ConversationDetail.friend_id)),
					cur.getLong(cur.getColumnIndex(Common.Tables.ConversationDetail.time)),
					cur.getString(cur.getColumnIndex(Common.Tables.ConversationDetail.message))
					);
		}
		
		@Override
		public View getView(Activity act, int position, View convertView,
				ViewGroup parent) {
			boolean needDivider = false;
			if(friend == null) friend = getFriendById(friend_id,act);
			
			if(previous == null || previous.friend_id!=this.friend_id 
					|| this.time - previous.time > DIFFERENCE){
				needDivider = true;
			}
			LayoutInflater inflater = act.getLayoutInflater();
			View content = null;
			if(friend_id == -1){ 
				content = inflater.inflate(R.layout.view_social_messageelement_user, null,false);
				TextView text = (TextView)content.findViewById(R.id.social_message_user_text);
				text.setText(message);
			}else{ 
				content = inflater.inflate(R.layout.view_social_messageelement_friend, null,false);
				TextView nameview = (TextView)content.findViewById(R.id.social_message_name);
				ImageView imageview = (ImageView)content.findViewById(R.id.social_message_photo);
				TextView messageview = (TextView)content.findViewById(R.id.social_message_message);
				String name = friend.first +" "+friend.last;
				nameview.setText(name);
				messageview.setText(message);
				if(!Utility.setImage(imagefolderPath+friend.photo, act.getAssets(), imageview)){
					imageview.setImageResource(R.drawable.defaultaccount);
				}
			}
			
			if(needDivider){
				View top = inflater.inflate(R.layout.view_social_message_timesegmentation, null,false);
				String timereadable = Utility.longToTime(time);
				TextView timeview = (TextView)top.findViewById(R.id.social_message_time);
				timeview.setText(timereadable);
				
				View wrapper = inflater.inflate(R.layout.view_social_messagesend_wrapper, null,false);
				LinearLayout linear = (LinearLayout)wrapper.findViewById(R.id.social_message_wrapper_linear);
				LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.MATCH_PARENT,
						LinearLayout.LayoutParams.WRAP_CONTENT);
				linear.addView(top,param);
				linear.addView(content,param);
				return linear;
			}else{
				return content;
			}
		}

		@Override
		public View getDropDownView(Activity act, int position,
				View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			return null;
		}
		
		private SocialFriend getFriendById(int _id, final Activity act){
			if(act instanceof ProjectInterface.ActivityInterface){
				SQLiteDatabase db =((ProjectInterface.ActivityInterface)act).getDatabaseReferenceFromActivity();				
				db.beginTransaction();
				Cursor cur = Utility.getDataById(db, Common.Tables.FRIEND,_id);
				db.setTransactionSuccessful();
				db.endTransaction();
				if(cur.moveToFirst()){ return new SocialFriend(cur);
				}else{ Utility.log("should not be"); }
			}
			return null;
		}
	}


	public static class SocialPhotoFragment extends Fragment{
		View ground;
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			ground = inflater.inflate(R.layout.frag_social_photo, container,false);
			updateGrid();
			Utility.fitViewToWindow(ground, getActivity());
			return ground;
		}
		
		@Override
		public String toString(){
			return "SocialPhotoFragment";
		}

		private void updateGrid(){
			GridView grid = (GridView)ground.findViewById(R.id.social_photo_grid);
			MyArrayListAdapter<SocialPhotoGridElement> adapter =  getAdapter();
			grid.setAdapter(adapter);
			grid.setOnItemClickListener(new AdapterView.OnItemClickListener(){
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					Activity act = getActivity();
					if(act instanceof ProjectInterface.SocialMediaInterface){
						((ProjectInterface.SocialMediaInterface)act)
								.socialPhotoOnClickCallBack(arg0, arg1, arg2, arg3);
					}
				}
			});
		}
		private MyArrayListAdapter<SocialPhotoGridElement> getAdapter(){
			Activity act = getActivity();
			if(act instanceof ProjectInterface.SocialMediaInterface){
				SQLiteDatabase db = ((ProjectInterface.ActivityInterface)act).getDatabaseReferenceFromActivity();
				db.beginTransaction();
				Cursor cur = db.query(Common.Tables.SOCIALPHOTO, null, null, null, null, null, null);
				SocialPhotoGridElement[] list = new SocialPhotoGridElement[cur.getCount()];
				if(cur.moveToFirst()){
					int index = 0;
					do{ list[index] = new SocialPhotoGridElement(cur);
						index += 1;
					}while(cur.moveToNext());
				}
				db.setTransactionSuccessful();
				db.endTransaction();
				return new MyArrayListAdapter<SocialPhotoGridElement>(this.getActivity(),list);
			}
			return new MyArrayListAdapter<SocialPhotoGridElement>(this.getActivity(),new SocialPhotoGridElement[0]);
		}
	}
	
	public static class SocialPhotoGridElement implements MyArrayListAdapter.GenerateView{
		String photoname, comment;
		int _id;
		
		public SocialPhotoGridElement(int _id, String photoname, String comment){
			this.photoname = photoname;this._id = _id;
			this.comment = comment;
		}
		public SocialPhotoGridElement(Cursor cur){
			this(
					cur.getInt(cur.getColumnIndex(Common.Tables.SocialPhoto._id)),
					cur.getString(cur.getColumnIndex(Common.Tables.SocialPhoto.name)),
					cur.getString(cur.getColumnIndex(Common.Tables.SocialPhoto.caption))
					);
		}
		@Override
		public View getView(Activity act, int position, View convertView,
				ViewGroup parent) {
			View ground = act.getLayoutInflater().inflate(R.layout.view_socialphoto_gridelement, parent,false);
			ImageView imageview = (ImageView)ground.findViewById(R.id.social_photogrid_image);
			TextView textview = (TextView)ground.findViewById(R.id.social_photogrid_text);
			
			Utility.BitmapWorkerTask loader = new Utility.BitmapWorkerTask(imageview,act.getAssets());
			loader.execute(socialPhotoFolder+photoname);
			
//			if(!Utility.setImage(socialPhotoFolder+photoname,act.getAssets(), imageview)){
//				imageview.setImageResource(R.drawable.noimage);
//			}
			textview.setText(comment);
			return ground;
		}

		@Override
		public View getDropDownView(Activity act, int position,
				View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			return null;
		}
	}
	
	public static class SocialPhotoPageFragment extends Fragment{
		private int index = 0;
		private View ground;
		public static String SELECTED = "seleclted";
		public static String PREVIOUS = "previous";
		private android.support.v4.view.ViewPager pager;
		@Override
		public void onCreate (Bundle savedInstanceState){
			super.onCreate(savedInstanceState);
			Bundle arg = this.getArguments();
			if(savedInstanceState==null || !savedInstanceState.containsKey(PREVIOUS)){
				if(arg != null && arg.containsKey(SELECTED)){
					index = arg.getInt(SELECTED);
				}
			}else{ index = savedInstanceState.getInt(PREVIOUS); }
		}
		public void onSaveInstanceState (Bundle outState){
			super.onSaveInstanceState(outState);
			outState.putInt(PREVIOUS,pager.getCurrentItem());
		}
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			ground = inflater.inflate(R.layout.frag_social_photopager, container,false);
			updatePager();
			Utility.fitViewToWindow(ground, getActivity());
			return ground;
		}
		
		@Override
		public String toString(){
			return "SocialPhotoPageFragment";
		}
		
		private void updatePager(){
			pager = (android.support.v4.view.ViewPager)ground.findViewById(R.id.pager);
			pager.setAdapter(getAdapter());
			pager.setCurrentItem(index);
		}
		private MyPageAdapter<SocialPhotoPageView> getAdapter(){
			Activity act = this.getActivity();
			if(act instanceof ProjectInterface.ActivityInterface){
				SQLiteDatabase db =((ProjectInterface.ActivityInterface)act).getDatabaseReferenceFromActivity();
				db.beginTransaction();
				Cursor cur = db.query(Common.Tables.SOCIALPHOTO, null, null, null, null, null, null);
				SocialPhotoPageView[] list = new SocialPhotoPageView[cur.getCount()];
				if(cur.moveToFirst()){
					int index = 0;
					do{ list[index] = new SocialPhotoPageView(cur);
						index += 1;
					}while(cur.moveToNext());
				}
				db.setTransactionSuccessful();
				db.endTransaction();
				return new MyPageAdapter<SocialPhotoPageView>(act, list);
			}
			return new MyPageAdapter<SocialPhotoPageView>(act, new SocialPhotoPageView[0]);
		}
	}
	
	public static class SocialPhotoPageView implements MyPageAdapter.GeneratePageView{
		int _id;
		String name, caption;
		View ground;
		public SocialPhotoPageView(int _id, String name, String caption){
			this._id = _id; this.name = name; this.caption = caption;
		}
		public SocialPhotoPageView(Cursor cur){
			this(
					cur.getInt(cur.getColumnIndex(Common.Tables.SocialPhoto._id)),
					cur.getString(cur.getColumnIndex(Common.Tables.SocialPhoto.name)),
					cur.getString(cur.getColumnIndex(Common.Tables.SocialPhoto.caption))
					);
		}
		
		@Override
		public View getPageView(final Activity act, ViewGroup container, int position) {
			ground = act.getLayoutInflater().inflate(R.layout.view_social_photo_pageview, null,false);
			ImageView imageview = (ImageView)ground.findViewById(R.id.social_photo_pageview_image);
			TextView comment = (TextView)ground.findViewById(R.id.social_photo_pageview_editfield);
			View submitview = ground.findViewById(R.id.social_photo_pageview_submit);
			
			if(!Utility.setImage(socialPhotoFolder+name, act.getAssets(), imageview)){
				imageview.setImageResource(R.drawable.noimage);
			}
			comment.setText(caption);
			submitview.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					TextView comment = (TextView)ground.findViewById(R.id.social_photo_pageview_editfield);
					if(act instanceof ProjectInterface.SocialMediaInterface){
						((ProjectInterface.SocialMediaInterface)act).
						socialPhotoCommentSubmitCallBack(_id,comment.getText().toString().trim());
					}
				}
			});
			return ground;
		}


	}



}

//implements ProjectInterface.SocialMediaInterface
//@Override
//public void setCenterContent(Bundle savedInstanceState) {
//	if (savedInstanceState == null) {
//		SocialMediaFragment fragment = new SocialMediaFragment();
//		this.replaceTheCenterView(fragment, "level0");
////		manager.beginTransaction().replace(toReplace, fragment, "level0")
////				.addToBackStack("level0").commit();
//	}
//}
//@Override
//public void socialGridOnItemClickCallBack(int id) {
//	switch(id){
//	case R.drawable.icon_news:{
//		SocialNewsFeedListFragment fragment = new SocialNewsFeedListFragment();
//		this.replaceTheCenterView(fragment, "level1");
////		manager.beginTransaction().replace(toReplace, fragment,"level1").addToBackStack("level1").commit();
//	}break;
//	case R.drawable.icon_friends:{
//		SocialFriendFragment fragment = new SocialFriendFragment();
//		this.replaceTheCenterView(fragment, "level1");
////		manager.beginTransaction().replace(toReplace, fragment,"level1").addToBackStack("level1").commit();
//	}break;
//	case R.drawable.icon_message:{
//		SocialMessageFragment fragment = new SocialMessageFragment();
//		this.replaceTheCenterView(fragment, "level1");
////		manager.beginTransaction().replace(toReplace, fragment,"level1").addToBackStack("level1").commit();
//	}break;
//	case R.drawable.icon_photo:{
//		SocialPhotoFragment fragment = new SocialPhotoFragment();
//		this.replaceTheCenterView(fragment, "level1");
////		manager.beginTransaction().replace(toReplace, fragment,"level1").addToBackStack("level1").commit();
//	}break;
//	}
//}
//@Override
//public void socialNewsItemOnClickCallBack(int id, int liked) {
//	SQLiteDatabase db = this.getDatabaseReferenceFromActivity();
//	db.beginTransaction();
//	ContentValues content = new ContentValues();
//	content.put(Common.Tables.NewsFeed.liked, liked);
//	content.put("new", 1);
//	String whereClause = Common.Tables.NewsFeed._id + " = ?";
//	String[] whereArgs = new String[]{id+""};
//	long result = db.update(Common.Tables.NEWSFEED, content, whereClause, whereArgs);
//	Log.v("mark","socialNewsItemOnClickCallBack result:"+result);
//	db.setTransactionSuccessful();
//	db.endTransaction();
//}
//@Override
//public void socialNewsCommentLikeCallBack(int _id,int liked) {
//	SQLiteDatabase db = this.getDatabaseReferenceFromActivity();
//	db.beginTransaction();
//	ContentValues content = new ContentValues();
//	content.put(Common.Tables.NewsComment.liked, liked);
//	content.put("new", 1);
//	String whereClause = Common.Tables.NewsComment._id + " = ?";
//	String[] whereArgs = new String[]{_id+""};
//	long result = db.update(Common.Tables.NEWSCOMMENT, content, whereClause, whereArgs);
//	Log.v("mark","socialNewsCommentLikeCallBack result:"+result);
//	db.setTransactionSuccessful();
//	db.endTransaction();
//}
//@Override
//public void socialNewsGoCommentCallBack(int _id) {
//	SocialNewsFeedDetail fragment = new SocialNewsFeedDetail();
//	Bundle arg = new Bundle();
//	arg.putInt(SocialNewsFeedDetail.NEWS_ID,_id);
//	fragment.setArguments(arg);
//	this.replaceTheCenterView(fragment, "level2");
////	manager.beginTransaction().replace(toReplace, fragment,"level2").addToBackStack("level2").commit();
//}
//
//@Override
//public void socialNewsCommentSubmitCallBack(int news_id, String first,
//		String second, String photo,String comment, int amount) {
//	
//	SQLiteDatabase db = this.getDatabaseReferenceFromActivity();
//	db.beginTransaction();
//	ContentValues values = new ContentValues();
//	values.put(Common.Tables.NewsComment.comment, comment);
//	values.put(Common.Tables.NewsComment.first, first);
//	values.put(Common.Tables.NewsComment.last, second);
//	values.put(Common.Tables.NewsComment.photo, photo);
//	values.put(Common.Tables.NewsComment.news_id, news_id);
//	values.put(Common.Tables.NewsComment.isuers, 1);
//	
//	long result = db.insert(Common.Tables.NEWSCOMMENT, null, values);
//	Utility.log("socialNewsCommentSubmitCallBack insert:"+result);
//	if(result>=0){
//		ContentValues content = new ContentValues();
//		content.put(Common.Tables.NewsFeed.comment,amount);
//		String selection = Common.Tables.NewsFeed._id+" = ?";
//		String[] where = new String[]{news_id+""};
//		content.put("new", 1);
//		long result1 = db.update(Common.Tables.NEWSFEED, content, selection, where);
//		Utility.log("socialNewsCommentSubmitCallBack update:"+result1);
//	}
//	db.setTransactionSuccessful();
//	db.endTransaction();
//}
//@Override
//public void socialNewsAddStatus(String message) {
//	if(message == null){//which means cancel
//
//	}else{
//		SQLiteDatabase db = this.getDatabaseReferenceFromActivity();
//		db.beginTransaction();
//		ContentValues values = new ContentValues();
//		values.put(Common.Tables.NewsFeed.isusers, 1);
//		values.put(Common.Tables.NewsFeed.first, userfirst);
//		values.put(Common.Tables.NewsFeed.last, userlast);
//		values.put(Common.Tables.NewsFeed.photo, userphoto);
//		values.put(Common.Tables.NewsFeed.status, message);
//		long result = db.insert(Common.Tables.NEWSFEED, null, values);
//		if(result <=0){
//			Log.v("mark","socialNewsAddStatus failure");
//		}
//		db.setTransactionSuccessful();
//		db.endTransaction();
//	}
//	this.closeKeyBoard();
//	this.onBackPressed();
//}
//@Override
//public void socialNewsOnStatusClickedCallBack() {
//	SocialNewsAddStatus fragment = new SocialNewsAddStatus();
//	this.replaceTheCenterView(fragment, "level2");
////	manager.beginTransaction().replace(toReplace, fragment,"level2").addToBackStack("level2").commit();
//}
//@Override
//public void socialFriendAddCallBack(int position, int _id, int isfriend) {
//	// TODO Auto-generated method stub
//	//show confirmation
//	
//	
//	//modify database
//	SQLiteDatabase db = this.getDatabaseReferenceFromActivity();
//	db.beginTransaction();
//	String where = Common.Tables.Friend._id+" = ?";
//	String[] whereargs = new String[]{_id+""};
//	ContentValues values = new ContentValues();
//	values.put(Common.Tables.Friend.isfriend, isfriend);
//	values.put("new", 1);
//	long result = db.update(Common.Tables.FRIEND, values, where, whereargs);
//	Utility.log("socialFriendAddCallBack result:"+result);
//	db.setTransactionSuccessful();
//	db.endTransaction();
//}
//@Override
//public int socialConversationCreationCallBack(int friend_id,
//		String message, long time) {
//	Log.v("mark","socialConversationCreationCallBack with "+friend_id+", "+message+", "+time);
//	SQLiteDatabase db = this.getDatabaseReferenceFromActivity();
//	db.beginTransaction();
//	int conversation_id = -1;
//	//seems to be unnecessary to check if the conversation exists
//	//as it should be checked before 
//	//create new record
//	CONVERSATION_TABLE:{
//		ContentValues values = new ContentValues();
//		values.put(Common.Tables.Conversation.friend_id, friend_id);
//		values.put(Common.Tables.Conversation.hasRead, 1);
//		values.put(Common.Tables.Conversation.message, message);
//		values.put(Common.Tables.Conversation.time, time);
//		long result = db.insert(Common.Tables.CONVERSATION, null, values);
//		if(result<=0){
//			Log.v("mark","socialConversationCreationCallBack CONVERSATION_TABLE failure");
//		}else{
//			String selection = Common.Tables.Conversation.friend_id + " = ?";
//			String[] selectionArgs = new String[]{""+friend_id};
//			Cursor cur_con = db.query(Common.Tables.CONVERSATION, null, selection, selectionArgs, null, null, null);
//			if(cur_con.moveToFirst()){
//				conversation_id = cur_con.getInt(cur_con.getColumnIndex(Common.Tables.Conversation._id));
//				cur_con.close();
//			}
//		}
//	}
//	
//	if(conversation_id < 0) return -1;
//	
//	//insert the conversation detail table
//	CONVERSATION_DETAIL_TABLE:{
//		ContentValues values = new ContentValues();
//		values.put(Common.Tables.ConversationDetail.conversation_id, conversation_id);
//		values.put(Common.Tables.ConversationDetail.friend_id, -1);//user himself
//		values.put(Common.Tables.ConversationDetail.message, message);
//		values.put(Common.Tables.ConversationDetail.time, time);
//		long result = db.insert(Common.Tables.CONVERSATION_DETAIL, null, values);
//		if(result<=0){
//			Log.v("mark","socialConversationCreationCallBack CONVERSATION_DETAIL_TABLE failure");
//		}
//	}
//	db.setTransactionSuccessful();
//	db.endTransaction();
//	Log.v("mark","socialConversationCreationCallBack return "+conversation_id);
//	return conversation_id;
//}
//@Override
//public void socialMessageSendCallBack(int id, String type) {
//	SocialMessageSendingFragment fragment = new SocialMessageSendingFragment();
//	Bundle arg = new Bundle();
//	arg.putInt(type,id);
//	fragment.setArguments(arg);
//	this.replaceTheCenterView(fragment, "level2");
////	manager.beginTransaction().replace(toReplace, fragment,"level2").addToBackStack("level2").commit();
//}
//@Override
//public void socialMessageRecordCallBack(int conversation_id, String message, long time) {
//	Log.v("mark","socialMessageRecordCallBack with "+conversation_id+", "+message+", time");
//	SQLiteDatabase db = this.getDatabaseReferenceFromActivity();
//	db.beginTransaction();
//	//update content in the conversation detail
//	CONVERSATION_DETAIL_TABLE:{
//		ContentValues values = new ContentValues();
//		values.put(Common.Tables.ConversationDetail.message, message);
//		values.put(Common.Tables.ConversationDetail.conversation_id, conversation_id);
//		values.put(Common.Tables.ConversationDetail.time, time);
//		values.put(Common.Tables.ConversationDetail.friend_id, -1);
//		long result = db.insert(Common.Tables.CONVERSATION_DETAIL,null, values);
//		if(result<=0){
//			Log.v("mark","socialMessageRecordCallBack CONVERSATION_DETAIL_TABLE failure");
//		}
//	}
//	
//	CONVERSATION_TABLE:{
//		ContentValues values = new ContentValues();
//		values.put(Common.Tables.Conversation.message, message);
//		values.put(Common.Tables.Conversation.hasRead, 1);
//		values.put(Common.Tables.Conversation.time, time);
//		values.put("new", 1);
//		String whereClause = Common.Tables.Conversation._id +" = ?";
//		String[] whereArgs = new String[]{conversation_id+""};
//		long result = db.update(Common.Tables.CONVERSATION, values, whereClause, whereArgs);
//		if(result<=0){
//			Log.v("mark","socialMessageRecordCallBack CONVERSATION_TABLE failure");
//		}
//	}
//	db.setTransactionSuccessful();
//	db.endTransaction();
//}
//@Override
//public void socialPhotoCommentSubmitCallBack(int _id, String message) {
//	SQLiteDatabase db = this.getDatabaseReferenceFromActivity();
//	db.beginTransaction();
//	ContentValues values = new ContentValues();
//	values.put(Common.Tables.SocialPhoto.caption, message);
//	String whereClause = Common.Tables.SocialPhoto._id + " = ?";
//	String[] whereArgs = new String[]{_id+""};
//	values.put("new", 1);
//	long result = db.update(Common.Tables.SOCIALPHOTO, values, whereClause, whereArgs);
//	Log.v("mark","socialPhotoCommentSubmitCallBack result:"+result);
//	db.setTransactionSuccessful();
//	db.endTransaction();
//}
//@Override
//public void socialPhotoOnClickCallBack(AdapterView<?> arg0, View arg1,
//		int position, long arg3) {
//	SocialPhotoPageFragment fragment = new SocialPhotoPageFragment();
//	Bundle arg = new Bundle();
//	arg.putInt(SocialPhotoPageFragment.SELECTED	, position);
//	fragment.setArguments(arg);
//	this.replaceTheCenterView(fragment, "level3");
////	manager.beginTransaction().replace(toReplace, fragment,"level3").addToBackStack("level3").commit();
//}
//

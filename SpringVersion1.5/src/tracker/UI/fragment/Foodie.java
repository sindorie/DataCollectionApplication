package tracker.UI.fragment;

import tracker.UI.activity.FrameActivity;
import tracker.UI.components.MyArrayListAdapter;
import tracker.springversion1.Common;
import tracker.springversion1.ProjectInterface;
import tracker.springversion1.R;
import tracker.springversion1.Common.Tables;
import tracker.springversion1.Common.Tables.Restaurant;
import tracker.springversion1.Common.Tables.Review;
import tracker.springversion1.ProjectInterface.ActivityInterface;
import tracker.springversion1.ProjectInterface.FoodieInterface;
import tracker.springversion1.R.drawable;
import tracker.springversion1.R.id;
import tracker.springversion1.R.layout;
import tracker.utility.Utility;
import android.app.Activity;
import android.app.Fragment;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;

public class Foodie {
	public static String userPhoto = "user.jpg";
	public static String userName = "You";
	public static String figureFolderPath = "figure/";
	public static String restruactfolderPath = "foodie/";
	
	public static class FoodieRestaurantList extends Fragment {
		private ListView listview;
		private MyArrayListAdapter<FoodieRestaurant> listAdapter;

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			if (this.getActivity() instanceof ProjectInterface.ActivityInterface) {
				SQLiteDatabase db = ((ProjectInterface.ActivityInterface) this
						.getActivity()).getDatabaseReferenceFromActivity();
				db.beginTransaction();
				listAdapter = getDataFromDatabase(db);
				db.setTransactionSuccessful();
				db.endTransaction();
			} else {
				listAdapter = fakeDataList();
			}
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstaceState) {
			View ground = inflater.inflate(R.layout.frag_foodielist, container,
					false);
			listview = (ListView) ground.findViewById(R.id.foodielist);
			// populate the list
			listview.setAdapter(listAdapter);
			listview.setOnItemClickListener(listener);
			
			FrameActivity act = (FrameActivity)this.getActivity();
			
//			listview.setOnTouchListener(act.disableInterceptListener);
			
			Utility.fitViewToWindow(ground, getActivity());
			return ground;
		}

		public String toString(){
			return "FoodieRestaurantList";
		}
		
		private AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int pos,
					long id) {
				Activity act = getActivity();
				if (act instanceof ProjectInterface.FoodieInterface) {
					((ProjectInterface.FoodieInterface) act)
							.foodieFrontListItemOnClickCallBack(adapter, view,
									pos, id);
				}
			}
		};

		private MyArrayListAdapter<FoodieRestaurant> getDataFromDatabase(
				SQLiteDatabase db) {
			Log.v("mark", "get data from db:" + (db != null));
			Cursor cur = db.query(Common.Tables.RESTAURANT, null, null, null,
					null, null, null);
			cur.moveToFirst();
			int count = cur.getCount();
			FoodieRestaurant[] list = new FoodieRestaurant[count];
			if (cur.moveToFirst()) {
				int i = 0;
				do {
					list[i] = new FoodieRestaurant(cur);
					i += 1;
				} while (cur.moveToNext() && i < count);
			}
			cur.close();
			return new MyArrayListAdapter<FoodieRestaurant>(getActivity(), list);
		}

		private MyArrayListAdapter<FoodieRestaurant> fakeDataList() {
			FoodieRestaurant[] list = new FoodieRestaurant[3];
			String[] names = { "qwe", "asd", "zxc" };
			String[] address = { "wer", "sdf", "xcv" };
			String[] phone = { "345", "234", "123" };
			String[] desc = { "asdhfalsdfha", "ahsdflashdfjkasdh",
					"eywruioqyqweuiryqwe" };
			for (int i = 0; i < 3; i++) {
				list[i] = new FoodieRestaurant(i, i, names[i], address[i],
						phone[i], null, desc[i]);
			}
			return new MyArrayListAdapter<FoodieRestaurant>(getActivity(), list);
		}
	}

	public static class FoodieRestaurantDetail extends Fragment {
		private int rest_id;
		public final static String REST_ID = "id";
		LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		
		View ground; 
		@Override
		public void onCreate(Bundle savedInstaceState) {
			super.onCreate(savedInstaceState);
			Bundle arg = this.getArguments();
			if(arg != null && arg.containsKey(REST_ID)){
				rest_id = arg.getInt(REST_ID);
			}
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstaceState) {
			Log.v("mark","foodie resturant id:"+rest_id);
			ground = inflater.inflate(R.layout.frag_foodierestdetail, container, false);
			ImageView bottomImage = (ImageView)ground.findViewById(R.id.bottom_image);
			if(!Utility.setImage(figureFolderPath+userPhoto, this.getActivity().getAssets(), bottomImage)){
				bottomImage.setImageResource(R.drawable.noimage);
			}
			Button button = (Button) ground.findViewById(R.id.bottom_submit);
			button.setOnClickListener(listener);
			
			updateRestuarantInfo();
			update();
			return ground;
		}
		
		public String toString(){
			return "FoodieRestaurantDetail";
		}
		
		private void updateRestuarantInfo(){
			FoodieRestaurant target =  getRestaurantInfoFromDatabase();
			TextView adderssview = (TextView) ground.findViewById(R.id.topaddress);
			TextView nameview = (TextView) ground.findViewById(R.id.topname);
			TextView phoneview = (TextView) ground.findViewById(R.id.topphone);
			RatingBar rateview = (RatingBar) ground.findViewById(R.id.toprate);
			ImageView imageview = (ImageView) ground.findViewById(R.id.topimage);

			rateview.setOnTouchListener(dumnListener);
			rateview.setFocusable(false);
			
			adderssview.setText(target.address);
			nameview.setText(target.name);
			phoneview.setText(target.phone);
			rateview.setRating(target.stars);
			
			if(!Utility.setImage(restruactfolderPath+target.imagename, this.getActivity().getAssets(), imageview)){
				imageview.setImageResource(R.drawable.noimage);
			}
		}
		
		private void update(){
			MyArrayListAdapter<FoodieRestaurantComment> adapter = getAdapter();
			if(adapter == null || adapter.getCount()<=0  ) return;
			LinearLayout linear = (LinearLayout)ground.findViewById(R.id.foodiedetail_list_linear);
			linear.removeAllViews();
			
			for(int i=0;i<adapter.getCount();i++){
				linear.addView(adapter.getView(i, null, null),param);
			}
		}
		private MyArrayListAdapter<FoodieRestaurantComment> getAdapter(){
			Activity act = this.getActivity();
			if(act instanceof ProjectInterface.ActivityInterface){
				SQLiteDatabase db = ((ProjectInterface.ActivityInterface)act).getDatabaseReferenceFromActivity();
				db.beginTransaction();
				String selection = Common.Tables.Review.rest_id+" = ?";
				String[] args = new String[]{this.rest_id+""};
				Cursor cur = db.query(Common.Tables.REVIEW, null, selection, args, null, null, null);
				FoodieRestaurantComment[] list = new FoodieRestaurantComment[cur.getCount()];
				if(cur.moveToFirst()){
					int index = 0;
					do{	list[index] = new FoodieRestaurantComment(cur);
						index += 1;
					}while(cur.moveToNext());
				}
				db.setTransactionSuccessful();
				db.endTransaction();
				return new MyArrayListAdapter<FoodieRestaurantComment>(act,list);
			}
			return new MyArrayListAdapter<FoodieRestaurantComment>(act,new FoodieRestaurantComment[0]);
		}
		
		private View.OnClickListener listener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				EditText edit = (EditText) ground.findViewById(R.id.bottom_edit);
				RatingBar ratingbar = (RatingBar) ground.findViewById(R.id.bottom_rate);
				final ScrollView scroll = (ScrollView) ground.findViewById(R.id.foodiedetail_scroll);
				String message = edit.getText().toString().trim();
				LinearLayout linear = (LinearLayout)ground.findViewById(R.id.foodiedetail_list_linear);
				
				int rate = (int) ratingbar.getRating();
				if ((message == null || message.equals(""))) {
					edit.setHint("Please enter comment");
				} else {
					Activity act = getActivity();
					if (act instanceof ProjectInterface.FoodieInterface) {
						//update database
						((ProjectInterface.FoodieInterface) act)
							.foodieDetailButtonOnClickCallBack(message, rate, rest_id);
						//update the adapter
						FoodieRestaurantComment newComment = new FoodieRestaurantComment(
								-1,rest_id,userName,rate,userPhoto,message);
						//update the layout
						View generated = newComment.getView(getActivity(), -1, null, null);
						linear.addView(generated,param);
						//update the bottom
						ratingbar.setRating(0);
						edit.setText("");
						scroll.post(new Runnable(){
							@Override
							public void run() {scroll.fullScroll(View.FOCUS_DOWN);}
						});
					}
				}
			}
		};

		private FoodieRestaurant getRestaurantInfoFromDatabase() {
			Activity act = this.getActivity();
			if(act instanceof ProjectInterface.ActivityInterface){
				SQLiteDatabase db = ((ProjectInterface.ActivityInterface)act).getDatabaseReferenceFromActivity();
				db.beginTransaction();
				Cursor cur = db.query(Common.Tables.RESTAURANT, null, Common.Tables.Restaurant._id + " = ? ",
						new String[] { rest_id + "" }, null, null, null);
				if(cur.moveToFirst()){
					FoodieRestaurant result =  new FoodieRestaurant(cur);
					cur.close();
					db.setTransactionSuccessful();
					db.endTransaction();
					return result;
				}
				db.setTransactionSuccessful();
				db.endTransaction();
			}
			return null;
		}
	}

	public static class FoodieRestaurant implements
			MyArrayListAdapter.GenerateView {
		public String name, address, phone, imagename, description;
		public int id;
		public float stars;
		

		public FoodieRestaurant(int id, float stars, String name, String phone,
				String address, String imagename, String description) {
			this.name = name;
			this.address = address;
			this.phone = phone;
			this.stars = stars;
			this.imagename = imagename;
			this.id = id;
			this.description = description;
		}

		public FoodieRestaurant(Cursor cur) {
			this(
					cur.getInt(cur.getColumnIndex(Common.Tables.Restaurant._id)),
					cur.getFloat(cur
							.getColumnIndex(Common.Tables.Restaurant.rate)),
					cur.getString(cur
							.getColumnIndex(Common.Tables.Restaurant.name)),
					cur.getString(cur
							.getColumnIndex(Common.Tables.Restaurant.phone)),
					cur.getString(cur
							.getColumnIndex(Common.Tables.Restaurant.address)),
					cur.getString(cur
							.getColumnIndex(Common.Tables.Restaurant.image)),
					cur.getString(cur
							.getColumnIndex(Common.Tables.Restaurant.description)));
		}

		@Override
		public View getView(Activity act, int position, View convertView,
				ViewGroup parent) {
			LayoutInflater inflater = act.getLayoutInflater();
			View ground = inflater.inflate(R.layout.view_foodierestrecord,
					parent, false);

			ImageView photoview = (ImageView) ground
					.findViewById(R.id.foodieoverview_image);
			TextView nameview = (TextView) ground
					.findViewById(R.id.foodieoverview_name);
			TextView addressview = (TextView) ground
					.findViewById(R.id.foodieoverview_address);
			TextView phoneview = (TextView) ground
					.findViewById(R.id.foodieoverview_phone);
			RatingBar rateview = (RatingBar) ground
					.findViewById(R.id.foodieoverview_rate);

			rateview.setOnTouchListener(dumnListener);
			rateview.setFocusable(false);

			nameview.setText(name);
			addressview.setText(address);
			rateview.setRating(stars);
			phoneview.setText(phone);
			AssetManager manager = act.getAssets();
			
			if(!Utility.setImage(restruactfolderPath +  imagename, manager, photoview)){
				photoview.setImageResource(R.drawable.noimage);
			}
			
			
//			boolean error = false;
//			try {
//				
//				InputStream is = manager.open();
//				Bitmap map = BitmapFactory.decodeStream(is);
//				if (map != null) {
//					photoview.setImageBitmap(map);
//				} else
//					error = true;
//			} catch (Exception e) {
//				error = true;
//			}
//			if (error)
//				
			return ground;
		}

		@Override
		public View getDropDownView(Activity act, int position,
				View convertView, ViewGroup parent) {
			return null;
		}
	}

	public static class FoodieRestaurantComment implements
			MyArrayListAdapter.GenerateView {
		String username = "", photo, comment;
		int star, rest_id, id;
		String pathfolder = "figure";

		public FoodieRestaurantComment(int id, int rest_id, String username,
				int star, String photo, String comment) {
			this.id = id;
			this.rest_id = rest_id;
			this.username = username;
			this.star = star;
			this.photo = photo;
			this.comment = comment;
		}

		public FoodieRestaurantComment(Cursor cur) {
			this(
					cur.getInt(cur.getColumnIndex(Common.Tables.Review._id)),
					cur.getInt(cur.getColumnIndex(Common.Tables.Review.rest_id)),
					cur.getString(cur.getColumnIndex(Common.Tables.Review.username)),
					cur.getInt(cur.getColumnIndex(Common.Tables.Review.stars)),
					cur.getString(cur.getColumnIndex(Common.Tables.Review.photo)),
					cur.getString(cur.getColumnIndex(Common.Tables.Review.comment)));
		}


		public void setUsername(String username) {
			if (username != null)
				this.username = username;
		}

		@Override
		public View getView(Activity act, int position, View convertView,
				ViewGroup parent) {
			View ground = act.getLayoutInflater().inflate(
					R.layout.view_foodiecomment, parent, false);

			ImageView photoview = (ImageView) ground
					.findViewById(R.id.foodiecomment_photo);
			TextView nameview = (TextView) ground
					.findViewById(R.id.foodiecomment_name);
			RatingBar ratebar = (RatingBar) ground
					.findViewById(R.id.foodiecomment_rate);
			TextView commentview = (TextView) ground
					.findViewById(R.id.foodiecomment_comment);			
			nameview.setText(username);
			ratebar.setRating(star);
			commentview.setText(comment);
			ratebar.setOnTouchListener(dumnListener);
			ratebar.setFocusable(false);
			
			nameview.setText(this.username);
			
			if(!Utility.setImage(pathfolder + "/" + photo, act.getAssets(), photoview)){
				photoview.setImageResource(R.drawable.noimage);
			}
			return ground;
		}

		@Override
		public View getDropDownView(Activity act, int position,
				View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			return null;
		}

	}


	private static View.OnTouchListener dumnListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			return true;
		}
	};
}

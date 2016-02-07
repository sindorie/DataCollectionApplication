package tracker.UI.fragment;

import java.io.InputStream;
import java.util.Scanner;
import tracker.UI.components.MyArrayListAdapter;
import tracker.UI.components.MyPageAdapter;
import tracker.springversion1.Common;
import tracker.springversion1.ProjectInterface;
import tracker.springversion1.R;
import tracker.springversion1.Common.Tables;
import tracker.springversion1.Common.Tables.NewsArticle;
import tracker.springversion1.ProjectInterface.ActivityInterface;
import tracker.springversion1.ProjectInterface.NewsInterface;
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
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class News {

	static String newsImageFolder = "news/image/";
	static String newsTextFolder = "news/text/";
	public static class NewsFragment extends Fragment {

		View ground;
		private int[] ids = { R.id.new_top_upper1, R.id.new_top_upper2, R.id.new_top_upper3, R.id.new_top_upper4, R.id.new_top_lower1, R.id.new_top_lower2, R.id.new_top_lower3, R.id.new_top_lower4 };

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			ground = inflater.inflate(R.layout.frag_news, container, false);
			updateList(null);
			for (int id : ids) {
				ground.findViewById(id).setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						int id = v.getId();
						switch (id) {
						case R.id.new_top_upper1: {
							id = 2;
						}
							break;
						case R.id.new_top_upper2: {
							id = 8;
						}
							break;
						case R.id.new_top_upper3: {
							id = 3;
						}
							break;
						case R.id.new_top_upper4: {
							id = 4;
						}
							break;
						case R.id.new_top_lower1: {
							id = 1;
						}
							break;
						case R.id.new_top_lower2: {
							id = 0;
						}
							break;
						case R.id.new_top_lower3: {
							id = 7;
						}
							break;
						case R.id.new_top_lower4: {
							id = 8;
						}
							break;
						default:
							Log.v("mark", "unidentified view: " + v.getClass().toString());
							return;
						}
						Activity act = getActivity();
						if (act instanceof ProjectInterface.NewsInterface) {
							((ProjectInterface.NewsInterface) act).newsFrontElementOnClickedCallBack(id);
						}
					}
				});;
			}
			return ground;
		}

		public void updateList(String input) {
			LinearLayout linear = (LinearLayout) ground.findViewById(R.id.news_linear);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			MyArrayListAdapter<NewsElement> adapter = getAdapter(input);
			if (adapter != null) {
				for (int i = 0; i < adapter.getCount(); i++) {
					View generated = adapter.getView(i, null, null);
					linear.addView(generated, params);
				}
			}
		}

		public String toString(){
			return "NewsFragment";
		}
		
		private MyArrayListAdapter<NewsElement> getAdapter(String input) {
			Activity act = getActivity();
			if (act instanceof ProjectInterface.ActivityInterface) {
				String selection = null;
				String[] args = null;
				if (input != null && !input.equals("")) {
					selection = Common.Tables.NewsArticle.titile + " LIKE ?";
					args = new String[] { input };
				}
				SQLiteDatabase db = ((ProjectInterface.ActivityInterface) act).getDatabaseReferenceFromActivity();
				db.beginTransaction();
				Cursor cur = db.query(Common.Tables.NEWSARTICLE, null, selection, args, null, null, null);
				NewsElement[] list = new NewsElement[cur.getCount()];
				if (cur.moveToFirst()) {
					int index = 0;
					do {
						list[index] = new NewsElement(cur);
						index += 1;
					} while (cur.moveToNext());
				}
				db.setTransactionSuccessful();
				db.endTransaction();
				return new MyArrayListAdapter<NewsElement>(act, list);
			}
			return new MyArrayListAdapter<NewsElement>(act, new NewsElement[0]);
		}
	}
	public static class NewsElement implements MyArrayListAdapter.GenerateView, MyPageAdapter.GeneratePageView {

		int _id;
		String title, photo, comment, content, author;
		View ground;

		public NewsElement(int _id, String title, String author, String photo, String comment, String content) {
			this._id = _id;
			this.title = title;
			this.photo = photo;
			this.comment = comment;
			this.content = content;
			this.author = author;
		}

		@Override
		public String toString() {
			return _id + "|" + title + "|" + author + "|" + photo + "|" + comment + "|" + content;
		}

		public NewsElement(Cursor cur) {
			this(cur.getInt(cur.getColumnIndex(Common.Tables.NewsArticle._id)), cur.getString(cur.getColumnIndex(Common.Tables.NewsArticle.titile)), cur.getString(cur
					.getColumnIndex(Common.Tables.NewsArticle.author)), cur.getString(cur.getColumnIndex(Common.Tables.NewsArticle.photo)), cur.getString(cur
					.getColumnIndex(Common.Tables.NewsArticle.comment)), cur.getString(cur.getColumnIndex(Common.Tables.NewsArticle.content)));
		}

		@Override
		public View getView(final Activity act, int position, View convertView, ViewGroup parent) {
			View ground = act.getLayoutInflater().inflate(R.layout.view_news_front_element, parent, false);
			TextView titleview = (TextView) ground.findViewById(R.id.news_frontelement_title);
			ImageView imageview = (ImageView) ground.findViewById(R.id.news_frontelement_image);
			titleview.setText(title);
			ground.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					if (act instanceof ProjectInterface.NewsInterface) {
						((ProjectInterface.NewsInterface) act).newsFrontElementOnClickedCallBack(_id);
					}
				}
			});
			Utility.BitmapWorkerTask loader = new Utility.BitmapWorkerTask(imageview, act.getAssets());
			loader.execute(newsImageFolder + photo);
			// if(!Utility.setImage(newsImageFolder+photo, act.getAssets() ,
			// imageview)){
			// imageview.setImageResource(R.drawable.noimage);
			// }
			return ground;
		}

		@Override
		public View getPageView(final Activity act, ViewGroup container, int position) {
			ground = act.getLayoutInflater().inflate(R.layout.view_news_detailelement, container, false);
			ImageView imageview = (ImageView) ground.findViewById(R.id.news_detalelement_image);
			TextView titleview = (TextView) ground.findViewById(R.id.news_detalelement_title);
			TextView authorview = (TextView) ground.findViewById(R.id.news_detalelement_author);
			TextView contentview = (TextView) ground.findViewById(R.id.news_detalelement_content);
			titleview.setText(title);
			authorview.setText(author);
			String actualcontent = getActualContent(act.getAssets(), content);
			contentview.setText(actualcontent);
			TextView commentSection = (TextView) ground.findViewById(R.id.news_detailelemtn_comment);
			if (!Utility.setImage(newsImageFolder + photo, act.getAssets(), imageview)) {
				imageview.setImageResource(R.drawable.noimage);
			}
			View submission = ground.findViewById(R.id.news_detailelement_submit);
			commentSection.setText(comment);
			submission.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					if (act instanceof ProjectInterface.NewsInterface) {
						EditText inputField = (EditText) ground.findViewById(R.id.news_detailelement_edit);
						String message = inputField.getText().toString().trim();
						TextView commentSection = (TextView) ground.findViewById(R.id.news_detailelemtn_comment);
						ScrollView scroll = (ScrollView) ground.findViewById(R.id.news_detailelement_scroll);
						commentSection.setText(message);
						scroll.fullScroll(View.FOCUS_DOWN);
						inputField.setText("");
						((ProjectInterface.NewsInterface) act).newsDetailCommentSubmitCallBack(_id, message);
					}
				}
			});
			// Utility.BitmapWorkerTask loader = new
			// Utility.BitmapWorkerTask(imageview,act.getAssets());
			// loader.execute(newsImageFolder+photo);
			return ground;
		}

		private String getActualContent(AssetManager manager, String name) {
			StringBuilder sb = new StringBuilder();
			try {
				InputStream is = manager.open(newsTextFolder + name);
				Scanner in = new Scanner(is);
				while (in.hasNextLine()) {
					sb.append(in.nextLine() + "\n");
				}
				in.close();
				is.close();
			} catch (Exception e) {
				Log.v("mark", "getActualContent with id:" + _id + " mees exception:" + e.getMessage());
			}
			String result = sb.toString();
			// Log.v("mark","String length:"+result.length());
			return result;
		}

		@Override
		public View getDropDownView(Activity act, int position, View convertView, ViewGroup parent) {
			return null;
		}
	}
	public static class NewsDetail extends Fragment {

		View ground;
		public static String ITEM_ID = "id";
		private int article_id = 0;

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			Bundle arg = this.getArguments();
			if (arg != null && arg.containsKey(ITEM_ID)) {
				article_id = arg.getInt(ITEM_ID);
			}
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			Log.v("mark", "NewsDetail article_id:" + article_id);
			ground = inflater.inflate(R.layout.frag_news_detail, container, false);
			update();
			return ground;
		}

		public String toString(){
			if(this.ground != null){
				ViewPager pager = (ViewPager)this.getView().findViewById(R.id.pager);
				if(pager!=null){
					return "NewsDetail:"+pager.getCurrentItem();
				}else{
					return "NewsDetail";
				}	
			}else{
				return "NewsDetail";
			}
		}
		
		private void update() {
			android.support.v4.view.ViewPager pager = (android.support.v4.view.ViewPager) ground.findViewById(R.id.pager);
			int index = 0;
			MyPageAdapter<NewsElement> adapter = getAdapter();
			for (int i = 0; i < adapter.getCount(); i++) {
				if (article_id == adapter.getElementAt(i)._id) {
					index = i;
					break;
				}
			}
			pager.setAdapter(adapter);
			pager.setCurrentItem(index);
		}

		private MyPageAdapter<NewsElement> getAdapter() {
			Activity act = this.getActivity();
			if (act instanceof ProjectInterface.ActivityInterface) {
				SQLiteDatabase db = ((ProjectInterface.ActivityInterface) act).getDatabaseReferenceFromActivity();
				db.beginTransaction();
				Cursor cur = db.query(Common.Tables.NEWSARTICLE, null, null, null, null, null, null);
				NewsElement[] list = new NewsElement[cur.getCount()];
				if (cur.moveToFirst()) {
					int index = 0;
					do {
						list[index] = new NewsElement(cur);
						index += 1;
					} while (cur.moveToNext());
				}
				db.setTransactionSuccessful();
				db.endTransaction();
				return new MyPageAdapter<NewsElement>(act, list);
			}
			return new MyPageAdapter<NewsElement>(act, new NewsElement[0]);
		}
	}
}

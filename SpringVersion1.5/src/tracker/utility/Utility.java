package tracker.utility;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import tracker.UI.activity.FrameActivity;
import tracker.UI.fragment.News.NewsElement;
import tracker.springversion1.Common;
import tracker.springversion1.ProjectInterface;
import tracker.springversion1.R;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.FrameLayout.LayoutParams;

public class Utility {
	public static boolean setImage(String path, AssetManager manager, ImageView imageview){
		try {
			InputStream is = manager.open(path);
			Bitmap map = BitmapFactory.decodeStream(is);
			if(map == null) return false;
			else imageview.setImageBitmap(map);
		} catch (IOException e) {return false;}
		return true;
	}
	
	//http://en.wikibooks.org/wiki/Algorithm_Implementation/Strings/Levenshtein_distance#Java
	public static int LevenshteinDistance (String s0, String s1) {
		int len0 = s0.length()+1;
		int len1 = s1.length()+1;
	 
		// the array of distances
		int[] cost = new int[len0];
		int[] newcost = new int[len0];
	 
		// initial cost of skipping prefix in String s0
		for(int i=0;i<len0;i++) cost[i]=i;
	 
		// dynamicaly computing the array of distances
	 
		// transformation cost for each letter in s1
		for(int j=1;j<len1;j++) {
	 
			// initial cost of skipping prefix in String s1
			newcost[0]=j-1;
	 
			// transformation cost for each letter in s0
			for(int i=1;i<len0;i++) {
				// matching current letters in both strings
				int match = (s0.charAt(i-1)==s1.charAt(j-1))?0:1;
				// computing cost for each transformation
				int cost_replace = cost[i-1]+match;
				int cost_insert  = cost[i]+1;
				int cost_delete  = newcost[i-1]+1;
				// keep minimum cost
				newcost[i] = Math.min(Math.min(cost_insert, cost_delete),cost_replace );
			}
			// swap cost/newcost arrays
			int[] swap=cost; cost=newcost; newcost=swap;
		}
		// the distance is the cost for transforming all letters in both strings
		return cost[len0-1];
	}
	
	public static Cursor getDataById(SQLiteDatabase db, String table, int _id){
		String selection = "_id = ?";
		return db.query(table, null, selection, new String[]{_id+""}, null, null, null);
	}
	
	public static Cursor getDataBySingleSecltion(SQLiteDatabase db, String table,String columnname, int _id){
		String selection = columnname+" = ?";
		return db.query(table, null, selection, new String[]{_id+""}, null, null, null);
	}
	
	public static void cloasKeyBoard(Activity act, View view){
		InputMethodManager imm = (InputMethodManager)act.getSystemService(
			      Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}
	
	public static void openKeyBoard(Activity act, View view){
	    InputMethodManager inputMethodManager = (InputMethodManager) act.getSystemService(Context.INPUT_METHOD_SERVICE);
	    if (inputMethodManager != null) {
	        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
	    }
	    
//	    InputMethodManager inputMethodManager=(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
//	    inputMethodManager.toggleSoftInputFromWindow(linearLayout.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0);
	}
	static DateFormat format = new SimpleDateFormat( "hh:mm a" );
	public static String longToTime(long time){
		Date date = new Date(time);
		return format.format(date);
	}

	public static void log(String message){
		Log.v("mark",message); 
	}
 
	
	public static boolean isPortaitOrientation(Activity act){
		return act.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
	}
	
	public static void fitViewToWindow(View view, Activity act){
		return ;
		
//		if(act instanceof DrawerActivity){
//			int height = ((DrawerActivity)act).getWindowHeight();
//			if(Utility.isPortaitOrientation(act)){
//				if(act.getActionBar().getTabCount()>0){
//					int actionBarHeight = 0;
//					TypedValue tv = new TypedValue();
//					if (act.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
//					    actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,act.getResources().getDisplayMetrics());
//					}
//					Log.v("mark","actionBarHeight:"+actionBarHeight);
//					height -= actionBarHeight;
//				}
//			}
//			
//			view.setLayoutParams(new FrameLayout.LayoutParams(
//					FrameLayout.LayoutParams.MATCH_PARENT,
//					height
//					));
//			
//		}
	}
	
	
	public static class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
	    private final WeakReference<ImageView> imageViewReference;
	    private AssetManager manager;
	    public BitmapWorkerTask(ImageView imageView, AssetManager manager) {
	        // Use a WeakReference to ensure the ImageView can be garbage collected
	        imageViewReference = new WeakReference<ImageView>(imageView);
	        this.manager = manager;
	    }

	    // Decode image in background.
	    @Override
	    protected Bitmap doInBackground(String... filename) {
			InputStream is = null;
			try { is = manager.open(filename[0]);
			} catch (IOException e) { return null; }
			return BitmapFactory.decodeStream(is);
	    }

	    // Once complete, see if ImageView is still around and set bitmap.
	    @Override
	    protected void onPostExecute(Bitmap bitmap) {
	        if (imageViewReference != null) {
	        	final ImageView imageView = imageViewReference.get();
	        	if(bitmap != null){
		            if (imageView != null) {
		                imageView.setImageBitmap(bitmap);
		            }
	        	}else{ imageView.setImageResource(R.drawable.noimage); }
	        }
	    }
	}
	public static void displayErrorMessage(String msg, Activity act){
		Toast toast = Toast.makeText(act.getApplicationContext(), msg, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.TOP, 00, 0);
		toast.show();
		return;
	}
	public static void displayErrorMessage(String msg, Application app){
		Toast toast = Toast.makeText(app.getApplicationContext(), msg, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.TOP, 00, 0);
		toast.show();
		return;
	}
	
	public static String connectStrings(String separator,String... s) {
		StringBuilder sb = new StringBuilder();
		for (String ss : s) {
			sb.append(ss).append(separator);
		}
		return sb.toString();
	}
	
	
	
	
	
	
	
	
	
	
	
	
	public static class SampledBitMapLoader extends AsyncTask<String, Void, Bitmap> {
	    private final WeakReference<ImageView> imageViewReference;
	    private int reqWidth, reqHeight;
	    private AssetManager manager;
	    public SampledBitMapLoader(ImageView imageView, AssetManager manager, int reqWidth, int reqHeight) {
	        // Use a WeakReference to ensure the ImageView can be garbage collected
	        imageViewReference = new WeakReference<ImageView>(imageView);
	        this.manager = manager;
	        this.reqWidth = reqWidth; this.reqHeight = reqHeight;
	    }

	    // Decode image in background.
	    @Override
	    protected Bitmap doInBackground(String... filename) {
			return decodeSampledBitmapFromAsset(manager,filename[0],reqWidth,reqHeight);
	    }

	    // Once complete, see if ImageView is still around and set bitmap.
	    @Override
	    protected void onPostExecute(Bitmap bitmap) {
	        if (imageViewReference != null) {
	        	final ImageView imageView = imageViewReference.get();
	        	if(bitmap != null){
		            if (imageView != null) {
		                imageView.setImageBitmap(bitmap);
		            }
	        	}else{ imageView.setImageResource(R.drawable.noimage); }
	        }
	    }
	}
	
	
	public static Bitmap decodeSampledBitmapFromAsset(AssetManager manager, String filename, int reqWidth, int reqHeight){
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    try {
			BitmapFactory.decodeStream(manager.open(filename), null, options);
			options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
		    
		    options.inJustDecodeBounds = false;
		    return BitmapFactory.decodeStream(manager.open(filename), null, options);
		} catch (IOException e) {
			Log.v("mark","decodeSampledBitmapFromAsset IOException "+e.getMessage());
			return null;
		}
	}
	
	
	public static Bitmap decodeSampledBitmapFromFile(String filePath, int reqWidth, int reqHeight){
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    BitmapFactory.decodeFile(filePath, options);
	    
	    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
	    
	    options.inJustDecodeBounds = false;
	    return BitmapFactory.decodeFile(filePath, options);
	}
	
	public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
	        int reqWidth, int reqHeight) {

	    // First decode with inJustDecodeBounds=true to check dimensions
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    BitmapFactory.decodeResource(res, resId, options);

	    // Calculate inSampleSize
	    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

	    // Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;
	    return BitmapFactory.decodeResource(res, resId, options);
	}
	
	public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
    // Raw height and width of image
    final int height = options.outHeight;
    final int width = options.outWidth;
    int inSampleSize = 1;

    if (height > reqHeight || width > reqWidth) {

        final int halfHeight = height / 2;
        final int halfWidth = width / 2;

        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
        // height and width larger than the requested height and width.
        while ((halfHeight / inSampleSize) > reqHeight
                && (halfWidth / inSampleSize) > reqWidth) {
            inSampleSize *= 2;
        }
    }

    return inSampleSize;
}
	
	public static void clearArray(float[] arr){
		for(int i=0;i<arr.length;i++){
			arr[i] = 0;
		}
	}
	public static String connectAsString(String separator, Object... os){
		StringBuilder sb = new StringBuilder();
		sb.append(os[0].toString());
		for(int i=1;i<os.length;i++){
			sb.append(",").append(os[i].toString());
		}
		return sb.toString();
	}
	
//	public abstract static class OnDoubleClickListener implements View.OnClickListener{
//		private long lastClick =-1;
//		private int duration = 200;
//		public OnDoubleClickListener(){super();}
//		public OnDoubleClickListener(int duration){super();this.duration = duration;}
//		@Override
//		public void onClick(View v) {
//			long current = System.currentTimeMillis();
//			if(current - lastClick < duration){
//				onDoubleClicked(v);
//				lastClick = -1;
//			}else{
//				lastClick = current;
//			}
//		}
//		public abstract void onDoubleClicked(View v);
//	}
	
//	public static <T extends MyArrayListAdapter.GenerateView> MyArrayListAdapter<T> 
//		getAdapter(Activity act, String tablename){
//		
//		if(act instanceof ProjectInterface.ActivityInterface){
//			SQLiteDatabase db = ((ProjectInterface.ActivityInterface)act).getDatabaseReferenceFromActivity();
//			Cursor cur = db.query(Common.Tables.NEWSARTICLE, null, null, null, null, null, null);
//			T[] list = new T[cur.getCount()];
//			if(cur.moveToFirst()){
//				int index = 0;
//				do{ list[index] = new T(cur);
//					index += 1;
//				}while(cur.moveToNext());
//			}
//			return new MyArrayListAdapter<T>(act, list);
//		}
//		
//		return new MyArrayListAdapter<T>(act, list);
//	}
	
//	public static View.OnClickListener functionUnAvailable(final Context context){
//		return new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				Toast.makeText(context, "Function unavailiable", Toast.LENGTH_SHORT);
//			}
//		};
//	}
	
//	public static String[] splitWithEmpty(String input,String separator){
//		ArrayList<String> arr = new ArrayList<String>();
//		int lenSep = separator.length();
//		int j = 0;
//		for(int i=0;i<input.length();i++){
//			if(input.startsWith(separator, i)){
//				i += lenSep;
//				arr.add(input.substring(j,i));
//				j = i;
//			}
//		}
//		return arr.toArray(new String[0]);
//	}
//	
//	public static void main(String[] args){
//		String me = "asd^asd";
//		System.out.println(Arrays.toString(splitWithEmpty(me,"^")));
//	}
}

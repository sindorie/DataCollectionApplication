package tracker.UI.fragment;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.zip.Inflater;
import deprecated.MyGridLayout;
import tracker.UI.components.MyArrayListAdapter;
import tracker.UI.view.TouchImageView;
import tracker.UI.view.WaterFall;
import tracker.springversion1.ProjectInterface;
import tracker.springversion1.R;
import tracker.springversion1.ProjectInterface.FragmentInterface;
import tracker.springversion1.ProjectInterface.GalleryInterface;
import tracker.springversion1.R.id;
import tracker.springversion1.R.layout;
import tracker.utility.Utility;
import tracker.utility.Utility.BitmapWorkerTask;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.app.FragmentTransaction;
import android.support.v4.util.LruCache;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class Gallery {

	// list of image -> grid of image -> single image
	public static class GalleryListFragment extends Fragment {

		public final static String LISTKEY = "list";
		private String[] list;
		private LruCacheRetainStateFragment retain;
		private int imageDimension = 200;
		private String folderPath = "tn_gallery";

		@Override 
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			AssetManager manager = this.getActivity().getAssets();
			if (savedInstanceState != null) {
				list = savedInstanceState.getStringArray(LISTKEY);
			} else {
				try {
					list = manager.list(folderPath);
				} catch (IOException e) {
					Log.v("mark", "GalleryListFragment oncreate cannot list file");
				}
			}
			retain = LruCacheRetainStateFragment.findOrCreateRetainFragment(this.getActivity().getFragmentManager(), "list");
			if (retain.mRetainedCache == null) {
				int maxMemory = (int) (list.length * 1.5 * 1024);
				retain.mRetainedCache = new LruCache<String, Bitmap>(maxMemory);// Math.min?
			}
		}
		
		public String toString(){
			return "GalleryListFragment";
		}

		private class BitMapLoader extends AsyncTask<String, Void, Void> {

			private ArrayList<WeakReference<ImageView>> imageReferenceList;
			private AssetManager manager;
			private String[] namelist;

			public BitMapLoader(AssetManager manager, ArrayList<WeakReference<ImageView>> imagelist) {
				imageReferenceList = imagelist;
				this.manager = manager;
			}

			@Override
			protected Void doInBackground(String... list) {
				for (int i = 0; i < list.length; i++) {
					Bitmap map = retain.mRetainedCache.get(list[i]);
					if (map == null) {
						try {
							String[] subnames = manager.list(folderPath + "/" + list[i]);
							String path = folderPath + "/" + list[i] + "/" + subnames[0];
							map = Utility.decodeSampledBitmapFromAsset(manager, path, imageDimension, imageDimension);
							retain.mRetainedCache.put(list[i], map);
						} catch (IOException e) {
							Log.v("mark", "BitMapLoader doInBackground IOException " + e.getMessage());
						}
					}
				}
				namelist = list;
				return null;
			}

			@Override
			protected void onPostExecute(Void v) {
				for (int i = 0; i < imageReferenceList.size(); i++) {
					ImageView iv = imageReferenceList.get(i).get();
					if (iv != null) {
						iv.setImageBitmap(retain.mRetainedCache.get(namelist[i]));
					}
				}
			}
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View view = inflater.inflate(R.layout.frag_gallerylist, container, false);
			ViewGroup vg = (ViewGroup) view.findViewById(R.id.gallerylist_frame);
			WaterFall layout = new WaterFall(this.getActivity(), 2);
			ArrayList<View> viewlist = new ArrayList<View>();
			ArrayList<WeakReference<ImageView>> imageReferenceList = new ArrayList<WeakReference<ImageView>>();
			for (int i = 0; i < list.length; i++) {
				View element = inflater.inflate(R.layout.view_gallerylist_element, null, false);
				ImageView imageView = (ImageView) element.findViewById(R.id.gallerylist_element_image);
				TextView titleView = (TextView) element.findViewById(R.id.gallerylist_element_title);
				titleView.setText(list[i]);
				element.setTag(list[i]);
				element.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						String tag = v.getTag().toString().trim();
						Activity act = getActivity();
						if (act instanceof ProjectInterface.GalleryInterface) {
							((ProjectInterface.GalleryInterface) act).galleryListItemClickCallBack(tag);
						}
					}
				});
				imageReferenceList.add(new WeakReference<ImageView>(imageView));
				viewlist.add(element);
			}
			BitMapLoader loader = new BitMapLoader(this.getActivity().getAssets(), imageReferenceList);
			loader.execute(list);
			for (View element : viewlist) {
				layout.addView(element);
			}
			ViewGroup.LayoutParams para = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
			vg.addView(layout.generateWaterFallLayout(), para);
			return view;
		}

		@Override
		public void onSaveInstanceState(Bundle outState) {
			super.onSaveInstanceState(outState);
			if (outState != null && list != null && list.length != 0) {
				outState.putStringArray(LISTKEY, list);
			}
		}
	}
	public static class GalleryWaterFallFragment extends Fragment {

		public static String PATHKEY = "path";
		private String path;

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			Bundle arg = this.getArguments();
			View view = inflater.inflate(R.layout.frag_gallerylist, container, false);
			if (arg != null && arg.containsKey(PATHKEY)) {
				path = arg.getString(PATHKEY);
				ViewGroup vg = (ViewGroup) view.findViewById(R.id.gallerylist_frame);
				WaterFall layout = new WaterFall(this.getActivity(), 2);
				ArrayList<View> viewlist = new ArrayList<View>();
				AssetManager manager = this.getActivity().getAssets();
				String[] names;
				try {
					names = this.getActivity().getAssets().list(path);
				} catch (IOException e) {
					names = new String[0];
				}
				for (int i = 0; i < names.length; i++) {
					String imagepath = path + "/" + names[i];
					View element = inflater.inflate(R.layout.view_gallerylist_element, null, false);
					ImageView imageView = (ImageView) element.findViewById(R.id.gallerylist_element_image);
					element.setTag(names[i]);
					element.setOnClickListener(listener);
					Utility.SampledBitMapLoader loader = new Utility.SampledBitMapLoader(imageView, manager, 400, 400);
					loader.execute(imagepath);
					viewlist.add(element);
				}
				for (View element : viewlist) {
					layout.addView(element);
				}
				ViewGroup.LayoutParams para = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
				vg.addView(layout.generateWaterFallLayout(), para);
			}
			return view;
		}

		public String toString(){
			return "GalleryWaterFallFragment";
		}
		private View.OnClickListener listener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Activity act = getActivity();
				String actual_path = path.replace("tn_", "");
				if (act instanceof ProjectInterface.GalleryInterface) {
					((ProjectInterface.GalleryInterface) act).galleryWaterFallItemClickCallBack(actual_path, v.getTag().toString().replace("tn_", ""));
				}
			}
		};

		@Override
		public void onSaveInstanceState(Bundle outState) {
			super.onSaveInstanceState(outState);
			if (path != null) outState.putString(PATHKEY, path);
		}
	}
	public static class GalleryDemoFragment extends Fragment {

		public final static String FOLDERKEY = "path";
		public final static String SELECTEDKEY = "selected";
		private final static String FILELIST = "flist";
		private final static String SELECTEDINDEX = "index";
		private String path = null, selected = null;
		private String[] fileNames;
		private int selectedIndex = -1;
		private View ground;
		
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			Bundle arg = this.getArguments();
			if (savedInstanceState != null) {
				fileNames = savedInstanceState.getStringArray(FILELIST);
				selectedIndex = savedInstanceState.getInt(SELECTEDINDEX);
				path = arg.getString(FOLDERKEY).replace("tn_", "");
				// retained =
				// RetainStateFragment.findOrCreateRetainFragment(manager,"demo");
			} else {
				if (arg.containsKey(FOLDERKEY)) {
					path = arg.getString(FOLDERKEY).replace("tn_", "");
					try {
						fileNames = this.getActivity().getAssets().list(path);
					} catch (IOException e) {
						Log.v("mark", "GalleryDemo cannot list filenames on " + path);
					}
				}
				if (arg.containsKey(SELECTEDKEY)) {
					selected = arg.getString(SELECTEDKEY).replace("tn_", "");
					boolean found = false;
					for (int i = 0; i < fileNames.length; i++) {
						if (!found && fileNames[i].trim().equals(selected)) {
							selectedIndex = i;
							break;
						}
					}
				}
			}
		}

		@Override
		public void onSaveInstanceState(Bundle outState) {
			super.onSaveInstanceState(outState);
			outState.putStringArray(FILELIST, fileNames);
			outState.putInt(SELECTEDINDEX, selectedIndex);
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			ground = inflater.inflate(R.layout.frag_gallerydemo, container, false);
			ViewPager pager = (ViewPager) ground.findViewById(R.id.gallerypager);
			GalleryDemoImagePageAdapter adapter = new GalleryDemoImagePageAdapter(this.getActivity().getAssets(), path, fileNames);
			pager.setAdapter(adapter);
			pager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

				@Override
				public void onPageSelected(int position) {
					selectedIndex = position;
				}
			});
			if (selectedIndex >= 0) pager.setCurrentItem(selectedIndex);
			return ground;
		}
	
		public String toString(){
			if(ground!=null){
				ViewPager pager = (ViewPager)ground.findViewById(R.id.gallerypager);
				if(pager!=null){return "GalleryDemoFragment:"+pager.getCurrentItem();	
				}else{return "GalleryDemoFragment";}
			}else{
				return "GalleryDemoFragment";
			}
		}
	}
	public static class GalleryDemoImagePageAdapter extends PagerAdapter {

		private String[] list;
		private AssetManager manager;
		private String path;

		public GalleryDemoImagePageAdapter(AssetManager manager, String path, String[] listname) {
			list = listname;
			this.manager = manager;
			this.path = path;
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public int getCount() {
			return list.length;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			final TouchImageView tiv = new TouchImageView(container.getContext());
			Bitmap map = Utility.decodeSampledBitmapFromAsset(manager, path + "/" + list[position], 500, 500);
			tiv.setImageBitmap(map);
			tiv.setTag(list[position]);
			tiv.setLayoutParams(new ViewGroup.LayoutParams(100, 100));
			container.addView(tiv);
			return tiv;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			View v = container.findViewWithTag(list[position]);
			container.removeView(v);
		}

		@Override
		public int getItemPosition(Object object) {
			return POSITION_UNCHANGED;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return list[position];
		}
	}
	public static class GalleryCacheDemoPageAdapter extends PagerAdapter {

		private String[] list;
		private LruCache<String, Bitmap> imagedata;

		public GalleryCacheDemoPageAdapter(String[] listname, LruCache<String, Bitmap> dataset) {
			list = listname;
			imagedata = dataset;
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public int getCount() {
			return list.length;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			final TouchImageView tiv = new TouchImageView(container.getContext());
			Bitmap map = imagedata.get(list[position]);
			tiv.setImageBitmap(map);
			tiv.setTag(list[position]);
			tiv.setLayoutParams(new ViewGroup.LayoutParams(100, 100));
			container.addView(tiv);
			return tiv;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			View v = container.findViewWithTag(list[position]);
			container.removeView(v);
		}

		@Override
		public int getItemPosition(Object object) {
			return POSITION_UNCHANGED;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return list[position];
		}
	}
	public static class GalleryDemoPagerAdapter extends PagerAdapter {

		private View[] list;

		public GalleryDemoPagerAdapter(View[] list) {
			this.list = list;
		}

		@Override
		public int getCount() {
			return list.length;
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			View iv = list[position];
			iv.setLayoutParams(new ViewGroup.LayoutParams(100, 100));
			container.addView(iv);
			return iv;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			View v = list[position];
			container.removeView(v);
		}

		@Override
		public void finishUpdate(ViewGroup container) {
			super.finishUpdate(container);
		}

		@Override
		public int getItemPosition(Object object) {
			return POSITION_UNCHANGED;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return list[position].getTag().toString();
		}
	}
}
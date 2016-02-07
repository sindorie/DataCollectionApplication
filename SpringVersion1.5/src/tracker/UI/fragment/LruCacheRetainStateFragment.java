package tracker.UI.fragment;

import java.util.Map;
import android.app.Fragment;
import android.app.FragmentManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.util.LruCache;


public class LruCacheRetainStateFragment extends Fragment {
    private static final String TAG = "RetainFragment";
    public LruCache<String, Bitmap> mRetainedCache;
    public LruCache genericStoragePlace;
    
    
    public LruCacheRetainStateFragment() {}

    public static LruCacheRetainStateFragment findOrCreateRetainFragment(FragmentManager fm) {
    	LruCacheRetainStateFragment fragment = (LruCacheRetainStateFragment) fm.findFragmentByTag(TAG);
        if (fragment == null) {
            fragment = new LruCacheRetainStateFragment();
            fm.beginTransaction().add(fragment, TAG).commit();
        }
        return fragment;
    }
    public static LruCacheRetainStateFragment removeAndRecreateRetainFragment(FragmentManager fm, String tag){
    	LruCacheRetainStateFragment fragment = (LruCacheRetainStateFragment) fm.findFragmentByTag(TAG+tag);
    	if(fragment != null){
    		fm.beginTransaction().remove(fragment).commit();
    		fragment.onDestroy();
    	}
    	fragment = new LruCacheRetainStateFragment();
        fm.beginTransaction().add(fragment, TAG+tag).commit();
    	return fragment;
    }
    
    public static LruCacheRetainStateFragment findOrCreateRetainFragment(FragmentManager fm, String tag) {
    	LruCacheRetainStateFragment fragment = (LruCacheRetainStateFragment) fm.findFragmentByTag(TAG+tag);
        if (fragment == null) {
            fragment = new LruCacheRetainStateFragment();
            fm.beginTransaction().add(fragment, TAG+tag).commit();
        }
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }
}
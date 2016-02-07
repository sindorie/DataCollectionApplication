package tracker.UI.fragment;


import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;


public class FileWriterFragment extends Fragment {
	public final static String TAG = "FileWriterFragment";
	private Map<String, BufferedOutputStream> streams;
	public void createFileOutputStream(int mode, String... names){
		if(streams == null) streams = new HashMap<String,BufferedOutputStream>();
		for(String name : names){
			try {
				FileOutputStream fout = act.openFileOutput(name, mode);
				BufferedOutputStream newStream = new BufferedOutputStream(fout);
				BufferedOutputStream original = streams.get(name);
				if(original == null){
					streams.put(name, newStream);
				}else{
					switch(mode){
					case Context.MODE_APPEND:{
						//do nothing
					}break;
					default:{
						try { original.close();
						} catch (IOException e) {}
						streams.put(name, newStream);
					}
					}
				}
			} catch (FileNotFoundException e) {}
		}
	}
	public void writeToStream(String name, String data){
		try {
			BufferedOutputStream bos = streams.get(name);
			if(bos != null){ bos.write(data.getBytes()); }
		} catch (IOException e) {}
	}
	
	public void removeAndCloseAll(){
		Set<String> keys = streams.keySet();
		for(String key : keys){
			removeAndCloseStreamByName(key);
		}
	}
	public void removeAndCloseStreamByName(String name){
		BufferedOutputStream bos = streams.get(name);
		try { bos.close();
		} catch (IOException e) { }
		streams.remove(name);
	}
	public int getStreamAmount(){
		if(streams == null) return 0;
		return this.streams.size();
	}
	public boolean isStreamAvailable(String name){
		if(streams == null) return false;
		BufferedOutputStream bos = streams.get(name);
		return bos != null;
	}
	@Override
	public void onDestroy (){
		this.removeAndCloseAll();
		super.onDestroy();
	}

	public static FileWriterFragment findOrCreateRetainFragment(Activity act) {
		FragmentManager fm = act.getFragmentManager();
		FileWriterFragment fragment = (FileWriterFragment) fm.findFragmentByTag(TAG);
        if (fragment == null) {
            fragment = new FileWriterFragment();
            fragment.act = act;
            fm.beginTransaction().add(fragment, TAG).commit();
        }
        return fragment;
    }
    public static FileWriterFragment removeAndRecreateRetainFragment(Activity act, String tag){
    	FragmentManager fm = act.getFragmentManager();
    	FileWriterFragment fragment = (FileWriterFragment) fm.findFragmentByTag(TAG+tag);
    	if(fragment != null){
    		fm.beginTransaction().remove(fragment).commit();
    		fragment.onDestroy();
    	}
    	fragment = new FileWriterFragment();
    	fragment.act = act;
    	fm.beginTransaction().add(fragment, TAG+tag).commit();
    	return fragment;
    }
    
    public static FileWriterFragment findOrCreateRetainFragment(Activity act, String tag) {
    	FragmentManager fm = act.getFragmentManager();
    	FileWriterFragment fragment = (FileWriterFragment) fm.findFragmentByTag(TAG+tag);
        if (fragment == null) {
            fragment = new FileWriterFragment();
            fragment.act = act;
            fm.beginTransaction().add(fragment, TAG+tag).commit();
        }
        return fragment;
    }
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }
    private Activity act;
    public void setActivity(Activity act){
    	this.act = act;
    }
}

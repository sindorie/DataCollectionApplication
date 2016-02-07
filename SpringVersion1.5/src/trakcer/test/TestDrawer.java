package trakcer.test;

import tracker.UI.activity.FrameActivity;
import tracker.springversion1.R;
import tracker.springversion1.R.layout;
import tracker.utility.Utility;
import android.app.FragmentManager;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class TestDrawer extends FrameActivity{

	@Override
	public void setCenterContent(Bundle savedInstanceState) {
		int toReplace = this.getCenterViewId();
        FragmentManager fragmentManager = getFragmentManager();
        Fragment fragment = new TestDrawerFragment();
        fragmentManager.beginTransaction().replace(toReplace, fragment).commit();
	}
	
	public static class TestDrawerFragment extends Fragment{
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
        	View view = inflater.inflate(R.layout.frag_test, container,false); 
 
//        	Utility.fitViewToWindow(view, getActivity());
            return view;
        }
	}
}

package tracker.UI.components;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

public class SimplePageAdapter extends PagerAdapter {
	private Activity act;
	private int[] ids;
	public SimplePageAdapter(Activity act , int[] ids){
		this.act = act;
		this.ids = ids;
	}

	@Override
	public int getCount() { 
		if(ids == null) return 0;
		return ids.length;
	}
	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == arg1;
	}
	@Override
	public Object instantiateItem(ViewGroup container, int position){
		View toshow = act.findViewById(ids[position]);
		if(toshow.getParent()!=null){
			((ViewGroup)toshow.getParent()).removeView(toshow);
		}
		container.addView(toshow);
		return toshow;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object){
		container.removeView(((View)object));
	}

}

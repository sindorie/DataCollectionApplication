package tracker.UI.components;

import android.app.Activity;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

public class MyPageAdapter<T extends MyPageAdapter.GeneratePageView> extends PagerAdapter {
	private T[] todisplay;
	private Activity act;
	public MyPageAdapter(Activity act , T[] input){
		this.todisplay = input;this.act = act;
	}
	
	@Override
	public int getCount() {
		return todisplay==null? 0:todisplay.length;
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == arg1;
	}

	public Object instantiateItem(ViewGroup container, int position){
		View toshow = todisplay[position].getPageView(act,container,position);
		ViewGroup vg = (ViewGroup)toshow.getParent();
		if(vg != null) vg.removeView(toshow);
		container.addView(toshow);
		return toshow;
	}

	public void destroyItem(ViewGroup container, int position, Object object){
//		container.removeViewAt(position);
	}
	
	public T getElementAt(int posiiton){
		return todisplay[posiiton];
	}
	
	public static interface GeneratePageView{
		public View getPageView(Activity act,ViewGroup container, int position);
	}
}

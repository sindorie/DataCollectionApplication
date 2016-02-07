package tracker.UI.components;

import java.util.ArrayList;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class MyArrayListAdapter<T extends MyArrayListAdapter.GenerateView> extends BaseAdapter {

	public interface GenerateView {
		public View getView(Activity act, int position, View convertView,
				ViewGroup parent);
		public View getDropDownView (Activity act, int position, View convertView, ViewGroup parent);
	}

	private boolean buildHiarachy = false;
	private ArrayList<T> list = new ArrayList<T>();
	private Activity act;

	public MyArrayListAdapter(Activity act, T list[]) {
		for(T element : list){ this.list.add(element); }
		this.act = act;
	}
	
	@Override 
	public View getDropDownView (int position, View convertView, ViewGroup parent){
		return list.get(position).getDropDownView(act, position, convertView, buildHiarachy ? parent : null);
	}
	
	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {// don't care now
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return list.get(position).getView(act, position, convertView, buildHiarachy ? parent : null);
	}

	@SuppressWarnings("unchecked")
	public T[] getList(){
		return (T[]) list.toArray(new Object[0]);
	}
	public void addElement(T element){
		list.add(element);
	}
	public void addElement(T element,int position){
		list.add(position,element);
	}
//	public void addView(View view, int position){
//		
//	}
	
	
	/**
	 * By default this adapter passes null as the parameter of parent for the
	 * getView method. It is necessary as the listView does not support addView
	 * 
	 * @param state
	 */
	public void setBuildHiarachy(boolean state) {
		this.buildHiarachy = state;
	}
}

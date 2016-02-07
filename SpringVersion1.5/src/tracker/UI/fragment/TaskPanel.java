package tracker.UI.fragment;

import java.util.ArrayList;
import java.util.Random;
import tracker.UI.activity.FrameActivity;
import tracker.UI.view.FixedGridLayout;
import tracker.UI.view.FixedGridLayout.FixedGridLayoutParams;
import tracker.UI.view.SquareDragShadowBuilder;
import tracker.springversion1.Common;
import tracker.springversion1.R;
import tracker.springversion1.Common.Task;
import tracker.springversion1.R.drawable;
import tracker.springversion1.R.id;
import tracker.springversion1.R.layout;
import tracker.utility.Utility;
import android.app.Activity;
import android.app.Fragment;
import android.content.ClipData;
import android.content.ClipDescription;
import android.os.Bundle;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class TaskPanel {

	public static class TaskPanelFragment extends Fragment {

		private String[] taskList = Common.Task.taskList;
		private Class[] fragmentList = { Contact.ContactList.class, Foodie.FoodieRestaurantList.class, Gallery.GalleryListFragment.class, Bank.BankFragment.class, News.NewsFragment.class,
				SocialMedia.SocialMediaFragment.class };
		private int[] imageId = { R.drawable.icon_contact, R.drawable.icon_foodie, R.drawable.icon_gallery, R.drawable.icon_bank, R.drawable.icon_news, R.drawable.icon_social };
		private FixedGridLayout layout;
		private LruCacheRetainStateFragment recordFragment = null;
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View ground = inflater.inflate(R.layout.frag_task, container, false);
			layout = (FixedGridLayout) ground.findViewById(R.id.task_grid);
			layout.setOnDragListener(dragListener);
			recordFragment = LruCacheRetainStateFragment.findOrCreateRetainFragment(this.getActivity().getFragmentManager(), "taskview");
			if (recordFragment.genericStoragePlace == null) {
				Log.v("mark", "recordFragment.genericStoragePlace is null");
				recordFragment.genericStoragePlace = new LruCache<String, Integer>(1024 * 2);// 2KB
			}
			if (recordFragment.genericStoragePlace.size() == 0) {// check empty
				// populate
				initReocrdMap(recordFragment.genericStoragePlace, layout.getRow() * layout.getCol());
			}
			for (int i = 0; i < taskList.length; i++) {
				String name = taskList[i];
				View view = inflater.inflate(R.layout.view_taskitem, layout, false);
				view.setTag(name);
				view.setClickable(true);// for now TODO
				view.setOnClickListener(listener);
				ImageView iv = (ImageView) view.findViewById(R.id.taskitem_image);
				TextView tv = (TextView) view.findViewById(R.id.taskitem_text);
				iv.setImageResource(imageId[i]);
				tv.setText(name);
				view.setOnLongClickListener(new View.OnLongClickListener() {

					@Override
					public boolean onLongClick(View v) {
						ClipData.Item item = new ClipData.Item((CharSequence) v.getTag());
						String[] mimeTypes = { ClipDescription.MIMETYPE_TEXT_PLAIN };
						ClipData dragData = new ClipData(v.getTag().toString(), mimeTypes, item);
						View.DragShadowBuilder myShadow = new SquareDragShadowBuilder(v);
						v.startDrag(dragData, myShadow, null, 0);
						return true;
					}
				});
				view.setOnTouchListener(((FrameActivity)this.getActivity()).getTouchListener());
				int position = (Integer) recordFragment.genericStoragePlace.get(name);
				layout.addView(view, position);
			}
			return ground;
		}

		public String getTaskIconMap() {
			if (recordFragment == null || recordFragment.genericStoragePlace == null) return "";
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < this.taskList.length; i++) {
				String name = taskList[i];
				sb.append(name + ":" + recordFragment.genericStoragePlace.get(name) + ";");
			}
			Log.v("mark", "getTaskIconMap\n" + sb.toString());
			return sb.toString();
		}

		public String toString(){
			return "TaskPanelFragment";
		}
		
		@Override
		public void onSaveInstanceState(Bundle outState) {
			super.onSaveInstanceState(outState);
		}
		private View.OnClickListener listener = new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				String name = arg0.getTag().toString().trim();
				boolean result = true;
				Activity act = getActivity();
				if (act instanceof FrameActivity) {
					result = ((FrameActivity) act).getMobileStudyApplication().requestAction(name);
				}
				if (result == false) {
					Utility.displayErrorMessage("cannot display " + name, act);
					return;
				}
				int index = -1;
				for (int i = 0; i < taskList.length; i++) {
					if (taskList[i].equals(name)) {
						index = i;
						break;
					}
				}
				if (index >= 0 && index < fragmentList.length && fragmentList[index] != null) {
					goToTask(fragmentList[index]);
				}
			}
		};

		private <T extends Fragment> void goToTask(Class<T> clz) {
			try {
				Fragment frag = clz.newInstance();
				Activity act = this.getActivity();
				if (act instanceof FrameActivity) {
					((FrameActivity) act).replaceTheCenterView(frag, "level0");
				}
			} catch (java.lang.InstantiationException e) {
				Log.e("mark", e.getClass() + "" + e.getMessage());
			} catch (IllegalAccessException e) {
				Log.e("mark", e.getClass() + "" + e.getMessage());
			}
		}
		private View.OnDragListener dragListener = new View.OnDragListener() {

			private int original_index;

			@Override
			public boolean onDrag(View view, DragEvent event) {
				if (layout == null) return false;
				ClipDescription description = event.getClipDescription();
				if (description == null || !description.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
					Log.v("Condition", "description is null, type: " + event.getAction());
					return false; // should not happen for most events except
									// ACTION_DRAG_ENDED
				}
				switch (event.getAction()) {
				case DragEvent.ACTION_DRAG_STARTED: {
					ViewGroup vg = (ViewGroup) view;
					View child = vg.findViewWithTag(description.getLabel());
					ViewGroup.LayoutParams param = child.getLayoutParams();
					if (param instanceof FixedGridLayoutParams) {
						original_index = ((FixedGridLayoutParams) param).getPositionIndex();
					}
					child.setVisibility(View.INVISIBLE);
					Log.v("mark", "starting index:" + original_index);
				}
					break;
				case DragEvent.ACTION_DRAG_LOCATION: {}
					break;
				case DragEvent.ACTION_DRAG_ENTERED: {}
					break;
				case DragEvent.ACTION_DRAG_EXITED: {}
					break;
				case DragEvent.ACTION_DRAG_ENDED: {}
					break;
				case DragEvent.ACTION_DROP: {
					float currentX = event.getX();
					float currentY = event.getY();
					int end_index = layout.pixelToPositionIndex((int) currentX, (int) currentY);
					Log.v("mark", "end_index: " + end_index);
					if (original_index == end_index) {
						layout.findViewByPos(original_index).setVisibility(View.VISIBLE);
					} else {
						View inLocation = layout.findViewByPos(end_index);
						if(inLocation != null){
							Toast.makeText(getActivity(), "Cannot overlap", Toast.LENGTH_SHORT).show();;
							layout.findViewByPos(original_index).setVisibility(View.VISIBLE);	
						}else{
							View movedView = layout.findViewByPos(original_index);
							layout.removeViewAtPos(original_index);
							layout.addView(movedView, end_index);
							layout.invalidate();
							movedView.setVisibility(View.VISIBLE);
							// update the informtion in record fragment
							recordFragment.genericStoragePlace.put(movedView.getTag().toString(), end_index);
							Activity act = getActivity();
							if (act instanceof FrameActivity) {
								((FrameActivity) act).getMobileStudyApplication().requestVerification(0, "drop");
							}
						}	
					}
				}
					break;
				}
				return true;
			}
		};

		private void initReocrdMap(LruCache<String, Integer> record, int max) {
			Activity act = this.getActivity();
			int id = ((FrameActivity) act).getMobileStudyApplication().getCurrentInstructionId();
			if (id >= 0 && id <= 1) {
				Random generator = new Random(5);
				ArrayList<Integer> arr = new ArrayList<Integer>();
				for (int i = 0; i < taskList.length; i++) {
					int temp;
					do {
						temp = generator.nextInt(max);
						if (!arr.contains(temp)) {
							arr.add(temp);
							break;
						}
					} while (true);
					record.put(taskList[i], temp);
				}
			} else {
				for (int i = 0; i < taskList.length; i++) {
					record.put(taskList[i], i);
				}
			}
		}
	}
}

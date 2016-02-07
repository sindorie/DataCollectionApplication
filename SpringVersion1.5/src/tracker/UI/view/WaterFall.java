package tracker.UI.view;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import tracker.utility.Utility;
import tracker.utility.Utility.BitmapWorkerTask;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

public class WaterFall {
	private Context context;
	private Activity act;
	private int col;
	private int count = 0;
	private View.OnClickListener onclick;
	private double ratio = 1;
	public WaterFall(Activity act, int col){
		this.context = act.getApplicationContext();this.col = col;
		height = new int[col];this.act = act;
		for(int i=0;i<col;i++)height[i] = 0;
	}
	
	private ArrayList<View> imageViewList = new ArrayList<View>();
	private int[] height;
	
	public void addImagesByPaths(String[] paths, String[] tags){
		AssetManager manager = act.getAssets();
//		InputStream istr = null;
		for(int i=0;i<paths.length;i++){
			ImageView iv = new ImageView(context);
			
			Utility.BitmapWorkerTask loader = new Utility.BitmapWorkerTask(iv,manager);
			loader.execute(paths[i]);
			iv.setTag(tags[i]);
			imageViewList.add(iv);
//			try {
//		    	String path = paths[i];
//				istr = manager.open(path);
//			    Bitmap bitmap = BitmapFactory.decodeStream(istr);
//				iv.setImageBitmap(bitmap);
//				iv.setTag(tags[i]);
//				imageViewList.add(iv);
//			} catch (IOException e) {
//				Log.v("mark","water fall decode image fails:"+e.getMessage());
//			}
		}
	}
	
	public void addView(View v){
		imageViewList.add(v);
	}
	public void setRatio(double ratio){
		this.ratio = ratio;
	}
	
	public void setOnItemClickListener(View.OnClickListener onclick){
		this.onclick = onclick;
	}
	
	public ScrollView generateWaterFallLayout(){
		final ScrollView scrollView = new ScrollView(context);

		LinearLayout topLinear = new LinearLayout(context);
		topLinear.setOrientation(LinearLayout.HORIZONTAL);
		topLinear.setWeightSum(col);
		LinearLayout.LayoutParams param_toplinear = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		scrollView.addView(topLinear,param_toplinear);
		
		final LinearLayout[] lines = new LinearLayout[col];
		for(int i=0;i<col;i++){
			lines[i] = new LinearLayout(context);
			lines[i].setOrientation(LinearLayout.VERTICAL);
			LinearLayout.LayoutParams param_line = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
//			lines[i].setPadding(2,2,2,2);
			//left, top, right, bottom
			param_line.weight = 1;
			topLinear.addView(lines[i],param_line);
		}
		
		final ViewTreeObserver viewTreeObserver = scrollView.getViewTreeObserver();
		if(viewTreeObserver.isAlive()){
			viewTreeObserver.addOnGlobalLayoutListener(new OnGlobalLayoutListener(){
				@Override
				public void onGlobalLayout() {
					Log.v("on global layout","mark");
					addImage(lines);
					scrollView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				}
			});
		}
		return scrollView;
	}
	
	
	private void addImage(LinearLayout[] lines){
		for(View iv : imageViewList){
			//choose the lowest height
			int index = getIndex();
			//calculate the intended height of imageview
			LinearLayout target = lines[index];
			int layout_width = target.getWidth() - 5;
			int layout_height = (int) (layout_width*ratio);
//			BitmapDrawable bd = (BitmapDrawable)iv.getDrawable();
//			float image_height = bd.getBitmap().getHeight();
//			float image_width = bd.getBitmap().getWidth();
//			int intendedHeight = (int) (layout_width/image_width * image_height);
			
			//add the imageview to layout
			LinearLayout.LayoutParams image_param = new LinearLayout.LayoutParams(layout_width,layout_height);			
			target.addView(iv,image_param);
			iv.setPadding(2, 2, 2, 2);
			if(iv instanceof ImageView){
				((ImageView)iv).setScaleType(ImageView.ScaleType.CENTER_CROP);
			}			
			if(onclick!=null)iv.setOnClickListener(onclick);
		}
	}
	
	
	private int getIndex(){
		int temp = count%col;
		count+=1;
		return temp;
	}
	
	private int getLowestCol(){
		int lowest = 0;
		for(int i=1;i<col;i++){
			if(height[i] < height[i-1]){
				lowest = i;
			}
		}
		return lowest;
	}
}

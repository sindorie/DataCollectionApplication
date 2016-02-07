package deprecated;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import tracker.springversion1.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

public class MyGridLayout extends RelativeLayout{
	private int col,row,rowStep, colStep;
	private Context context;
	private int leftPadding, topPadding;
	private SparseArray<ViewBundle> gridBundle;
	private boolean enableOverlay = false;
	public MyGridLayout(Context context) { 
		this(context,4,5);
	}
	public MyGridLayout(Context context, AttributeSet attrs) {
		super(context,attrs);
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
			    R.styleable.MyGridLayout, 0, 0);
		this.col = a.getInteger(R.styleable.MyGridLayout_col_count, 0);
		this.row = a.getInteger(R.styleable.MyGridLayout_row_count, 0);
		if(col <= 0) col = 1;
		if(row <= 0) row = 1;
		a.recycle();
		initGirdBundle();
	}
	public MyGridLayout(Context context, int col, int row) {
		super(context);this.col = col; this.row = row;
		initGirdBundle();
	}
	
	@Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
	}
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		Log.v("mark","MyGridLayout onLayout, changed:"+changed);
		leftPadding = l;
		topPadding = t;
		if(changed){
			this.removeAllViews();
			int availableWidth = this.getWidth() - this.getPaddingLeft() - this.getPaddingRight();
			int availableHeight = this.getHeight() - this.getPaddingTop() - this.getPaddingBottom();
			leftPadding = this.getPaddingLeft();
			topPadding = this.getPaddingTop();
			this.rowStep = availableHeight / row;
			this.colStep = availableWidth / col;
		
			for(int index = 0 ;index<row*col;index++){
				addpartialView(index);
			}
		}
		super.onLayout(changed, l, t, r, b);
	}
	public void refresh(){
		initGirdBundle();
	}
	public int getRow(){
		return this.row;
	}
	public int getCol(){
		return this.col;
	}
	public int getMaxIndex(){
		return row*col;
	}
	public void hideViewAt(int index){
		ViewBundle vb = this.gridBundle.get(index);
		if(vb == null){
			return;
		}
		if(!vb.isEmpty()){
			vb.displayed.setVisibility(View.INVISIBLE);
		}
	}
	
	public void showViewAt(int index){
		ViewBundle vb = this.gridBundle.get(index);
		if(vb == null){
			return;
		}
		if(!vb.isEmpty()){
			vb.displayed.setVisibility(View.VISIBLE);
		}
	}
	
	private void addpartialView(int index){
		ViewBundle spot = gridBundle.get(index);
		if(spot == null) return;//should not be
		View displayed = spot.displayed;
		if(displayed == null) return;
		
		RelativeLayout.LayoutParams rlp = spot.rlp;
		int x = index%col;
		int y = index/col;
		rlp.leftMargin = x*colStep;
		rlp.topMargin = y*rowStep;
		rlp.height = (int) (rowStep*1.1);
		rlp.width = colStep;
		
		ViewGroup vg = (ViewGroup)displayed.getParent();
		if(vg != null) vg.removeView(displayed);
		super.addView(displayed,rlp);
	}
	
	public Map<String,Integer> getTagIndexMap(){
		Map<String,Integer> tagIndexMap = new HashMap<String,Integer>();
		for(int i=0;i<col*row;i++){
			ViewBundle vb = gridBundle.get(i);
			if(vb.isEmpty()) continue;
			for(View v: vb.vlist){
				tagIndexMap.put((String) v.getTag(), i);
			}
		}
		return tagIndexMap;
	}
	
	
	public View[] getViewAt(int x, int y){//get views from a certain spot
		int index = this.coordinatesToIndex(new Point(x,y));
		ArrayList<View> vlist = this.gridBundle.get(index).vlist;
		if(vlist.isEmpty()) return null;
		else return vlist.toArray(new View[0]);
	}

	public int actualPixelToIndex(int x, int y){
		int colComponent = (x-leftPadding)/this.colStep;
		int rowComponent = (y-topPadding)/this.rowStep;
		Log.v("mark","col&row step:"+colStep+" "+rowStep);
		Log.v("mark","lef&top pad:"+leftPadding+" "+topPadding);
		Log.v("mark","x&y:"+x+" "+y);
		Log.v("mark","components:  "+colComponent+" "+rowComponent);
		return coordinatesToIndex(colComponent,rowComponent);
	}
	
	public void addView(View v){
		for(int i=0;i<col*row;i++){
			if(gridBundle.get(i).isEmpty()){ gridBundle.get(i).addView(v); break; }
		}
	}
	public void addView(View v, Point p){ addView(v,p.x,p.y); }
	public void addView(View v, int x, int y){
		int index = coordinatesToIndex(x,y);
		ViewBundle spot = gridBundle.get(index);
		spot.addView(v);
	}
	public void addView(View v, int index){
		ViewBundle spot = gridBundle.get(index);
		spot.addView(v);
	}
	
	public void transportView(int sourceIndex, int destIndex){
		if(sourceIndex == destIndex) return ;
		
		ViewBundle spot_src = gridBundle.get(sourceIndex);
		ViewBundle spot_dest = gridBundle.get(destIndex);
		if(spot_src.displayed==null) return;//should not be
		
		if(spot_dest.isEmpty() || enableOverlay){
			spot_dest.addFromViewBundle(spot_src);
			spot_src.clear();
			addpartialView(destIndex);
		}else{ showViewAt(sourceIndex); }
	}

	private int coordinatesToIndex(Point p){ return coordinatesToIndex(p.x,p.y); }
	private int coordinatesToIndex(int x, int y){
		if(x<0 || y< 0 || x >= col || y >= row){ throw new IndexOutOfBoundsException(); }
		int index = x +  y*col; return index;
	}
	private Point indexToCoordinates(int index){ return new Point(index/col,index%col); }
	private void initGirdBundle(){
		gridBundle = new SparseArray<ViewBundle>();
		for(int i=0;i<row*col;i++){ gridBundle.put(i, new ViewBundle()); }
	}
	
	public int getIndex(View v){
		int result = -1;
		for(int i = 0 ;i<col;i++){
			for(int j =0;j<row;j++){
				int index = coordinatesToIndex(i,j);
				ViewBundle bundle = gridBundle.get(index);
				if(bundle!=null && !bundle.isEmpty() && bundle.displayed == v){
					return index;
				}
			}
		}
		return result;
	}
	
	private class ViewBundle{
		public ViewBundle(){
			displayed = null;
			vlist = new ArrayList<View>();
			rlp = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.WRAP_CONTENT,
					RelativeLayout.LayoutParams.WRAP_CONTENT);
		}
		View displayed;
		ArrayList<View> vlist;
		RelativeLayout.LayoutParams rlp;
		public boolean isEmpty(){
			return vlist.isEmpty();
		}
		public void clear(){ displayed = null; vlist.clear(); }
		public void addView(View v){vlist.add(v); updateDisplayedView();}
		public void addFromViewBundle(ViewBundle other){
			this.vlist.addAll(other.vlist);
			updateDisplayedView();
		}
		public void removeView(View v){ vlist.remove(v); updateDisplayedView(); }
		private void updateDisplayedView(){
			if(vlist.isEmpty()){ displayed = null; return;
			}else if(vlist.size() == 1){ displayed = vlist.get(0);
			
			}else{ displayed = vlist.get(0); }//TODO -- can be modified
			displayed.setVisibility(View.VISIBLE);
		}
	}
}
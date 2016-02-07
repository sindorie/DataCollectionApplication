package tracker.UI.view;

import tracker.springversion1.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

public class FixedGridLayout extends ViewGroup {

	private int row = 1, col = 1;
	private boolean gridOccupation[][];
	private boolean allowOverLay = false;
	private int cellWidth, cellHeight;
	private boolean childViewChanged = false;
	public FixedGridLayout(Context context) {
		super(context);
		gridOccupation = new boolean[row][col];
	}

	public FixedGridLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.FixedGridLayout, 0, 0);
		this.col = a.getInteger(R.styleable.FixedGridLayout_col, 0);
		this.row = a.getInteger(R.styleable.FixedGridLayout_row, 0);
		if (col <= 0) col = 1;
		if (row <= 0) row = 1;
		a.recycle();
		gridOccupation = new boolean[row][col];
	}
	
	public static class FixedGridLayoutParams extends ViewGroup.MarginLayoutParams {

		public final static int UPTO_PARENT = -1;
		private int positionIndex;

		public FixedGridLayoutParams(int positionIndex) {
			super(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
			this.positionIndex = positionIndex;
		}

		public FixedGridLayoutParams(LayoutParams param) {
			super(param);
			if (param instanceof FixedGridLayoutParams) {
				this.positionIndex = ((FixedGridLayoutParams) param).positionIndex;
			} else {
				this.positionIndex = UPTO_PARENT;
			}
		}
		
		public FixedGridLayoutParams(LayoutParams param, int index) {
			super(param);
			this.positionIndex = index;
		}

		public int getPositionIndex() {
			return this.positionIndex;
		}
		
		public void setPositionIndex(int index){
			this.positionIndex = index;
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		
		final int mode_w = MeasureSpec.getMode(widthMeasureSpec);
		final int mode_h = MeasureSpec.getMode(heightMeasureSpec);
		final int width = MeasureSpec.getSize(widthMeasureSpec);
		final int height = MeasureSpec.getSize(heightMeasureSpec);
		
		final int pw = getPaddingLeft() + getPaddingRight();
		final int ph = getPaddingTop() + getPaddingBottom();	
		
		int measuredX = width, measuredY = height;
		int intendedCellWith = (width -pw)/col;
		int intendedCellHeight = (height - ph)/row;
		
		for(int i=0;i<this.getChildCount();i++){
			View child = this.getChildAt(i);
			if(child == null) continue;
			measureChildWithMargins(child, 
					MeasureSpec.makeMeasureSpec(intendedCellWith, MeasureSpec.EXACTLY), 0, 
					MeasureSpec.makeMeasureSpec(intendedCellHeight, MeasureSpec.EXACTLY), 0);
		}
		
		setMeasuredDimension(measuredX,measuredY);
	}

	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// calculate boundary
		int pL = this.getPaddingLeft();
		int pR = this.getPaddingRight();
		int pT = this.getPaddingTop();
		int pB = this.getPaddingBottom();
		cellWidth = (r - l - pL - pR) / col;
		cellHeight = (b - t - pT - pB) / row;
		
		if(!childViewChanged && !changed){
			return;
		}
		for (int i = 0; i < this.getChildCount(); i++) {
			View view = this.getChildAt(i);
			if (view == null) continue; // should not be
			LayoutParams rawParam = view.getLayoutParams();
			if (rawParam == null || !(rawParam instanceof FixedGridLayoutParams)) {
				Log.v("mark", "FixedGridLayout getLayoutParams " + rawParam);
				continue;
			}
			FixedGridLayoutParams param = (FixedGridLayoutParams) rawParam;
			int positionIndex = param.getPositionIndex();
			if (positionIndex == FixedGridLayoutParams.UPTO_PARENT) {
				// find the first empty spot
				int x = 0, y = 0;
				boolean found = false;
				Loop:for (y = 0; y < row; y++) {
					for (x = 0; x < col; x++) {
						if (gridOccupation[y][x] == false) {
							found = true;
							break Loop;
						}
					}
				}
				if (found) {
					int newPositionIndex = xyToPositionIndex(x,y);
					Point startingXY = calculatePixelPosition(new Point(x,y));
					view.layout(startingXY.x, startingXY.y, startingXY.x + cellWidth, startingXY.y + cellHeight);
					LayoutParams originalParam = view.getLayoutParams();
					if(originalParam instanceof FixedGridLayoutParams){
						((FixedGridLayoutParams) originalParam).setPositionIndex(newPositionIndex);
					}
					gridOccupation[y][x] = true;
				}
			} else if (positionIndex < row*col) {
				Point xy = positionIndexToXY(positionIndex);
				Point startingXY = calculatePixelPosition(xy);
				
				if (gridOccupation[xy.y][xy.x]) {
					if (allowOverLay) {
						view.layout(startingXY.x, startingXY.y, startingXY.x + cellWidth, startingXY.y + cellHeight);
					} else {
						// oh no ...
					}
				} else {
					view.layout(startingXY.x, startingXY.y, startingXY.x + cellWidth, startingXY.y + cellHeight);
					gridOccupation[xy.y][xy.x] = true;
				}
			} else {
				// wont be shown
				Log.v("mark","FixedGridLayout onLayout -- !.. should not be here");
			}
		}
		childViewChanged = false;
	}

	public Point positionIndexToXY(int positionIndex) {
		return new Point(positionIndex % col, positionIndex / row);
	}

	public int xyToPositionIndex(Point xy) {
		return xy.x % col + xy.y * row;
	}

	public int xyToPositionIndex(int x, int y) {
		return x % col + y * row;
	}

	public Point calculatePixelPosition(Point xy){
		int startingX = xy.x * cellWidth + this.getPaddingLeft();
		int startingY = xy.y * cellHeight + this.getPaddingTop();
		return new Point(startingX, startingY);
	}

	@Override
	protected LayoutParams generateDefaultLayoutParams() {
		return new FixedGridLayoutParams(FixedGridLayoutParams.UPTO_PARENT);
	}

	@Override
	protected LayoutParams generateLayoutParams(LayoutParams p) {
		return new FixedGridLayoutParams(p);
	}
	
	
	@Override
	public void addView (View child, int index){
		this.addView(child, new FixedGridLayoutParams(index));
		childViewChanged = true;
	}
	
	@Override
	public void removeAllViews (){
		clearOccupationRecord();
		super.removeAllViews();
		childViewChanged = true;
	}
	@Override
	public void removeView (View view){
		if(view == null) return;
		LayoutParams originalParam = view.getLayoutParams();
		if(originalParam instanceof FixedGridLayoutParams){
			int index = ((FixedGridLayoutParams) originalParam).getPositionIndex();
			if(index >= 0 && index < row*col){
				Point xy = this.positionIndexToXY(index);
				this.gridOccupation[xy.y][xy.x] = false;
			}
		}
		super.removeView(view);
		childViewChanged = true;
	}
	@Override
	public void removeViewAt (int index){
		View view = this.getChildAt(index);
		this.removeView(view);
		childViewChanged = true;
	}
	public void removeViewAtPos(int positionIndex){
		Point xy = this.positionIndexToXY(positionIndex);
		if(gridOccupation[xy.y][xy.x]){//occupied
			this.removeView(this.findViewByPos(positionIndex));
			childViewChanged = true;
		}
	}
	public View findViewByPos(int positionIndex){
		if(positionIndex <0 || positionIndex >= row*col) return null;
		for(int i=0;i<this.getChildCount();i++){
			View view = this.getChildAt(i);
			LayoutParams originalParam = view.getLayoutParams();
			if(originalParam instanceof FixedGridLayoutParams){
				int index = ((FixedGridLayoutParams) originalParam).getPositionIndex();
				if(index == positionIndex) return view;
			}
		}
		return null;
	}
	public boolean isXYPosOccupied(int x, int y){
		return gridOccupation[y][x];
	}
	public boolean isPosIndexOccupied(int index){
		Point p = this.positionIndexToXY(index);
		return gridOccupation[p.y][p.x];
	}
	public int getRow(){
		return this.row;
	}
	public int getCol(){
		return this.col;
	}
	
	private void clearOccupationRecord(){
		this.gridOccupation = new boolean[row][col];
	}
	
	public int pixelToPositionIndex(int x, int y){
		//assume to be full window
		Point xy = new Point();
		xy.x = (x-this.getPaddingLeft())/this.cellWidth;
		xy.y = (y-this.getPaddingTop())/this.cellHeight;
		
		return this.xyToPositionIndex(xy);
	}
	
	
	
}

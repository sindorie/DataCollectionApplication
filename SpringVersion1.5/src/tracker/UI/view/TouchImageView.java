package tracker.UI.view;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageView;
 
public class TouchImageView extends EventCheckerImageView {
 
    Matrix matrix;
 
    // We can be in one of these 3 states
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    int mode = NONE;
 
    // Remember some things for zooming
    PointF last = new PointF();
    PointF start = new PointF();
    float minScale = 1f;
    float maxScale = 5f;
    float[] m;
 
    int viewWidth, viewHeight;
    static final int CLICK = 3;
    float saveScale = 1f;
    protected float origWidth, origHeight;
    int oldMeasuredWidth, oldMeasuredHeight;
 
    ScaleGestureDetector mScaleDetector;
 
    Context context;
 
    public TouchImageView(Context context) {
        super(context);
        sharedConstructing(context);
    }
 
    public TouchImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        sharedConstructing(context);
    }
 
    private void sharedConstructing(Context context) {
        super.setClickable(true);
        this.context = context;
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        matrix = new Matrix();
        m = new float[9];
        setImageMatrix(matrix);
        setScaleType(ScaleType.MATRIX);
 
        setOnTouchListener(new OnTouchListener() {
        	private long previousClicked = -1;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
            	if(saveScale>1.1){
            		v.getParent().requestDisallowInterceptTouchEvent(true);
            	}
                mScaleDetector.onTouchEvent(event);
                PointF curr = new PointF(event.getX(), event.getY());
 
                switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    last.set(curr);
                    start.set(last);
                    mode = DRAG;
                    break;
 
                case MotionEvent.ACTION_MOVE:
                    if (mode == DRAG) {
                        float deltaX = curr.x - last.x;
                        float deltaY = curr.y - last.y;
                        float fixTransX = getFixDragTrans(deltaX, viewWidth,
                                origWidth * saveScale);
                        float fixTransY = getFixDragTrans(deltaY, viewHeight,
                                origHeight * saveScale);
                        matrix.postTranslate(fixTransX, fixTransY);
                        fixTrans();
                        last.set(curr.x, curr.y);
                    }
                    break;
 
                case MotionEvent.ACTION_UP:
                    mode = NONE;
                    int xDiff = (int) Math.abs(curr.x - start.x);
                    int yDiff = (int) Math.abs(curr.y - start.y);
                    if (xDiff < CLICK && yDiff < CLICK)
                        performClick();
                    
                    long currentCick = System.currentTimeMillis();
    				float current = getCurrentScale();
    				float max = getMaxScale();
    				float x = event.getX(), y = event.getY();
                    if(currentCick - previousClicked < 300){
        				if(current > max/3){
        					controledScale(1/current,x,y);
        				}else{
        					controledScale((float) ((max/current)*0.6),x,y);
        				}
                    }else{ previousClicked = currentCick; }
                    
                    break;
 
                case MotionEvent.ACTION_POINTER_UP:
                    mode = NONE;
                    break;
                }
 
                setImageMatrix(matrix);
                invalidate();
                return true; // indicate event was handled
            }
 
        });
    }
 
    public void setMaxZoom(float x) {
        maxScale = x;
    }
 
    private class ScaleListener extends
            ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            mode = ZOOM;
            return true;
        }
 
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float mScaleFactor = detector.getScaleFactor();
            float origScale = saveScale;
            saveScale *= mScaleFactor;
            if (saveScale > maxScale) {
                saveScale = maxScale;
                mScaleFactor = maxScale / origScale;
            } else if (saveScale < minScale) {
                saveScale = minScale;
                mScaleFactor = minScale / origScale;
            }
 
            if (origWidth * saveScale <= viewWidth
                    || origHeight * saveScale <= viewHeight)
                matrix.postScale(mScaleFactor, mScaleFactor, viewWidth / 2,
                        viewHeight / 2);
            else
                matrix.postScale(mScaleFactor, mScaleFactor,
                        detector.getFocusX(), detector.getFocusY());
 
            fixTrans();
            return true;
        }
    }
    
    public void controledScale(float mScaleFactor,float focusedX, float focusedY){
    	mode = ZOOM;
    	float origScale = saveScale;
    	saveScale *= mScaleFactor;
        if (saveScale > maxScale) {
            saveScale = maxScale;
            mScaleFactor = maxScale / origScale;
        } else if (saveScale < minScale) {
            saveScale = minScale;
            mScaleFactor = minScale / origScale;
        }

        if (origWidth * saveScale <= viewWidth
                || origHeight * saveScale <= viewHeight)
            matrix.postScale(mScaleFactor, mScaleFactor, viewWidth / 2,
                    viewHeight / 2);
        else
            matrix.postScale(mScaleFactor, mScaleFactor,
            		focusedX, focusedY);
        fixTrans();
    }
    
    public float getCurrentScale(){
    	return this.saveScale;
    }
    public float getMaxScale(){
    	return this.maxScale;
    }
 
    void fixTrans() {
        matrix.getValues(m);
        float transX = m[Matrix.MTRANS_X];
        float transY = m[Matrix.MTRANS_Y];
 
        float fixTransX = getFixTrans(transX, viewWidth, origWidth * saveScale);
        float fixTransY = getFixTrans(transY, viewHeight, origHeight
                * saveScale);
        if (fixTransX != 0 || fixTransY != 0)
            matrix.postTranslate(fixTransX, fixTransY);
    }
 
    float getFixTrans(float trans, float viewSize, float contentSize) {
        float minTrans, maxTrans;
 
        if (contentSize <= viewSize) {
            minTrans = 0;
            maxTrans = viewSize - contentSize;
        } else {
            minTrans = viewSize - contentSize;
            maxTrans = 0;
        }
 
        if (trans < minTrans)
            return -trans + minTrans;
        if (trans > maxTrans)
            return -trans + maxTrans;
        return 0;
    }
 
    float getFixDragTrans(float delta, float viewSize, float contentSize) {
        if (contentSize <= viewSize) {
            return 0;
        }
        return delta;
    }
 
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        viewWidth = MeasureSpec.getSize(widthMeasureSpec);
        viewHeight = MeasureSpec.getSize(heightMeasureSpec);
//        Log.v("view size", ""+viewWidth+"  "+viewHeight);
//        Log.v("visibility",""+(this.getVisibility()==View.VISIBLE));
//        Log.v("view size 1",""+this.getWidth()+"  "+this.getHeight());
        //
        // Rescales image on rotation
        //
        if (oldMeasuredHeight == viewWidth && oldMeasuredHeight == viewHeight
                || viewWidth == 0 || viewHeight == 0){
        	return;
        }
            
        oldMeasuredHeight = viewHeight;
        oldMeasuredWidth = viewWidth;
 
        Drawable drawable = getDrawable();
        int bmWidth = drawable.getIntrinsicWidth();
        int bmHeight = drawable.getIntrinsicHeight();
        
        if (drawable == null || drawable.getIntrinsicWidth() == 0
                || drawable.getIntrinsicHeight() == 0){
        	Log.v("condition_mark1", ""+(drawable == null)+"  "+( drawable.getIntrinsicWidth() == 0)+" "+
                (drawable.getIntrinsicHeight() == 0));
        	return;
        }
        
        if(bmWidth<viewWidth && bmHeight<viewHeight){
        	
        }
        
        
        if (saveScale == 1) {
            // Fit to screen.
            float scale;

            float scaleX = (float) viewWidth / (float) bmWidth;
            float scaleY = (float) viewHeight / (float) bmHeight;
            scale = Math.min(scaleX, scaleY);
            matrix.setScale(scale, scale);
 
            // Center the image
            float redundantYSpace = (float) viewHeight - (scale * (float) bmHeight);
            float redundantXSpace = (float) viewWidth - (scale * (float) bmWidth);
            redundantYSpace /= (float) 2;
            redundantXSpace /= (float) 2;
 
            matrix.postTranslate(redundantXSpace, redundantYSpace);
 
            origWidth = viewWidth - 2 * redundantXSpace;
            origHeight = viewHeight - 2 * redundantYSpace;
            setImageMatrix(matrix);
        }
        fixTrans();
    }
}
package tracker.utility;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class ResizeHeightAnimation extends Animation {
	View view;
	int starting, ending, diff;
	public ResizeHeightAnimation(View v, int newh) {
		view = v;
		starting = v.getLayoutParams().height;
		ending = newh;
		diff = ending - starting;
	}

	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t) {
		view.getLayoutParams().height = starting + (int) (diff * interpolatedTime);
		view.requestLayout();
	}

	@Override
	public void initialize(int width, int height, int parentWidth,
			int parentHeight) {
		super.initialize(width, height, parentWidth, parentHeight);
	}

	@Override
	public boolean willChangeBounds() {
		return true;
	}
}
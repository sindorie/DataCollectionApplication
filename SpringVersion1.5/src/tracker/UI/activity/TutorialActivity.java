package tracker.UI.activity;

import tracker.springversion1.R;
import tracker.springversion1.R.id;
import tracker.springversion1.R.layout;
import android.os.Bundle;
import android.app.Activity;
import android.support.v4.view.ViewPager;
import android.view.Menu;

public class TutorialActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tutorial);
		ViewPager pager = (ViewPager)this.findViewById(R.id.tutorial_viewpager);
//		SimplePageAdapter adapter = new SimplePageAdapter(this,null);
//		pager.setAdapter(adapter);
	}

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.tutorial, menu);
//		return true;
//	}
}

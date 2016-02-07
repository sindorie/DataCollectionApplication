package tracker.UI.activity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import tracker.springversion1.MobileStudyApplication;
import tracker.springversion1.R;
import tracker.springversion1.MobileStudyApplication.UserInfo;
import tracker.springversion1.R.id;
import tracker.springversion1.R.layout;
import tracker.utility.Utility;
import android.net.http.SslError;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

@SuppressLint("JavascriptInterface")
public class MainActivity extends Activity {

	private MobileStudyApplication app;
	private UserInfo info;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = (MobileStudyApplication)this.getApplication(); //must be
		info = app.getUserInfo();
		String tocheck = app.getUserInfo().username.trim();
		
		if(info.allDataUploaded >= 1){
			Intent intent = new Intent(this, PaymentActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			this.finish();
			this.startActivity(intent);
		}else{
			if(tocheck == null || tocheck.equals("")||tocheck.equals("null")){//needs login
				setContentView(R.layout.act_login);
				WebView webBrowser = (WebView) findViewById(R.id.webBrowser);
				webBrowser.getSettings().setJavaScriptEnabled(true);
				webBrowser.getSettings().setBuiltInZoomControls(true);
				webBrowser.addJavascriptInterface(new MyJavaScriptInterface(), "INTERFACE");
				
				webBrowser.loadUrl("http://csl.ece.iastate.edu/test/login.php");
				webBrowser.setWebViewClient(new webBrowserClient());
				webBrowser.setWebChromeClient(new webChromeClient());
			}else{
				//check if can preceed
				long waketime = app.getWakeUpTime();
				Log.v("mark","waketime: "+waketime);
				if(waketime >= System.currentTimeMillis()){
					Log.v("mark","cannot wake up yet");
					Intent intent = new Intent(this,WaitingActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					app.initTaskManager();
					this.finish();
					this.startActivity(intent);
				}else{
					Log.v("mark","MainActivity userid:"+tocheck);
					goToTasks();
				}
			}
		}
	}

	private void goToTasks(){
		app.initTaskManager();
		Intent intent = new Intent(this,ExperimentActivity.class);
		intent.putExtra(ExperimentActivity.needShowLeftPanel, true);
		startActivity(intent);
		this.finish();
	}
	private void setUserId(String id){
		Log.v("mark","MainActivity setUserId "+id);
		//TODO maybe need to synchronize other data
		app.setUserInfomraiton(id, 0, 0, 0);
		app.gatherDeviceInformation(this);
	}
	
	private class MyJavaScriptInterface {
		@JavascriptInterface
		public void processContent(String aContent) {
//			Log.v("mark","processContent");
			if(aContent.contains("Hello")) {
			    Pattern pattern = Pattern.compile("Hello (.*?)!");
			    Matcher matcher = pattern.matcher(aContent);
			    while (matcher.find()) {
			        final String content = matcher.group(1);
			        setUserId(content);
			        goToTasks();
			    }
			}
		}
	}
	private class webBrowserClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView webView, String url) {
			webView.loadUrl(url);
			return false;
		}
		
		@Override
		public void onPageFinished(WebView view, String url) {
//			Log.v("mark","onPageFinished");
//			view.loadUrl("javascript:window.INTERFACE.processContent(document.getElementsByTagName('html')[0].innerHTML+'</head>');");
			view.loadUrl("javascript:INTERFACE.processContent(document.documentElement.outerHTML);");
//			view.loadUrl("javascript:console.log('MAGIC'+document.getElementByTagName('html')[0].innerHTML);");		
		}
		
		@Override
		public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
			new AlertDialog.Builder(MainActivity.this).setTitle("HTML").setMessage("Error Code: "+errorCode+" and Description is: "+description+" with URL: "+failingUrl)
			.setPositiveButton(android.R.string.ok, null)
			.setCancelable(false).create().show();
			super.onReceivedError(view, errorCode, description, failingUrl);
		}
		
	    @Override
	    public void onReceivedSslError (WebView view, SslErrorHandler handler, SslError error) {
	        handler.proceed();
	    }
	}
	private class webChromeClient extends WebChromeClient {
		//TODO ??
	}
}

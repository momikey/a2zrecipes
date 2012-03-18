package net.potterpcs.recipebook;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

public class HelpDialog extends DialogFragment {
	// Tag for logging
	private static final String TAG = "HelpDialog";

	// Bundle extra
	public static final String HELP_RESOURCE = "help-resource";
	
	// Name of the help page
	private String helpFile;
	

	static HelpDialog newInstance(String uri) {
		// Standard Android-style factory method
		HelpDialog hd = new HelpDialog();
		
		Bundle args = new Bundle();
		args.putString(HELP_RESOURCE, uri);
		hd.setArguments(args);
		
		return hd;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(STYLE_NO_TITLE, 0);
		helpFile = getArguments().getString(HELP_RESOURCE);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.helpdialog, container);
		WebView web = (WebView) v.findViewById(R.id.helpwebview);
		
		// All we have to do is give the WebView a location, and it does
		// all the work for us.
		if (helpFile != null) {
			String baseUrl = "file:///android_res/raw/";
			String url = baseUrl + helpFile + ".html";
			Log.v(TAG, url);
			web.loadUrl(url);
		}
		
		return v;
	}
}

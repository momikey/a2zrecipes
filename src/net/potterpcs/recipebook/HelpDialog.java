package net.potterpcs.recipebook;

import java.io.IOException;
import java.io.InputStream;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

public class HelpDialog extends DialogFragment {

	public static final String HELP_RESOURCE = "help-resource";
	private static final String TAG = "HelpDialog";
	private String helpFile;
	

	static HelpDialog newInstance(String uri) {
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
		
		if (helpFile != null) {
			String baseUrl = "file:///android_res/raw/";
			String url = baseUrl + helpFile + ".html";
			Log.v(TAG, url);
			web.loadUrl(url);
		}
		
		return v;
	}
}

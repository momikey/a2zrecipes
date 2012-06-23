/*
 *    Copyright 2012 Michael Potter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

/*
 * HelpDialog.java - Popup to display application help pages. 
 */

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

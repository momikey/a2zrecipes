package net.potterpcs.recipebook;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class TimeSearchDialog extends DialogFragment {
	private static final String TAG = "TimeSearchDialog";
	
	EditText minHoursEdit;
	EditText minMinutesEdit;
	EditText maxHoursEdit;
	EditText maxMinutesEdit;
	int minh;
	int minm;
	int maxh;
	int maxm;
	Button searchButton;
	
	static TimeSearchDialog newInstance() {
		TimeSearchDialog tsd = new TimeSearchDialog();
		
		// Bundle stuff goes here;
		
		return tsd;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.timesearchdialog, container, false);
		
		minHoursEdit = (EditText) v.findViewById(R.id.searchminhours);
		maxHoursEdit = (EditText) v.findViewById(R.id.searchmaxhours);
		minMinutesEdit = (EditText) v.findViewById(R.id.searchminminutes);
		maxMinutesEdit = (EditText) v.findViewById(R.id.searchmaxminutes);
		
		searchButton = (Button) v.findViewById(R.id.timesearchbutton);
		searchButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				try {
					minh = Integer.parseInt(minHoursEdit.getText().toString());
				} catch (NumberFormatException e) {
					minh = 0;
				}
				
				try {
					maxh = Integer.parseInt(maxHoursEdit.getText().toString());
				} catch (NumberFormatException e) {
					maxh = 0;
				}
				
				try {
					minm = Integer.parseInt(minMinutesEdit.getText().toString());
				} catch (NumberFormatException e) {
					minm = 0;
				}
				
				try {
					maxm = Integer.parseInt(maxMinutesEdit.getText().toString());
				} catch (NumberFormatException e) {
					maxm = 0;
				}
				
				Intent intent = null;
				Intent oldIntent = getActivity().getIntent();
				if (!Intent.ACTION_MAIN.equals(oldIntent.getAction())) {
					intent = new Intent(oldIntent);
					intent.setClass(getActivity(), RecipeBookActivity.class);
				} else {
					intent = new Intent(getActivity(), RecipeBookActivity.class);
					if (oldIntent.hasExtra(RecipeBook.TAG_EXTRA)) {
						intent.putExtra(RecipeBook.TAG_EXTRA, 
								oldIntent.getStringExtra(RecipeBook.TAG_EXTRA));
					}
					if (oldIntent.hasExtra(RecipeBook.SEARCH_EXTRA)) {
						intent.putExtra(RecipeBook.SEARCH_EXTRA, 
								oldIntent.getStringExtra(RecipeBook.SEARCH_EXTRA));
					}
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				}
				
				intent.putExtra(RecipeBook.TIME_EXTRA, true);
				intent.putExtra(RecipeBook.TIME_EXTRA_MIN, minh * 60 + minm);
				intent.putExtra(RecipeBook.TIME_EXTRA_MAX, maxh * 60 + maxm);
				startActivity(intent);
				dismiss();
			}
		});
		
		return v;
	}
}

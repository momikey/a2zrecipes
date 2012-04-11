package net.potterpcs.recipebook;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

public class TagSearchDialog extends DialogFragment {
	// Tag for logging
//	private static final String TAG = "TagSearch";
	
	String clicked;
	SimpleCursorAdapter adapter;
	
	static TagSearchDialog newInstance() {
		TagSearchDialog tsd = new TagSearchDialog();
		// Standard Android factory method
		Bundle args = new Bundle();
		tsd.setArguments(args);
		return tsd;
	}
	
	public TagSearchDialog() {
		adapter = null;
		setStyle(STYLE_NO_TITLE, getTheme());
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.tagsearch, container, false);
		RecipeBook app = (RecipeBook) getActivity().getApplication();
		Cursor cursor = app.getData().getAllTags();
		GridView grid = (GridView) v.findViewById(R.id.tagsearchgrid);
		
		clicked = null;
		
		adapter = new SimpleCursorAdapter(getActivity(), 
				android.R.layout.simple_list_item_1, cursor, new String[] { RecipeData.TT_TAG }, 
				new int[] { android.R.id.text1 }, 0);
		
		grid.setAdapter(adapter);
		grid.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				RecipeBookActivity activity = (RecipeBookActivity) getActivity();
				Intent intent = new Intent(activity, RecipeBookActivity.class);
				if (activity.isSearchMode()) {
					intent.putExtra(RecipeBook.SEARCH_EXTRA, activity.getSearchQuery());
				}			
				if (activity.isTimeSearch()) {
					intent.putExtra(RecipeBook.TIME_EXTRA, true);
					intent.putExtra(RecipeBook.TIME_EXTRA_MIN, activity.getMinTime());
					intent.putExtra(RecipeBook.TIME_EXTRA_MAX, activity.getMaxTime());
				}
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

				adapter.getCursor().moveToPosition(position);
				intent.putExtra(RecipeBook.TAG_EXTRA, 
						adapter.getCursor().getString(adapter.getCursor().getColumnIndex(RecipeData.TT_TAG)));
				startActivity(intent);
				dismiss();
			}
		});
					
		return v;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		adapter.getCursor().close();
	}
}

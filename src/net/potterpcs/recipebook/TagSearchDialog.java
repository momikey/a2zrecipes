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
	private static final String TAG = "TagSearch";
	String clicked;
	SimpleCursorAdapter adapter;
	
	static TagSearchDialog newInstance() {
		TagSearchDialog tsd = new TagSearchDialog();
		
		// Bundle stuff goes here
		
		return tsd;
	}
	
	public TagSearchDialog() {
		adapter = null;
	}
	
	@SuppressWarnings("deprecation")
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
				new int[] { android.R.id.text1 });
		
		grid.setAdapter(adapter);
		grid.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				Intent intent = null;
				Intent oldIntent = getActivity().getIntent();
				if (!Intent.ACTION_MAIN.equals(oldIntent.getAction())) {
					intent = new Intent(oldIntent);
					intent.setClass(getActivity(), RecipeBookActivity.class);
				} else {
					intent = new Intent(getActivity(), RecipeBookActivity.class);
					if (oldIntent.hasExtra(RecipeBook.SEARCH_EXTRA)) {
						intent.putExtra(RecipeBook.SEARCH_EXTRA, 
								oldIntent.getStringExtra(RecipeBook.SEARCH_EXTRA));
					}			
					if (oldIntent.hasExtra(RecipeBook.TIME_EXTRA)) {
						intent.putExtra(RecipeBook.TIME_EXTRA, 
								oldIntent.getStringExtra(RecipeBook.TIME_EXTRA));
						intent.putExtra(RecipeBook.TIME_EXTRA_MIN, 
								oldIntent.getIntExtra(RecipeBook.TIME_EXTRA_MIN, 0));
						intent.putExtra(RecipeBook.TIME_EXTRA_MAX, 
								oldIntent.getIntExtra(RecipeBook.TIME_EXTRA_MAX, 0));
					}
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				}
				adapter.getCursor().moveToPosition(position);
				intent.putExtra(RecipeBook.TAG_EXTRA, 
						adapter.getCursor().getString(adapter.getCursor().getColumnIndex(RecipeData.TT_TAG)));
				startActivity(intent);
				adapter.getCursor().close();
				dismiss();
			}
		});
					
		return v;
	}
}

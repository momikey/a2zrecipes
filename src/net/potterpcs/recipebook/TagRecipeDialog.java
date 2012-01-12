package net.potterpcs.recipebook;

import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

public class TagRecipeDialog extends DialogFragment {
	private static final String TAG = "TagRecipeDialog";
	
	String clicked;
	SimpleCursorAdapter adapter;
	private TextView newtag;
	private GridView grid;
	private long recipeId;
	private RecipeBook app;
	
	static TagRecipeDialog newInstance(long rid) {
		TagRecipeDialog tsd = new TagRecipeDialog(rid);
		
		// Bundle stuff goes here
		
		return tsd;
	}
	
	public TagRecipeDialog(long rid) {
		adapter = null;
		recipeId = rid;
		setStyle(STYLE_NO_TITLE, getTheme());
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.tagrecipe, container, false);

		app = (RecipeBook) getActivity().getApplication();
		grid = (GridView) v.findViewById(R.id.tagrecipegrid);
		newtag = (TextView) v.findViewById(R.id.tagrecipenew);
		clicked = null;

		Cursor cursor = app.getData().getAllTags();
		Button createtag = (Button) v.findViewById(R.id.tagrecipecreatebutton);
		createtag.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				clicked = newtag.getText().toString();
				dismiss();
			}
		});
		
		adapter = new SimpleCursorAdapter(getActivity(), 
				android.R.layout.simple_list_item_1, cursor, new String[] { RecipeData.TT_TAG }, 
				new int[] { android.R.id.text1 }, 0);
		
		grid.setAdapter(adapter);
		grid.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				clicked = adapter.getCursor().getString(adapter.getCursor().getColumnIndex(RecipeData.TT_TAG));
				dismiss();
			}
		});

		return v;
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);
		RecipeData data = app.getData();
		Log.v(TAG, clicked);
		if (clicked != null && !data.recipeHasTag(recipeId, clicked)) {
			data.insertTags(RecipeData.createTagsCV(recipeId, clicked));
		}
	}
}

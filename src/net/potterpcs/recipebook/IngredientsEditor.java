package net.potterpcs.recipebook;

import java.util.ArrayList;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

public class IngredientsEditor extends Fragment {
	static final String TAG = "IngredientsEditor";
	ListView listview;
	ArrayList<String> ingredients;
	private ArrayAdapter<String> adapter;
	private RecipeEditor activity;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		activity = (RecipeEditor) getActivity();
		ingredients = new ArrayList<String>();
		
		long rid = activity.getRecipeId();
		Log.i(TAG, "id=" + rid);
			
		if (rid > 0) {
			RecipeBook app = (RecipeBook) activity.getApplication();
			Cursor c = app.getData().getRecipeIngredients(rid);
			Log.i(TAG, "count=" + c.getCount());
			
			c.moveToFirst();
			while (!c.isAfterLast()) {
				Log.i(TAG, c.getString(c.getColumnIndex(RecipeData.IT_NAME)));
				ingredients.add(c.getString(c.getColumnIndex(RecipeData.IT_NAME)));
				c.moveToNext();
			}
			
			c.close();
				
		} else {

		}
		
		adapter = new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, ingredients);

		return inflater.inflate(R.layout.ingredientsedit, container, false);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getActivity().getMenuInflater();
		inflater.inflate(R.menu.ingredientscontext, menu);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		switch (item.getItemId()) {
		case R.id.ctxdeleteingredient:
			adapter.remove(adapter.getItem(info.position));
			return true;
			
		default:
			return super.onContextItemSelected(item);
		}
	}
	
	@Override
	public void onStart() {
		super.onStart();

		listview = (ListView) activity.findViewById(R.id.ingredients);
		listview.setAdapter(adapter);
		
		ImageButton add = (ImageButton) getActivity().findViewById(R.id.addingredient);
		add.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				EditText edit = (EditText) getActivity().findViewById(R.id.ingredientsedit);
				if (edit.getText().length() > 0) {
					adapter.add(edit.getText().toString());
				}
				edit.setText("");
				edit.requestFocus();
			}
		});
		
		registerForContextMenu(listview);
	}
	
	public String[] getIngredients() {
		String[] ings = new String[adapter.getCount()];
		int l = ings.length;
		for (int i = 0; i < l; i++) {
			ings[i] = adapter.getItem(i);
		}
		return ings;
	}
}

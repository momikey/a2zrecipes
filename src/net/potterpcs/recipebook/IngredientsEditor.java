package net.potterpcs.recipebook;

import java.util.ArrayList;
import java.util.Arrays;

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
	static final String STATE = "ingredients";
	ListView listview;
	ArrayList<String> ingredients;
	private ArrayAdapter<String> adapter;
	private RecipeEditor activity;
	int currentIngredient;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		activity = (RecipeEditor) getActivity();
		ingredients = new ArrayList<String>();
		currentIngredient = -1;
		
		if (savedInstanceState != null) {
			String[] saved = savedInstanceState.getStringArray(STATE);
			if (saved != null) {
				ingredients.addAll(Arrays.asList(saved));
			}
		}
		
		long rid = activity.getRecipeId();
		Log.i(TAG, "id=" + rid);
			
		// load an existing recipe's ingredients
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
			// creating a new recipe, so no setup required
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
		// The Ingredients editor has no "Move Up"/"Move Down" options,
		// because the list of ingredients is unordered, unlike directions,
		// which must come in a specific order.
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		switch (item.getItemId()) {
		case R.id.ctxdeleteingredient:
			// "Delete" option
			adapter.remove(adapter.getItem(info.position));
			currentIngredient = -1;
			return true;
		case R.id.ctxeditingredient:
			// "Edit" option
			String selected = adapter.getItem(info.position);
			currentIngredient = info.position;
			// replace the editbox text
			EditText edit = ((EditText) getView().findViewById(R.id.ingredientsedit));
			edit.setText(selected);
			edit.requestFocus();
			// put a placeholder in the list
			adapter.remove(selected);
			adapter.insert(getResources().getString(R.string.recipereplacetext), currentIngredient);
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
					if (currentIngredient == -1) {
					adapter.add(edit.getText().toString());
					} else {
						adapter.insert(edit.getText().toString(), currentIngredient);
						adapter.remove(getResources().getString(R.string.recipereplacetext));
						currentIngredient = -1;
					}
				}
				edit.setText("");
				edit.requestFocus();
			}
		});
		registerForContextMenu(listview);
	}
	
	public String[] getIngredients() {
		// Ingredients are not sensitive to order, unlike directions.
		String[] ings = new String[adapter.getCount()];
		int l = ings.length;
		for (int i = 0; i < l; i++) {
			ings[i] = adapter.getItem(i);
		}
		return ings;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putStringArray(STATE, getIngredients());
	}
}

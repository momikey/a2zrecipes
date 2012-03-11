package net.potterpcs.recipebook;

import java.io.IOException;
import java.util.ArrayList;

import net.potterpcs.recipebook.RecipeData.Recipe;
import android.app.ListActivity;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;

public class ImporterActivity extends ListActivity {
	public static final String IMPORT_PATH_EXTRA = "import-path";
	RecipeData data;
	ArrayList<Recipe> importedRecipes;
	String[] recipeNames;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		data = ((RecipeBook) getApplication()).getData();
		String path = savedInstanceState.getString(IMPORT_PATH_EXTRA);
		try {
			importedRecipes = data.importRecipes(path);
		} catch (IOException e) {
			// TODO logging
		}

		ArrayList<String> als = new ArrayList<String>();
		for ( Recipe r : importedRecipes ) {
			als.add(r.name);
		}
		recipeNames = (String[]) als.toArray(new String[als.size()]);
		
		
		setContentView(R.layout.importer);
		ListAdapter adapter = new ArrayAdapter<String>(this, 
				android.R.layout.simple_list_item_checked, 
				android.R.id.text1, 
				recipeNames);
		
		setListAdapter(adapter);
	}
	
	public void onImportButton(View v) {
		long[] ids = getListView().getCheckedItemIds();
		// TODO import
		ArrayList<Recipe> selectedRecipes = new ArrayList<Recipe>(); 
		for (int i = 0; i < ids.length; i++) {
			selectedRecipes.add(importedRecipes.get((int) ids[i]));
		}
		data.insertImportedRecipes(selectedRecipes);
		finish();
	}
}

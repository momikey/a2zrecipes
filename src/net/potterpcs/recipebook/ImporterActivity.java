package net.potterpcs.recipebook;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import net.potterpcs.recipebook.RecipeData.Recipe;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import android.app.ListActivity;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;

public class ImporterActivity extends ListActivity {
	// Tag for logging
//	private static final String TAG = "ImporterActivity";
	
	// Bundle extra that holds the imported file's location.
	// This isn't really used yet.
	public static final String IMPORT_PATH_EXTRA = "import-path";
	
	// Handle to the data layer
	RecipeData data;
	
	// The list of recipes to import (will be filled by the worker thread)
	ArrayList<Recipe> importedRecipes;
	
	// The names of all the recipes in the imported batch
	String[] recipeNames;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		data = ((RecipeBook) getApplication()).getData();
		
		// Read the location from the Bundle's state, if any, or get it
		// straight from the Intent.
		String path;
		if (savedInstanceState != null) {
			path = savedInstanceState.getString(IMPORT_PATH_EXTRA);
		} else {
			path = getIntent().getDataString();
		}
		
		// All of the work will be done by the worker thread,
		// because the recipebook file may be online, and 3.0+
		// apps can't access the network in the UI thread.
		try {
			importedRecipes = new DownloadHelperTask(this).execute(path).get();
		} catch (InterruptedException e) {
			importedRecipes = new ArrayList<Recipe>();
		} catch (ExecutionException e) {
			importedRecipes = new ArrayList<Recipe>();
		}
		
	}
	
	public void onImportButton(View v) {
		// We can't simply use getCheckedItemIds(), because it only works
		// if the ListAdapter has stable IDs, and ArrayAdapters don't.
		// So, we have to use getCheckedItemPositions instead, and loop
		// through the array.
		SparseBooleanArray sba = getListView().getCheckedItemPositions();
		ArrayList<Recipe> selectedRecipes = new ArrayList<Recipe>(); 
		for (int i = 0; i < sba.size(); i++) {
			if (sba.get(i)) {
				selectedRecipes.add(importedRecipes.get(i));
			}
		}
		data.insertImportedRecipes(selectedRecipes);
		finish();
	}
	
	
	// The class representing the worker thread
	private class DownloadHelperTask extends AsyncTask<String, Void, ArrayList<Recipe>> {
		// Handle to the parent activity
		private ListActivity activity;
		
		public DownloadHelperTask(ListActivity a) {
			activity = a;
		}

		@Override
		protected ArrayList<Recipe> doInBackground(String... params) {
			RecipeData appData = ((RecipeBook) getApplication()).getData();
			ArrayList<Recipe> importedRecipes = new ArrayList<Recipe>();
			String path = params[0];
			
			// Parse the path parameter as a URI. If it's a "file:///" URI,
			// then we're loading from the device, and we can use the simpler
			// method offered by RecipeData. Otherwise, we have to load from
			// the network.
			Uri uriPath = Uri.parse(path);
			if (uriPath.getScheme().startsWith("file")) {
				try {
					importedRecipes = appData.importRecipes(path);
				} catch (IOException e) {
					// Not much we can do if there's an I/O exception.
					// TODO Maybe pop up a dialog?
				}
			} else {
				// Set up the networking stuff
				AndroidHttpClient client = AndroidHttpClient.newInstance("A to Z Recipes for Android");
				HttpGet request = new HttpGet(path);
				HttpParams httpPars = new BasicHttpParams();
				HttpConnectionParams.setSoTimeout(httpPars, 60000);
				request.setParams(httpPars);
				HttpResponse response;
				
				// Recipebook batches are JSON files, but we'll let the data
				// layer handle that. All we do here is read the batch into
				// a string, then hand that off to RecipeData.
				String json;
				try {
					response = client.execute(request);
					byte[] file = EntityUtils.toByteArray(response.getEntity());
					json = new String(file);
				} catch (IOException e) {
					// TODO logging
					json = null;
				}
				importedRecipes = appData.parseJsonRecipes(json);
				client.close();
			}
			
			return importedRecipes;
		}
		
		@Override
		protected void onPostExecute(ArrayList<Recipe> result) {
			// Here we set up the UI stuff, making a list of the recipes'
			// names, and creating a checkable ListView to hold them.
			ArrayList<String> als = new ArrayList<String>();
			for ( Recipe r : importedRecipes ) {
				als.add(r.name);
			}
			recipeNames = (String[]) als.toArray(new String[als.size()]);
			
			
			activity.setContentView(R.layout.importer);
			ListAdapter adapter = new ArrayAdapter<String>(activity, 
					android.R.layout.simple_list_item_checked, 
					android.R.id.text1, 
					recipeNames);
			
			activity.setListAdapter(adapter);

		}
	}
}

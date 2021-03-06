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
 * ExporterActivity.java - Export screen that allows the user to choose
 * which recipes are exported. 
 */

package net.potterpcs.recipebook;

import java.io.IOException;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.ListAdapter;

public class ExporterActivity extends ListActivity {
	// Handle to the data layer
	RecipeData data;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		data = ((RecipeBook) getApplication()).getData();
		
		// Simple setup for a checked list of recipe names
		setContentView(R.layout.exporter);
		ListAdapter adapter = new SimpleCursorAdapter(this, 
				android.R.layout.simple_list_item_checked, 
				data.getAllRecipes(),
				new String[] { RecipeData.RT_NAME }, new int[] { android.R.id.text1 }, 0);
		
		setListAdapter(adapter);
	}
	
	public void onExportButton(View v) {
		// TODO Allow the user to choose export location/filename
		
		// Unlike in the importer, here we *can* use getCheckedItemIds(),
		// because CursorAdapters have stable IDs.
		long[] ids = getListView().getCheckedItemIds();
		
		// Try to export
		String filename = null;
		try {
			filename = data.exportRecipes(ids);
		} catch (IOException e) {
			// Export failed
			// TODO Dialog
		}
		
		// Show the filename
		if (filename != null) {
			new AlertDialog.Builder(this)
			.setMessage(filename)
			.setPositiveButton(android.R.string.ok, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			})
			.show();
		}
	}
	
	public void onSelectAllButton(View v) {
		for (int i = 0; i < getListView().getCount(); i++) {
			getListView().setItemChecked(i, true);
		}
	}
	
	public void onClearAllButton(View v) {
		for (int i = 0; i < getListView().getCount(); i++) {
			getListView().setItemChecked(i, false);
		}
	}
}

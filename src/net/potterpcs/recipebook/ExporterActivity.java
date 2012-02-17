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
	RecipeData data;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		data = ((RecipeBook) getApplication()).getData();
		
		setContentView(R.layout.exporter);
		ListAdapter adapter = new SimpleCursorAdapter(this, 
				android.R.layout.simple_list_item_checked, 
				data.getAllRecipes(),
				new String[] { RecipeData.RT_NAME }, new int[] { android.R.id.text1 }, 0);
		
		setListAdapter(adapter);
	}
	
	public void onExportButton(View v) {
		long[] ids = getListView().getCheckedItemIds();
		String filename = null;
		try {
			filename = data.exportRecipes(ids);
		} catch (IOException e) {
		}
		
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
}

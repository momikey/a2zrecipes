package net.potterpcs.recipebook;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ImportFileListActivity extends ListActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onResume();
		setContentView(R.layout.importfilelist);

		// Get a list of all "rcp" files in the download directory
		File sd = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
		FileFilter filter = new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				String path = pathname.getPath();
				String ext = path.substring(path.lastIndexOf(".") + 1);
				return "rcp".equalsIgnoreCase(ext);
			}
		};

		File[] found = sd.listFiles(filter);
		ArrayList<String> rcpFiles = new ArrayList<String>();
		if (found != null && found.length > 0) {
			for (File f : found) {
				rcpFiles.add(f.getName());
			}
		}

		String[] ss = new String[rcpFiles.size()];
		rcpFiles.toArray(ss);
		setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, ss));

		ListView lv = getListView();
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
//				Log.i("ImportFileListActivity", parent.getAdapter().getItem(position).toString());
				File dldir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
				File importFile = new File(dldir, parent.getAdapter().getItem(position).toString());
				Intent intent = new Intent(parent.getContext(), ImporterActivity.class);
				intent.setData(Uri.fromFile(importFile));
				startActivity(intent);
			}
		});
	}
}

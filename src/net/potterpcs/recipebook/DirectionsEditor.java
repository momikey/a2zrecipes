package net.potterpcs.recipebook;

import java.util.ArrayList;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

public class DirectionsEditor extends Fragment {
	private RecipeEditor activity;
	ListView listview;
	ArrayList<String> directions;
	private ArrayAdapter<String> adapter;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		activity = (RecipeEditor) getActivity();
		long rid = activity.getRecipeId();
		directions = new ArrayList<String>();
		
		if (rid > 0) {
			RecipeBook app = (RecipeBook) activity.getApplication();
			Cursor c = app.getData().getRecipeDirections(rid);
			
			c.moveToFirst();
			while (!c.isAfterLast()) {
//				int key = c.getInt(c.getColumnIndex(RecipeData.DT_SEQUENCE));
				String value = c.getString(c.getColumnIndex(RecipeData.DT_STEP));
				directions.add(value);
				c.moveToNext();
			}
			c.close();
		} else {
			
		}

		adapter = new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, directions);

		return inflater.inflate(R.layout.directionsedit, container, false);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getActivity().getMenuInflater();
		inflater.inflate(R.menu.directionscontext, menu);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		String selected = adapter.getItem(info.position);
		switch (item.getItemId()) {
		case R.id.ctxdeletedirection:
			adapter.remove(selected);
			return true;
		case R.id.ctxeditdirection:
			// TODO finish dialog box, etc.
			return true;
		case R.id.ctxmovedowndirection:
			if (info.position < adapter.getCount()) {
				adapter.remove(selected);
				adapter.insert(selected, info.position + 1);
				return true;
			}
			return false;
		case R.id.ctxmoveupdirection:
			if (info.position > 0) {
				adapter.remove(selected);
				adapter.insert(selected, info.position - 1);
				return true;
			}
			return false;
		default:
			return super.onContextItemSelected(item);
		}
	}
	
	@Override
	public void onStart() {
		super.onStart();
//		RecipeEditor activity = (RecipeEditor) getActivity();
//		long rid = activity.getRecipeId();
		// TODO: restore state on resume
//		directions = new ArrayList<String>();
//		
//		if (rid > 0) {
//			RecipeBook app = (RecipeBook) activity.getApplication();
//			Cursor c = app.getData().getRecipeDirections(rid);
//			
//			c.moveToFirst();
//			while (!c.isAfterLast()) {
////				int key = c.getInt(c.getColumnIndex(RecipeData.DT_SEQUENCE));
//				String value = c.getString(c.getColumnIndex(RecipeData.DT_STEP));
//				directions.add(value);
//				c.moveToNext();
//			}
//			c.close();
//		} else {
//			
//		}
//
//		adapter = new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, directions);
		listview = (ListView) activity.findViewById(R.id.directionslist);
		listview.setAdapter(adapter);

		ImageButton add = (ImageButton) getActivity().findViewById(R.id.adddirection);
		add.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				EditText edit = (EditText) getActivity().findViewById(R.id.directionsedit);
				if (edit.getText().length() > 0) {
					adapter.add(edit.getText().toString());
					((EditText) getActivity().findViewById(R.id.directionsedit)).setText(null);
				}
				edit.setText("");
				edit.requestFocus();
			}
		});
		
		registerForContextMenu(listview);
	}

	public String[] getDirections() {
		String[] dirs = new String[adapter.getCount()];
		int l = dirs.length;
		for (int i = 0; i < l; i++) {
			dirs[i] = adapter.getItem(i);
		}
		return dirs;
	}
}

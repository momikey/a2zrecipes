package net.potterpcs.recipebook;

import java.util.ArrayList;
import java.util.Arrays;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

public class DirectionsEditor extends Fragment {
	static final String STATE_DIRS = "directions";
	static final String STATE_PHOTOS = "direction-photos";
	private RecipeEditor activity;
	ListView listview;
	ArrayList<String> directions;
	ArrayList<String> photoUris;
	private ArrayAdapter<String> adapter;
	int currentDirection;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		activity = (RecipeEditor) getActivity();
		long rid = activity.getRecipeId();
		directions = new ArrayList<String>();
		photoUris = new ArrayList<String>();
		currentDirection = -1;
		
		if (savedInstanceState != null) {
			String[] savedDirections = savedInstanceState.getStringArray(STATE_DIRS);
			String[] savedUris = savedInstanceState.getStringArray(STATE_PHOTOS);
			if (savedDirections != null) {
				directions.addAll(Arrays.asList(savedDirections));
			}
			if (savedUris != null) {
				photoUris.addAll(Arrays.asList(savedUris));
			}
		} else {

			// load an existing recipe's directions
			if (rid > 0) {
				RecipeBook app = (RecipeBook) activity.getApplication();
				Cursor c = app.getData().getRecipeDirections(rid);

				c.moveToFirst();
				while (!c.isAfterLast()) {
					String value = c.getString(c.getColumnIndex(RecipeData.DT_STEP));
					directions.add(value);

					String photo = c.getString(c.getColumnIndex(RecipeData.DT_PHOTO));
					photoUris.add(photo);
					c.moveToNext();
				}
				c.close();
			} else {
				// this is a new recipe, so no setup required (yet?)
			}
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
		if (photoUris.get(((AdapterContextMenuInfo) menuInfo).position) == null) {
			menu.removeItem(R.id.ctxremovephotodirection);
		}
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		String selected = adapter.getItem(info.position);
		String selectedPhoto = photoUris.get(info.position);
		switch (item.getItemId()) {
		case R.id.ctxdeletedirection:
			// "Delete" option
			adapter.remove(selected);
			photoUris.remove(info.position);
			currentDirection = -1;
			return true;
		case R.id.ctxeditdirection:
			// "Edit" option
			currentDirection = info.position;
			// set the editor box to have the old text
			EditText edit = ((EditText) getView().findViewById(R.id.directionsedit));
			edit.setText(selected);
			edit.requestFocus();
			adapter.remove(selected);
			photoUris.remove(info.position);
			// put a placeholder into the list
			adapter.insert(getResources().getString(R.string.recipereplacetext), currentDirection);
			return true;
		case R.id.ctxmovedowndirection:
			// "Move Down" option
			currentDirection = -1;
			if (info.position < adapter.getCount()) {
				// can't move down the last direction
				adapter.remove(selected);
				adapter.insert(selected, info.position + 1);
				
				photoUris.remove(info.position);
				photoUris.add(info.position + 1, selectedPhoto);
				return true;
			}
			return false;
		case R.id.ctxmoveupdirection:
			// "Move Up" option
			currentDirection = -1;
			if (info.position > 0) {
				// can't move up the first direction
				adapter.remove(selected);
				adapter.insert(selected, info.position - 1);
				
				photoUris.remove(info.position);
				photoUris.add(info.position - 1, selectedPhoto);
				return true;
			}
			return false;
		case R.id.ctxphotodirection:
			attachPhoto(info.position);
			return true;
		case R.id.ctxremovephotodirection:
			photoUris.set(info.position, null);
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}
	
	@Override
	public void onStart() {
		super.onStart();

		listview = (ListView) activity.findViewById(R.id.directionslist);
		listview.setAdapter(adapter);

		ImageButton add = (ImageButton) getActivity().findViewById(R.id.adddirection);
		add.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				EditText edit = (EditText) getView().findViewById(R.id.directionsedit);
				if (edit.getText().length() > 0) {
					if (currentDirection == -1) {
						adapter.add(edit.getText().toString());
						photoUris.add(null);
					} else {
						adapter.insert(edit.getText().toString(), currentDirection);
						photoUris.add(currentDirection, edit.getText().toString());
						adapter.remove(getResources().getString(R.string.recipereplacetext));
						currentDirection = -1;
					}
					((EditText) getView().findViewById(R.id.directionsedit)).setText(null);
				}
				edit.setText("");
				edit.requestFocus();
			}
		});
		
		listview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (photoUris.get(position) != null) {
					FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
					PhotoDialog pd = PhotoDialog.newInstance(photoUris.get(position));
					pd.show(ft, "dialog");
				}
			}
		});
		registerForContextMenu(listview);
	}

	public String[] getDirections() {
		// Directions are intended to be in a set order, and they are stored in
		// the database with sequence numbers. In code, we just use the index in
		// an array.
		String[] dirs = new String[adapter.getCount()];
		int l = dirs.length;
		for (int i = 0; i < l; i++) {
			if (!adapter.getItem(i).contentEquals(getResources().getString(R.string.recipereplacetext))) {
				dirs[i] = adapter.getItem(i);
			}
		}
		return dirs;
	}
	
	public String[] getPhotos() {
		// Photos are stored with each direction, but are handled separately.
		String[] photos = new String[photoUris.size()];
		for (int i = 0; i < photos.length; i++) {
			photos[i] = photoUris.get(i);
		}
		return photos;
	}
	
	private void attachPhoto(int position) {
		Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(intent, position);		
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			String selectedImageUri = data.getData().toString();
			photoUris.set(requestCode, selectedImageUri);
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putStringArray(STATE_DIRS, getDirections());
		outState.putStringArray(STATE_PHOTOS, getPhotos());
	}
}

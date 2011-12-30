package net.potterpcs.recipebook;

import java.io.IOException;

import net.potterpcs.recipebook.RecipeData.Recipe;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.view.MenuCompat;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

public class RecipeListFragment extends ListFragment {
	Cursor cursor;
	static final String TAG = "RecipeListFragment";
	
	static final String[] FROM = { RecipeData.RT_ID, RecipeData.RT_NAME, RecipeData.RT_DESCRIPTION,
		RecipeData.RT_CREATOR, RecipeData.RT_RATING, RecipeData.RT_TIME };
	static final int[] TO = { R.id.rowhidden, R.id.rowname, R.id.rowdescription, R.id.rowcreator,
		R.id.rowrating, R.id.rowtime };
	
	static final ViewBinder VIEW_BINDER = new ViewBinder() {
		@Override
		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
			if (view.getId() == R.id.rowrating) {
				RatingBar rating = (RatingBar) view;
				rating.setRating(cursor.getFloat(columnIndex));
				return true;
			} else if (view.getId() == R.id.rowtime) {
				// TODO: this is a hack until we can get something better
				TextView theTime = (TextView) view;
				theTime.setText(DateUtils.formatElapsedTime(cursor.getInt(columnIndex)));
				return true;
			} else {
				return false;
			}
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.recipelist, null);
	}
	
	@Override
	public void onStart() {
		super.onStart();
		registerForContextMenu(getListView());
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		retrieveRecipes();
	}

	@Override
	public void onPause() {
		super.onPause();
		cursor.close();
	}
	
	private void retrieveRecipes() {
		RecipeBookActivity activity = (RecipeBookActivity) getActivity();
		RecipeBook app = (RecipeBook) activity.getApplication();
		RecipeData data = app.getData();
		
		String sort;
		switch (activity.getSortKey()) {
		case R.id.menusortrating:
			sort = RecipeData.RT_RATING;
			break;
		case R.id.menusorttime:
			sort = RecipeData.RT_TIME;
			break;
		case R.id.menusortdate:
			// TODO actually sort by date instead of ID
			sort = RecipeData.RT_ID;
			break;
		case R.id.menusortname:
		default:
			sort = RecipeData.RT_NAME;
			break;
		}
		
		if (activity.getSortDirection()) {
			// descending sort
			sort += RecipeBookActivity.SORT_DESCENDING;
		}

    	getRecipes(activity, data, sort);    	

	}

	void getRecipes(RecipeBookActivity activity, RecipeData data, String sortData) {
		// this uses the "multi-query" method so filtering previous searches works
		cursor = data.query(activity.getSearchQuery(), activity.getSearchTag(), 
				activity.getMinTime(), activity.getMaxTime(), sortData);

//		@SuppressWarnings("deprecation")
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(activity, R.layout.rowlayout, cursor, FROM, TO, 0);

		adapter.setViewBinder(VIEW_BINDER);
		setListAdapter(adapter);

		int nr = cursor.getCount();
		
		TextView footer = (TextView) activity.findViewById(R.id.footertext);
		footer.setText(String.format(activity.getResources().getString(R.string.numrecipes), nr));
	}
	
	long[] getItemIds() {
		ListAdapter adapter = getListAdapter();
		long[] ids = new long[adapter.getCount()];
		for (int i = 0; i < ids.length; i++) {
			ids[i] = adapter.getItemId(i);
		}
		return ids;
	}


	public void onExportItemSelected(MenuItem item) {
		RecipeData data = ((RecipeBook) getActivity().getApplication()).getData();
		try {
			String filename = data.exportRecipes(getItemIds());
			if (filename != null) {
				Log.v(TAG, "Exported to " + filename);
				MediaScannerConnection.scanFile(getActivity(), new String[] { filename }, null, null);
//				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//				intent.setType("text/plain");
//				startActivity(Intent.createChooser(intent, ""));
			} else {
				Log.e(TAG, "Unable to export recipes");
			}
		} catch (IOException e) {
			Log.e(TAG, e.toString());
		}
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.listviewmenu, menu);
		MenuCompat.setShowAsAction(menu.findItem(R.id.switchtoflipbook), 
				MenuItem.SHOW_AS_ACTION_IF_ROOM);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getActivity().getMenuInflater();
		inflater.inflate(R.menu.recipelistcontext, menu);
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Uri uri = new Uri.Builder()
			.scheme("content")
			.authority("net.potterpcs.recipebook")
			.build();
		uri = ContentUris.withAppendedId(uri, id);
		Log.i(TAG, uri.toString());
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		startActivity(intent);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.ctxmenuedit:
			onEditItemSelected(item);
			return true;
		case R.id.ctxmenudelete:
			onDeleteItemSelected(item);
			return true;
		case R.id.ctxmenurate:
			onRateItemSelected(item);
			return true;
		case R.id.ctxmenutag:
			onTagItemSelected(item);
			return true;
		default:
			return super.onContextItemSelected(item);			
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.switchtoflipbook:
			RecipeBookActivity activity = (RecipeBookActivity) getActivity();
			activity.switchToFlipBook();
			return true;
    	case R.id.menuexport:
    		onExportItemSelected(item);
    		return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void onEditItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		Uri uri = new Uri.Builder()
			.scheme("content")
			.authority("net.potterpcs.recipebook")
			.build();
		uri = ContentUris.withAppendedId(uri, info.id);
		Log.i(TAG, "edit option selected, id=" + info.id);
		Intent intent = new Intent(Intent.ACTION_INSERT_OR_EDIT, uri);
		startActivity(intent);
	}
	
	public void onDeleteItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		Log.i(TAG, "delete option selected, id=" + info.id);
		RecipeBookActivity activity = (RecipeBookActivity) getActivity();
		RecipeData data = ((RecipeBook) activity.getApplication()).getData();
		data.deleteRecipe(info.id);
		retrieveRecipes();
	}
	
	public void onRateItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		Log.i(TAG, "rate option selected, id=" + info.id);
		RecipeBookActivity activity = (RecipeBookActivity) getActivity();
		FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
		Recipe r = ((RecipeBook) activity.getApplication()).getData().getSingleRecipeObject(info.id);
		RateRecipeDialog rrd = RateRecipeDialog.newInstance(this, r.id, r.name, r.rating);
		rrd.show(ft, "dialog");
	}
	
	public void onTagItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		Log.i(TAG, "tag option selected, id=" + info.id);
		RecipeBookActivity activity = (RecipeBookActivity) getActivity();
		FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
		Recipe r = ((RecipeBook) activity.getApplication()).getData().getSingleRecipeObject(info.id);
	}
	
	public void onRateDialogDismissed() {
		retrieveRecipes();
	}
}

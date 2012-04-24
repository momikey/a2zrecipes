package net.potterpcs.recipebook;

import net.potterpcs.recipebook.RecipeData.Recipe;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuCompat;
import android.support.v4.view.ViewPager;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

public class RecipeFlipbook extends FragmentActivity {
	// Tag for logging
//	private static final String TAG = "RecipeFlipbook";
	
	// Helpfile name
	private static final String HELP_FILENAME = "flipbook";
	FlipAdapter flip;
	ViewPager pager;
	Intent intent;

	public class FlipAdapter extends FragmentStatePagerAdapter {
		// Tag for logging
//		private static final String TAG = "FlipAdapater";
		String searchQuery;
		String searchTag;
		int sortKey;
		boolean sortDescending;
		boolean timeSearch;
		int max;
		int min;
		Cursor cursor;
		int[] ids;
		
		public FlipAdapter(FragmentManager fm) {
			super(fm);
			searchQuery = getIntent().getStringExtra(RecipeBook.SEARCH_EXTRA);
			searchTag = getIntent().getStringExtra(RecipeBook.TAG_EXTRA);
			sortKey = getIntent().getIntExtra(RecipeBookActivity.SORT_KEY, R.id.menusortname);
			sortDescending = getIntent().getBooleanExtra(RecipeBookActivity.SORT_DESCENDING, false);
			timeSearch = getIntent().getBooleanExtra(RecipeBook.TIME_EXTRA, false);
			max = getIntent().getIntExtra(RecipeBook.TIME_EXTRA_MAX, 0);
			min = getIntent().getIntExtra(RecipeBook.TIME_EXTRA_MIN, 0);
			if (max == 0 && min != 0) {
				max = Integer.MAX_VALUE;
			}
			
			String sortBy = "";
			switch (sortKey) {
			case R.id.menusortrating:
				sortBy = RecipeData.RT_RATING;
				break;
			case R.id.menusorttime:
				sortBy = RecipeData.RT_TIME;
				break;
			case R.id.menusortdate:
				sortBy = RecipeData.RT_DATE;
			case R.id.menusortname:
			default:
				sortBy = RecipeData.RT_NAME;
				break;
			}
			
			if (sortDescending) {
				sortBy += RecipeBookActivity.SORT_DESCENDING;
			}
			
			cursor = ((RecipeBook) getApplication()).getData().query(searchQuery, searchTag, min, max, sortBy);
					
			ids = new int[cursor.getCount()];
			cursor.moveToFirst();
			for (int i = 0 ; i < cursor.getCount(); ++i) {
				cursor.moveToPosition(i);
				ids[i] = cursor.getInt(cursor.getColumnIndex(RecipeData.RT_ID));
//				Log.i(TAG, "Cursor: " + cursor.getString(cursor.getColumnIndex(RecipeData.RT_NAME)));
			}
//			Log.i(TAG, "Searching: " + searchQuery + ", Matches: " + cursor.getCount());
//			Log.i(TAG, "Sorting by: " + sortBy + " with descending: " + sortDescending + " and key: " + sortKey);
			cursor.close();
		}

		@Override
		public Fragment getItem(int position) {
			return FlipperFragment.newInstance(ids[position]);
		}

		@Override
		public int getCount() {
			return ids.length;
		}

	}

	public static class FlipperFragment extends Fragment {
		int recipeId;
		Recipe recipe;
		ArrayAdapter<String> ingredients;
		ArrayAdapter<String> directions;
		ArrayAdapter<String> tags;
		
		static FlipperFragment newInstance(int rid) {
			// Standard Android factory method
			FlipperFragment f = new FlipperFragment();
			Bundle args = new Bundle();
			args.putInt("rid", rid);
			f.setArguments(args);
			return f;
		}
		
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			recipeId = getArguments() != null ? getArguments().getInt("rid") : 1;
			recipe = ((RecipeBook) getActivity().getApplication()).getData().getSingleRecipeObject(recipeId);
//			setHasOptionsMenu(true);
		}
		
		@Override
		public void onResume() {
			super.onResume();
			FrameLayout fl = (FrameLayout) getView().findViewById(R.id.photofragment);
			ImageView iv = (ImageView) fl.getChildAt(0);
			RecipeBook.setImageViewBitmapDecoded(getActivity(), iv, recipe.photo);

		}
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View v = inflater.inflate(R.layout.recipeviewer, container, false);
			
			// Fill in the UI
			TextView rvname = (TextView) v.findViewById(R.id.rvname);
			TextView rvcreator = (TextView) v.findViewById(R.id.rvcreator);
			TextView rvserving = (TextView) v.findViewById(R.id.rvserving);
			TextView rvtime = (TextView) v.findViewById(R.id.rvtime);
			RatingBar rvrating = (RatingBar) v.findViewById(R.id.rvrating);
			FrameLayout rvphoto = (FrameLayout) v.findViewById(R.id.photofragment);
			
			GridView lvingredients = (GridView) v.findViewById(R.id.ingredients);
			ListView lvdirections = (ListView) v.findViewById(R.id.directions);
			
			ingredients = new ArrayAdapter<String>(getActivity(), 
					android.R.layout.simple_list_item_1, recipe.ingredients);
			directions = new ArrayAdapter<String>(getActivity(), 
					android.R.layout.simple_list_item_1, recipe.directions);
			tags = new ArrayAdapter<String>(getActivity(), 
					android.R.layout.simple_list_item_1, recipe.tags);
			
			rvname.setText(recipe.name);
			rvcreator.setText(recipe.creator);
			rvserving.setText(Integer.toString(recipe.serving));
			rvtime.setText(DateUtils.formatElapsedTime(recipe.time));
			rvrating.setRating(recipe.rating);
			lvingredients.setAdapter(ingredients);
			lvdirections.setAdapter(directions);
			
			ImageView iv = new ImageView(getActivity());
			iv.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
					PhotoDialog pd = PhotoDialog.newInstance(recipe.photo);
					pd.show(ft, "dialog");
				}
			});
			rvphoto.addView(iv);
//			}	
			return v;
		}
}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.flipper);
		flip = new FlipAdapter(getSupportFragmentManager());
		pager = (ViewPager) findViewById(R.id.pager);
		pager.setAdapter(flip);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.flippermenu, menu);
		// Show items on the action bar if we have one
		MenuCompat.setShowAsAction(menu.findItem(R.id.switchtolist), MenuItem.SHOW_AS_ACTION_IF_ROOM);
		MenuCompat.setShowAsAction(menu.findItem(R.id.flipbookhelp), MenuItem.SHOW_AS_ACTION_IF_ROOM);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent intent = new Intent(this, RecipeBookActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return true;
		case R.id.switchtolist:
			switchToListMode();
			return true;
		case R.id.flipbookhelp:
			onHelpItemSelected(item);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void onHelpItemSelected(MenuItem item) {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		DialogFragment helpFragment = HelpDialog.newInstance(HELP_FILENAME);
		helpFragment.show(ft, "help");
	}
	
	public void onSelectRecipe(View v) {
		Uri uri = new Uri.Builder()
			.scheme("content")
			.authority("net.potterpcs.recipebook")
			.build();
		int id = flip.ids[pager.getCurrentItem()];
		uri = ContentUris.withAppendedId(uri, id);
		Intent intent = new Intent(RecipeBook.OPEN_RECIPE_ACTION, uri);
		startActivity(intent);		
	}

	void switchToListMode() {
		Intent intent = new Intent(this, RecipeBookActivity.class);
		
		// When switching back to list mode, we have to restore the
		// state of searching/sorting.
		if (flip.searchQuery != null) {
			intent.putExtra(RecipeBook.SEARCH_EXTRA, flip.searchQuery);
		}
		if (flip.searchTag != null) {
			intent.putExtra(RecipeBook.TAG_EXTRA, flip.searchTag);
		}
		if (flip.timeSearch) {
			intent.putExtra(RecipeBook.TIME_EXTRA, true);
			intent.putExtra(RecipeBook.TIME_EXTRA_MIN, flip.min);
			intent.putExtra(RecipeBook.TIME_EXTRA_MAX, flip.max);
		}
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}
}

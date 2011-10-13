package net.potterpcs.recipebook;

import net.potterpcs.recipebook.RecipeData.Recipe;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
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
	private static final String TAG = "RecipeFlipbook";
	FlipAdapter flip;
	ViewPager pager;
	Intent intent;

	public class FlipAdapter extends FragmentPagerAdapter {
		private static final String TAG = "FlipAdapater";
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
			
			String sortBy = "";
			switch (sortKey) {
			case R.id.menusortrating:
				sortBy = RecipeData.RT_RATING;
				break;
			case R.id.menusorttime:
				sortBy = RecipeData.RT_TIME;
				break;
			case R.id.menusortdate:
				// TODO actually sort by date instead of ID
				sortBy = RecipeData.RT_ID;
			case R.id.menusortname:
			default:
				sortBy = RecipeData.RT_NAME;
				break;
			}
			
			if (sortDescending) {
				sortBy += RecipeBookActivity.SORT_DESCENDING;
			}
			
			if (searchQuery != null) {
				cursor = ((RecipeBook) getApplication()).getData().getMatchingRecipes(searchQuery, sortBy);
			} else if (searchTag != null) {
				cursor = ((RecipeBook) getApplication()).getData().getRecipesByTag(searchTag, sortBy);
			} else if (timeSearch) {
				cursor = ((RecipeBook) getApplication()).getData().getMatchingRecipesByTime(max, min, sortBy);
			} else {
				cursor = ((RecipeBook) getApplication()).getData().getAllRecipes(sortBy);
			}

			ids = new int[cursor.getCount()];
			cursor.moveToFirst();
			for (int i = 0 ; i < cursor.getCount(); ++i) {
				cursor.moveToPosition(i);
				ids[i] = cursor.getInt(cursor.getColumnIndex(RecipeData.RT_ID));
				Log.i(TAG, "Cursor: " + cursor.getString(cursor.getColumnIndex(RecipeData.RT_NAME)));
			}
			Log.i(TAG, "Searching: " + searchQuery + ", Matches: " + cursor.getCount());
			Log.i(TAG, "Sorting by: " + sortBy + " with descending: " + sortDescending + " and key: " + sortKey);
			cursor.close();
		}

		@Override
		public Fragment getItem(int position) {
			// TODO Auto-generated method stub
//			if (searchQuery == null) {
//				return FlipperFragment.newInstance(position + 1);
//			} else {
//				Log.i(TAG, "Moving to position: " + position + " with ID " + cursor.getInt(cursor.getColumnIndex(RecipeData.RT_ID)));
//				cursor.moveToPosition(position);
//				return FlipperFragment.newInstance(cursor.getInt(cursor.getColumnIndex(RecipeData.RT_ID)));
//				return FlipperFragment.newInstance(ids[position]);
//			}
			return FlipperFragment.newInstance(ids[position]);
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
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
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
//			return super.onCreateView(inflater, container, savedInstanceState);
			View v = inflater.inflate(R.layout.recipeviewer, container, false);
			
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
			
			if (recipe.photo != null) {
				Uri uri = Uri.parse(recipe.photo);
				ImageView iv = new ImageView(getActivity());
				
				if (uri.getScheme().equals("content")) {
					iv.setImageURI(uri);
				} else {
					DownloadImageTask.doDownload(recipe.photo, iv);
				}
//					rvphoto.setForeground(new BitmapDrawable(bitmap));
				iv.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
						PhotoDialog pd = PhotoDialog.newInstance(recipe.photo);
						pd.show(ft, "dialog");
					}
				});
				rvphoto.addView(iv);
			}	
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
		MenuCompat.setShowAsAction(menu.findItem(R.id.switchtolist), MenuItem.SHOW_AS_ACTION_IF_ROOM);
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
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	void switchToListMode() {
		Log.i(TAG, getIntent().toString());
		Intent intent;
		if (!Intent.ACTION_MAIN.equals(getIntent().getAction())) {
			intent = new Intent(getIntent());
			intent.setClass(this, RecipeBookActivity.class);
		} else {
			intent = new Intent(this, RecipeBookActivity.class);
			if (getIntent().hasExtra(RecipeBook.SEARCH_EXTRA)) {
				intent.putExtra(RecipeBook.SEARCH_EXTRA, getIntent().getStringExtra(RecipeBook.SEARCH_EXTRA));
			}
			if (getIntent().hasExtra(RecipeBook.TAG_EXTRA)) {
				intent.putExtra(RecipeBook.TAG_EXTRA, getIntent().getStringExtra(RecipeBook.TAG_EXTRA));
			}
			if (getIntent().hasExtra(RecipeBook.TIME_EXTRA)) {
				intent.putExtra(RecipeBook.TIME_EXTRA, true);
				intent.putExtra(RecipeBook.TIME_EXTRA_MIN, getIntent().getIntExtra(RecipeBook.TIME_EXTRA_MIN, 0));
				intent.putExtra(RecipeBook.TIME_EXTRA_MAX, getIntent().getIntExtra(RecipeBook.TIME_EXTRA_MAX, 0));
			}
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		}
		startActivity(intent);
	}
}

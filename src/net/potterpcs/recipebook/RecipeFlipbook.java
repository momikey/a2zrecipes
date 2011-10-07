package net.potterpcs.recipebook;

import java.io.FileNotFoundException;
import java.io.IOException;

import net.potterpcs.recipebook.RecipeData.Recipe;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

public class RecipeFlipbook extends FragmentActivity {
	FlipAdapter flip;
	ViewPager pager;
	Intent intent;

	// TODO work with searches
	public class FlipAdapter extends FragmentPagerAdapter {
		private static final String TAG = "FlipAdapater";
		String searchQuery;
		int sortKey;
		boolean sortDescending;
		Cursor cursor;
		int[] ids;
		
		public FlipAdapter(FragmentManager fm) {
			super(fm);
			searchQuery = getIntent().getStringExtra(RecipeBook.SEARCH_EXTRA);
			sortKey = getIntent().getIntExtra(RecipeBookActivity.SORT_KEY, R.id.menusortname);
			sortDescending = getIntent().getBooleanExtra(RecipeBookActivity.SORT_DESCENDING, false);
			
			String sortBy;
			switch (sortKey) {
			case R.id.menusortrating:
				sortBy = RecipeData.RT_RATING;
				break;
			case R.id.menusorttime:
				sortBy = RecipeData.RT_TIME;
				break;
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
			} else {
				cursor = ((RecipeBook) getApplication()).getData().getAllRecipes(sortBy);
			}

			ids = new int[cursor.getCount()];
			cursor.moveToFirst();
			for (int i = 0 ; i < cursor.getCount(); ++i) {
				cursor.moveToPosition(i);
				ids[i] = cursor.getInt(cursor.getColumnIndex(RecipeData.RT_ID));
			}
			Log.i(TAG, "Searching: " + searchQuery + ", Matches: " + cursor.getCount());
			Log.i(TAG, ids.toString());
			cursor.close();
		}

		@Override
		public Fragment getItem(int position) {
			// TODO Auto-generated method stub
			if (searchQuery == null) {
				return FlipperFragment.newInstance(position + 1);
			} else {
//				Log.i(TAG, "Moving to position: " + position + " with ID " + cursor.getInt(cursor.getColumnIndex(RecipeData.RT_ID)));
//				cursor.moveToPosition(position);
//				return FlipperFragment.newInstance(cursor.getInt(cursor.getColumnIndex(RecipeData.RT_ID)));
				return FlipperFragment.newInstance(ids[position]);
			}
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
				try {
					Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), 
							Uri.parse(recipe.photo));
//					rvphoto.setForeground(new BitmapDrawable(bitmap));
					ImageView iv = new ImageView(getActivity());
					iv.setImageBitmap(bitmap);
					rvphoto.addView(iv);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
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
		
		Button listmode = (Button) findViewById(R.id.recipelistmode);
		listmode.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(v.getContext(), RecipeBookActivity.class);
				if (getIntent().hasExtra(RecipeBook.SEARCH_EXTRA)) {
					intent.putExtra(RecipeBook.SEARCH_EXTRA, getIntent().getStringExtra(RecipeBook.SEARCH_EXTRA));
				}
	            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	            startActivity(intent);
			}
		});
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent intent = new Intent(this, RecipeBookActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
}

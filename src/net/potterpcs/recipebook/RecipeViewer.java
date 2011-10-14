package net.potterpcs.recipebook;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuCompat;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class RecipeViewer extends FragmentActivity {
	RecipeBook app;
	TextView rvname;
	TextView rvcreator;
	TextView rvserving;
	TextView rvtime;
	RatingBar rvrating;
	GridView lvingredients;
	ListView lvdirections;
	FrameLayout rvphoto;
	String photoUri;
	
	static final String TAG = "RecipeViewer";
	
	static final String[] INGREDIENTS_FIELDS = { RecipeData.IT_NAME };
	static final String[] DIRECTIONS_FIELDS = { RecipeData.DT_STEP, RecipeData.DT_SEQUENCE };
	static final int[] INGREDIENTS_IDS = { android.R.id.text1 };
	static final int[] DIRECTIONS_IDS = { R.id.direction, R.id.number };
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.recipeviewer);

		app = (RecipeBook) getApplication();
		RecipeData data = app.getData();

		rvname = (TextView) findViewById(R.id.rvname);
		rvcreator = (TextView) findViewById(R.id.rvcreator);
		rvserving = (TextView) findViewById(R.id.rvserving);
		rvtime = (TextView) findViewById(R.id.rvtime);
		rvrating = (RatingBar) findViewById(R.id.rvrating);
		
		lvingredients = (GridView) findViewById(R.id.ingredients);
		lvdirections = (ListView) findViewById(R.id.directions);
		
		long rid = Long.parseLong(getIntent().getData().getLastPathSegment());
		
		Cursor mdc = data.getSingleRecipe(rid);
		startManagingCursor(mdc);
		mdc.moveToPosition(0);
		rvname.setText(mdc.getString(mdc.getColumnIndex(RecipeData.RT_NAME)));
		rvcreator.setText(mdc.getString(mdc.getColumnIndex(RecipeData.RT_CREATOR)));
		rvserving.setText(mdc.getString(mdc.getColumnIndex(RecipeData.RT_SERVING)));
		rvtime.setText(DateUtils.formatElapsedTime(
				mdc.getLong(mdc.getColumnIndex(RecipeData.RT_TIME))));
		rvrating.setRating(mdc.getFloat(mdc.getColumnIndex(RecipeData.RT_RATING)));
		
		photoUri = mdc.getString(mdc.getColumnIndex(RecipeData.RT_PHOTO));
		if (photoUri != null) {
			rvphoto = (FrameLayout) findViewById(R.id.photofragment);
			ImageView iv = new ImageView(this);
			if (Uri.parse(photoUri).getScheme().equals("content")) {
				iv.setImageURI(Uri.parse(photoUri));
			} else {
				DownloadImageTask.doDownload(photoUri, iv);
			}
			iv.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
					PhotoDialog pd = PhotoDialog.newInstance(photoUri);
					pd.show(ft, "dialog");
				}
			});
			rvphoto.addView(iv);
		}

		Cursor dirc = data.getRecipeDirections(rid);
		startManagingCursor(dirc);
		SimpleCursorAdapter directions = new SimpleCursorAdapter(this, R.layout.recipedirectionrow,
				dirc, DIRECTIONS_FIELDS, DIRECTIONS_IDS);
		lvdirections.setAdapter(directions);
		lvdirections.setDividerHeight(0);
		
		Cursor ingc = data.getRecipeIngredients(rid);
		startManagingCursor(ingc);
		SimpleCursorAdapter ingredients = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, 
				ingc, INGREDIENTS_FIELDS, INGREDIENTS_IDS);
		lvingredients.setAdapter(ingredients);
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.viewermenu, menu);
		MenuCompat.setShowAsAction(menu.findItem(R.id.viewertimer), 
				MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.viewertimer:
			onTimerSelected(item);
			return true;
		case android.R.id.home:
			Intent intent = new Intent(this, RecipeBookActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	public void onTimerSelected(MenuItem item) {
    	FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
    	Fragment timerFragment = new TimerFragment();
    	transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
    	transaction.replace(R.id.timerfragment, timerFragment);
    	transaction.addToBackStack(null);
    	transaction.commit();
	}
}

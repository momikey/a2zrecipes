package net.potterpcs.recipebook;

import java.util.Date;

import net.potterpcs.recipebook.RecipeData.Recipe;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

public class RecipeEditor extends FragmentActivity {
	public static final String TAG = "RecipeEditor";
	static final int GALLERY_ACTIVITY = 1;
	private static final int METADATA = 0;
	private static final int INGREDIENTS = 1;
	private static final int DIRECTIONS = 2;
	private static final int TAGS = 3;
	
	private Recipe recipe;
	private long recipeId;
	FragmentManager manager;
	MetadataEditor meta;
	IngredientsEditor ingredients;
	DirectionsEditor directions;
	TagsEditor tags;
	RecipeData recipeData;
	ListView sslistview;
	String[] fragmentnames;
	FrameLayout ssfragmentframe;
	Fragment nextFragment;
	Fragment lastFragment;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.recipeeditor);
		
		sslistview = (ListView) findViewById(R.id.ssfragmentlist);
		ssfragmentframe = (FrameLayout) findViewById(R.id.ssfragment);
		fragmentnames = getResources().getStringArray(R.array.fragmentnames);
		
		recipeId = Long.parseLong(getIntent().getData().getLastPathSegment());
		manager = getSupportFragmentManager();
		meta = (MetadataEditor) manager.findFragmentById(R.id.metafragment);
		ingredients = (IngredientsEditor) manager.findFragmentById(R.id.ingredientsfragment);
		directions = (DirectionsEditor) manager.findFragmentById(R.id.directionsfragment);
		tags = (TagsEditor) manager.findFragmentById(R.id.tagsfragment);
		recipeData = ((RecipeBook) getApplication()).getData();
		
		if (recipeId != -1) {
			recipe = recipeData.getSingleRecipeObject(recipeId);
		} else {
			recipe = new Recipe();
		}
		
		if (sslistview != null) {
			meta = new MetadataEditor();
			ingredients = new IngredientsEditor();
			directions = new DirectionsEditor();
			tags = new TagsEditor();
			
			FragmentTransaction setup = manager.beginTransaction();
			setup.add(R.id.ssfragment, meta, "meta");
			setup.hide(meta);
			setup.add(R.id.ssfragment, ingredients, "ingredients");
			setup.hide(ingredients);
			setup.add(R.id.ssfragment, directions, "directions");
			setup.hide(directions);
			setup.add(R.id.ssfragment, tags, "tags");
			setup.hide(tags);
			setup.commit();
			
			sslistview.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					String itemText = ((TextView) view).getText().toString();
					
					lastFragment = nextFragment;
					
					if (itemText.equals(fragmentnames[METADATA])) {
						nextFragment = meta;
					} else if (itemText.equals(fragmentnames[INGREDIENTS])) {
						nextFragment = ingredients;
					} else if (itemText.equals(fragmentnames[DIRECTIONS])) {
						nextFragment = directions;
					} else if (itemText.equals(fragmentnames[TAGS])) {
						nextFragment = tags;
					} else {
						nextFragment = null;
					}
					
					if (nextFragment != null && !nextFragment.equals(lastFragment)) {
						FragmentTransaction ft = manager.beginTransaction();
						if (lastFragment != null) {
							ft.hide(lastFragment);
						}
						ft.show(nextFragment);
						ft.commit();
					}
				}
			});
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.editormenu, menu);
		MenuCompat.setShowAsAction(menu.findItem(R.id.editormenusave), 
				MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		MenuCompat.setShowAsAction(menu.findItem(R.id.editormenuattach), 
				MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.editormenusave:
			onSaveItem(item);
			return true;
		case R.id.editormenuattach:
			onAttachPhoto(item);
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
	
	public long getRecipeId() {
		return recipeId;
	}
	
	public Recipe getRecipe() {
		return recipe;
	}
	
	public void onSaveItem(MenuItem item) {
		recipe = meta.getRecipeMetadata();
		recipe.id = recipeId;
		recipe.date = new Date().toString();
		recipe.ingredients = ingredients.getIngredients();
		recipe.directions = directions.getDirections();
		recipe.tags = tags.getTags();
		
		if (recipe.id == -1) {
			// an insert of a new recipe
			recipeData.insertRecipe(recipe);
			recipeId = recipeData.getLastInsertRecipeId();
		} else {
			// an edit of an existing recipe
			recipeData.updateRecipe(recipe);
		}
	}
	
	public void onAttachPhoto(MenuItem item) {
		// TODO
		Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(intent, GALLERY_ACTIVITY);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (requestCode == GALLERY_ACTIVITY) {
				Uri selectedImage = data.getData();
				Log.i(TAG, selectedImage.toString());
				recipe.photo = selectedImage.toString();
			}
		}
	}
}

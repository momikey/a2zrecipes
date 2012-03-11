package net.potterpcs.recipebook;

import android.app.Application;

public class RecipeBook extends Application {
	private RecipeData recipeData;
	static final String SEARCH_EXTRA = "search-query";
	static final String TAG_EXTRA = "tag-search";
	static final String TIME_EXTRA = "time-search";
	static final String TIME_EXTRA_MAX = "time-search-maximum";
	static final String TIME_EXTRA_MIN = "time-search-minimum";
	public static final String OPEN_RECIPE_ACTION = "net.potterpcs.recipebook.OPEN_RECIPE";

	public void onCreate() {
		super.onCreate();
		recipeData = new RecipeData(this);
	}

    public RecipeData getData() {
    	return recipeData;
    }
}

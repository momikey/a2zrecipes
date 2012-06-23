/*
 *    Copyright 2012 Michael Potter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

/*
 * RateRecipeDialog.java - Simple popup allowing a user to change a recipe's
 * rating without editing it. 
 */

package net.potterpcs.recipebook;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

public class RateRecipeDialog extends DialogFragment {
	// Tag for logging
//	private static final String TAG = "RateRecipeDialog";

	// Handle for the fragment holding the list of recipes 
	private RecipeListFragment fragment;
	
	// Information for the recipe we're rating
	long recipeId;
	String recipeName;
	float oldRating;
	
	// The rating bar itself
	private RatingBar bar;
	
	static RateRecipeDialog newInstance(RecipeListFragment frag, long id, String name, float old) {
		// Standard Android factory method
		RateRecipeDialog rrd = new RateRecipeDialog(frag, id, name, old);
		Bundle args = new Bundle();
		rrd.setArguments(args);
		return rrd;
	}
	
	public RateRecipeDialog(RecipeListFragment frag, long id, String name, float old) {
		fragment = frag;
		recipeId = id;
		recipeName = name;
		oldRating = old;
		setStyle(STYLE_NO_TITLE, getTheme());
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.raterecipedialog, container, false);
		TextView tv = (TextView) v.findViewById(R.id.raterecipetext);
		String txt = String.format(getActivity().getResources().getString(R.string.raterecipetext), 
				recipeName);
		tv.setText(txt);
		
		bar = (RatingBar) v.findViewById(R.id.raterecipebar);
		bar.setRating(oldRating);
		
		// Nothing happens until the user hits the "Rate It" button. Then the
		// new rating is actually committed to the database. This means that
		// you can keep playing with the stars as much as you want, then hit
		// "Back", and you won't have to worry about the rating actually changing.
		Button button = (Button) v.findViewById(R.id.raterecipebutton);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (bar.getRating() != oldRating) {
					ContentValues cv = new ContentValues();
					cv.put(RecipeData.RT_RATING, bar.getRating());
					RecipeData data = ((RecipeBook) getActivity().getApplication()).getData();
					data.updateRecipe(recipeId, cv);
					dismiss();
				}
			}
		});
		
		return v;
	}
	
	@Override
	public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);
		fragment.onRateDialogDismissed();
	}
}

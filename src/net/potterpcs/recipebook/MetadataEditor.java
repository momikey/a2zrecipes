package net.potterpcs.recipebook;

import net.potterpcs.recipebook.RecipeData.Recipe;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RatingBar;

public class MetadataEditor extends Fragment {
	Recipe r;
	private RecipeEditor activity;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.metadataedit, container, false);
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		activity = (RecipeEditor) getActivity();
		r = activity.getRecipe();
		
		if (activity.getRecipeId() != -1) {
			((EditText) activity.findViewById(R.id.nameedit)).setText(r.name);
			((EditText) activity.findViewById(R.id.descriptionedit)).setText(r.description);
			((EditText) activity.findViewById(R.id.hoursedit)).setText(Integer.toString(r.time / 60));
			((EditText) activity.findViewById(R.id.minutesedit)).setText(Integer.toString(r.time % 60));
			((EditText) activity.findViewById(R.id.servingedit)).setText(Integer.toString(r.serving));
			((EditText) activity.findViewById(R.id.creatoredit)).setText(r.creator);
			((RatingBar) activity.findViewById(R.id.ratingbar)).setRating(r.rating);
		}
	}
	
	public Recipe getRecipeMetadata() {
		r.name = ((EditText) activity.findViewById(R.id.nameedit)).getText().toString();
		r.description = ((EditText) activity.findViewById(R.id.descriptionedit)).getText().toString();
		
		String hs = ((EditText) activity.findViewById(R.id.hoursedit)).getText().toString();
		int hours;
		try {
			hours = Integer.parseInt(hs);
		} catch (NumberFormatException e) {
			hours = 0;
		}
		
		String ms = ((EditText) activity.findViewById(R.id.minutesedit)).getText().toString();
		int minutes;
		try {
			minutes = Integer.parseInt(ms);
		} catch (NumberFormatException e) {
			minutes = 0;
		}
		
		r.time = hours * 60 + minutes;
		
		try {
			r.serving = Integer.parseInt(((EditText) activity.findViewById(R.id.servingedit)).getText().toString());
		} catch (NumberFormatException e) {
			r.serving = 0;
		}
		
		r.creator = ((EditText) activity.findViewById(R.id.creatoredit)).getText().toString();
		r.rating = ((RatingBar) activity.findViewById(R.id.ratingbar)).getRating();
		
		return r;
	}
}

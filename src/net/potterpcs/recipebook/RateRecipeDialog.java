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
	private static final String TAG = "RateRecipeDialog";
	long recipeId;
	String recipeName;
	float oldRating;
	private RatingBar bar;
	private RecipeListFragment fragment;
	
	static RateRecipeDialog newInstance(RecipeListFragment frag, long id, String name, float old) {
		RateRecipeDialog rrd = new RateRecipeDialog(frag, id, name, old);
		
		// Bundle stuff here
		
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

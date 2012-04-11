package net.potterpcs.recipebook;

import net.potterpcs.recipebook.RecipeData.Recipe;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

public class PhotoEditor extends Fragment {
	// Tag for logging
//	private static final String TAG = "PhotoEditor";

	// Bundle state
	static final String STATE = "photo";
	
	// Request code for browsing the photo gallery
	static final int GALLERY_ACTIVITY = 1;

	// Handle to the parent activity
	private RecipeEditor activity;
	
	// The URI of the photo (in a String, for putting in a Recipe object)
	String photo;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		activity = (RecipeEditor) getActivity();
		long rid = activity.getRecipeId();
		photo = null;
		
		// Load old state if we have it...
		if (savedInstanceState != null) {
			photo = savedInstanceState.getString(STATE);
		} else {
			// No old state, so create a new editor
			if (rid > 0 && photo == null) {
				RecipeBook app = (RecipeBook) activity.getApplication();
				Recipe r = app.getData().getSingleRecipeObject(rid);
				photo = r.photo;
			} else {

			}
		}
		
		return inflater.inflate(R.layout.photoedit, container, false); 
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		// Set up the UI elements' OnClickListeners. We do this here to make
		// sure that the UI is actually created before we start using it.
		Button attach = (Button) activity.findViewById(R.id.photoeditattach);
		attach.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onAttachPhoto(v);
			}
		});
		
		Button remove = (Button) activity.findViewById(R.id.photoeditremove);
		remove.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onRemovePhoto(v);
			}
		});
		
		View photoView = activity.findViewById(R.id.photoeditphoto);
		photoView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onPhotoClick(v);
			}
		});
		
	}
	
	@Override
	public void onResume() {
		super.onResume();
		changeImage(photo);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(STATE, photo);
	}
	
	public void onAttachPhoto(View v) {
		activity.onAttachPhoto(v);
	}
	
	public void onRemovePhoto(View v) {
		photo = null;
		changeImage(null);
	}
	
	public void onPhotoClick(View v) {
		FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
		PhotoDialog pd = PhotoDialog.newInstance(photo);
		pd.show(ft, "dialog");
	}
	
	void changeImage(String uri) {
		// Change the recipe's image. We have to do a lot of work to get
		// a bitmap that won't break Android's memory limits, but that's
		// all hidden away in the loader method. We do still have to set
		// the scaling method here, so that the image fits on the screen.
		ImageView iv = (ImageView) getActivity().findViewById(R.id.photoeditphoto);
		iv.setScaleType(ScaleType.CENTER_INSIDE);
		RecipeBook.setImageViewBitmapDecoded(getActivity(), iv, uri);
//		Log.v(TAG, uri == null ? "no image" : uri);
		photo = uri;
	}
	
	public String getPhotoUri() {
		return photo;
	}
}

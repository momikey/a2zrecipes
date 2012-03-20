package net.potterpcs.recipebook;

import net.potterpcs.recipebook.RecipeData.Recipe;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

public class PhotoEditor extends Fragment {
	private static final String TAG = "PhotoEditor";

	static final int GALLERY_ACTIVITY = 1;
	static final String STATE = "photo";
	String photo;
	private RecipeEditor activity;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		activity = (RecipeEditor) getActivity();
		long rid = activity.getRecipeId();
		photo = null;
		
		if (savedInstanceState != null) {
			photo = savedInstanceState.getString(STATE);
		} else {

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
		ImageView iv = (ImageView) getActivity().findViewById(R.id.photoeditphoto);
		RecipeBook.setImageViewBitmapDecoded(getActivity(), iv, uri);
		Log.v(TAG, uri == null ? "no image" : uri);
		photo = uri;
	}
	
	public String getPhotoUri() {
		return photo;
	}
}

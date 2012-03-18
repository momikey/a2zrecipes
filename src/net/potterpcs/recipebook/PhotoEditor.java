package net.potterpcs.recipebook;

import java.io.IOException;
import java.io.InputStream;

import net.potterpcs.recipebook.RecipeData.Recipe;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
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
	private Bitmap bitmap;
	
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
//		Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//		startActivityForResult(intent, GALLERY_ACTIVITY);
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
	
//	@Override
//	public void onActivityResult(int requestCode, int resultCode, Intent data) {
//		if (resultCode == Activity.RESULT_OK) {
//			if (requestCode == GALLERY_ACTIVITY) {
//				Uri selectedImage = data.getData();
//				photo = selectedImage.toString();
//				changeImage(photo);
//			}
//		}
//	}
	
	void changeImage(String uri) {
		ImageView iv = (ImageView) getActivity().findViewById(R.id.photoeditphoto);
		// TODO handle online uris
//		if (uri != null) {
//			Uri u = Uri.parse(uri);
//			if (u.getScheme().startsWith("content")) {
//				bitmap = decodeStream(u);
//				iv.setImageBitmap(bitmap);
//			} else {
//				// TODO scale other bitmaps to avoid OOM
//				iv.setImageURI(u);
//			}
//			iv.setImageBitmap(bitmap);
//			iv.setAdjustViewBounds(true);
//			iv.setMaxWidth(getView().getWidth());
//		} else {
//			iv.setImageBitmap(null);
//		}
		RecipeBook.setImageViewBitmapDecoded(getActivity(), iv, uri);
		Log.v(TAG, uri == null ? "no image" : uri);
		photo = uri;
	}
	
	public String getPhotoUri() {
		return photo;
	}
	
	private Bitmap decodeStream(Uri uri) {
		Bitmap b = null;
		final int REQUIRED_SIZE = 960;
		
		try {
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			
			InputStream is = getActivity().getContentResolver().openInputStream(uri);
			BitmapFactory.decodeStream(is, null, o);
			is.close();
			
			int scale=1;
	        while(o.outWidth/scale/2>=REQUIRED_SIZE && o.outHeight/scale/2>=REQUIRED_SIZE)
	            scale*=2;
			
	        InputStream is2 = getActivity().getContentResolver().openInputStream(uri);
	        BitmapFactory.Options o2 = new BitmapFactory.Options();
	        o2.inSampleSize = scale;
	        b = BitmapFactory.decodeStream(is2, null, o2);

		} catch (IOException e) {
			// return a null bitmap, same as if we removed the photo
		}
		return b;
	}
}

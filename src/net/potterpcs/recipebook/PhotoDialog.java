package net.potterpcs.recipebook;

import java.io.IOException;
import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

public class PhotoDialog extends DialogFragment {
	Uri photoUri;
	
	public static final String URI = "uri";
	private static final String TAG = "PhotoDialog";
	
	static PhotoDialog newInstance(String uri) {
		PhotoDialog pd = new PhotoDialog();
		
		Bundle args = new Bundle();
		args.putString(URI, uri);
		pd.setArguments(args);
		
		return pd;
	}
	
	static PhotoDialog newInstance(Uri uri) {
		return newInstance(uri.toString());
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		photoUri = Uri.parse(getArguments().getString(URI));
		setStyle(STYLE_NO_TITLE, 0);
		Log.i(TAG, "Creating photo dialog with uri: " + photoUri.toString());
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.photodialog, container, false);
		ImageView photo = (ImageView) v.findViewById(R.id.dialogphoto);
		
//		if (!photoUri.getScheme().contains("http")) {
//			if (photoUri.getScheme().startsWith("content")) {
//				photo.setImageBitmap(decodeStream(photoUri));
//			} else {
//				photo.setImageURI(photoUri);
//			}
//		} else {
//			DownloadImageTask.doDownload(getActivity(), photoUri.toString(), photo);
//		}
		RecipeBook.setImageViewBitmapDecoded(getActivity(), photo, photoUri);
		
		photo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View clicked) {
				Log.i(TAG, "Dismissing dialog");
				dismiss();
			}
		});
		
		return v;
	}
	
	private Bitmap decodeStream(Uri uri) {
		Bitmap b = null;
		final int REQUIRED_SIZE = 1280;
		
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

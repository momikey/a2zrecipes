package net.potterpcs.recipebook;

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
		String u = getArguments().getString(URI);
		if (u != null) {
			photoUri = Uri.parse(u);
		} else {
			photoUri = null;
		}
		setStyle(STYLE_NO_TITLE, 0);
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
}

package net.potterpcs.recipebook;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.ImageView;

public class RecipeBook extends Application {
	// The data layer. This is accessed by pretty much every part of the app.
	// All database queries, updates, and deletes go through here.
	private RecipeData recipeData;
	
	// Various bundle state extras for use with searching and sorting
	static final String SEARCH_EXTRA = "search-query";
	static final String TAG_EXTRA = "tag-search";
	static final String TIME_EXTRA = "time-search";
	static final String TIME_EXTRA_MAX = "time-search-maximum";
	static final String TIME_EXTRA_MIN = "time-search-minimum";
	
	// A custom intent action for opening a recipe. We can use this instead of
	// the generic open action so that only our app is used when importing.
	public static final String OPEN_RECIPE_ACTION = "net.potterpcs.recipebook.OPEN_RECIPE";
	
	// The default maximum size of a recipe photo. This is only used when
	// loading a bitmap, because most camera images have to be scaled down
	// when loading, or they'll cause an out-of-memory error.
	private static final int DEFAULT_REQUIRED_SIZE = 800;

	public void onCreate() {
		super.onCreate();
		recipeData = new RecipeData(this);
	}

    public RecipeData getData() {
    	return recipeData;
    }
    
    public static ImageView setImageViewBitmapDecoded(Activity a, ImageView iv, String s) {
    	return setImageViewBitmapDecoded(a, iv, s, DEFAULT_REQUIRED_SIZE);
    }
    
    public static ImageView setImageViewBitmapDecoded(Activity a, ImageView iv, String s, int size) {
    	Uri u = null;
    	
    	if (s != null && !s.equalsIgnoreCase("")) {
    		u = Uri.parse(s);
    	}
    	
    	return setImageViewBitmapDecoded(a, iv, u, size);
    }
    
    public static ImageView setImageViewBitmapDecoded(Activity a, ImageView iv, Uri uri) {
    	return setImageViewBitmapDecoded(a, iv, uri, DEFAULT_REQUIRED_SIZE);
    }
    
    public static ImageView setImageViewBitmapDecoded(Activity a, ImageView iv, Uri uri, int size) {
    	// The real meat of this method runs in a background thread, unless we
    	// get a null URI.
    	if (uri != null) {
    		if (!uri.getScheme().startsWith("http")) {
    			// A local file gets loaded this way...
    			new BackgroundBitmapDecoder(a, iv, size).execute(uri);
    		} else {
    			// ...while online data has to go through this.
    			new DownloadImageTask(a, iv).execute(uri.toString());
    		}
    	} else {
    		iv.setImageBitmap(null);
    	}
    	return iv;
    }
    
    static Bitmap decodeScaledBitmap(Activity a, Uri uri) 
    		throws FileNotFoundException, IOException {
    	return decodeScaledBitmap(a, uri, DEFAULT_REQUIRED_SIZE);
    }

	static Bitmap decodeScaledBitmap(Activity a, Uri uri, int size)
			throws FileNotFoundException, IOException {
		// This is a copy of the "background" loader below, but this
		// one is made to run in the thread that calls it. Right now,
		// this means that the image downloader can use it without
		// spawning another new thread.
		
		// The idea for this method (and much of the code) comes from
		// a post on StackOverflow.
		Bitmap b;
		BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;

		InputStream is = createInputStream(uri, a);
		BitmapFactory.decodeStream(is, null, o);
		is.close();

		int scale=1;
		while(o.outWidth/scale/2>=size || o.outHeight/scale/2>=size)
			scale*=2;

		InputStream is2 = createInputStream(uri, a);
		BitmapFactory.Options o2 = new BitmapFactory.Options();
		o2.inSampleSize = scale;
		o2.inPurgeable = true;
		o2.inInputShareable = true;
		
		b = BitmapFactory.decodeStream(is2, null, o2);
		return b;
	}
    
    private static InputStream createInputStream(Uri uri, Activity a) throws FileNotFoundException {
    	InputStream is;
    	String scheme = uri.getScheme();
    	
    	if (scheme.startsWith("content")) {
    		is = a.getContentResolver().openInputStream(uri);
    	} else if (scheme.startsWith("file")) {
    		is = new FileInputStream(uri.getPath());
    	} else {
    		is = null;
    	}
    	
    	return is;
    }
    
    public static class BackgroundBitmapDecoder extends AsyncTask<Uri, Integer, Bitmap> {
    	// This is a bitmap loader/scaler that is made to run as
    	// a background thread. Not only does this prevent ANRs,
    	// but it also means that multi-core devices could load
    	// multiple bitmaps simultaneously.
    	
    	Activity parent;
    	ImageView view;
    	int size;
    	
    	public BackgroundBitmapDecoder(Activity a, ImageView v, int s) {
    		parent = a;
    		view = v;
    		size = s;
    	}

		@Override
		protected Bitmap doInBackground(Uri... params) {
			Bitmap b;
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;

			InputStream is;
			try {
				is = createInputStream(params[0], parent);
				BitmapFactory.decodeStream(is, null, o);
				is.close();
			} catch (IOException e) {
				b = null;
			}

			int scale=1;
			while(o.outWidth/scale/2>=size || o.outHeight/scale/2>=size)
				scale*=2;

			InputStream is2;
			try {
				is2 = createInputStream(params[0], parent);
				BitmapFactory.Options o2 = new BitmapFactory.Options();
				o2.inSampleSize = scale;
				o2.inPurgeable = true;
				o2.inInputShareable = true;

				b = BitmapFactory.decodeStream(is2, null, o2);
			} catch (FileNotFoundException e) {
				b = null;
			}

			return b;
		}
    	
		@Override
		protected void onPostExecute(Bitmap result) {
			view.setImageBitmap(result);
		}
    }
}

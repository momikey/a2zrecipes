package net.potterpcs.recipebook;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;

import android.app.Activity;
import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.ImageView;

public class RecipeBook extends Application {
	private RecipeData recipeData;
	static final String SEARCH_EXTRA = "search-query";
	static final String TAG_EXTRA = "tag-search";
	static final String TIME_EXTRA = "time-search";
	static final String TIME_EXTRA_MAX = "time-search-maximum";
	static final String TIME_EXTRA_MIN = "time-search-minimum";
	public static final String OPEN_RECIPE_ACTION = "net.potterpcs.recipebook.OPEN_RECIPE";
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
    	
    	if (s != null) {
    		u = Uri.parse(s);
    	}
    	
    	return setImageViewBitmapDecoded(a, iv, u, size);
    }
    
    public static ImageView setImageViewBitmapDecoded(Activity a, ImageView iv, Uri uri) {
    	return setImageViewBitmapDecoded(a, iv, uri, DEFAULT_REQUIRED_SIZE);
    }
    
    public static ImageView setImageViewBitmapDecoded(Activity a, ImageView iv, Uri uri, int size) {
    	Bitmap b = null;
    	final int REQUIRED_SIZE = size;
    	
    	if (uri != null) {
    		if (!uri.getScheme().startsWith("http")) {
    			try {
    				BitmapFactory.Options o = new BitmapFactory.Options();
    				o.inJustDecodeBounds = true;

    				InputStream is = createInputStream(uri, a);
    				BitmapFactory.decodeStream(is, null, o);
    				is.close();

    				int scale=1;
    				while(o.outWidth/scale/2>=REQUIRED_SIZE && o.outHeight/scale/2>=REQUIRED_SIZE)
    					scale*=2;

    				InputStream is2 = createInputStream(uri, a);
    				BitmapFactory.Options o2 = new BitmapFactory.Options();
    				o2.inSampleSize = scale;
    				o2.inPurgeable = true;
    				o2.inInputShareable = true;
    				
    				b = BitmapFactory.decodeStream(is2, null, o2);

    			} catch (IOException e) {
    				// return a null bitmap, same as if we removed the photo
    			}
    		} else {
    			try {
    				b = new DownloadImageTask(a, iv).get();
    			} catch (InterruptedException e) {
    				b = null;
    			} catch (ExecutionException e) {
    				b = null;
    			}
    		}
    	}

    	if (iv != null) {
    		iv.setImageBitmap(b);
    	}
    	
    	return iv;
    }
    
    private static InputStream createInputStream(Uri uri, Activity a) throws FileNotFoundException {
    	InputStream is;
    	String scheme = uri.getScheme();
    	
    	if (scheme.startsWith("content")) {
    		is = a.getContentResolver().openInputStream(uri);
    	} else if (scheme.startsWith("file")) {
    		is = new FileInputStream(uri.toString());
    	} else {
    		is = null;
    	}
    	
    	return is;
    }
}

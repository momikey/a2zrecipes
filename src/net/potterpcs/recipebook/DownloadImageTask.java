package net.potterpcs.recipebook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.widget.ImageView;

public class DownloadImageTask extends AsyncTask<String, Integer, Bitmap> {
	// Tag for logging
//	private static final String TAG = "DownloadImageTask";

	// Handle for the parent activity
	Activity parent;
	
	// Handle for the ImageView that we will fill
	ImageView view;

	public DownloadImageTask(Activity a, ImageView v) {
		parent = a;
		view = v;
	}

	@Override
	protected Bitmap doInBackground(String... params) {
		return downloadImage(params);
	}

	private Bitmap downloadImage(String... urls) {
		Bitmap bitmap = null;
		RecipeData data = ((RecipeBook) parent.getApplication()).getData();

		AndroidHttpClient client = AndroidHttpClient.newInstance("A to Z Recipes for Android");

		if (data.isCached(urls[0])) {
			// Retrieve a cached image if we have one
			String pathName = data.findCacheEntry(urls[0]);
			Uri pathUri = Uri.fromFile(new File(parent.getCacheDir(), pathName));
			try {
				bitmap = RecipeBook.decodeScaledBitmap(parent, pathUri);
			} catch (IOException e) {
				e.printStackTrace();
				bitmap = null;
			}
		} else {
			try {
				// If the image isn't in the cache, we have to go and get it.
				// First, we set up the HTTP request.
				HttpGet request = new HttpGet(urls[0]);
				HttpParams params = new BasicHttpParams();
				HttpConnectionParams.setSoTimeout(params, 60000);
				request.setParams(params);
				
				// Let the UI know we're working.
				publishProgress(25);

				// Retrieve the image from the network.
				HttpResponse response = client.execute(request);
				publishProgress(50);

				// Create a bitmap to put in the ImageView.
				byte[] image = EntityUtils.toByteArray(response.getEntity());
				bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
				publishProgress(75);
				
				// Cache the file for offline use, and to lower data usage.
				File cachePath = parent.getCacheDir();
				String cacheFile = "recipecache-" + Long.toString(System.currentTimeMillis());
				if (bitmap.compress(Bitmap.CompressFormat.PNG, 0, 
						new FileOutputStream(new File(cachePath, cacheFile)))) {
					RecipeData appData = ((RecipeBook) parent.getApplication()).getData();
					appData.insertCacheEntry(urls[0], cacheFile);
				}
//				Log.v(TAG, cacheFile);
				
				// We're done!
				publishProgress(100);
			} catch (IOException e) {
				// TODO Maybe a dialog?
			}

		}
		client.close();
		return bitmap;
	}

	@Override
	protected void onPostExecute(Bitmap result) {
		// UI changes happen here
		view.setImageBitmap(result);
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		// TODO If the download is going to take a while, pop up a dialog
//		Log.v(TAG, values[0].toString());
	}

	public static void doDownload(Activity a, String url, ImageView view) {
		new DownloadImageTask(a, view).execute(url);
	}
}

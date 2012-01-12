package net.potterpcs.recipebook;

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
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

public class DownloadImageTask extends AsyncTask<String, Integer, Bitmap> {
	private static final String TAG = "DownloadImageTask";

	Activity parent;
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
		// TODO change the user-agent to something sensible
		AndroidHttpClient client = AndroidHttpClient.newInstance("net.potterpcs.recipebook");
		Bitmap bitmap = null;
		RecipeData data = ((RecipeBook) parent.getApplication()).getData();
		if (data.isCached(urls[0])) {
			bitmap = BitmapFactory.decodeFile(data.findCacheEntry(urls[0]));
		} else {
			try {
				// TODO cache the image to lower data usage
				HttpGet request = new HttpGet(urls[0]);
				HttpParams params = new BasicHttpParams();
				HttpConnectionParams.setSoTimeout(params, 60000);
				request.setParams(params);
				publishProgress(25);

				HttpResponse response = client.execute(request);
				publishProgress(50);

				byte[] image = EntityUtils.toByteArray(response.getEntity());
				publishProgress(75);

				bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
				publishProgress(100);
			} catch (IOException e) {
				e.printStackTrace();
			}

			client.close();
		}
		return bitmap;
	}

	@Override
	protected void onPostExecute(Bitmap result) {
		view.setImageBitmap(result);
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		Log.i(TAG, values[0].toString());
	}

	public static void doDownload(Activity a, String url, ImageView view) {
		new DownloadImageTask(a, view).execute(url);
	}
}

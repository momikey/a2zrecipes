package net.potterpcs.recipebook;

import java.io.File;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class CacheService extends IntentService {
	// The maximum amount of memory the cache should use
	// TODO allow user-defined cachesize
	private static final long MAXCACHE = 10240000;

	public CacheService() {
		super("CacheService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		RecipeData data = ((RecipeBook) getApplication()).getData();
		File cachePath = getCacheDir();
		
		// Keep removing the oldest cache entry until we're under the limit.
		while (isOverCache()) {
			String oldest = data.removeOldestCacheEntry();
			File f = new File(cachePath, oldest);
			f.delete();
		}
	}
		
	private boolean isOverCache() {
		long csz = 0;

		File cachePath = getCacheDir();
		File[] files = cachePath.listFiles();

		for (int i = 0; i < files.length; i++) {
			if (files[i].isFile()) {
				csz += files[i].length();
			}
		}
		return csz >= MAXCACHE;
	}

}

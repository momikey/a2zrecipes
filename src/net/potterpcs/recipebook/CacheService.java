package net.potterpcs.recipebook;

import java.io.File;

import android.app.IntentService;
import android.content.Intent;

public class CacheService extends IntentService {
	// TODO allow user-defined cachesize
	private static final long MAXCACHE = 1024000;

	public CacheService() {
		super("CacheService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		RecipeData data = ((RecipeBook) getApplication()).getData();
		File cachePath = getCacheDir();
		
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
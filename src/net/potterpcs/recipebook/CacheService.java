/*
 *    Copyright 2012 Michael Potter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

/*
 * CacheService.java - A service that runs as the application is closing to
 * reclaim device storage space used by old cached images.
 */

package net.potterpcs.recipebook;

import java.io.File;

import android.app.IntentService;
import android.content.Intent;

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

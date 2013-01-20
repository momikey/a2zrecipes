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
 * RecipeBackupAgent.java - Implements an Android backup agent. 
 */

package net.potterpcs.recipebook;

import java.io.File;
import java.io.IOException;

import android.app.backup.BackupAgentHelper;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.app.backup.FileBackupHelper;
import android.app.backup.SharedPreferencesBackupHelper;
import android.os.ParcelFileDescriptor;

public class RecipeBackupAgent extends BackupAgentHelper {
	// This is the name of the default shared preferences file.
	// Android doesn't give us an easy way of passing that file to the backup
	// agent, so it took a little digging to find it.
	// For reference, the format is "<packagename>_preferences".
	static final String DEFAULT_SHARED_PREFS = "net.potterpcs.recipebook_preferences";
	
	@Override
	public void onCreate() {
		// We have two backup helpers: one for the database, the other for preferences.
		// Note that, strictly speaking, a FileBackupHelper isn't meant to backup
		// a database, but since our recipe DB is all text, it probably won't hurt.
		FileBackupHelper dbs = new FileBackupHelper(this, RecipeData.DB_FILENAME);
		addHelper("dbs", dbs);
		
		SharedPreferencesBackupHelper shp = 
			new SharedPreferencesBackupHelper(this, DEFAULT_SHARED_PREFS);
		addHelper("shp", shp);
	}
	
	@Override
	public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data,
			ParcelFileDescriptor newState) throws IOException {
		// We need to synchronize when using the FileBackupHelper
		synchronized (RecipeData.DB_LOCK) {
			super.onBackup(oldState, data, newState);			
		}
	}
	
	@Override
	public void onRestore(BackupDataInput data, int appVersionCode,
			ParcelFileDescriptor newState) throws IOException {
		// We need to synchronize when using the FileBackupHelper
		synchronized (RecipeData.DB_LOCK) {
			super.onRestore(data, appVersionCode, newState);			
		}
	}
	
	@Override
	public File getFilesDir() {
		// This is a small hack (from StackOverflow) to allow us to use
		// the FileBackupHelper to backup a database, because they are
		// stored in a different directory than "normal" internal files.
		return getDatabasePath(RecipeData.DB_FILENAME).getParentFile();
	}
}

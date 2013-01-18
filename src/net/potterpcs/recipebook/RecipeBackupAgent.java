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
import android.os.ParcelFileDescriptor;

public class RecipeBackupAgent extends BackupAgentHelper {
	@Override
	public void onCreate() {
		FileBackupHelper dbs = new FileBackupHelper(this, RecipeData.DB_FILENAME);
		addHelper("dbs", dbs);
	}
	
	@Override
	public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data,
			ParcelFileDescriptor newState) throws IOException {
		synchronized (RecipeData.DB_LOCK) {
			super.onBackup(oldState, data, newState);			
		}
	}
	
	@Override
	public void onRestore(BackupDataInput data, int appVersionCode,
			ParcelFileDescriptor newState) throws IOException {
		synchronized (RecipeData.DB_LOCK) {
			super.onRestore(data, appVersionCode, newState);			
		}
	}
	
	@Override
	public File getFilesDir() {
		return getDatabasePath(RecipeData.DB_FILENAME).getParentFile();
	}
}

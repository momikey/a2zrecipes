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
 * Preferences.java - Activity to allow setting of user preferences
 */

package net.potterpcs.recipebook;

import android.os.Bundle;
import android.preference.PreferenceActivity;

// Because the app is made to work on pre-Honeycomb devices,
// we have to use the "old-style" preferences without fragments.
// However, we are compiling for a fragment-aware SDK, which
// considers the old way of doing things to be deprecated.
// Thus, we'll suppress deprecation warnings on the whole class,
// and, once enough people upgrade, we'll rewrite into something
// more modern.
@SuppressWarnings("deprecation")
public class Preferences extends PreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Load preferences from XML
		addPreferencesFromResource(R.xml.preferences);
	}
}

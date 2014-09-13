/*
 * Copyright 2014 Bassel Bakr
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bassel.flashlightfixer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;
import com.bassel.cmd.Cmd;
import java.io.StringReader;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

public class MainActivity extends PreferenceActivity implements Preference.OnPreferenceClickListener
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO: Implement this method
		super.onCreate(savedInstanceState);
		setPreferenceScreen(getPreferenceManager().createPreferenceScreen(this));
		Preference mContact = new Preference(this)
		{{
				setTitle("Contact me");
				setSummary("For help and support for other devices");
				setKey("key_contact");
				setOnPreferenceClickListener(MainActivity.this);
			}};

		Preference mUituner = new Preference(this)
		{{
				setTitle("UI Tuner");
				setSummary("Check my other app on play store");
				setKey("key_uituner");
				setOnPreferenceClickListener(MainActivity.this);
			}};

		Preference mDonate = new Preference(this)
		{{
				setTitle("Donate");
				setSummary("Donate if you would like to support my work");
				setKey("key_donate");
				setOnPreferenceClickListener(MainActivity.this);
			}};

		getPreferenceScreen().addPreference(mContact);
		getPreferenceScreen().addPreference(mUituner);
		getPreferenceScreen().addPreference(mDonate);
	}

	@Override
	public boolean onPreferenceClick(Preference p1)
	{
		// TODO: Implement this method
		Intent mIntent = new Intent();
		switch (p1.getKey())
		{
			case "key_contact":
				mIntent.setAction(Intent.ACTION_VIEW)
					.setData(Uri.parse("mailto:basselbakr@gmail.com"))
					.putExtra(Intent.EXTRA_SUBJECT, "Flashlight Fixer");
				startActivity(mIntent);
				break;

			case "key_uituner":
				mIntent.setAction(Intent.ACTION_VIEW)
					.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.bassel.uituner"));
				startActivity(mIntent);
				break;

			case "key_donate":
				mIntent.setAction(Intent.ACTION_VIEW)
					.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.bassel.uituner.donation"));
				startActivity(mIntent);
				break;
		}
		return true;
	}
}

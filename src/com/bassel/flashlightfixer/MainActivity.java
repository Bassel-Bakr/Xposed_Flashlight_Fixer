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
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.text.InputType;

public class MainActivity extends PreferenceActivity implements Constants, Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO: Implement this method
		super.onCreate(savedInstanceState);
		setPreferenceScreen(getPreferenceManager().createPreferenceScreen(this));

		CheckBoxPreference mHookFlash = new CheckBoxPreference(this)
		{{
				setTitle("Hook flash");
				setSummary("Change default flash toggling method");
				setKey(KEY_HOOK_FLASH);
				setDefaultValue(false);
				setOnPreferenceChangeListener(MainActivity.this);
				getPreferenceScreen().addPreference(this);
			}};

		CheckBoxPreference mHookCameraParams = new CheckBoxPreference(this)
		{{
				setTitle("Hook camera params");
				setSummary("If (Camera + Flash = force close)!");
				setKey(KEY_HOOK_CAMERA_PARAMS);
				setOnPreferenceChangeListener(MainActivity.this);
				getPreferenceScreen().addPreference(this);
			}};

		CheckBoxPreference mHookFlashDevice = new CheckBoxPreference(this)
		{{
				setTitle("Hook flash device");
				setSummary("Change default flash device");
				setKey(KEY_HOOK_FLASH_DEVICE);
				setOnPreferenceChangeListener(MainActivity.this);
				getPreferenceScreen().addPreference(this);
			}};

		EditTextPreference mFlashDevice = new EditTextPreference(this)
		{{
				setTitle("Flash device");
				setSummary("If it worked, report it to me to hardcode it");
				setKey(KEY_FLASH_DEVICE);
				setDialogMessage("Empty to reset");
				setOnPreferenceChangeListener(MainActivity.this);
				getPreferenceScreen().addPreference(this);
			}};
		
		CheckBoxPreference mHookFlashModes = new CheckBoxPreference(this)
		{{
				setTitle("Hook flash modes");
				setSummary("Change default supported flash modes");
				setKey(KEY_HOOK_FLASH_MODES);
				setOnPreferenceChangeListener(MainActivity.this);
				getPreferenceScreen().addPreference(this);
			}};

		EditTextPreference mSupportedFlashModes = new EditTextPreference(this)
		{{
				setTitle("Supported flash modes");
				setSummary("If it worked, report it to me to hardcode it");
				setKey(KEY_SUPPORTED_FLASH_MODES);
				setOnPreferenceChangeListener(MainActivity.this);
				setDialogMessage("Separate each mode with a comma: (on, off, auto, torch, red-eye)\nEmpty to reset");
				getPreferenceScreen().addPreference(this);
			}};

		CheckBoxPreference mHookAutoFocus = new CheckBoxPreference(this)
		{{
				setTitle("Hook auto focus");
				setSummary("Change the delay between flash and auto focus");
				setKey(KEY_HOOK_AUTO_FOCUS);
				setOnPreferenceChangeListener(MainActivity.this);
				getPreferenceScreen().addPreference(this);
			}};

		EditTextPreference mAutoFocusDelay = new EditTextPreference(this)
		{{
				setTitle("Auto focus delay");
				setSummary("Delay between auto flash and auto focus");
				setKey(KEY_AUTO_FOCUS_DELAY);
				setDialogMessage("Milliseconds, empty to reset");
				getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
				setOnPreferenceChangeListener(MainActivity.this);
				getPreferenceScreen().addPreference(this);
			}};

		CheckBoxPreference mHookInfiniteFocus = new CheckBoxPreference(this)
		{{
				setTitle("Hook infinite focus");
				setSummary("Change the delay between flash and infinite focus");
				setKey(KEY_HOOK_INFINITE_FOCUS);
				setOnPreferenceChangeListener(MainActivity.this);
				getPreferenceScreen().addPreference(this);
			}};

		EditTextPreference mInfiniteFocusDelay = new EditTextPreference(this)
		{{
				setTitle("Infinite focus delay");
				setSummary("Delay between auto flash and infinite or no focus");
				setKey(KEY_INFINITE_FOCUS_DELAY);
				setDialogMessage("Milliseconds, empty to reset");
				getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
				setOnPreferenceChangeListener(MainActivity.this);
				getPreferenceScreen().addPreference(this);
			}};

		Preference mContact = new Preference(this)
		{{
				setTitle("Contact me");
				setSummary("For help and support for other devices");
				setKey("key_contact");
				setOnPreferenceClickListener(MainActivity.this);
				getPreferenceScreen().addPreference(this);
			}};

		Preference mUituner = new Preference(this)
		{{
				setTitle("UI Tuner");
				setSummary("Check my other app on play store");
				setKey("key_uituner");
				setOnPreferenceClickListener(MainActivity.this);
				getPreferenceScreen().addPreference(this);
			}};

		Preference mDonate = new Preference(this)
		{{
				setTitle("Donate");
				setSummary("Donate if you would like to support my work");
				setKey("key_donate");
				setOnPreferenceClickListener(MainActivity.this);
				getPreferenceScreen().addPreference(this);
			}};

		mHookFlashDevice.setDependency(KEY_HOOK_FLASH);
		mFlashDevice.setDependency(KEY_HOOK_FLASH_DEVICE);

		//mHookFlashModes.setDependency(KEY_HOOK_FLASH);
		mSupportedFlashModes.setDependency(KEY_HOOK_FLASH_MODES);

		mAutoFocusDelay.setDependency(KEY_HOOK_AUTO_FOCUS);
		mInfiniteFocusDelay.setDependency(KEY_HOOK_INFINITE_FOCUS);
	}

	@Override
	public boolean onPreferenceChange(Preference p1, Object p2)
	{
		// TODO: Implement this method
		SharedPreferences mPrefs = getSharedPreferences("prefs", MODE_WORLD_READABLE);
		if (p2 instanceof String) mPrefs.edit().putString(p1.getKey(), (String)p2).commit();
		else if (p2 instanceof Boolean || p2 instanceof Boolean) mPrefs.edit().putBoolean(p1.getKey(), (boolean)p2).commit();
		return true;
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

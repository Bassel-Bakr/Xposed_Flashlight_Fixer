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

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.text.InputType;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TableRow.LayoutParams;
import com.bassel.cmd.Cmd;

public class MainActivity extends PreferenceActivity implements Constants, Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener
{
	private SharedPreferences mPrefs;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO: Implement this method
		super.onCreate(savedInstanceState);
		//setTheme(android.R.style.Theme.Holo);
		
		setPreferenceScreen(getPreferenceManager().createPreferenceScreen(this));

		mPrefs = getSharedPreferences("prefs", MODE_WORLD_READABLE);
		final PreferenceCategory mGeneral = new PreferenceCategory(this)
		{{
				setTitle("General");
				setKey("key_general");
				getPreferenceScreen().addPreference(this);
			}};

		final PreferenceCategory mInfo = new PreferenceCategory(this)
		{{			
				setTitle("Info");
				setKey("key_info");
				getPreferenceScreen().addPreference(this);
			}};

		CheckBoxPreference mHookFlash = new CheckBoxPreference(this)
		{{
				setTitle("Use flashlight sysfs path");
				setSummary("Use flashlight sysfs path instead of camera interface");
				setKey(KEY_HOOK_FLASH);
				setDefaultValue(false);
				setOnPreferenceChangeListener(MainActivity.this);
				mGeneral.addPreference(this);
			}};

		CheckBoxPreference mHookCameraParams = new CheckBoxPreference(this)
		{{
				setTitle("Hook camera params");
				setSummary("If (Camera + Flash = crash!)");
				setKey(KEY_HOOK_CAMERA_PARAMS);
				setOnPreferenceChangeListener(MainActivity.this);
				mGeneral.addPreference(this);
			}};

		CheckBoxPreference mHookFlashDevice = new CheckBoxPreference(this)
		{{
				setTitle("Hook flash device");
				setSummary("Change default flash device / sysfs path");
				setKey(KEY_HOOK_FLASH_DEVICE);
				setOnPreferenceChangeListener(MainActivity.this);
				mGeneral.addPreference(this);
			}};

		EditTextPreference mFlashDevice = new EditTextPreference(this)
		{{
				setTitle("Flash device");
				setSummary("If it worked, report it to me to hardcode it");
				setKey(KEY_FLASH_DEVICE);
				setDialogMessage("Empty to reset");
				setOnPreferenceChangeListener(MainActivity.this);
				mGeneral.addPreference(this);
			}};

		CheckBoxPreference mHookFlashModes = new CheckBoxPreference(this)
		{{
				setTitle("Hook flash modes");
				setSummary("Change default supported flash modes");
				setKey(KEY_HOOK_FLASH_MODES);
				setOnPreferenceChangeListener(MainActivity.this);
				mGeneral.addPreference(this);
			}};

		EditTextPreference mSupportedFlashModes = new EditTextPreference(this)
		{{
				setTitle("Supported flash modes");
				setSummary("If it worked, report it to me to hardcode it");
				setKey(KEY_SUPPORTED_FLASH_MODES);
				setOnPreferenceChangeListener(MainActivity.this);
				setDialogMessage("Separate each mode with a comma: (on, off, auto, torch, red-eye)");
				mGeneral.addPreference(this);
			}};

		CheckBoxPreference mHookAutoFocus = new CheckBoxPreference(this)
		{{
				setTitle("Hook auto focus");
				setSummary("Change the delay between flash and auto focus");
				setKey(KEY_HOOK_AUTO_FOCUS);
				setOnPreferenceChangeListener(MainActivity.this);
				mGeneral.addPreference(this);
			}};

		EditTextPreference mAutoFocusDelay = new EditTextPreference(this)
		{{
				setTitle("Auto focus delay");
				setSummary("Delay between auto flash and auto focus");
				setKey(KEY_AUTO_FOCUS_DELAY);
				setDialogMessage("In milliseconds (1 second = 1000 milliseconds):");
				getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
				setOnPreferenceChangeListener(MainActivity.this);
				mGeneral.addPreference(this);
			}};

		CheckBoxPreference mHookInfiniteFocus = new CheckBoxPreference(this)
		{{
				setTitle("Hook infinite focus");
				setSummary("Change the delay between flash and infinite focus");
				setKey(KEY_HOOK_INFINITE_FOCUS);
				setOnPreferenceChangeListener(MainActivity.this);
				mGeneral.addPreference(this);
			}};

		EditTextPreference mInfiniteFocusDelay = new EditTextPreference(this)
		{{
				setTitle("Infinite focus delay");
				setSummary("Delay between auto flash and infinite or no focus");
				setKey(KEY_INFINITE_FOCUS_DELAY);
				setDialogMessage("In milliseconds (1 second = 1000 milliseconds):");
				getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
				setOnPreferenceChangeListener(MainActivity.this);
				mGeneral.addPreference(this);
			}};

		CheckBoxPreference mHookCameraPreviewStop = new CheckBoxPreference(this)
		{{
				setTitle("Hook camera stopPreview");
				setSummary("Turn flash off when camera preview stops");
				setKey(KEY_HOOK_CAMERA_STOP_PREVIEW);
				setOnPreferenceChangeListener(MainActivity.this);
				mGeneral.addPreference(this);
			}};

		CheckBoxPreference mHookCameraRelease = new CheckBoxPreference(this)
		{{
				setTitle("Hook camera release");
				setSummary("Turn flash off when camera is released");
				setKey(KEY_HOOK_CAMERA_RELEASE);
				setOnPreferenceChangeListener(MainActivity.this);
				mGeneral.addPreference(this);
			}};

		Preference mContact = new Preference(this)
		{{
				setTitle("Contact me");
				setSummary("For help and support for other devices");
				setKey("key_contact");
				setOnPreferenceClickListener(MainActivity.this);
				mInfo.addPreference(this);
			}};

		Preference mUituner = new Preference(this)
		{{
				setTitle("UI Tuner");
				setSummary("Check my other app on play store");
				setKey("key_uituner");
				setOnPreferenceClickListener(MainActivity.this);
				mInfo.addPreference(this);
			}};

		Preference mDonate = new Preference(this)
		{{
				setTitle("Donate");
				setSummary("Donate if you would like to support my work");
				setKey("key_donate");
				setOnPreferenceClickListener(MainActivity.this);
				mInfo.addPreference(this);
			}};

		//getPreferenceScreen().addPreference(mGeneral);
		//getPreferenceScreen().addPreference(mInfo);

		mHookFlashDevice.setDependency(KEY_HOOK_FLASH);
		mFlashDevice.setDependency(KEY_HOOK_FLASH_DEVICE);

		mSupportedFlashModes.setDependency(KEY_HOOK_FLASH_MODES);

		mAutoFocusDelay.setDependency(KEY_HOOK_AUTO_FOCUS);
		//mHookInfiniteFocus.setDependency(KEY_HOOK_FLASH);
		mInfiniteFocusDelay.setDependency(KEY_HOOK_INFINITE_FOCUS);
	}

	@Override
	public boolean onPreferenceChange(Preference p1, Object p2)
	{
		// TODO: Implement this method
		if (p2 instanceof String) mPrefs.edit().putString(p1.getKey(), (String)p2).commit();
		else if (p2 instanceof Boolean) mPrefs.edit().putBoolean(p1.getKey(), (boolean)p2).commit();
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
					.putExtra(Intent.EXTRA_SUBJECT, "Flashlight Fixer [board name = " + Cmd.SH.ex("getprop ro.product.board").getString() + "]")
					.putExtra(Intent.EXTRA_TEXT, "Flashlight sysfs path = " + Cmd.SH.ex("busybox find /sys -group camera 2> /dev/null").getString() + "\n\n");
				try
				{startActivity(mIntent);}
				catch (Exception e)
				{
					EditText mEt = new EditText(this);
					mEt.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
					mEt.setText(
						"Board name = " + Cmd.SH.ex("getprop ro.product.board").getString() +
						"\nFlashlight sysfs path = " + Cmd.SH.ex("busybox find /sys -group camera 2> /dev/null").getString()
					);
					new AlertDialog.Builder(this)
						.setTitle("May I have some data")
						.setView(mEt)
						.show();
				}
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

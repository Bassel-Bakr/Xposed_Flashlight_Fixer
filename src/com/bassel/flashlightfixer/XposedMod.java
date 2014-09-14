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

import android.content.pm.PackageManager;
import android.hardware.Camera;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class XposedMod implements IXposedHookZygoteInit
{

	@Override
	public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) throws Throwable
	{

		// Called when an app checks for flashlight availability
		XposedHelpers.findAndHookMethod("android.app.ApplicationPackageManager", null, "hasSystemFeature", String.class, new XC_MethodReplacement()
			{
				@Override
				protected Object replaceHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable
				{
					// Is it flashlight that we're checking?
					if (((String) param.args[0]).equals(PackageManager.FEATURE_CAMERA_FLASH)) return true;
					// No flashlight! then let's not temper with the natural flow
					return XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args);
				}
			}
		);

		// Called when an app checks for available flash modes
		XposedHelpers.findAndHookMethod("android.hardware.Camera$Parameters", null, "getSupportedFlashModes", new XC_MethodReplacement()
			{
				@Override
				protected Object replaceHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable
				{
					// Return our supported flash modes
					return Flash.getSupportedFlashModes();
				}
			}
		);

		// Called when default camera parameters changes
		XposedHelpers.findAndHookMethod("android.hardware.Camera$Parameters", null, "set", String.class, String.class, new XC_MethodHook()
			{
				@Override
				protected void afterHookedMethod(MethodHookParam param)
				{
					if (((String)param.args[0]).equals("flash-mode")) Flash.setFlashMode((String) param.args[1]);
				}
			}
		);

		// Called when auto-focus starts
		XposedHelpers.findAndHookMethod("android.hardware.Camera", null, "autoFocus", Camera.AutoFocusCallback.class , new XC_MethodHook()
			{
				@Override
				protected void beforeHookedMethod(MethodHookParam param)
				{
					// Turn flash on if we're using auto-flash mode
					if (Flash.isAuto()) Flash.on();
					else return;
					if (Flash.getPrefs().contains(Flash.KEY_AUTO_FOCUS_DELAY))
						try
						{
							Thread.sleep(Integer.valueOf(Flash.getPrefs().getString(Flash.KEY_AUTO_FOCUS_DELAY, "2000")));
						}
						catch (InterruptedException e)
						{e.printStackTrace();}
				}
			}
		);

		// Called when auto-focus is canceled
		XposedHelpers.findAndHookMethod("android.hardware.Camera", null, "cancelAutoFocus", new XC_MethodHook()
			{
				@Override
				protected void afterHookedMethod(MethodHookParam param)
				{
					// Turn flash off if we're using auto-flash mode
					if (Flash.isAuto()) Flash.off();
				}
			}
		);

		// Called when a camera app attempts to take a picture
		XposedHelpers.findAndHookMethod("android.hardware.Camera", null, "takePicture", Camera.ShutterCallback.class, Camera.PictureCallback.class, Camera.PictureCallback.class, Camera.PictureCallback.class, new XC_MethodHook()
			{
				@Override
				protected void beforeHookedMethod(MethodHookParam param)
				{
					// If auto-focus is off, turn flash on and let the camera adapt to flashlight for 2.5 seconds
					if (Flash.isAuto() && !Flash.isOn())
					{
						Flash.on();
						try
						{
							Thread.sleep(!Flash.getPrefs().contains(Flash.KEY_INFINITE_FOCUS_DELAY) ? 2500 : Integer.valueOf(Flash.getPrefs().getString(Flash.KEY_INFINITE_FOCUS_DELAY, "2500")));
						}
						catch (InterruptedException e)
						{e.printStackTrace();}
					}
				}
			}
		);

		// Called when camera preview starts and when a picture is taken an saved
		XposedHelpers.findAndHookMethod("android.hardware.Camera", null, "startPreview", new XC_MethodHook()
			{
				@Override
				protected void beforeHookedMethod(MethodHookParam param)
				{
					// Save your battery power for later use
					if (Flash.isAuto()) Flash.off();
				}					
			}
		);

		// Called when camera preview stops
		XposedHelpers.findAndHookMethod("android.hardware.Camera", null, "stopPreview", new XC_MethodHook()
			{
				@Override
				protected void beforeHookedMethod(MethodHookParam param)
				{
					// We are not doing anything, save your battery power for later use
					Flash.off();
				}					
			}
		);

		// Called when camera is no longer in use
		XposedHelpers.findAndHookMethod("android.hardware.Camera", null, "release", new XC_MethodHook()
			{
				@Override
				protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param)
				{
					// Obviously our camera or flashlight app isn't active, save your battery power for later use
					Flash.off();
				}
			}
		);
	}
}

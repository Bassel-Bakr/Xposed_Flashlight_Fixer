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
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import java.lang.reflect.InvocationTargetException;

public class XposedMod implements Constants, IXposedHookLoadPackage
{

	@Override
	public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable
	{
		// Reload our preferences
		// Flash.getPrefs().reload();

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
					if (Flash.getPrefs().getBoolean(KEY_HOOK_FLASH_MODES, false)) return Flash.getSupportedFlashModes();
					// If we don't have custom flash modes, let's not temper with the natural flow
					return XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args);	
				}
			}
		);

		// Called when an app attempts to change camera parameters
		XposedHelpers.findAndHookMethod("android.hardware.Camera", null, "setParameters", Camera.Parameters.class, new XC_MethodReplacement()
			{
				@Override
				protected Object replaceHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable
				{
					// Remove flash-mode if it crash our device
					if (Flash.getPrefs().getBoolean(KEY_HOOK_CAMERA_PARAMS, false))
						((Camera.Parameters) param.args[0]).remove("flash-mode");
					// No problems! Then let's not temper with the natural flow
					return XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args);	
				}
			}
		);

		// Called when default camera parameters changes
		XposedHelpers.findAndHookMethod("android.hardware.Camera$Parameters", null, "set", String.class, String.class, new XC_MethodReplacement()
			{
				@Override
				protected Object replaceHookedMethod(MethodHookParam param) throws IllegalAccessException, IllegalArgumentException, NullPointerException, InvocationTargetException
				{
					if (((String)param.args[0]).equals("flash-mode"))
						if (Flash.getPrefs().getBoolean(KEY_HOOK_FLASH, false))
							Flash.setFlashMode((String) param.args[1]);
						else Flash.changeFlashMode((String) param.args[1]);
					return XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args);
				}
			}
		);

		// Called when auto-focus starts
		XposedHelpers.findAndHookMethod("android.hardware.Camera", null, "autoFocus", Camera.AutoFocusCallback.class , new XC_MethodReplacement()
			{
				@Override
				protected Object replaceHookedMethod(MethodHookParam param) throws IllegalAccessException, IllegalArgumentException, NullPointerException, InvocationTargetException
				{
					// Turn flash on if we're using auto-flash mode
					if (Flash.getPrefs().getBoolean(KEY_HOOK_AUTO_FOCUS, false))
					{
						if (Flash.isAuto())
							try
							{
								if (!Flash.isOn()) Flash.on((Camera)param.thisObject);
								
								Thread.sleep(Integer.valueOf(Flash.getPrefs().getString(KEY_AUTO_FOCUS_DELAY, "0")));
							}
							catch (InterruptedException e)
							{e.printStackTrace();}
					}
					return XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args);
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
					if (Flash.getPrefs().getBoolean(KEY_HOOK_FLASH, false) && Flash.isAuto() && Flash.isOn())
						Flash.off((Camera)param.thisObject);
				}
			}
		);

		// Called when a camera app attempts to take a picture
		XposedHelpers.findAndHookMethod("android.hardware.Camera", null, "takePicture", Camera.ShutterCallback.class, Camera.PictureCallback.class, Camera.PictureCallback.class, Camera.PictureCallback.class, new XC_MethodReplacement()
			{
				@Override
				protected Object replaceHookedMethod(MethodHookParam param) throws IllegalAccessException, IllegalArgumentException, NullPointerException, InvocationTargetException
				{
					// If auto-focus is off, turn flash on and let the camera adapt to flashlight for 2.5 seconds
					if (Flash.getPrefs().getBoolean(KEY_HOOK_INFINITE_FOCUS, false))
					{
						if (Flash.isAuto())
							try
							{
								if (!Flash.isOn()) Flash.on((Camera)param.thisObject);

								Thread.sleep(Integer.valueOf(Flash.getPrefs().getString(Flash.KEY_INFINITE_FOCUS_DELAY, "0")));
							}
							catch (InterruptedException e)
							{e.printStackTrace();}
					}
					return XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args);
				}
			}
		);

		// Called when camera preview starts and when a picture is taken and saved
		XposedHelpers.findAndHookMethod("android.hardware.Camera", null, "startPreview", new XC_MethodReplacement()
			{
				@Override
				protected Object replaceHookedMethod(MethodHookParam param) throws IllegalAccessException, IllegalArgumentException, NullPointerException, InvocationTargetException
				{
					// Save your battery power for later use
					if (Flash.getPrefs().getBoolean(KEY_HOOK_FLASH, false) && Flash.isAuto() && Flash.isOn())
						Flash.off((Camera)param.thisObject);
					return XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args);
				}					
			}
		);

		// Called when camera preview stops
		XposedHelpers.findAndHookMethod("android.hardware.Camera", null, "stopPreview", new XC_MethodReplacement()
			{
				@Override
				protected Object replaceHookedMethod(MethodHookParam param) throws IllegalAccessException, IllegalArgumentException, NullPointerException, InvocationTargetException
				{
					// We are doing nothing, let's save our battery power for later use
					if (Flash.getPrefs().getBoolean(KEY_HOOK_CAMERA_STOP_PREVIEW, false) && Flash.isAuto() && Flash.isOn())
						Flash.off((Camera)param.thisObject);
					return XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args);
				}					
			}
		);

		// Called when camera is no longer in use
		XposedHelpers.findAndHookMethod("android.hardware.Camera", null, "release", new XC_MethodReplacement()
			{
				@Override
				protected Object replaceHookedMethod(XC_MethodHook.MethodHookParam param) throws IllegalAccessException, IllegalArgumentException, NullPointerException, InvocationTargetException
				{
					// Obviously our camera or flashlight app isn't active, save your battery power for later use
					if (Flash.getPrefs().getBoolean(KEY_HOOK_CAMERA_RELEASE, false))
						Flash.off((Camera)param.thisObject);
					return XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args);
				}
			}
		);
	}
}

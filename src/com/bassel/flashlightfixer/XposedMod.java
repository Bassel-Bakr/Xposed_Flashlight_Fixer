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

import android.hardware.Camera;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import java.util.ArrayList;
import java.util.List;

public class XposedMod implements IXposedHookZygoteInit
{

	@Override
	public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) throws Throwable
	{
		XposedHelpers.findAndHookMethod("android.app.ApplicationPackageManager", null, "hasSystemFeature", String.class, new XC_MethodReplacement()
			{
				@Override
				protected Object replaceHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable
				{
					// TODO: Implement this method
					String mFeature = (String) param.args[0];
					if (mFeature == "android.hardware.camera.flash" || mFeature.equals("android.hardware.camera.flash"))
						return true;
					return XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args);
				}
			});

		XposedHelpers.findAndHookMethod("android.hardware.Camera$Parameters", null, "getSupportedFlashModes", new XC_MethodReplacement()
			{
				@Override
				protected Object replaceHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable
				{
					// TODO: Implement this method
					return Flash.getFlashModes();
				}
			}
		);

		XposedHelpers.findAndHookMethod("android.hardware.Camera$Parameters", null, "set", String.class, String.class, new XC_MethodHook()
			{
				@Override
				protected void afterHookedMethod(MethodHookParam param)
				{
					if (((String)param.args[0]).contains("flash-mode"))Flash.setFlashMode((String) param.args[1]);
				}
			}
		);

		XposedHelpers.findAndHookMethod("android.hardware.Camera", null, "autoFocus", Camera.AutoFocusCallback.class , new XC_MethodHook()
			{
				@Override
				protected void beforeHookedMethod(MethodHookParam param)
				{
					if (Flash.isAuto() && !Flash.isOn())
					{
						Flash.on();
					}
				}
			}
		);

		XposedHelpers.findAndHookMethod("android.hardware.Camera", null, "cancelAutoFocus", new XC_MethodHook()
			{
				@Override
				protected void afterHookedMethod(MethodHookParam param)
				{
					if (Flash.isAuto())
					{
						Flash.off();
					}
				}
			}
		);

		XposedHelpers.findAndHookMethod("android.hardware.Camera", null, "takePicture", Camera.ShutterCallback.class, Camera.PictureCallback.class, Camera.PictureCallback.class, Camera.PictureCallback.class, new XC_MethodHook()
			{
				@Override
				protected void beforeHookedMethod(MethodHookParam param)
				{
					if (Flash.isAuto() && !Flash.isOn())
					{
						Flash.on();
						try
						{
							Thread.sleep(2500);
						}
						catch (InterruptedException e)
						{e.printStackTrace();}
					}
				}
			}
		);

		XposedHelpers.findAndHookMethod("android.hardware.Camera", null, "startPreview", new XC_MethodHook()
			{
				@Override
				protected void afterHookedMethod(MethodHookParam param)
				{
					if (Flash.isAuto() && Flash.isOn()) Flash.off();
				}					
			}
		);

		XposedHelpers.findAndHookMethod("android.hardware.Camera", null, "stopPreview", new XC_MethodHook()
			{
				@Override
				protected void beforeHookedMethod(MethodHookParam param)
				{
					Flash.off();
				}					
			}
		);

		XposedHelpers.findAndHookMethod("android.hardware.Camera", null, "release", new XC_MethodHook()
			{
				@Override
				protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param)
				{
					Flash.off();
				}
			}
		);
	}
}

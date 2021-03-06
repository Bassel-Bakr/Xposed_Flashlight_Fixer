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
import com.bassel.cmd.Cmd;
import com.bassel.flashlightfixer.Flash;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class Flash implements Constants
{
	private static Camera sCam;
	private static XSharedPreferences sPrefs;
	private static String sFlashDevice, sStandardFlashDevice, sFlashMode, sBoardName;
	private static List<String> sFlashModes, sStandardFlashModes;
	private static FileWriter sFileWriter;
	private static boolean sOn;

	static
	{
		sCam = null;
		if  (BuildConfig.DEBUG) XposedBridge.log("Static initialization");
		// Get shared preferences
		sPrefs = new XSharedPreferences(Flash.class.getPackage().getName(), "prefs");
		sPrefs.makeWorldReadable();

		// Check board name to use the right flash device
		sBoardName = getBoardName();
		sStandardFlashDevice = getStandardFlashDevice();
		sStandardFlashModes = getStandardFlashModes();

		if (getPrefs().getBoolean(KEY_HOOK_FLASH_DEVICE, true) && getPrefs().getString(KEY_FLASH_DEVICE, null) != null)
			setFlashDevice(sPrefs.getString(KEY_FLASH_DEVICE, "/none"));
		else setFlashDevice(getStandardFlashDevice());
	}

	public static XSharedPreferences getPrefs()
	{
		sPrefs.reload();
		return sPrefs;
	}

	// Retrieve board name from /system/build.prop file
	public static String getBoardName()
	{
		Properties sProps = new Properties();
		try
		{
			sProps.load(new FileReader(new File("/system/build.prop")));
		}
		catch (IOException e)
		{e.printStackTrace();}
		return sProps.getProperty("ro.product.board");
	}

	// Return standard flash device
	public static String getStandardFlashDevice()
	{
		switch (sBoardName)
		{
				// Samsung GALAXY Ace Plus (GT-S7500)
			case "trebon":
				return "/sys/devices/virtual/camera/rear/rear_flash";

				// Other devices
			default:
				return Cmd.SH.ex("busybox find /sys -group camera 2> /dev/null").getString();
		}
	}

	// Return standard flash modes
	public static List<String> getStandardFlashModes()
	{
		switch (sBoardName)
		{
			case "trebon":
				setFlashModes(new ArrayList<String>()
					{{
							add(Camera.Parameters.FLASH_MODE_ON);
							add(Camera.Parameters.FLASH_MODE_OFF);
							add(Camera.Parameters.FLASH_MODE_AUTO);
							add(Camera.Parameters.FLASH_MODE_TORCH);
						}});
				break;

			default:
				setFlashModes(new ArrayList<String>()
					{{
							add(Camera.Parameters.FLASH_MODE_ON);
							add(Camera.Parameters.FLASH_MODE_OFF);
							//add(Camera.Parameters.FLASH_MODE_AUTO);
							add(Camera.Parameters.FLASH_MODE_TORCH);
						}});
				break;
		}
		return sFlashModes;
	}

	// Set flash device
	public static void setFlashDevice(String sFlashDevice)
	{
		Flash.sFlashDevice = sFlashDevice;
		try
		{
			if (sFileWriter != null) sFileWriter.close();
			sFileWriter = null;
		}
		catch (Exception e)
		{e.printStackTrace();}
		try
		{
			sFileWriter = new FileWriter(sFlashDevice);
		}
		catch (IOException e)
		{
			/*
			 * It's unlikely to get thrown inside this block unless our flash device's group isn't camera (1006)
			 * So we need to change its group to camera (1006)
			 */

			fixGroup();
			try
			{
				sFileWriter = new FileWriter(sFlashDevice);
			}
			catch (IOException ex)
			{ex.printStackTrace();}
			e.printStackTrace();}
	}

	// Set available flash modes
	public static void setFlashModes(List<String> mFlashModes)
	{
		Flash.sFlashModes = mFlashModes;
	}

	/*
	 * This method will be called when a camera or flashlight app tries to get supported flash modes
	 * Since it's not likely for the android.hardware.Camera$Parameters getSupportedFlashModes to return the right modes
	 * we have to do it ourselves!
	 */
	public static List<String> getSupportedFlashModes()
	{
		if (getPrefs().contains(Flash.KEY_SUPPORTED_FLASH_MODES))
			setFlashModes(Arrays.asList(Flash.getPrefs().getString(Flash.KEY_SUPPORTED_FLASH_MODES, "on,off,auto").replace(" ", "").split("[,]")));
		else getStandardFlashModes();
		return sFlashModes;
	}

	// Change flash device group to camera (1006)
	public static void fixGroup()
	{
		Cmd.SU.ex("busybox chown 1000:1006 %s", sFlashDevice);
	}

	// Are we using auto-flash?
	public static boolean isAuto()
	{
		return getFlashMode().contains("auto");
	}

	// Return current flash state
	public static boolean isOn()
	{
		return sOn;
	}

	// Turn flash on ( the same goes to off() )
	static void on(Camera sCamera)
	{
		if (isOn()) return;
		sPrefs.reload();

		if (getPrefs().getBoolean(KEY_HOOK_FLASH, false))
			try
			{
				if (sPrefs.getString(KEY_FLASH_DEVICE, null) != null && !sPrefs.getString(KEY_FLASH_DEVICE, "").equals(sFlashDevice)) setFlashDevice(sPrefs.getString(KEY_FLASH_DEVICE, ""));
				else if (!sStandardFlashDevice.equals(sFlashDevice)) setFlashDevice(sStandardFlashDevice);

				// Write "1" to turn it on
				sFileWriter.write(String.valueOf(1));
				sFileWriter.flush();

				// Let's do this to keep track of flash state
				sOn = true;
			}
			catch (IOException e)
			{fixGroup();
				e.printStackTrace();}
		else if (sCamera != null)
		{
			if (sCam == null) Flash.sCam = sCamera;
			try
			{
				Camera.Parameters sParams = sCam.getParameters();
				sParams.setFlashMode(sParams.FLASH_MODE_ON);
				sCam.setParameters(sParams);
				
				if  (BuildConfig.DEBUG) XposedBridge.log("Camera interface on");

				sOn = true;
			}
			catch (Exception e)
			{e.printStackTrace();}
		}
	}

	static void off(Camera sCamera)
	{
		if (!isOn()) return;
		sPrefs.reload();

		if (getPrefs().getBoolean(KEY_HOOK_FLASH, false))
			try
			{
				if (sPrefs.getString(KEY_FLASH_DEVICE, null) != null && !sPrefs.getString(KEY_FLASH_DEVICE, "").equals(sFlashDevice)) setFlashDevice(sPrefs.getString(KEY_FLASH_DEVICE, ""));
				else if (!sStandardFlashDevice.equals(sFlashDevice)) setFlashDevice(sStandardFlashDevice);

				// Write "0" to turn it on
				sFileWriter.write(String.valueOf(0));
				sFileWriter.flush();

				// Let's do this to keep track of flash state
				sOn = false;
			}
			catch (IOException e)
			{fixGroup();
				e.printStackTrace();}
		else if (sCam != null)
		{
			if (sCam == null) sCam = sCamera;
			try
			{
				Camera.Parameters sParams = sCam.getParameters();
				sParams.setFlashMode(sParams.FLASH_MODE_OFF);
				sCam.setParameters(sParams);

				if  (BuildConfig.DEBUG) XposedBridge.log("Camera interface off");
				
				sOn = false;
			}
			catch (Exception e)
			{e.printStackTrace();}
		}
	}

	// Return current flash mode set by the camera or flashlight app
	static String getFlashMode()
	{
		if (sFlashMode == null) return "off";
		return sFlashMode;
	}
	
	// Keep track of flash mode
	static void changeFlashMode(final String mode)
	{
		sFlashMode = mode;
	}

	// This will be called when an app tries to change flash mode
	static void setFlashMode(final String mode)
	{
		switch (mode)
		{
			case Camera.Parameters.FLASH_MODE_ON:
				on(sCam);
				break;

			case Camera.Parameters.FLASH_MODE_OFF:
				off(sCam);
				break;

			case Camera.Parameters.FLASH_MODE_AUTO:
				off(sCam);
				break;

			case Camera.Parameters.FLASH_MODE_TORCH:
				on(sCam);
				break;
		}

		// Let's do this to keep track of current flash mode set by app
		sFlashMode = mode;
	}
}

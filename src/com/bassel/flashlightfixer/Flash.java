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
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import de.robv.android.xposed.XposedBridge;

public class Flash
{
	static final String
	KEY_FLASH_DEVICE = "key_flash_device",
	KEY_SUPPORTED_FLASH_MODES = "key_supported_flash_modes",
	KEY_AUTO_FOCUS_DELAY = "key_auto_focus_delay",
	KEY_INFINITE_FOCUS_DELAY = "key_infinite_focus_delay";
	private static XSharedPreferences sPrefs;
	private static String sFlashDevice, sFlashMode, sBoardName;
	private static List<String> sFlashModes;
	private static FileWriter sFileWriter;
	private static boolean sOn;

	static
	{
		XposedBridge.log("Static initialization");
		// Get shared preferences
		sPrefs = new XSharedPreferences("com.bassel.flashlightfixer");
		sPrefs.makeWorldReadable();
		// Check board name to use the right flash device
		sBoardName = getBoardName();
		if (sPrefs.contains(KEY_FLASH_DEVICE))
			setFlashDevice(sPrefs.getString(KEY_FLASH_DEVICE, "/none"));
		else switch (sBoardName)
			{
					// Samsung GALAXY Ace Plus (GT-S7500)
				case "trebon":
					setFlashDevice("/sys/devices/virtual/camera/rear/rear_flash");
					break;

					// Other devices
				default:
					setFlashDevice(Cmd.SH.ex("busybox find /sys -group camera 2> /dev/null").getString());
					break;
			}

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

	public static XSharedPreferences getPrefs()
	{
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

	// Set flash device
	public static void setFlashDevice(String sFlashDevice)
	{
		Flash.sFlashDevice = sFlashDevice;
	}

	// Set available flash modes
	public static void setFlashModes(List<String> mFlashModes)
	{
		Flash.sFlashModes = mFlashModes;
	}

	/*
	 * This method will be called when a camera or flashlight app tries to get supported flash modes
	 * Since it's not likely for tge android.hardware.Camera$Parameters getSupportedFlashModes to return the right modes
	 * we have to do it ourselves!
	 */
	public static List<String> getSupportedFlashModes()
	{
		if (Flash.getPrefs().contains(Flash.KEY_SUPPORTED_FLASH_MODES))
			setFlashModes(Arrays.asList(Flash.getPrefs().getString(Flash.KEY_SUPPORTED_FLASH_MODES, "on,off,auto").replace(" ", "").split("[,]")));
		else switch (sBoardName)
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
							}});
					break;
			}
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
	static void on()
	{
		if (isOn()) return;
		try
		{
			// Write "1" to turn it on
			sFileWriter.write(String.valueOf(1));
			sFileWriter.flush();

			// Let's do this to keep track of flash state
			sOn = true;
		}
		catch (IOException e)
		{fixGroup();
			e.printStackTrace();}
	}

	static void off()
	{
		if (!isOn()) return;
		try
		{
			// Write "0" to turn it off
			sFileWriter.write(String.valueOf(0));
			sFileWriter.flush();

			// Let's do this to keep track of flash state
			sOn = false;
		}
		catch (IOException e)
		{fixGroup();
			e.printStackTrace();}
	}

	// Return current flash mode set by the camera or flashlight app
	static String getFlashMode()
	{
		if(sFlashMode == null) return "off";
		return sFlashMode;
	}

	// This will be called when an app tries to change flash mode
	static void setFlashMode(final String mode)
	{
		switch (mode)
		{
			case Camera.Parameters.FLASH_MODE_ON:
				on();
				break;

			case Camera.Parameters.FLASH_MODE_OFF:
				off();
				break;

			case Camera.Parameters.FLASH_MODE_AUTO:
				off();
				break;

			case Camera.Parameters.FLASH_MODE_TORCH:
				on();
				break;
		}

		// Let's do this to keep track of current flash mode set by app
		sFlashMode = mode;
	}
}

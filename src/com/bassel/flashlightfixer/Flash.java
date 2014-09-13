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
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Flash
{
	private static final String sFlashDevice = "/sys/devices/virtual/camera/rear/rear_flash";
	private static List<String> mFlashModes;
	private static FileWriter mFileWriter;
	private static String sFlashMode;
	private static boolean isChecked;
	private static boolean on;

	public static void setFlashModes(List<String> mFlashModes)
	{
		Flash.mFlashModes = mFlashModes;
	}

	public static List<String> getFlashModes()
	{
		if (mFlashModes == null)setFlashModes(new ArrayList<String>()
				{{
						add("on");
						add("off");
						add("auto");
						add("torch");
					}});
		return mFlashModes;
	}

	public static void fixGroup()
	{
		Cmd.SU.ex("busybox chown 1000:1006 " + sFlashDevice);
	}

	public static void setChecked()
	{
		Flash.isChecked = true;
	}

	public static boolean isChecked()
	{
		return isChecked;
	}

	public static boolean isAuto()
	{
		return getFlashMode().contains("auto");
	}

	public static boolean isOn()
	{
		return on;
	}

	static void on()
	{
		try
		{
			if (mFileWriter == null) mFileWriter = new FileWriter(sFlashDevice);
		}
		catch (IOException e)
		{fixGroup();
			try
			{
				mFileWriter = new FileWriter(sFlashDevice);
			}
			catch (IOException ex)
			{ex.printStackTrace();}
			e.printStackTrace();}
		try
		{
			mFileWriter.write(String.valueOf(1));
			mFileWriter.flush();
			on = true;
		}
		catch (IOException e)
		{fixGroup();
			e.printStackTrace();}
	}

	static void off()
	{
		try
		{
			if (mFileWriter == null) mFileWriter = new FileWriter(sFlashDevice);
		}
		catch (IOException e)
		{fixGroup();
			try
			{
				mFileWriter = new FileWriter(sFlashDevice);
			}
			catch (IOException ex)
			{ex.printStackTrace();}
			e.printStackTrace();}
		try
		{
			mFileWriter.write(String.valueOf(0));
			mFileWriter.flush();
			on = false;
		}
		catch (IOException e)
		{fixGroup();
			e.printStackTrace();}
	}

	static String getFlashMode()
	{
		return sFlashMode;
	}

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

		Flash.sFlashMode = mode;
	}
}

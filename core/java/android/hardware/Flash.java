/*
 * Copyright (C) 2013 The CyanogenMod Project
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */

package android.hardware;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.ITorchService;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;

import java.io.FileWriter;
import java.io.IOException;

public class Flash {

    private static final String MSG_TAG = "TorchDevice";

    /* New variables, init'ed by resource items */
    private static int mValueOff=0;
    private static int mValueOn=1;
    private static String mFlashDevice="/sys/devices/virtual/camera/rear/rear_flash";
    private WakeLock mWakeLock;

    private static Flash sInstance;

    private FileWriter mFlashDeviceWriter = null;

    private String mFlashMode = Camera.Parameters.FLASH_MODE_OFF;
    private int state = mValueOff;

    public static class InitializationException extends RuntimeException {
        public InitializationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    private boolean isFlashNeeded(){
		boolean ret=false;
		
		return ret;
	}
	
	public synchronized void setFlashMode(String mode){ 
		Log.d(MSG_TAG, "setShot " + mode);
		if (mFlashMode.equals(mode)) return;
		mFlashMode = mode;
	}

    public synchronized void setShot() {
		int value=0;
        Log.d(MSG_TAG, "setShot " + mFlashMode);

        try {
            String mod=mFlashMode;
            
            if (mFlashDeviceWriter == null) {
				mFlashDeviceWriter = new FileWriter(mFlashDevice);
			}
            
            if(mod.equals(Camera.Parameters.FLASH_MODE_ON)){
               mFlashDeviceWriter.write(mValueOn);
				state=mValueOn;
            }else if(mod.equals(Camera.Parameters.FLASH_MODE_AUTO)){
				if(isFlashNeeded()){ 
					mFlashDeviceWriter.write(mValueOn);
					state=mValueOn;
				}else{
					mFlashDeviceWriter.write(mValueOff);
					state=mValueOff;
				}
            }else if(mod.equals(Camera.Parameters.FLASH_MODE_OFF)){ 
				mFlashDeviceWriter.write(mValueOff);
				state=mValueOff;
			}			
			mFlashDeviceWriter.close();
		} catch (IOException e) {
			throw new InitializationException("Can't open flash device", e);
		}
	}
	
	public synchronized void switchOff(){
		if(state==mValueOn)
			try{
				mFlashDeviceWriter.write(mValueOff);
			} catch (IOException e) {
				// ignore
			}
	}

    public synchronized String getFlashMode() {
        return mFlashMode;
    }
}

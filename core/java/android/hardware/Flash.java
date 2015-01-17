/*
 * Copyright (C) 2015 Dirty Developers
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

import android.hardware.Camera;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import android.util.Log;

public class Flash {

    private static final String MSG_TAG = "TorchDevice";
    
    private FileWriter mFlashDeviceWriter = null;

    private String mFlashMode = null;
    private int mState,mLuxIntensity;

    public static class InitializationException extends RuntimeException {
        public InitializationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    public Flash(){
		try{
			mFlashDeviceWriter = new FileWriter("/sys/class/camera/rear/rear_flash");
		} catch (IOException e) {
			Log.e(MSG_TAG,"Can't open flash sysfs device");
		}
		mState = 0; mLuxIntensity=100;
		mFlashMode = Camera.Parameters.FLASH_MODE_AUTO;
	}
	
	public void setFlashMode(String mode){ 
		if (mFlashMode.equals(mode)) return;
		mFlashMode = mode;
	}
	
	public boolean isNeeded(boolean isFocus){
		
		if(mFlashMode.equals(Camera.Parameters.FLASH_MODE_OFF))
			return false;
		
		if(isFocus){
			
			String lux_val = null;
			
			try {
				BufferedReader luxReader = new BufferedReader(new FileReader("/sys/kernel/lux/ext_lux_val"));
				lux_val = luxReader.readLine();
				luxReader.close();
			} catch (IOException ioE) {
				Log.e(MSG_TAG,"Can't read ambient lux value");
			}
			
			if(lux_val==null) return false;
			
			mLuxIntensity = Integer.parseInt(lux_val);
		
		}
		
		if(mFlashMode.equals(Camera.Parameters.FLASH_MODE_AUTO) && mLuxIntensity>=50)
			return false;
		
		return true;
	}

    public void on() {
		try {            
            mFlashDeviceWriter.write(Integer.toString(7));
			mState=1;
			mFlashDeviceWriter.flush();
			Log.d(MSG_TAG,"Flash switched on");
		} catch (IOException e) {
			Log.e(MSG_TAG,"Can't write on flash sysfs device");
		}
	}
	
	public void off(){		
		try{
			mFlashDeviceWriter.write(Integer.toString(0));
			mState=0;
			mFlashDeviceWriter.flush();
			Log.d(MSG_TAG,"Flash switched off");	
		} catch (IOException e) {
			Log.e(MSG_TAG,"Can't write on flash sysfs device");
		}
	}
	
	public synchronized void off(boolean isFocus){		
		try{
			if(isFocus) Thread.sleep(5000);
			else { 
				mLuxIntensity=100; 
				Thread.sleep(1000);
			}
		} catch (InterruptedException ie){
			Log.e(MSG_TAG,"Can't wait finish of tasks");
		}
		off();
	}

    public String getFlashMode() {
        return mFlashMode;
    }
    
    public boolean isOn(){
		return (mState==1);
	}
    
    public void close(){
		if(mFlashDeviceWriter!=null){
			try{
				mFlashDeviceWriter.close();
			} catch (IOException e) { 
				Log.e(MSG_TAG,"Can't close flash sysfs device");
			}
		}
	} 
		
}

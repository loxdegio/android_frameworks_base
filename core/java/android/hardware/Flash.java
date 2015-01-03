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
import java.io.FileWriter;
import java.io.IOException;

public class Flash {

    private static final String MSG_TAG = "TorchDevice";

    /* New variables, init'ed by resource items */
    private static int mValueOff=0;
    private static int mValueOn=1;
    private static String mFlashDevice="/sys/devices/virtual/camera/rear/rear_flash";

    private FileWriter mFlashDeviceWriter = null;

    private String mFlashMode = null;
    private int mState;
    private int mCameraId;

    public static class InitializationException extends RuntimeException {
        public InitializationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    protected Flash(int camId){
		try{
			mFlashDeviceWriter = new FileWriter(mFlashDevice);
		} catch (IOException e) {
			throw new InitializationException("Can't open flash device", e);
		}
		mState = mValueOff;
		mCameraId=camId;
	}
	
	private boolean exposureCompensationHigh(){
		boolean ret=false;
		Camera camera=new Camera(mCameraId);
		int compensationZero = (camera.getMaxExposureCompensation()+camera.getMinExposureCompensation())/2;
		if(camera.getExposureCompensation()>=(compensationZero+(2*getExposureCompensationStep()))
			ret = true;
		camera.close();
		return ret;
	}
    
    protected boolean isNeeded(){
		boolean ret=false;
		
		if (mFlashMode.equals(Camera.Parameters.FLASH_MODE_AUTO)) ret=true;
		else if (mFlashMode.equals(Camera.Parameters.FLASH_MODE_AUTO) &&
					exposureCompensationHigh())
			ret=true;
		else ret=false;
		
		return ret;
	}
	
	protected void setFlashMode(String mode){ 
		if (mFlashMode.equals(mode)) return;
		Log.d(MSG_TAG, "setFlashMode " + mode);
		mFlashMode = mode;
	}

    protected void on() {
        try {
			if(mState==mValueOn) return;            
            mFlashDeviceWriter.write(String.valueOf(mValueOn));
			mState=mValueOn;
		} catch (IOException e) {
			throw new InitializationException("Can't open flash device", e);
		}
	}
	
	protected void off(){
		if(mState==mValueOn)
			try{
				if (mFlashDeviceWriter == null) {
					mFlashDeviceWriter = new FileWriter(mFlashDevice);
				}
				mFlashDeviceWriter.write(String.valueOf(mValueOff));
			} catch (IOException e) {
				// ignore
			}
	}

    protected String getFlashMode() {
        return mFlashMode;
    }
    
    protected boolean isON(){
		return mState==mValueOn? true : false;
	}
    
    protected void close(){
		try{
			mFlashDeviceWriter.close();
		}
	}
}

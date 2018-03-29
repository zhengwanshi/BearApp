package com.example.bearapp.hostlive;

import com.tencent.ilivesdk.ILiveConstants;

/**
 * Created by zhengyg on 2018/3/24.
 */

public class HostControlState {
    private boolean isBeautyOn = false;
    private boolean isFlashOn = false;
    private boolean isVoiceOn = true;
    private int cameraid = ILiveConstants.FRONT_CAMERA;
    public boolean isBeautyOn() {
        return isBeautyOn;
    }

    public void setBeautyOn(boolean beautyOn) {
        isBeautyOn = beautyOn;
    }

    public boolean isFlashOn() {
        return isFlashOn;
    }

    public void setFlashOn(boolean flashOn) {
        isFlashOn = flashOn;
    }

    public boolean isVoiceOn() {
        return isVoiceOn;
    }

    public void setVoiceOn(boolean voiceOn) {
        isVoiceOn = voiceOn;
    }

    public int getCameraid() {
        return cameraid;
    }

    public void setCameraid(int cameraid) {
        this.cameraid = cameraid;
    }
}

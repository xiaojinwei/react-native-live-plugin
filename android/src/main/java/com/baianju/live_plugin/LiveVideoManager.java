package com.baianju.live_plugin;

import com.baianju.live_plugin.util.Const;
import com.baianju.live_plugin.widget.LiveVideoContainer;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;

public
class LiveVideoManager extends SimpleViewManager<LiveVideoContainer> {
    @Override
    public String getName() {
        return "LiveVideoView";
    }

    @Override
    protected LiveVideoContainer createViewInstance(ThemedReactContext reactContext) {
        return new LiveVideoContainer(reactContext);
    }

    @ReactProp(name = "play")
    public void play(LiveVideoContainer view, ReadableMap map) {
        String deviceSerial = map.getString(Const.KEY_DEVICE_SERIAL);
        int channelNo = map.getInt(Const.KEY_DEVICE_CHANNEL_NO);
        String verifyCode = map.getString(Const.KEY_VERIFY_CODE);
        view.initData(deviceSerial,channelNo,verifyCode);
        System.out.println("------------------------1");
    }

}

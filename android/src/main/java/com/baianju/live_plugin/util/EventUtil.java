package com.baianju.live_plugin.util;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import javax.annotation.Nullable;

public
class EventUtil {

    public static final String EVENT_NAME_NATIVE_TO_JS = "LiveNativeToJs";

    public static final int WHAT_CLOSE = 0; //页面关闭事件
    public static final int WHAT_UNBIND = 1;//解除绑定事件
    public static final int WHAT_FEEDBACK = 2;//投诉建议事件
    public static final int WHAT_CONTRACTS = 3;//联系人事件
    public static final int WHAT_SHARE = 4;//分享事件

    private EventUtil(){}
    private static EventUtil instance = new EventUtil();
    public static EventUtil getInstance(){
        return instance;
    }

    private ReactApplicationContext reactContext;

    public void setReactContext(ReactApplicationContext reactContext){
        this.reactContext = reactContext;
    }

    public void emit(String eventName, @Nullable Object data){
        if(reactContext != null){
            reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName,data);
            System.out.println("--------------------emit : " + data);
        }
    }
}

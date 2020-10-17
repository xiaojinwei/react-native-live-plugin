package com.baianju.live_plugin;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.baianju.live_plugin.util.Const;
import com.baianju.live_plugin.util.EventUtil;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReadableMap;
import com.hikvision.cloud.sdk.CloudOpenSDK;
import com.hikvision.cloud.sdk.core.OnCommonCallBack;

public class LivePluginModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;

    public LivePluginModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        EventUtil.getInstance().setReactContext(reactContext);
    }

    @Override
    public String getName() {
        return "LivePlugin";
    }

    @ReactMethod
    public void init(String authToken, final Callback callback){
        CloudOpenSDK.getInstance()
                .setLogDebugMode(true) // 默认日志开关状态：打开
                //sdk数据缓存加密开关（例如SP存储），放在init()方法前设置
                //isEncrypt,true:开启加密,false:不加密
                .setDataCacheEncrypt(true, "123456")//密码长度不限制
                .init(
                        (Application) reactContext.getApplicationContext(),
                        authToken,
                        new OnCommonCallBack() {
                            @Override
                            public void onSuccess() {
                                callback.invoke(1);
                                Log.d("BNQ", "CloudOpenSDK SDK初始化成功");
                            }

                            @Override
                            public void onFailed(Exception e) {
                                callback.invoke(0);
                                Log.d("BNQ", "CloudOpenSDK SDK初始化失败");
                            }
                        });
    }

    @ReactMethod
    public void play(ReadableMap map) {
        String deviceSerial = map.getString(Const.KEY_DEVICE_SERIAL);
        int channelNo = map.getInt(Const.KEY_DEVICE_CHANNEL_NO);
        String verifyCode = map.getString(Const.KEY_VERIFY_CODE);

        final Context context = this.reactContext.getBaseContext();
        Intent intent = new Intent(context, LiveVideoActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Const.KEY_DEVICE_SERIAL, deviceSerial);
        intent.putExtra(Const.KEY_DEVICE_CHANNEL_NO, channelNo);
        intent.putExtra(Const.KEY_VERIFY_CODE, verifyCode);
        context.startActivity(intent);
    }

    @ReactMethod
    public void sampleMethod(String stringArgument, int numberArgument, Callback callback) {
        // TODO: Implement some actually useful functionality
        callback.invoke("Received numberArgument: " + numberArgument + " stringArgument: " + stringArgument);

    }

}

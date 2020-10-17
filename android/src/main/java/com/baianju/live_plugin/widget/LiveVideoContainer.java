package com.baianju.live_plugin.widget;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.baianju.live_plugin.R;
import com.baianju.live_plugin.util.Const;
import com.baianju.live_plugin.util.RxUtils;
import com.baianju.live_plugin.util.ScreenOrientationHelper;
import com.hikvision.cloud.sdk.CloudOpenSDK;
import com.hikvision.cloud.sdk.core.CloudOpenSDKListener;
import com.hikvision.cloud.sdk.core.CloudVideoPlayer;
import com.videogo.exception.BaseException;
import com.videogo.openapi.EZConstants;
import com.videogo.openapi.bean.EZCameraInfo;
import com.videogo.openapi.bean.EZDeviceInfo;
import com.videogo.openapi.bean.EZVideoQualityInfo;
import com.videogo.widget.CheckTextButton;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;

public
class LiveVideoContainer extends FrameLayout implements View.OnClickListener {

    private SurfaceView mSurfaceView;
    private ProgressBar mProgressBar;
    private RelativeLayout mPlayerPlayLargeBtn;
    private RelativeLayout mPlayerAreaRl;
    private LinearLayout mPlayerControlLl;
    private ImageView mPlayerStopBtn;
    private ImageView mPlayerSoundBtn;
    private CheckTextButton mPlayerFullScreenBtn;
    private boolean isHolderFirstCreated = true;

    private String mDeviceSerial; // 设备序列号
    private int mChannelNo; // 通道号
    private String mVerifyCode;

    // 视频预览
    private boolean isPlayOpenStatus;
    private boolean isOldPlaying; //用于界面不可见和可见切换时，记录是否预览的状态
    // 声音开关
    private boolean isSoundOpenStatus;
    // 录制功能
    private boolean isRecordOpenStatus;

    private CloudVideoPlayer mRealPlayer;
    //private CloudVideoPlayer mTalkPlayer;// 预览和对讲用两个player，避免有回音、啸叫
    private EZDeviceInfo mDeviceInfo;
    //private ScreenOrientationHelper mScreenOrientationHelper = null;// 转屏控制器

    private boolean isEncry = false;
    private boolean isSupportPTZ; // 是否支持云台操作
    private EZConstants.EZTalkbackCapability mTalkAbility;//设备对讲信息
    private int mCurrentlevelQuality = EZConstants.EZVideoLevel.VIDEO_LEVEL_FLUNET.getVideoLevel(); // 保存当前的视频码流清晰度
    private ArrayList<EZVideoQualityInfo> mVideoQualityList; // 用来存放监控点清晰度的列表

    private Disposable mPlayerDeviceInfoDisposable;
    private Disposable mPlayerLevelSettingDisposable;

    public LiveVideoContainer(@NonNull Context context) {
        this(context,null);
    }

    public LiveVideoContainer(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public LiveVideoContainer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater.from(context).inflate(R.layout.activity_live_video,this,true);

        mSurfaceView = findViewById(R.id.realplay_id_surface_v);
        mProgressBar = findViewById(R.id.realplay_id_pb);
        mPlayerPlayLargeBtn = findViewById(R.id.player_play_btn);
        mPlayerAreaRl = findViewById(R.id.realplay_player_area);
        mPlayerControlLl = findViewById(R.id.play_control_bar);
        mPlayerStopBtn = findViewById(R.id.play_stop_btn);
        mPlayerSoundBtn = findViewById(R.id.play_sound_btn);
        mPlayerFullScreenBtn = findViewById(R.id.fullscreen_button);
        //mScreenOrientationHelper = new ScreenOrientationHelper((Activity) getContext(), mPlayerFullScreenBtn);

        initEvent();
        /**
         * 'DEVICE_SERIAL':'D72067192',
         *           'DEVICE_CHANNEL_NO':1,
         *           'VERIFY_CODE':'XHXBHK'
         */
        //initData("D72067192",1,"XHXBHK");
    }

    private void initEvent() {
        mPlayerPlayLargeBtn.setOnClickListener(this);
        mSurfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("--------------------mSurfaceView onclick isPlayOpenStatus : " + isPlayOpenStatus);
                if (!isPlayOpenStatus) {
                    return;
                }
                if (mPlayerControlLl.getVisibility() == View.VISIBLE) {
                    mPlayerControlLl.setVisibility(View.GONE);
                } else {
                    mPlayerControlLl.setVisibility(View.VISIBLE);
                }
            }
        });
        // 控制栏的声音控制
        mPlayerStopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPlayOpenStatus) {
                    // TODO 关闭对讲，云台操作
                    stopPlay();
                    isPlayOpenStatus = false;
                } else {
                    startPlay(isEncry);
                    isPlayOpenStatus = true;
                }
            }
        });
        mPlayerSoundBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String toastMsg;
                if (!isPlayOpenStatus) {
                    return;
                }
                boolean isSuccess;
                if (isSoundOpenStatus) {
                    isSuccess = mRealPlayer.closeSound();
                    if (isSuccess) {
                        isSoundOpenStatus = false;
                        toastMsg = "声音关闭成功";
                        //mSoundStatusTv.setText("关闭");
                        mPlayerSoundBtn.setBackgroundResource(R.drawable.play_control_sound_off_selector);
                    } else {
                        toastMsg = "声音关闭失败";
                    }
                } else {
                    isSuccess = mRealPlayer.openSound();
                    if (isSuccess) {
                        isSoundOpenStatus = true;
                        toastMsg = "声音开启成功";
                        //mSoundStatusTv.setText("开启");
                        mPlayerSoundBtn.setBackgroundResource(R.drawable.play_control_sound_selector);
                    } else {
                        toastMsg = "声音开启失败";
                    }
                }
                if (null != toastMsg) {
                    Toast.makeText(getContext(), toastMsg, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void initData(String deviceSerial,int channelNo,String verifyCode) {
        System.out.println("------------------------deviceSerial:"+deviceSerial);
        //Intent intent = getIntent();
        mDeviceSerial = deviceSerial;
        mChannelNo = channelNo;
        mVerifyCode = verifyCode;
        initPlayer();
    }

    private void initPlayer() {

        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                // 可见的时候，创建SurfaceView的holder
                // 每次回到该界面，holder都会被重新创建
                if (isHolderFirstCreated) {
                    isHolderFirstCreated = false;
                    getDeviceInfo(); // demo这里顺序：先获取设备信息，再开始播放，顺序可按需求自行调整
                } else {
                    if (isOldPlaying) {
                        startPlay(isEncry);
                    }
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                // 不可见的时候  销毁SurfaceView的holder
                // 切到后台界面或返回主界面，holder被销毁了
                isOldPlaying = isPlayOpenStatus;
                stopPlay();
            }
        });
    }

    /**
     * CloudOpenSDK.getEZDeviceInfo()需要在子线程中调用
     */
    private void getDeviceInfo() {
        mPlayerDeviceInfoDisposable = Observable.create(new ObservableOnSubscribe<EZDeviceInfo>() {
            @Override
            public void subscribe(ObservableEmitter<EZDeviceInfo> emitter) throws Exception {
                EZDeviceInfo deviceInfo = CloudOpenSDK.getEZDeviceInfo(mDeviceSerial);
                if (null != deviceInfo) {
                    emitter.onNext(deviceInfo);
                } else {
                    emitter.onError(new Throwable());
                }
                emitter.onComplete();
            }
        }).compose(RxUtils.<EZDeviceInfo>io2Main())
                .subscribeWith(new DisposableObserver<EZDeviceInfo>() {
                    @Override
                    public void onNext(EZDeviceInfo deviceInfo) {
                        mDeviceInfo = deviceInfo;
                        // 获取对讲信息,对讲模式类型:
                        // 不支持对讲:EZConstants.EZTalkbackCapability.EZTalkbackNoSupport
                        // 支持全双工对讲:EZConstants.EZTalkbackCapability.EZTalkbackFullDuplex
                        // 支持半双工对讲:EZConstants.EZTalkbackCapability.EZTalkbackHalfDuplex
                        mTalkAbility = mDeviceInfo.isSupportTalk();
                        isSupportPTZ = mDeviceInfo.isSupportPTZ();
                        //获取视频清晰度信息
                        List<EZCameraInfo> cameraInfoList = mDeviceInfo.getCameraInfoList();
                        if (null == cameraInfoList) {
                            return;
                        }
                        for (EZCameraInfo cameraInfo : cameraInfoList) {
                            // 先判断通道号
                            if (cameraInfo.getCameraNo() == mChannelNo) {
                                mVideoQualityList = cameraInfo.getVideoQualityInfos();
                                // 设备默认的清晰度为
                                mCurrentlevelQuality = cameraInfo.getVideoLevel().getVideoLevel();
                                String levelName;
                                if (mCurrentlevelQuality == EZConstants.EZVideoLevel.VIDEO_LEVEL_FLUNET.getVideoLevel()) {
                                    levelName = "流畅";
                                } else if (mCurrentlevelQuality == EZConstants.EZVideoLevel.VIDEO_LEVEL_BALANCED.getVideoLevel()) {
                                    levelName = "均衡";
                                } else if (mCurrentlevelQuality == EZConstants.EZVideoLevel.VIDEO_LEVEL_HD.getVideoLevel()) {
                                    levelName = "高清";
                                } else if (mCurrentlevelQuality == EZConstants.EZVideoLevel.VIDEO_LEVEL_SUPERCLEAR.getVideoLevel()) {
                                    levelName = "超清";
                                } else {
                                    levelName = "流畅";
                                }
                                //mVideoLevelStatusTv.setText(levelName);
                            }
                        }
                        startPlay(isEncry);
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (e instanceof BaseException) {
                            //toast(e.getMessage());
                            Toast.makeText(getContext(),e.getMessage(),Toast.LENGTH_SHORT);
                        }
                        if (mPlayerDeviceInfoDisposable != null && !mPlayerDeviceInfoDisposable.isDisposed()) {
                            mPlayerDeviceInfoDisposable.dispose();
                        }
                    }

                    @Override
                    public void onComplete() {
                        if (mPlayerDeviceInfoDisposable != null && !mPlayerDeviceInfoDisposable.isDisposed()) {
                            mPlayerDeviceInfoDisposable.dispose();
                        }
                    }
                });
    }

    /**
     * 开始预览
     *
     * @param isEncry 是否加密,加密的话，设置设备验证码
     */
    private void startPlay(boolean isEncry) {
        mRealPlayer = CloudOpenSDK.getInstance().createPlayer(mDeviceSerial, mChannelNo);
        mRealPlayer.setSurfaceHolder(mSurfaceView.getHolder());
        //mTalkPlayer = CloudOpenSDK.getInstance().createPlayer(mDeviceSerial, mChannelNo);
        //mTalkPlayer.setOnVoicTalkListener(onVoiceTalkListener);
        if (isEncry) {
            //mRealPlayer.setPlayVerifyCode(ConfigCst.VERIFY_CODE);
            mRealPlayer.setPlayVerifyCode(mVerifyCode);
            //mTalkPlayer.setPlayVerifyCode(ConfigCst.VERIFY_CODE);
        }
        mRealPlayer.closeSound();
        mRealPlayer.startRealPlay();
        mPlayerPlayLargeBtn.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
        mRealPlayer.setOnRealPlayListener(new CloudOpenSDKListener.OnRealPlayListener() {
            @Override
            public void onVideoSizeChanged(int videoWidth, int videoHeight) {

            }

            @Override
            public void onRealPlaySuccess() {
                //mScreenOrientationHelper.enableSensorOrientation();
                isPlayOpenStatus = true;
                mPlayerPlayLargeBtn.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.GONE);
                //mPlayStatusTv.setText("开启");
                // 默认开启声音
                if (mRealPlayer.openSound()) {
                    isSoundOpenStatus = true;
                    //mSoundStatusTv.setText("开启");
                }
                mPlayerSoundBtn.setBackgroundResource(R.drawable.play_control_sound_selector);
                mPlayerStopBtn.setBackgroundResource(R.drawable.player_stop_selector);
            }

            @Override
            public void onStopRealPlaySuccess() {
                isPlayOpenStatus = false;
                isSoundOpenStatus = false;
                //mPlayStatusTv.setText("关闭");
                //mSoundStatusTv.setText("关闭");
                mPlayerSoundBtn.setBackgroundResource(R.drawable.play_control_sound_off_selector);
                mPlayerStopBtn.setBackgroundResource(R.drawable.player_play_selector);
                mPlayerControlLl.setVisibility(View.GONE);
                mPlayerPlayLargeBtn.setVisibility(View.VISIBLE);
            }

            /**
             * 播放失败回调,得到失败信息
             *
             * @param errorCode   播放失败错误码
             * @param moduleCode  播放失败模块错误码
             * @param description 播放失败描述
             * @param sulution    播放失败解决方方案
             */
            @Override
            public void onRealPlayFailed(int errorCode, String moduleCode, String description, String sulution) {
                //toast(String.format("errorCode：%d, %s", errorCode, description));
                isPlayOpenStatus = false;
                isSoundOpenStatus = false;
                mProgressBar.setVisibility(View.GONE);

                if (errorCode == 400035 || errorCode == 400036) {
                    //
                    //回调时查看errorCode，如果为400035（需要输入验证码）和400036（验证码错误），
                    // 则需要开发者自己处理让用户重新输入验证密码，并调用setPlayVerifyCode设置密码，
                    // 然后重新启动播放
                    // TODO
                    /*if (!mVerifyCodeAlertDialog.isShowing()) {
                        mVerifyCodeAlertDialog.show();
                    }*/
                    return;
                }

                stopPlay();
                //mPlayStatusTv.setText("关闭");
            }
        });
    }

    private void stopPlay() {
        //mScreenOrientationHelper.disableSensorOrientation();
        if (null != mRealPlayer) {
            mRealPlayer.closeSound();
            mRealPlayer.stopRealPlay(); // 停止播放
            //mTalkPlayer.stopVoiceTalk(); //停止对讲
            //如果是录制状态的，调用stop
            if (isRecordOpenStatus) {
                mRealPlayer.stopLocalRecord();
            }
        }
    }

    @Override
    public void onClick(View v) {
        // 视频预览
        if (v.getId() == R.id.player_play_btn) {
            startPlay(isEncry);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (null != mRealPlayer) {
            mRealPlayer.release();
        }
    }
}

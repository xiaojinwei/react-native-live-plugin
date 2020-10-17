package com.baianju.live_plugin;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import com.baianju.live_plugin.backplay.BackPlayFragment;
import com.baianju.live_plugin.backplay.OnBackPlayFileSelected;
import com.baianju.live_plugin.data.ResultEvent;
import com.baianju.live_plugin.util.Const;
import com.baianju.live_plugin.util.DisplayUtils;
import com.baianju.live_plugin.util.EventUtil;
import com.baianju.live_plugin.util.FragmentUtils;
import com.baianju.live_plugin.util.MediaUtil;
import com.baianju.live_plugin.util.RxUtils;
import com.baianju.live_plugin.util.ScreenOrientationHelper;
import com.baianju.live_plugin.widget.MoreBottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.hikvision.cloud.sdk.CloudOpenSDK;
import com.hikvision.cloud.sdk.core.CloudOpenSDKListener;
import com.hikvision.cloud.sdk.core.CloudVideoPlayer;
import com.hikvision.cloud.sdk.core.OnCommonCallBack;
import com.hikvision.cloud.sdk.core.ptz.PTZAction;
import com.hikvision.cloud.sdk.core.ptz.PTZCommand;
import com.hikvision.cloud.sdk.util.LogUtils;
import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.videogo.exception.BaseException;
import com.videogo.openapi.EZConstants;
import com.videogo.openapi.EZOpenSDKListener;
import com.videogo.openapi.bean.EZCameraInfo;
import com.videogo.openapi.bean.EZCloudRecordFile;
import com.videogo.openapi.bean.EZDeviceInfo;
import com.videogo.openapi.bean.EZDeviceRecordFile;
import com.videogo.openapi.bean.EZVideoQualityInfo;
import com.videogo.widget.CheckTextButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.DisposableObserver;

public
class LiveVideoActivity extends AppCompatActivity implements View.OnClickListener, OnBackPlayFileSelected {

    private SurfaceView mSurfaceView;
    private ProgressBar mProgressBar;
    private RelativeLayout mPlayerPlayLargeBtn;
    private RelativeLayout mPlayerAreaRl;
    private LinearLayout mPlayerControlLl;
    private ImageView mPlayerStopBtn;
    private ImageView mPlayerSoundBtn;
    private CheckTextButton mPlayerFullScreenBtn;
    private boolean isHolderFirstCreated = true;

    private TextView mCapturePictureBtn; // 拍照
    private TextView mRecordVideoBtn; // 录像
    private TextView mTalkBtn; // 对讲
    private TextView mPtzBtn; // 云台控制
    private TextView mPlaybackBtn; // 回放
    private TextView mFeedbackBtn; // 反馈

    private PopupWindow mCapturePicPopupWindow;
    private ImageView mCaptureImgIv;

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
    //对讲功能
    private boolean isTalkOpenStatus;
    private boolean isTalkOpenStatusBtn;

    private CloudVideoPlayer mRealPlayer;
    //private CloudVideoPlayer mBackPlayer;
    private CloudVideoPlayer mTalkPlayer;// 预览和对讲用两个player，避免有回音、啸叫
    private EZDeviceInfo mDeviceInfo;
    private ScreenOrientationHelper mScreenOrientationHelper = null;// 转屏控制器

    private PopupWindow mPtzPopupWindow;// 云台控制窗口
    private LinearLayout mPtzControlLy;// 云台控制区中间的轮盘

    private boolean isEncry = false;
    private boolean isSupportPTZ; // 是否支持云台操作
    private EZConstants.EZTalkbackCapability mTalkAbility;//设备对讲信息
    private int mCurrentlevelQuality = EZConstants.EZVideoLevel.VIDEO_LEVEL_FLUNET.getVideoLevel(); // 保存当前的视频码流清晰度
    private ArrayList<EZVideoQualityInfo> mVideoQualityList; // 用来存放监控点清晰度的列表

    private Disposable mPlayerDeviceInfoDisposable;
    private Disposable mPlayerLevelSettingDisposable;

    private RxPermissions mRxPermissions;

    private boolean isPlaybackStatus;//是否在放回页面

    //回放
    private EZDeviceRecordFile mEZDeviceRecordile;
    private EZCloudRecordFile mEZCloudRecordFile;
    private boolean isDeviceRecord; // 是为本地，否为云端
    private ImageView photographBtn;
    private ImageView videotapeButton;
    private ImageView share_button;
    private ImageView contacts_button;
    private ImageView iv_live_broadcast;
    private ImageView iv_video_playback;
    private TextView tv_live_broadcast;
    private TextView tv_video_playback;

    private int bottomTabIndex = 0;
    private ImageView mk_speck_btn;
    private TextView live_title;
    private TextView tv_live_level_name;
    private ImageView live_more;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_video);
        mSurfaceView = findViewById(R.id.realplay_id_surface_v);
        mProgressBar = findViewById(R.id.realplay_id_pb);
        mPlayerPlayLargeBtn = findViewById(R.id.player_play_btn);
        mPlayerAreaRl = findViewById(R.id.realplay_player_area);
        mPlayerControlLl = findViewById(R.id.play_control_bar);
        mPlayerStopBtn = findViewById(R.id.play_stop_btn);
        mPlayerSoundBtn = findViewById(R.id.play_sound_btn);
        mPlayerFullScreenBtn = findViewById(R.id.fullscreen_button);
        mScreenOrientationHelper = new ScreenOrientationHelper(this, mPlayerFullScreenBtn);
        //抓图
        mCapturePictureBtn = findViewById(R.id.realplay_id_capture_btn);
        mRecordVideoBtn = findViewById(R.id.realplay_id_record_btn);
        mTalkBtn = findViewById(R.id.realplay_id_talk_btn);
        mPtzBtn = findViewById(R.id.realplay_id_ptz_btn);
        mPlaybackBtn = findViewById(R.id.realplay_id_playback_btn);
        mFeedbackBtn = findViewById(R.id.realplay_id_feedback_btn);
        //controller
        photographBtn = findViewById(R.id.photograph_btn);
        videotapeButton = findViewById(R.id.videotape_button);
        contacts_button = findViewById(R.id.contacts_button);
        share_button = findViewById(R.id.share_button);


        mRxPermissions = new RxPermissions(this);
        initEvent();
        initData();
    }

    private void initEvent() {
        photographBtn.setOnClickListener(this);
        videotapeButton.setOnClickListener(this);
        share_button.setOnClickListener(this);
        contacts_button.setOnClickListener(this);
        mPlayerPlayLargeBtn.setOnClickListener(this);
        mCapturePictureBtn.setOnClickListener(this);
        mRecordVideoBtn.setOnClickListener(this);
        mTalkBtn.setOnClickListener(this);
        mPtzBtn.setOnClickListener(this);
        mPlaybackBtn.setOnClickListener(this);
        mFeedbackBtn.setOnClickListener(this);
        mSurfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isPlayOpenStatus || bottomTabIndex != 0) {
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
                    if(!isPlaybackStatus){
                        LiveVideoActivity.this.stopPlay();
                    }else{
                        stopBackPlay();
                    }
                    isPlayOpenStatus = false;
                } else {
                    if(!isPlaybackStatus){
                        LiveVideoActivity.this.startPlay(isEncry);
                    }else{
                        startBackPlay(isEncry,mEZDeviceRecordile,mEZCloudRecordFile);
                    }
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
                    Toast.makeText(LiveVideoActivity.this, toastMsg, Toast.LENGTH_SHORT).show();
                }
            }
        });

        initToolbar();
        initPtzView();
        initBottomTab();
        initLevel();
    }

    private void initLevel() {
        tv_live_level_name = findViewById(R.id.tv_live_level_name);
    }

    private void initToolbar() {
        live_more = findViewById(R.id.live_more);
        live_more.setOnClickListener(this);
        findViewById(R.id.live_back).setOnClickListener(this);
        live_title = findViewById(R.id.live_title);
    }

    private void initBottomTab() {
        findViewById(R.id.live_broadcast_container).setOnClickListener(this);
        findViewById(R.id.video_playback_container).setOnClickListener(this);
        iv_live_broadcast = findViewById(R.id.iv_live_broadcast);
        iv_video_playback = findViewById(R.id.iv_video_playback);
        tv_live_broadcast = findViewById(R.id.tv_live_broadcast);
        tv_video_playback = findViewById(R.id.tv_video_playback);
        mk_speck_btn = findViewById(R.id.mk_speck_btn);
        mk_speck_btn.setOnClickListener(this);
    }

    private void initData() {
        Intent intent = getIntent();
        mDeviceSerial = intent.getStringExtra(Const.KEY_DEVICE_SERIAL);
        mChannelNo = intent.getIntExtra(Const.KEY_DEVICE_CHANNEL_NO,1);
        mVerifyCode = intent.getStringExtra(Const.KEY_VERIFY_CODE);
        initPlayer();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mScreenOrientationHelper.postOnStart();
        mScreenOrientationHelper.disableSensorOrientation();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mScreenOrientationHelper.postOnStop();
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
                                tv_live_level_name.setText(levelName);
                            }
                        }
                        startPlay(isEncry);
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (e instanceof BaseException) {
                            //toast(e.getMessage());
                            Toast.makeText(LiveVideoActivity.this,e.getMessage(),Toast.LENGTH_SHORT);
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

    private boolean containVideoLevel(int level) {
        if (null == mVideoQualityList) {
            return false;
        }
        for (EZVideoQualityInfo qualityInfo : mVideoQualityList) {
            if (level == qualityInfo.getVideoLevel()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 设置清晰度
     * 设置清晰度接口为耗时操作，必须在子线程中调用
     * <p>
     * level清晰度：{@link com.videogo.openapi.EZConstants.EZVideoLevel}
     * <li>流畅：{@link com.videogo.openapi.EZConstants.EZVideoLevel.VIDEO_LEVEL_FLUNET}</li>
     * <li>均衡：{@link com.videogo.openapi.EZConstants.EZVideoLevel.VIDEO_LEVEL_BALANCED}</li>
     * <li>高清：{@link com.videogo.openapi.EZConstants.EZVideoLevel.VIDEO_LEVEL_HD}</li>
     * <li>超清：{@link com.videogo.openapi.EZConstants.EZVideoLevel.VIDEO_LEVEL_SUPERCLEAR}</li>
     * </p>
     *
     * @param levelName 清晰度名称
     * @param levelVal  清晰度值
     */
    private void setVideoLevel(final String levelName, final int levelVal) {
        if (mCurrentlevelQuality == levelVal) {
            Toast.makeText(this, String.format("当前清晰度已是%s，请勿重复操作", levelName), Toast.LENGTH_SHORT).show();
            return;
        }
        mPlayerLevelSettingDisposable = Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter<Boolean> emitter) throws Exception {
                // 设置清晰度接口为耗时操作，必须在子线程中调用
                boolean isSuccess = mRealPlayer.setVideoLevel(levelVal);
                emitter.onNext(isSuccess);
                emitter.onComplete();
            }
        }).compose(RxUtils.<Boolean>io2Main())
                .subscribeWith(new DisposableObserver<Boolean>() {

                    @Override
                    public void onNext(Boolean isSuccess) {
                        String toastMsg;
                        if (isSuccess) {
                            mCurrentlevelQuality = levelVal;
                            toastMsg = String.format("设置清晰度(%s)成功", levelName);
                            tv_live_level_name.setText(levelName);
                            // 视频播放成功后设置了清晰度需要先停止播放stopRealPlay然后重新开启播放startRealPlay才能生效
                            stopPlay();
                            startPlay(isEncry);
                        } else {
                            toastMsg = String.format("设置清晰度(%s)失败", levelName);
                        }
                        Toast.makeText(LiveVideoActivity.this, toastMsg, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mPlayerLevelSettingDisposable != null && !mPlayerLevelSettingDisposable.isDisposed()) {
                            mPlayerLevelSettingDisposable.dispose();
                        }
                    }

                    @Override
                    public void onComplete() {
                        if (mPlayerLevelSettingDisposable != null && !mPlayerLevelSettingDisposable.isDisposed()) {
                            mPlayerLevelSettingDisposable.dispose();
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
        mTalkPlayer = CloudOpenSDK.getInstance().createPlayer(mDeviceSerial, mChannelNo);
        mTalkPlayer.setOnVoicTalkListener(onVoiceTalkListener);
        if (isEncry) {
            //mRealPlayer.setPlayVerifyCode(ConfigCst.VERIFY_CODE);
            mRealPlayer.setPlayVerifyCode(mVerifyCode);
            //mTalkPlayer.setPlayVerifyCode(ConfigCst.VERIFY_CODE);
            mTalkPlayer.setPlayVerifyCode(mVerifyCode);
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
                mScreenOrientationHelper.enableSensorOrientation();
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
                toast(String.format("errorCode：%d, %s", errorCode, description));
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
        mScreenOrientationHelper.disableSensorOrientation();
        if (null != mRealPlayer) {
            mRealPlayer.closeSound();
            mRealPlayer.stopRealPlay(); // 停止播放
            mTalkPlayer.stopVoiceTalk(); //停止对讲
            //如果是录制状态的，调用stop
            if (isRecordOpenStatus) {
                mRealPlayer.stopLocalRecord();
            }
        }
    }

    /**
     * 抓图
     */
    private void capturePicture(){
        // 获取权限逻辑
        mRxPermissions.requestEach(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(new Consumer<Permission>() {
                    @Override
                    public void accept(Permission permission) throws Exception {
                        if (permission.granted) {
                            if (!isPlayOpenStatus) {
                                return;
                            }
                            Bitmap captureBitmap = mRealPlayer.capturePicture(); //图片很大的情况下，建议先写到本地，再压缩读出来渲染到界面上
                            showCapturePicPopupWindow(captureBitmap);
                            if(captureBitmap != null){
                                MediaUtil.saveImageToAlbum(LiveVideoActivity.this,captureBitmap,System.currentTimeMillis() + ".jpg");
                            }
                            System.out.println("--------------------captureBitmap:"+captureBitmap);
                        } else if (permission.shouldShowRequestPermissionRationale) {
                            toast("存储功能开启失败，拒绝权限，等待下次询问哦");
                        } else {
                            toast("存储功能开启失败，不再弹出询问框，请前往APP应用设置中打开此权限");
                        }
                    }
                });
    }

    @Override
    public void onClick(View v) {
        // 视频预览
        if (v.getId() == R.id.player_play_btn) {
            startPlay(isEncry);
        }else if(v.getId() == R.id.realplay_id_capture_btn || v.getId() == R.id.photograph_btn){
            capturePicture();
        }else if(v.getId() == R.id.realplay_id_record_btn || v.getId() == R.id.videotape_button){
            videotape();
        }else if(v.getId() == R.id.realplay_id_talk_btn){
            talk();
        }else if(v.getId() == R.id.realplay_id_ptz_btn){
            if (isSupportPTZ) {
                showPtzPopupWindow();
            } else {
                toast("该设备不支持云台操作");
            }
        }else if(v.getId() == R.id.realplay_id_playback_btn){
            playbackController();
        }else if(v.getId() == R.id.realplay_id_feedback_btn){
            EventUtil.getInstance().emit(EventUtil.EVENT_NAME_NATIVE_TO_JS, ResultEvent.newEvent(EventUtil.WHAT_FEEDBACK,null).toJson());
        }else if(v.getId() == R.id.live_broadcast_container){
            switchBottomTab(0);
        }else if(v.getId() == R.id.video_playback_container){
            switchBottomTab(1);
        }else if(v.getId() == R.id.mk_speck_btn){
            pressTalk();
        }else if(v.getId() == R.id.live_more){
            liveMore();
        }else if(v.getId() == R.id.live_back){
            finish();
        }else if(v.getId() == R.id.share_button){
            //分享
            EventUtil.getInstance().emit(EventUtil.EVENT_NAME_NATIVE_TO_JS, ResultEvent.newEvent(EventUtil.WHAT_SHARE,null).toJson());
        }else if(v.getId() == R.id.contacts_button){
            //联系人
            EventUtil.getInstance().emit(EventUtil.EVENT_NAME_NATIVE_TO_JS, ResultEvent.newEvent(EventUtil.WHAT_CONTRACTS,null).toJson());
        }
    }

    private void liveMore() {
        List<String> data = new ArrayList<>();
        data.add("清晰度");
        data.add("解除绑定");
        data.add("投诉建议");
        MoreBottomSheetDialog dialog = new MoreBottomSheetDialog();
        dialog.setListData(data,-1);
        dialog.show(getSupportFragmentManager(),"dialog");
        dialog.setOnItemClickListener(new MoreBottomSheetDialog.OnItemClickListener() {

            @Override
            public void onItemClick(List<String> data, int position, int selectedIndex) {
                if(position == 0){
                    settingDefinitionLive();
                }else if(position == 1){
                    EventUtil.getInstance().emit(EventUtil.EVENT_NAME_NATIVE_TO_JS, ResultEvent.newEvent(EventUtil.WHAT_UNBIND,null).toJson());
                }else if(position == 2){
                    EventUtil.getInstance().emit(EventUtil.EVENT_NAME_NATIVE_TO_JS, ResultEvent.newEvent(EventUtil.WHAT_FEEDBACK,null).toJson());
                }
            }
        });
    }

    private int getLevelStatusIndex(List<String> data,String select){
        if(data != null && !data.isEmpty()){
            for (int i = 0; i < data.size(); i++) {
                if(TextUtils.equals(data.get(i),select)){
                    return i;
                }
            }
        }
        return -1;
    }

    private void settingDefinitionLive(){
        List<String> data = new ArrayList<>();
        data.add("流畅");
        data.add("均衡");
        data.add("高清");
        data.add("超清");
        final MoreBottomSheetDialog dialog = new MoreBottomSheetDialog();
        dialog.setListData(data,getLevelStatusIndex(data,tv_live_level_name.getText().toString()));
        dialog.show(getSupportFragmentManager(),"dialog");
        dialog.setOnItemClickListener(new MoreBottomSheetDialog.OnItemClickListener() {

            @Override
            public void onItemClick(List<String> data, int position, int selectedIndex) {
                if(position == 0){
                    if (!containVideoLevel(EZConstants.EZVideoLevel.VIDEO_LEVEL_FLUNET.getVideoLevel())) {
                        toast("该设备不支持切流畅清晰度");
                    } else {
                        setVideoLevel(data.get(position), EZConstants.EZVideoLevel.VIDEO_LEVEL_FLUNET.getVideoLevel());
                    }
                }else if(position == 1){
                    if (!containVideoLevel(EZConstants.EZVideoLevel.VIDEO_LEVEL_BALANCED.getVideoLevel())) {
                        toast("该设备不支持切均衡清晰度");
                    } else {
                        setVideoLevel(data.get(position), EZConstants.EZVideoLevel.VIDEO_LEVEL_BALANCED.getVideoLevel());
                    }
                }else if(position == 2){
                    if (!containVideoLevel(EZConstants.EZVideoLevel.VIDEO_LEVEL_HD.getVideoLevel())) {
                        toast("该设备不支持切高清清晰度");
                    } else {
                        setVideoLevel(data.get(position), EZConstants.EZVideoLevel.VIDEO_LEVEL_HD.getVideoLevel());
                    }
                }else if(position == 3){
                    if (!containVideoLevel(EZConstants.EZVideoLevel.VIDEO_LEVEL_SUPERCLEAR.getVideoLevel())) {
                        toast("该设备不支持切超清清晰度");
                    } else {
                        setVideoLevel(data.get(position), EZConstants.EZVideoLevel.VIDEO_LEVEL_SUPERCLEAR.getVideoLevel());
                    }
                }
                dialog.dismiss();
            }
        });
    }

    private void pressTalk() {
        if(bottomTabIndex == 0){
            if(isTalkOpenStatusBtn){
                mk_speck_btn.setImageResource(R.drawable.icon_mk_speck_normal);
            }else{
                mk_speck_btn.setImageResource(R.drawable.icon_mk_speck_selected);
            }
            talk();
        }else{
            toast("请切换到工地直播进行语音对话");
        }
    }

    private void switchBottomTab(int index){
        if(index == 0 && index != bottomTabIndex){
            //工地直播
            switchBottomTabView(0);
            playbackController();
        }else if(index == 1 && index != bottomTabIndex){
            //视频回放
            switchBottomTabView(1);
            playbackController();
        }
    }

    private void switchBottomTabView(int index){
        bottomTabIndex = index;
        if(index == 0){
            //工地直播
            iv_live_broadcast.setImageResource(R.drawable.icon_live_broadcast_selected);
            iv_video_playback.setImageResource(R.drawable.icon_video_playback_normal);
            tv_live_broadcast.setTextColor(getResources().getColor(R.color.gray_333));
            tv_video_playback.setTextColor(getResources().getColor(R.color.gray_999));

            tv_live_level_name.setVisibility(View.VISIBLE);
            mPlayerControlLl.setVisibility(View.VISIBLE);
            live_more.setVisibility(View.VISIBLE);
        }else if(index == 1){
            //视频回放
            iv_live_broadcast.setImageResource(R.drawable.icon_live_broadcast_normal);
            iv_video_playback.setImageResource(R.drawable.icon_video_playback_selected);
            tv_live_broadcast.setTextColor(getResources().getColor(R.color.gray_999));
            tv_video_playback.setTextColor(getResources().getColor(R.color.gray_333));

            tv_live_level_name.setVisibility(View.GONE);
            mPlayerControlLl.setVisibility(View.GONE);
            live_more.setVisibility(View.GONE);
        }
    }

    /**
     * 打开/关闭回放
     */
    private void playbackController(){
        if(!isPlaybackStatus){
            isPlaybackStatus = true;
            findViewById(R.id.realplay_player_control_area).setVisibility(View.GONE);
            findViewById(R.id.fragment_container).setVisibility(View.VISIBLE);
            stopPlay();
            FragmentUtils.showFragment(getSupportFragmentManager(), BackPlayFragment.newInstance(mDeviceSerial, mChannelNo, 0), R.id.fragment_container);
        }else{
            isPlaybackStatus = false;
            findViewById(R.id.realplay_player_control_area).setVisibility(View.VISIBLE);
            findViewById(R.id.fragment_container).setVisibility(View.GONE);
            FragmentUtils.hasFragment(getSupportFragmentManager(),R.id.fragment_container);
            stopBackPlay();
            startPlay(isEncry);
        }
    }

    /**
     * 对讲功能
     */
    private void talk(){
        if(!isTalkOpenStatusBtn){
            isTalkOpenStatusBtn = true;
            mRxPermissions.requestEach(Manifest.permission.RECORD_AUDIO)
                    .subscribe(new Consumer<Permission>() {
                        @Override
                        public void accept(Permission permission) throws Exception {
                            if (permission.granted) {
                                if (mTalkAbility == EZConstants.EZTalkbackCapability.EZTalkbackFullDuplex) {
                                    if (null == mTalkPlayer) {
                                        return;
                                    }
                                    mTalkPlayer.setOnVoicTalkListener(onVoiceTalkListener);
                                    mTalkPlayer.startVoiceTalk();
                                } else if (mTalkAbility == EZConstants.EZTalkbackCapability.EZTalkbackHalfDuplex) {
                                    //showHalfVideoTalkPopupWindow();
                                } else {
                                    LiveVideoActivity.this.toast("该设备不支持对讲功能");
                                }
                            } else if (permission.shouldShowRequestPermissionRationale) {
                                LiveVideoActivity.this.toast("对讲开启失败，拒绝权限，等待下次询问哦");
                            } else {
                                LiveVideoActivity.this.toast("对讲开启失败，不再弹出询问框，请前往APP应用设置中打开此权限");
                            }
                        }
                    });
        }else{
            mTalkPlayer.stopVoiceTalk();
            isTalkOpenStatusBtn = false;
        }
    }



    /**
     * 对讲监听回调
     * 对讲功能，需要有录音的权限
     */
    private CloudOpenSDKListener.OnVoiceTalkListener onVoiceTalkListener = new CloudOpenSDKListener.OnVoiceTalkListener() {
        @Override
        public void onStartVoiceTalkSuccess() {
            isTalkOpenStatus = true;
            // 对讲的时候需要关闭预览player的声音
            if (isSoundOpenStatus) {
                isSoundOpenStatus = false;
                mRealPlayer.closeSound();
            }
            //mTalkStatusTv.setText("开启");
            // 如果为半双工的情况下，设置pressed状态
            if (mTalkAbility == EZConstants.EZTalkbackCapability.EZTalkbackHalfDuplex) {
                mTalkPlayer.setVoiceTalkStatus(true);
            }
            // TODO
            //mRealPlayer.setSpeakerphoneOn(true);
        }

        @Override
        public void onStopVoiceTalkSuccess() {
            // 停止对讲成功
            isTalkOpenStatus = false;
            //mTalkStatusTv.setText("关闭");
            if (!isSoundOpenStatus) {
                isSoundOpenStatus = true;
                mRealPlayer.openSound();
            }
        }

        /**
         * 对讲失败回调,得到失败信息
         *
         * @param errorCode   播放失败错误码
         * @param moduleCode  播放失败模块错误码
         * @param description 播放失败描述
         * @param sulution    播放失败解决方方案
         */
        @Override
        public void onVoiceTalkFail(int errorCode, String moduleCode, String description, String sulution) {
            //开启对讲失败或停止对讲失败，这里需要开发者自己去判断是开启操作还是停止的操作
            //停止对讲失败后，不影响下一次的start使用
            // TODO
            toast(description);
        }
    };


    /**
     * 视频录制
     */
    private void videotape(){
        if (!isRecordOpenStatus) {
            // 录制本地路径,例如：Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath())：+"/hikvision/123.mp4"
            final String fileName = String.format("hik_%s.mp4", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
            //final String recordPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + "/Camera/" + fileName;
            final String recordPath = getExternalFilesDir(Environment.DIRECTORY_MOVIES).getAbsolutePath() + "/Live/" + fileName;
            System.out.println("------------------------recordPath:"+recordPath);
            boolean recordStartSuccess = mRealPlayer.startLocalRecordWithFile(recordPath);
            if (recordStartSuccess) {
                isRecordOpenStatus = true;
                toast("开启成功，开始录制");
                //mRecordStatusTv.setText("开启");
                mRealPlayer.getEzPlayer().setStreamDownloadCallback(new EZOpenSDKListener.EZStreamDownloadCallback() {
                    @Override
                    public void onSuccess(String s) {
                        LogUtils.deBug("StreamDownload onSuccess"); //录制成功
                        toast("onSuccess");
                    }

                    @Override
                    public void onError(EZOpenSDKListener.EZStreamDownloadError ezStreamDownloadError) {
                        toast("onError");
                        LogUtils.deBug("StreamDownload onError"); //录制失败
                    }
                });
            } else {
                toast("录制开启失败");
            }
        } else {
            boolean recordStopSuccess = mRealPlayer.stopLocalRecord();
            if (recordStopSuccess) {
                isRecordOpenStatus = false;
                toast("录制关闭成功");
                //mRecordStatusTv.setText("关闭");
            } else {
                toast("录制关闭失败");
            }
        }
    }

    /**
     * 打开抓图图片显示窗口
     */
    private void showCapturePicPopupWindow(Bitmap bitmap) {
        hideCapturePicPopupWindow();

        if (null == mCapturePicPopupWindow) {
            LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
            ViewGroup layoutView = (ViewGroup) layoutInflater.inflate(R.layout.dialog_capture_pic, null, true);
            ImageView closeBtn = layoutView.findViewById(R.id.capture_close_ic);
            mCaptureImgIv = layoutView.findViewById(R.id.capture_img);
            mCapturePicPopupWindow = new PopupWindow(layoutView, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT, true);
            mCapturePicPopupWindow.setBackgroundDrawable(new BitmapDrawable());
            mCapturePicPopupWindow.setAnimationStyle(R.style.popwindowAppearAnim);
            mCapturePicPopupWindow.setFocusable(true);
            mCapturePicPopupWindow.setOutsideTouchable(false);
            mCapturePicPopupWindow.showAsDropDown(mPlayerAreaRl);
            mCapturePicPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    LiveVideoActivity.this.hideCapturePicPopupWindow();
                }
            });
            closeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LiveVideoActivity.this.hideCapturePicPopupWindow();
                }
            });
        }
        mCaptureImgIv.setImageBitmap(bitmap);
        mCapturePicPopupWindow.update();
    }

    private void hideCapturePicPopupWindow() {
        if (mCapturePicPopupWindow != null) {
            mCapturePicPopupWindow.dismiss();
            mCapturePicPopupWindow = null;
        }
    }

    private void initPtzView(){
        mPtzControlLy = findViewById(R.id.ptz_control_ly);
        findViewById(R.id.ptz_top_btn).setOnTouchListener(cloudTouchListener);
        findViewById(R.id.ptz_bottom_btn).setOnTouchListener(cloudTouchListener);
        findViewById(R.id.ptz_left_btn).setOnTouchListener(cloudTouchListener);
        findViewById(R.id.ptz_right_btn).setOnTouchListener(cloudTouchListener);
    }

    /**
     * 打开云台控制窗口
     */
    @SuppressWarnings("deprecation")
    private void showPtzPopupWindow() {
        hidePtzPopupWindow();

        if (null == mPtzPopupWindow) {
            LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
            ViewGroup layoutView = (ViewGroup) layoutInflater.inflate(R.layout.video_ptz_control, null, true);
            mPtzControlLy = layoutView.findViewById(R.id.ptz_control_ly);
            //layoutView.findViewById(R.id.ptz_close_btn).setOnClickListener(v -> hidePtzPopupWindow());
            layoutView.findViewById(R.id.ptz_top_btn).setOnTouchListener(cloudTouchListener);
            layoutView.findViewById(R.id.ptz_bottom_btn).setOnTouchListener(cloudTouchListener);
            layoutView.findViewById(R.id.ptz_left_btn).setOnTouchListener(cloudTouchListener);
            layoutView.findViewById(R.id.ptz_right_btn).setOnTouchListener(cloudTouchListener);
            //layoutView.findViewById(R.id.btnBigger).setOnTouchListener(touchListener);
            //layoutView.findViewById(R.id.btnSmaller).setOnTouchListener(touchListener);
            int height = DisplayUtils.getScreenHeight(this) - (DisplayUtils.dp2px(this,56) + mPlayerAreaRl.getHeight());
            mPtzPopupWindow = new PopupWindow(layoutView, RelativeLayout.LayoutParams.MATCH_PARENT, height, true);
            mPtzPopupWindow.setBackgroundDrawable(new BitmapDrawable());
            mPtzPopupWindow.setAnimationStyle(R.style.popwindowUpAnim);
            mPtzPopupWindow.setFocusable(true);
            mPtzPopupWindow.setOutsideTouchable(false);
            mPtzPopupWindow.showAsDropDown(mPlayerAreaRl);
            mPtzPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    LiveVideoActivity.this.hidePtzPopupWindow();
                }
            });
        }
        mPtzPopupWindow.update();
    }

    private void hidePtzPopupWindow() {
        if (mPtzPopupWindow != null) {
            mPtzPopupWindow.dismiss();
            mPtzPopupWindow = null;
            mPtzControlLy = null;
        }
    }

    /**
     * 云台操作上下左右监听回调
     */
    private View.OnTouchListener cloudTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionevent) {
            int action = motionevent.getAction();
            PTZCommand ptzCommand = null;
            PTZAction ptzAction = null;
            int backgroundResourceId = -1;
            if (action == MotionEvent.ACTION_DOWN) {
                int id = view.getId();
                if (id == R.id.ptz_top_btn) {// 上
                    backgroundResourceId = R.drawable.ptz_up_sel;
                    ptzCommand = PTZCommand.UP;
                } else if (id == R.id.ptz_bottom_btn) {// 下
                    backgroundResourceId = R.drawable.ptz_bottom_sel;
                    ptzCommand = PTZCommand.DOWN;
                } else if (id == R.id.ptz_left_btn) {// 左
                    backgroundResourceId = R.drawable.ptz_left_sel;
                    ptzCommand = PTZCommand.LEFT;
                } else if (id == R.id.ptz_right_btn) {// 右
                    backgroundResourceId = R.drawable.ptz_right_sel;
                    ptzCommand = PTZCommand.RIGHT;
                }
                ptzAction = PTZAction.START;
            } else if (action == MotionEvent.ACTION_UP) {
                int id = view.getId();
                if (id == R.id.ptz_top_btn) {
                    ptzCommand = PTZCommand.UP;
                } else if (id == R.id.ptz_bottom_btn) {
                    ptzCommand = PTZCommand.DOWN;
                } else if (id == R.id.ptz_left_btn) {
                    ptzCommand = PTZCommand.LEFT;
                } else if (id == R.id.ptz_right_btn) {
                    ptzCommand = PTZCommand.RIGHT;
                }
                backgroundResourceId = R.drawable.ptz_bg;
                ptzAction = PTZAction.STOP;
            }
            if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_UP) {
                mPtzControlLy.setBackgroundResource(backgroundResourceId);
                invokePTZControl(ptzCommand, ptzAction);
            }
            return true;
        }
    };

    /**
     * 执行云台指令
     * <p>
     * controlPTZ()方法须在子线程中调用
     *
     * @param ptzCommand
     * @param ptzAction
     */
    private void invokePTZControl(final PTZCommand ptzCommand,
                                  final PTZAction ptzAction) {
        if (null == ptzCommand || null == ptzAction) {
            return;
        }
        final int speed = 2;// 0-2，默认为2
        Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(final ObservableEmitter<Boolean> emitter) throws Exception {
                // 必须在子线程中调用
                CloudOpenSDK.getInstance().controlPTZ(mDeviceSerial,
                        mChannelNo,
                        ptzCommand,
                        ptzAction,
                        speed
                        , new OnCommonCallBack() {
                            @Override
                            public void onSuccess() {
                                emitter.onNext(true);
                            }

                            @Override
                            public void onFailed(Exception e) {
                                emitter.onError(e);
                            }
                        });
            }
        }).compose(RxUtils.<Boolean>io2Main())
                .subscribe(new Observer<Boolean>() {
                    Disposable disposable;

                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onNext(Boolean value) {
                        if (null != disposable && !disposable.isDisposed()) {
                            disposable.dispose();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        toast(e.getMessage());
                        if (null != disposable && !disposable.isDisposed()) {
                            disposable.dispose();
                        }
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // 横屏
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //隐身通知栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

            ViewGroup.LayoutParams rootParams = mPlayerAreaRl.getLayoutParams();
            rootParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            rootParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            //mPlayerControlArea.setVisibility(View.GONE);

            //getSupportActionBar().hide();
            DisplayUtils.hideNavKey(this);

        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            // 竖屏
            //展示通知栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

            ViewGroup.LayoutParams rootParams = mPlayerAreaRl.getLayoutParams();
            rootParams.height = DisplayUtils.dp2px(this, 200);
            rootParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            //mPlayerControlArea.setVisibility(View.VISIBLE);

            //getSupportActionBar().show();
            DisplayUtils.showNavKey(this, 0);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mScreenOrientationHelper.portrait();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mRealPlayer) {
            mRealPlayer.release();
        }
        if (null != mTalkPlayer) {
            mTalkPlayer.release();
        }
        if (null != mPtzPopupWindow && mPtzPopupWindow.isShowing()) {
            mPtzPopupWindow.dismiss();
        }
        /*if (null != mHalfVideoTlakPopupWindow && mHalfVideoTlakPopupWindow.isShowing()) {
            mHalfVideoTlakPopupWindow.dismiss();
        }*/
    }

    public void toast(String msg){
        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocalBackFileSelected(EZDeviceRecordFile ezDeviceRecordFile) {
        if (isPlayOpenStatus) {
            stopBackPlay();
        }
        mEZDeviceRecordile = ezDeviceRecordFile;
        isDeviceRecord = true;
        startBackPlay(isEncry, ezDeviceRecordFile, null);
    }

    @Override
    public void onCloudBackFileSelected(EZCloudRecordFile ezCloudRecordFile) {
        if (isPlayOpenStatus) {
            stopBackPlay();
        }
        mEZCloudRecordFile = ezCloudRecordFile;
        isDeviceRecord = false;
        startBackPlay(isEncry, null, ezCloudRecordFile);
    }

    @Override
    public void onSelectOtherDay() {
        stopBackPlay();
    }

    @Override
    public void onClosePlayback() {
        playbackController();
        switchBottomTabView(0);
    }

    private void stopBackPlay() {
        isPlayOpenStatus = false;
        isSoundOpenStatus = false;
        if (null != mRealPlayer) {
            mRealPlayer.closeSound();
            mRealPlayer.stopPlayback();
        }
        mPlayerStopBtn.setBackgroundResource(R.drawable.player_play_selector);
        mPlayerSoundBtn.setBackgroundResource(R.drawable.play_control_sound_off_selector);
        mProgressBar.setVisibility(View.GONE);
        mPlayerControlLl.setVisibility(View.GONE);
        mPlayerPlayLargeBtn.setVisibility(View.VISIBLE);
    }

    private void startBackPlay(boolean isEncry, EZDeviceRecordFile ezDeviceRecordFile, EZCloudRecordFile ezCloudRecordFile) {
        if (null == ezDeviceRecordFile && null == ezCloudRecordFile) {
            return;
        }
        mPlayerPlayLargeBtn.setVisibility(View.GONE);
        mRealPlayer = CloudOpenSDK.getInstance().createPlayer(mDeviceSerial, mChannelNo);
        mRealPlayer.setSurfaceHolder(mSurfaceView.getHolder());
        if (isEncry) {
            mRealPlayer.setPlayVerifyCode(mVerifyCode);
        }
        mRealPlayer.setOnBackPlayListener(new CloudOpenSDKListener.OnBackPlayListener() {
            @Override
            public void onVideoSizeChanged(int videoWidth, int videoHeight) {

            }

            @Override
            public void onBackPlaySuccess() {
                isPlayOpenStatus = true;
                isSoundOpenStatus = true;
                mRealPlayer.openSound();
                mProgressBar.setVisibility(View.GONE);
                mPlayerStopBtn.setBackgroundResource(R.drawable.player_stop_selector);
                mPlayerSoundBtn.setBackgroundResource(R.drawable.play_control_sound_selector);
            }

            @Override
            public void onStopBackPlaySuccess() {

            }

            @Override
            public void onBackPlayFailed(int errorCode, String moduleCode, String description, String sulution) {
                toast(String.format("errorCode：%d, %s", errorCode, description));
                stopBackPlay();
                if (errorCode == 400035 || errorCode == 400036) {
                    //回调时查看errorCode，如果为400035（需要输入验证码）和400036（验证码错误），
                    // 则需要开发者自己处理让用户重新输入验证密码，并调用setPlayVerifyCode设置密码，
                    // 然后重新启动播放
                    // TODO
                    /*if (!mVerifyCodeAlertDialog.isShowing()) {
                        mVerifyCodeAlertDialog.show();
                    }*/
                    return;
                }
            }
        });

        mProgressBar.setVisibility(View.VISIBLE);
        if (null != ezDeviceRecordFile) {
            mRealPlayer.startPlayback(ezDeviceRecordFile);
        } else {
            mRealPlayer.startPlayback(ezCloudRecordFile);
        }
    }
}

package com.baianju.live_plugin.mvp.presenter;

import com.baianju.live_plugin.mvp.view.BackPlayView;
import com.baianju.live_plugin.util.RxUtils;
import com.hikvision.cloud.sdk.CloudOpenSDK;
import com.videogo.exception.BaseException;
import com.videogo.openapi.bean.EZCloudRecordFile;
import com.videogo.openapi.bean.EZDeviceRecordFile;

import java.util.Calendar;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * @author zhulongzhen
 * @date 2019/6/24
 * @desc
 */
public class BackPlayPresenter extends BasePresenter<BackPlayView> {

    @Override
    public void initialize() {

    }

    public void getLocalBackFile(final long queryTime, final String deviceSerial, final int channelNo, final Calendar[] calendars) {
        Observable.create(new ObservableOnSubscribe<List<EZDeviceRecordFile>>() {
            @Override
            public void subscribe(ObservableEmitter<List<EZDeviceRecordFile>> emitter) throws Exception {
                emitter.onNext(CloudOpenSDK.getEZOpenSDK().searchRecordFileFromDevice(deviceSerial, channelNo, calendars[0], calendars[1]));
            }
        })
                .compose(RxUtils.<List<EZDeviceRecordFile>>io2Main())
                .flatMap(new Function<List<EZDeviceRecordFile>, ObservableSource<EZDeviceRecordFile>>() {
                    @Override
                    public ObservableSource<EZDeviceRecordFile> apply(List<EZDeviceRecordFile> ezDeviceRecordFiles) throws Exception {
                        if (ezDeviceRecordFiles != null && ezDeviceRecordFiles.size() > 0) {
                            BackPlayPresenter.this.getView().getLocalBackSuccess(calendars, ezDeviceRecordFiles);
                            if (queryTime != 0) {
                                return BackPlayPresenter.this.queryLocal(true, queryTime, ezDeviceRecordFiles);
                            } else {
                                return Observable.just(ezDeviceRecordFiles.get(0));
                            }
                        }
                        throw new Exception("查询不到录像文件");
                    }
                }).subscribe(new DisposableObserver<EZDeviceRecordFile>() {
            @Override
            public void onNext(EZDeviceRecordFile ezCloudRecordFiles) {
                getView().queryLocalBackSuccess(true, ezCloudRecordFiles);
                if (isDisposed()) {
                    dispose();
                }
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                if (e instanceof BaseException) {
                    BaseException baseException = (BaseException) e;
                    getView().error(baseException.getMessage(), baseException.getErrorCode() + "");
                } else if (e instanceof NullPointerException) {
                    getView().queryLocalBackSuccess(true, null);
                } else if (e instanceof RuntimeException) {
                    getView().queryNull("查询不到录像文件");
                } else {
                    getView().error("查询不到录像文件", null);
                }
                if (isDisposed()) {
                    dispose();
                }
            }

            @Override
            public void onComplete() {

            }

        });
    }

    public void getCloudBackFile(final long queryTime, final String deviceSerial, final int channelNo, final Calendar[] calendars) {
        Observable.create(new ObservableOnSubscribe<List<EZCloudRecordFile>>() {
            @Override
            public void subscribe(ObservableEmitter<List<EZCloudRecordFile>> emitter) throws Exception {
                emitter.onNext(CloudOpenSDK.getEZOpenSDK().searchRecordFileFromCloud(deviceSerial, channelNo, calendars[0], calendars[1]));
            }
        })
                .compose(RxUtils.<List<EZCloudRecordFile>>io2Main())
                .flatMap(new Function<List<EZCloudRecordFile>, ObservableSource<EZCloudRecordFile>>() {
                    @Override
                    public ObservableSource<EZCloudRecordFile> apply(List<EZCloudRecordFile> ezDeviceRecordFiles) throws Exception {
                        if (ezDeviceRecordFiles != null && ezDeviceRecordFiles.size() > 0) {
                            BackPlayPresenter.this.getView().getCloudBackSuccess(calendars, ezDeviceRecordFiles);
                            if (queryTime != 0) {
                                return BackPlayPresenter.this.queryCloud(true, queryTime, ezDeviceRecordFiles);
                            } else {
                                return Observable.just(ezDeviceRecordFiles.get(0));
                            }
                        }
                        throw new Exception("查询不到录像文件");
                    }
                }).subscribe(new DisposableObserver<EZCloudRecordFile>() {
            @Override
            public void onNext(EZCloudRecordFile ezCloudRecordFiles) {
                getView().queryCloudBackSuccess(true, ezCloudRecordFiles);
                if (isDisposed()) {
                    dispose();
                }
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                if (e instanceof BaseException) {
                    BaseException baseException = (BaseException) e;
                    getView().error(baseException.getMessage(), baseException.getErrorCode() + "");
                } else if (e instanceof NullPointerException) {
                    getView().queryCloudBackSuccess(true, null);
                } else if (e instanceof RuntimeException) {
                    getView().queryNull("查询不到录像文件");
                } else {
                    getView().error("查询不到录像文件", null);
                }
                if (isDisposed()) {
                    dispose();
                }
            }

            @Override
            public void onComplete() {

            }

        });
    }

    public void queryLocalBackFile(final long time, List<EZDeviceRecordFile> ezDeviceRecordFiles) {
        Observable.just(ezDeviceRecordFiles).flatMap(new Function<List<EZDeviceRecordFile>, ObservableSource<EZDeviceRecordFile>>() {
            @Override
            public ObservableSource<EZDeviceRecordFile> apply(List<EZDeviceRecordFile> list) throws Exception {
                return BackPlayPresenter.this.queryLocal(false, time, list);
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<EZDeviceRecordFile>() {
                    @Override
                    public void onNext(EZDeviceRecordFile ezDeviceRecordFile) {
                        getView().queryLocalBackSuccess(false, ezDeviceRecordFile);
                        if (isDisposed()) {
                            dispose();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        getView().queryLocalBackSuccess(false, null);
                        if (isDisposed()) {
                            dispose();
                        }
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void queryCloudBackFile(final long time, List<EZCloudRecordFile> ezCloudRecordFiles) {
        Observable.just(ezCloudRecordFiles).flatMap(new Function<List<EZCloudRecordFile>, ObservableSource<EZCloudRecordFile>>() {
            @Override
            public ObservableSource<EZCloudRecordFile> apply(List<EZCloudRecordFile> list) throws Exception {
                return BackPlayPresenter.this.queryCloud(false, time, list);
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<EZCloudRecordFile>() {
                    @Override
                    public void onNext(EZCloudRecordFile ezDeviceRecordFile) {
                        getView().queryCloudBackSuccess(false, ezDeviceRecordFile);
                        if (isDisposed()) {
                            dispose();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        getView().queryCloudBackSuccess(false, null);
                        if (isDisposed()) {
                            dispose();
                        }
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private ObservableSource<EZDeviceRecordFile> queryLocal(boolean needBefore, long queryTime, List<EZDeviceRecordFile> ezDeviceRecordFiles) {
        EZDeviceRecordFile queryFile = null;
        for (int i = 0; i < ezDeviceRecordFiles.size(); i++) {
            EZDeviceRecordFile ezDeviceRecordFile = ezDeviceRecordFiles.get(i);
            if (ezDeviceRecordFile.getStopTime().getTimeInMillis() >= queryTime && ezDeviceRecordFile.getStartTime().getTimeInMillis() <= queryTime) {
                queryFile = ezDeviceRecordFile;
                break;
            }
        }
        if (queryFile != null) {
            EZDeviceRecordFile ezDeviceRecordFile = new EZDeviceRecordFile();
            Calendar temp = Calendar.getInstance();
            if (needBefore) {
                if (queryTime - 10000 >= queryFile.getStartTime().getTimeInMillis()) {
                    temp.setTimeInMillis(queryTime - 10000);
                    ezDeviceRecordFile.setStartTime(temp);
                    ezDeviceRecordFile.setStopTime(queryFile.getStopTime());
                } else {
                    ezDeviceRecordFile.setStartTime(queryFile.getStartTime());
                    ezDeviceRecordFile.setStopTime(queryFile.getStopTime());
                }
            } else {
                temp.setTimeInMillis(queryTime);
                ezDeviceRecordFile.setStartTime(temp);
                ezDeviceRecordFile.setStopTime(queryFile.getStopTime());
            }
            return Observable.just(ezDeviceRecordFile);
        } else {
            throw new RuntimeException();
        }
    }

    private ObservableSource<EZCloudRecordFile> queryCloud(boolean needBefore, long queryTime, List<EZCloudRecordFile> ezDeviceRecordFiles) {
        EZCloudRecordFile queryFile = null;
        for (int i = 0; i < ezDeviceRecordFiles.size(); i++) {
            EZCloudRecordFile ezDeviceRecordFile = ezDeviceRecordFiles.get(i);
            if (ezDeviceRecordFile.getStopTime().getTimeInMillis() >= queryTime && ezDeviceRecordFile.getStartTime().getTimeInMillis() <= queryTime) {
                queryFile = ezDeviceRecordFile;
                break;
            }
        }
        if (queryFile != null) {
            EZCloudRecordFile ezDeviceRecordFile = new EZCloudRecordFile();
            ezDeviceRecordFile.setCoverPic(queryFile.getCoverPic());
            ezDeviceRecordFile.setDownloadPath(queryFile.getDownloadPath());
            ezDeviceRecordFile.setFileId(queryFile.getFileId());
            ezDeviceRecordFile.setEncryption(queryFile.getEncryption());
            //云存储类别:1 单文件存储模式；2 连续存储模式；3 待定（排查电信云端播放发现的问题修复）
            ezDeviceRecordFile.setiStorageVersion(queryFile.getiStorageVersion());
            //云存储录像类型，默认为0，表示不属于云存储录像
            ezDeviceRecordFile.setVideoType(queryFile.getVideoType());
            Calendar temp = Calendar.getInstance();
            if (needBefore) {
                if (queryTime - 10000 >= queryFile.getStartTime().getTimeInMillis()) {
                    temp.setTimeInMillis(queryTime - 10000);
                    ezDeviceRecordFile.setStartTime(temp);
                    ezDeviceRecordFile.setStopTime(queryFile.getStopTime());
                } else {
                    ezDeviceRecordFile.setStartTime(queryFile.getStartTime());
                    ezDeviceRecordFile.setStopTime(queryFile.getStopTime());
                }
            } else {
                temp.setTimeInMillis(queryTime);
                ezDeviceRecordFile.setStartTime(temp);
                ezDeviceRecordFile.setStopTime(queryFile.getStopTime());
            }
            return Observable.just(ezDeviceRecordFile);
        } else {
            throw new RuntimeException();
        }
    }

    @Override
    public void resume() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void destroy() {
    }
}

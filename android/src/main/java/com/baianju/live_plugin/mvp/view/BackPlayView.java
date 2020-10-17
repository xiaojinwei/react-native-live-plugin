package com.baianju.live_plugin.mvp.view;

import com.videogo.openapi.bean.EZCloudRecordFile;
import com.videogo.openapi.bean.EZDeviceRecordFile;

import java.util.Calendar;
import java.util.List;

/**
 * @author zhulongzhen
 * @date 2019/6/24
 * @desc
 */
public interface BackPlayView extends IView {
    void getLocalBackSuccess(Calendar[] time, List<EZDeviceRecordFile> ezDeviceRecordFiles);

    void getCloudBackSuccess(Calendar[] time, List<EZCloudRecordFile> ezCloudRecordFiles);

    void queryLocalBackSuccess(boolean needMove, EZDeviceRecordFile ezDeviceRecordFile);

    void queryCloudBackSuccess(boolean needMove, EZCloudRecordFile ezCloudRecordFile);

    void queryNull(String msg);

    void error(String msg, String code);
}

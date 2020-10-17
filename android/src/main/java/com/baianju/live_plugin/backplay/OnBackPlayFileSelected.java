package com.baianju.live_plugin.backplay;

import com.videogo.openapi.bean.EZCloudRecordFile;
import com.videogo.openapi.bean.EZDeviceRecordFile;

public interface OnBackPlayFileSelected {
    void onLocalBackFileSelected(EZDeviceRecordFile ezDeviceRecordFile);

    void onCloudBackFileSelected(EZCloudRecordFile ezCloudRecordFile);

    void onSelectOtherDay();

    void onClosePlayback();
}

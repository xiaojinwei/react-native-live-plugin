package com.baianju.live_plugin.backplay;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.baianju.live_plugin.R;
import com.baianju.live_plugin.mvp.presenter.BackPlayPresenter;
import com.baianju.live_plugin.mvp.view.BackPlayView;
import com.baianju.live_plugin.util.Const;
import com.baianju.live_plugin.widget.MyRadioButton;
import com.baianju.live_plugin.widget.timeRulerView.TimeRulerPickView;
import com.baianju.live_plugin.widget.timeRulerView.TimeRulerView;
import com.videogo.openapi.bean.EZCloudRecordFile;
import com.videogo.openapi.bean.EZDeviceRecordFile;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author zhulongzhen
 * @date 2019/6/24
 * @desc
 */
public class BackPlayFragment extends Fragment implements TimeRulerPickView.TimeRulerPickViewListener, BackPlayView {
    private static final String DEVICE_SERIAL = "DEVICE_SERIAL";
    private static final String CHANNEL_NO = "CHANNEL_NO";
    private static final String DATE = "DATE";

    private View mViewTop;
    private RadioGroup mGroupbox;
    private MyRadioButton mLocalBack, mCloudBack;
    private TimeRulerView mTimeRulerView;
    private ImageView mBeforeMonth, mNextMonth;
    private TextView mCalendarYearMonth;
    private CalendarView mSystemCalendarView;
    private FrameLayout mFlTimeRulerPick;
    private TimeRulerPickView mTimeRulerPickView;
    private ImageView mIvShowPick;
    private ImageView mClosePlayback;

    private String mDeviceSerial;
    private int mChannelNo;
    private long mDate = 0;

    OnBackPlayFileSelected callback;

    List<EZDeviceRecordFile> ezDeviceRecordFiles;
    List<EZCloudRecordFile> ezCloudRecordFiles;

    private BackPlayPresenter mBackPlayPresenter;


    public static BackPlayFragment newInstance(String deviceSerial, int channelNo, long date) {
        Bundle args = new Bundle();
        args.putString(DEVICE_SERIAL, deviceSerial);
        args.putInt(CHANNEL_NO, channelNo);
        args.putLong(DATE, date);
        BackPlayFragment fragment = new BackPlayFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(getLayoutRes(), container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews();
        initListeners();
        initData();
    }

    public <T extends View> T findViewById(int id) {
        if (null == getView()) {
            return null;
        }
        return getView().findViewById(id);
    }

    protected int getLayoutRes() {
        return R.layout.fragment_back_play;
    }

    protected void initViews() {

        mViewTop = findViewById(R.id.view_top);
        mGroupbox = findViewById(R.id.groupbox);
        mLocalBack = findViewById(R.id.local_back);
        mCloudBack = findViewById(R.id.cloud_back);
        mFlTimeRulerPick = findViewById(R.id.fl_timerulerpick);
        mTimeRulerPickView = findViewById(R.id.timeRulerPickView);
        mTimeRulerView = findViewById(R.id.timeRulerView);
        mIvShowPick = findViewById(R.id.iv_showpick);
        mNextMonth = findViewById(R.id.next_month);
        mBeforeMonth = findViewById(R.id.before_month);
        mCalendarYearMonth = findViewById(R.id.calendar_year_month);
        mSystemCalendarView = findViewById(R.id.system_calendarView);
        mClosePlayback = findViewById(R.id.close_playback);
    }


    protected void initData() {

        mBackPlayPresenter = new BackPlayPresenter();
        mBackPlayPresenter.setView(this);

        Bundle arguments = getArguments();
        //mDeviceSerial = ConfigCst.DEVICE_SERIAL;
        mDeviceSerial = arguments.getString(DEVICE_SERIAL);
        //mChannelNo = ConfigCst.DEVICE_CHANNEL_NO;
        mChannelNo = arguments.getInt(CHANNEL_NO);

        Date queryDate = new Date();
        if (mDate != 0) {
            mSystemCalendarView.setDate(mDate);
            queryDate.setTime(mDate);
        }
        if (callback != null) {
            callback.onSelectOtherDay();
        }
        mBackPlayPresenter.getLocalBackFile(mDate, mDeviceSerial, mChannelNo, getQueryDate(queryDate));
    }


    protected void initListeners() {

        mSystemCalendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, dayOfMonth);
                Date date = new Date();
                date.setTime(calendar.getTimeInMillis());
                if (callback != null) {
                    callback.onSelectOtherDay();
                }
                if (mTimeRulerView != null) {
                    mTimeRulerView.reset();
                }
                if (mLocalBack.isChecked()) {
                    mBackPlayPresenter.getLocalBackFile(0, mDeviceSerial, mChannelNo, BackPlayFragment.this.getQueryDate(date));
                } else {
                    mBackPlayPresenter.getCloudBackFile(0, mDeviceSerial, mChannelNo, BackPlayFragment.this.getQueryDate(date));
                }
            }
        });
        mGroupbox.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (mLocalBack.isChecked()) {
                    mBackPlayPresenter.getLocalBackFile(0, mDeviceSerial, mChannelNo, BackPlayFragment.this.getQueryDate(new Date()));
                } else {
                    mBackPlayPresenter.getCloudBackFile(0, mDeviceSerial, mChannelNo, BackPlayFragment.this.getQueryDate(new Date()));
                }
            }
        });
        mTimeRulerPickView.setTimeRulerPickViewListener(this);
        mTimeRulerView.setOnChooseTimeListener(new TimeRulerView.OnChooseTimeListener() {
            @Override
            public void onChooseTime(Calendar calendar) {
                if (mLocalBack.isChecked()) {
                    if (ezDeviceRecordFiles != null) {
                        mBackPlayPresenter.queryLocalBackFile(calendar.getTimeInMillis(), ezDeviceRecordFiles);
                    }
                } else {
                    if (ezCloudRecordFiles != null) {
                        mBackPlayPresenter.queryCloudBackFile(calendar.getTimeInMillis(), ezCloudRecordFiles);
                    }
                }
            }
        });
        mIvShowPick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFlTimeRulerPick.setVisibility(View.VISIBLE);
            }
        });
        mViewTop.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mFlTimeRulerPick.getVisibility() == View.VISIBLE) {
                    mFlTimeRulerPick.setVisibility(View.GONE);
                    return true;
                } else {
                    return false;
                }
            }
        });
        mClosePlayback.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                if (callback != null) {
                    callback.onClosePlayback();
                }
            }
        });
    }

    //处理查询时间
    private Calendar[] getQueryDate(Date queryDate) {
        Calendar startTime = Calendar.getInstance();
        Calendar endTime = Calendar.getInstance();
        startTime.setTime(queryDate);
        endTime.setTime(queryDate);
        startTime.set(Calendar.HOUR_OF_DAY, 0);
        startTime.set(Calendar.MINUTE, 0);
        startTime.set(Calendar.SECOND, 0);
        endTime.set(Calendar.HOUR_OF_DAY, 23);
        endTime.set(Calendar.MINUTE, 59);
        endTime.set(Calendar.SECOND, 59);
        return new Calendar[]{startTime, endTime};
    }

    @Override
    public void totalTimePerCellChanged(int totalTimePerCell) {
        //刻度选择回调
        mTimeRulerView.setTotalCellNum(totalTimePerCell);
    }

    @Override
    public void getLocalBackSuccess(Calendar[] time, List<EZDeviceRecordFile> ezDeviceRecordFiles) {
        this.ezDeviceRecordFiles = ezDeviceRecordFiles;
        ArrayList<TimeRulerView.TimeInfo> timeInfos = new ArrayList<>();
        for (EZDeviceRecordFile ezDeviceRecordFile : ezDeviceRecordFiles) {
            if (ezDeviceRecordFile.getStartTime().getTimeInMillis() < time[0].getTimeInMillis()) {
                ezDeviceRecordFile.setStartTime(time[0]);
            }
            if (ezDeviceRecordFile.getStopTime().getTimeInMillis() > time[1].getTimeInMillis()) {
                ezDeviceRecordFile.setStopTime(time[1]);
            }
            TimeRulerView.TimeInfo timeInfo = new TimeRulerView.TimeInfo(ezDeviceRecordFile.getStartTime(), ezDeviceRecordFile.getStopTime());
            timeInfos.add(timeInfo);
        }
        mTimeRulerView.setTimeInfos(timeInfos);
        mTimeRulerView.invalidate();
    }

    @Override
    public void getCloudBackSuccess(Calendar[] time, List<EZCloudRecordFile> ezCloudRecordFiles) {
        this.ezCloudRecordFiles = ezCloudRecordFiles;
        ArrayList<TimeRulerView.TimeInfo> timeInfos = new ArrayList<>();
        for (EZCloudRecordFile ezCloudRecordFile : ezCloudRecordFiles) {
            if (ezCloudRecordFile.getStartTime().getTimeInMillis() < time[0].getTimeInMillis()) {
                ezCloudRecordFile.setStartTime(time[0]);
            }
            if (ezCloudRecordFile.getStopTime().getTimeInMillis() > time[1].getTimeInMillis()) {
                ezCloudRecordFile.setStopTime(time[1]);
            }
            TimeRulerView.TimeInfo timeInfo = new TimeRulerView.TimeInfo(ezCloudRecordFile.getStartTime(), ezCloudRecordFile.getStopTime());
            timeInfos.add(timeInfo);
        }
        mTimeRulerView.setTimeInfos(timeInfos);
        mTimeRulerView.invalidate();
    }

    @Override
    public void queryLocalBackSuccess(boolean needMove, EZDeviceRecordFile ezDeviceRecordFile) {
        if (ezDeviceRecordFile == null) {
            if (callback != null) {
                callback.onSelectOtherDay();
            }
            toast("查询不到录像文件");
        } else {
            if (needMove) {
                mTimeRulerView.setTime(ezDeviceRecordFile.getStartTime());
                mTimeRulerView.invalidate();
            }
            if (callback != null) {
                callback.onLocalBackFileSelected(ezDeviceRecordFile);
            }
        }
    }

    @Override
    public void queryCloudBackSuccess(boolean needMove, EZCloudRecordFile ezCloudRecordFile) {
        if (ezCloudRecordFile == null) {
            if (callback != null) {
                callback.onSelectOtherDay();
            }
            toast("查询不到录像文件");
        } else {
            if (needMove) {
                mTimeRulerView.setTime(ezCloudRecordFile.getStartTime());
                mTimeRulerView.invalidate();
            }
            if (callback != null) {
                callback.onCloudBackFileSelected(ezCloudRecordFile);
            }
        }
    }

    @Override
    public void queryNull(String msg) {
        if (callback != null) {
            callback.onSelectOtherDay();
        }
        toast(msg);
    }

    @Override
    public void error(String msg, String code) {
        if (callback != null) {
            callback.onSelectOtherDay();
        }
        Calendar temp = Calendar.getInstance();
        temp.set(temp.get(Calendar.YEAR), temp.get(Calendar.MONTH), temp.get(Calendar.DAY_OF_MONTH), 0, 0, 1);
        mTimeRulerView.setTime(temp);
        mTimeRulerView.setTimeInfos(null);
        mTimeRulerView.invalidate();
        toast(msg);
    }

    public void setRulerTime(Calendar calendar) {
        if (!mTimeRulerView.isMoving()) {
            mTimeRulerView.setTime(calendar);
            mTimeRulerView.invalidate();
        }
    }

    /**
     * 解禁还是禁用
     *
     * @param enabled True if this view is enabled, false otherwise.
     */
    public void setTimeRulerViewEnabled(boolean enabled) {
        if (null != mTimeRulerView) {
            mTimeRulerView.setEnabled(enabled);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (getActivity() instanceof OnBackPlayFileSelected) {
            callback = (OnBackPlayFileSelected) getActivity();
        }
    }

    @Override
    public void showLoading() {

    }

    @Override
    public void hideLoading() {

    }

    public void toast(String msg){
        Toast.makeText(getActivity(),msg,Toast.LENGTH_SHORT).show();
    }
}

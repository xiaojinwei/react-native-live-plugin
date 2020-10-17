package com.baianju.live_plugin.widget.timeRulerView;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.baianju.live_plugin.R;


/**
 * 时间刻度选择
 */
public class TimeRulerPickView extends View {

    private float maxTotalTimePerCell;
    private float minTotalTimePerCell;
    private float degree;
    private float currentTotalTimePerCell;
    private float downY;
    private float touchY;
    private float bitmapPosition;//图片top位置
    private RectF clickRecF;
    private float width;
    private float height;

    private Bitmap bitmap;
    private boolean isDownRight = false;
    private Paint bitmapPaint;

    public TimeRulerPickView(Context context) {
        this(context, null);
    }

    public TimeRulerPickView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimeRulerPickView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.TimeRulerPickView);
        initDefaultValue(typedArray);
        init(context);
    }

    private void initDefaultValue(@NonNull TypedArray typedArray) {
        maxTotalTimePerCell = typedArray.getFloat(R.styleable.TimeRulerPickView_maxTotalTimePerCell, 288);
        minTotalTimePerCell = typedArray.getFloat(R.styleable.TimeRulerPickView_minTotalTimePerCell, 24);
    }

    private void init(Context context) {
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.timerulerview_move);
        width = getResources().getDimension(R.dimen.timerulerpick_width);
        height = getResources().getDimension(R.dimen.timerulerpick_height);

        bitmapPaint = new Paint();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (clickRecF == null) {
            clickRecF = new RectF();
            bitmapPosition = height / 2.0f - bitmap.getHeight() / 2.0f;
            clickRecF.left = 0;
            clickRecF.right = bitmap.getWidth();
            clickRecF.top = bitmapPosition;
            clickRecF.bottom = bitmapPosition + bitmap.getHeight();

            degree = (maxTotalTimePerCell - minTotalTimePerCell) / (height - bitmap.getHeight());
            currentTotalTimePerCell = maxTotalTimePerCell - degree * bitmapPosition;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                clickRecF.top = bitmapPosition;
                clickRecF.bottom = bitmapPosition + bitmap.getHeight();
                if (clickRecF.contains(event.getX(), event.getY())) {
                    isDownRight = true;
                }
                downY = event.getY();
                return true;
            case MotionEvent.ACTION_MOVE:
                if (isDownRight) {
                    touchY = event.getY();
                    float diff = touchY - downY;
                    bitmapPosition += diff;
                    if (bitmapPosition < 0) {
                        bitmapPosition = 0;
                    } else if (bitmapPosition > (height - bitmap.getHeight())) {
                        bitmapPosition = height - bitmap.getHeight();
                    }
                    currentTotalTimePerCell = maxTotalTimePerCell - degree * bitmapPosition;
                    if (timeRulerPickViewListener != null) {
                        timeRulerPickViewListener.totalTimePerCellChanged((int) currentTotalTimePerCell);
                    }
                    invalidate();
                    downY = touchY;
                }
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isDownRight = false;
                //传递最后的值
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(bitmap, width / 2.0f - bitmap.getWidth() / 2.0f, bitmapPosition, bitmapPaint);
    }

    private TimeRulerPickViewListener timeRulerPickViewListener;

    public void setTimeRulerPickViewListener(TimeRulerPickViewListener timeRulerPickViewListener) {
        this.timeRulerPickViewListener = timeRulerPickViewListener;
    }

    public interface TimeRulerPickViewListener {
        void totalTimePerCellChanged(int totalTimePerCell);
    }
}

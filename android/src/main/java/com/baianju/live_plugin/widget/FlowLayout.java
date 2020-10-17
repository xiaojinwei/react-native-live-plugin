package com.baianju.live_plugin.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;


import com.baianju.live_plugin.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 流式布局
 */
public class FlowLayout extends ViewGroup {

    public static final int MAX_LINES_COUNT = 100;

    /**
     * 行对象
     */
    private Line mLine;
    /**
     * 已使用的宽度
     */
    private int usedWidth;
    /**
     * 水平间距
     */
    private int mHorizontalSpacing = 6;
    /**
     * 垂直间隙
     */
    private int mVerticalSpacing = 6;
    /**
     * 保存行的集合
     */
    private List<Line> lineList = new ArrayList<Line>();

    /**
     * 是否平分空余的部分
     */
    private boolean surplus;

    private int maxLine;

    public void setSurplus(boolean surplus) {
        this.surplus = surplus;
    }

    public void setMaxLine(int maxLine){
        this.maxLine = maxLine;
    }

    public FlowLayout(Context context) {
        this(context,null);
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public FlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FlowLayout);
        surplus = a.getBoolean(R.styleable.FlowLayout_surplus, false);
        maxLine = a.getInteger(R.styleable.FlowLayout_maxLine,MAX_LINES_COUNT);
        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        //获取当前控件的测量模式
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        //获取当前控件的测量尺寸
        int widthSize = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();
        int heightSize = MeasureSpec.getSize(heightMeasureSpec) - getPaddingTop() - getPaddingBottom();

        //清空数据
        restore();
        //获取当前控件所有字view的个数
        int childCount = getChildCount();
        //遍历获取所有子view
        for (int i = 0; i < childCount; i++) {
            //获取当前的子view
            View childView = getChildAt(i);
            if (childView.getVisibility() == GONE) {
                continue;
            }
            //测量子view 规范子view的大小,不让他超过父view的大小
            int childWidthSpec = MeasureSpec.makeMeasureSpec(widthSize, widthMode == MeasureSpec.EXACTLY ? MeasureSpec.AT_MOST : widthMode);
            int childHeightSpec = MeasureSpec.makeMeasureSpec(heightSize, heightMode == MeasureSpec.EXACTLY ? MeasureSpec.AT_MOST : heightMode);
            childView.measure(childWidthSpec, childHeightSpec);
            //创建行对象
            if (mLine == null) {
                mLine = new Line();
            }

            int childWidth = childView.getMeasuredWidth();
            usedWidth += childWidth;
            if (usedWidth <= widthSize) {
                mLine.addView(childView);
                usedWidth += mHorizontalSpacing;
                //对应的换行情况是第二种
                if (usedWidth >= widthSize) {
                    if (!newLine()) {
                        break;
                    }
                }
            } else {
                //对应的换行的第一种情况
                if (!newLine()) {
                    break;
                }
                mLine.addView(childView);
                usedWidth += childWidth + mHorizontalSpacing;
            }


        }
        //将最后一行添加到行集合中
        if (mLine != null && mLine.getViewCount() > 0 && !lineList.contains(mLine)) {
            lineList.add(mLine);
        }
        //flowLayout的宽
        int flowLayoutWidth = MeasureSpec.getSize(widthMeasureSpec);
        //当前控件行高的总和
        int totalLineHeight = 0;
        for (int i = 0; i < lineList.size(); i++) {
            //行高总和
            totalLineHeight += lineList.get(i).totalHeight;
        }
        //flowLayout的高
        int flowLayoutHeight = childCount == 0 ? 0 : totalLineHeight + (lineList.size() - 1) * mVerticalSpacing + getPaddingTop() + getPaddingBottom();
        setMeasuredDimension(flowLayoutWidth, flowLayoutHeight);
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * 清空数据的方法
     */
    private void restore() {
        lineList.clear();
        mLine = new Line();
        usedWidth=0;
    }

    /**
     * 创建一个新行
     *
     * @return
     */
    private boolean newLine() {
        lineList.add(mLine);
        if (lineList.size() < maxLine) {
            mLine = new Line();
            usedWidth = 0;
            return true;
        }

        return false;
    }

    /**
     * 布局每一行的位置
     *
     * @param changed
     * @param l
     * @param t
     * @param r
     * @param b
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int left = getPaddingLeft();
        int top = getPaddingTop();
        for (int i = 0; i < lineList.size(); i++) {
            Line line = lineList.get(i);
            line.layout(left, top);
            top += line.totalHeight + mVerticalSpacing;
        }


    }

    /**
     * 行对象
     */
    class Line {
        /**
         * 记录每一行view的集合
         */
        private List<View> viewList = new ArrayList<View>();
        /**
         * 行高
         */
        private int totalHeight;
        /**
         * 当前行控件宽度的和
         */
        private int totalLineWidth;

        /**
         * 往当前行添加子view的方法
         *
         * @param view
         */
        private void addView(View view) {
            viewList.add(view);
            //获取当前行的行高
            int viewHeight = view.getMeasuredHeight();
            totalHeight = Math.max(viewHeight, totalHeight);
            //获取当前行每一个控件的宽度
            int viewWidth = view.getMeasuredWidth();
            totalLineWidth += viewWidth;
        }

        /**
         * 获取当前行中有多少个子view
         *
         * @return
         */
        private int getViewCount() {
            return viewList.size();
        }

        /**
         * 确定当前行中所有子view的位置
         *
         * @param left
         * @param top
         */
        public void layout(int left, int top) {
            //1.处理水平留白区域
            int layoutWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
            //水平留白区域
            int surplusWidth = layoutWidth - totalLineWidth - (getViewCount() - 1) * mHorizontalSpacing;
            //将水平留白区域平均分配跟当前行的每一个控件
            int oneSurplusWidth =surplusWidth/getViewCount();
            if(oneSurplusWidth>=0){
                for (int i=0;i<viewList.size();i++){
                    View view = viewList.get(i);
                    int viewWidth = view.getMeasuredWidth() + (surplus ? oneSurplusWidth : 0);
                    int viewheight = view.getMeasuredHeight();

                    int viewWidthSec =MeasureSpec.makeMeasureSpec(viewWidth, MeasureSpec.EXACTLY);
                    int viewHeightSec =MeasureSpec.makeMeasureSpec(viewheight,MeasureSpec.EXACTLY);
                    view.measure(viewWidthSec,viewHeightSec);
                    //解决细节2,获取让当前控件垂直居中的top
                    int childTop = (totalHeight-viewheight)/2;
                    //布局每一个子view的位置
                    view.layout(left,top+childTop,left+view.getMeasuredWidth(),top+childTop+viewheight);
                    left+=view.getMeasuredWidth()+mHorizontalSpacing;

                }
            }


        }
    }
}


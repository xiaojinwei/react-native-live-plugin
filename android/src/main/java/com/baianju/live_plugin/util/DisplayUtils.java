package com.baianju.live_plugin.util;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;

/**
 * @author zhulongzhen
 * @date 2019/6/20
 * @desc
 */
public class DisplayUtils {


    public static int dp2px(Context context, int dpValue) {
        return (int) (context.getResources().getDisplayMetrics().density * dpValue + 0.5f);
    }

    public static int px2dp(Context context, int pxVal) {
        return (int) (pxVal / getDisplayMetrics(context).density + 0.5f);
    }

    public static int sp2px(Context context, int spVal) {
        return (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spVal, getDisplayMetrics(context)) + 0.5f);
    }

    public static int getScreenHeight(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        return dm.heightPixels;
    }

    public static int getScreenWidth(Context context) {
//        WindowManager wm= (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
//        return wm.getDefaultDisplay().getWidth();
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    public static DisplayMetrics getDisplayMetrics(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        return dm;
    }

    public static void hideNavKey(Activity activity) {
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) {
            View v = activity.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            View decorView = activity.getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    public static void showNavKey(Activity activity, int systemUiVisibility) {
        activity.getWindow().getDecorView().setSystemUiVisibility(systemUiVisibility);
    }
}

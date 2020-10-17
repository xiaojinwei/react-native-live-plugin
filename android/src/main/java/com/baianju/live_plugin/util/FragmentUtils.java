package com.baianju.live_plugin.util;


import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

/**
 * Created by Amosnail on 2015/8/19.
 */
public class FragmentUtils {


    public static void showFragment(final FragmentManager fragmentManager, final Fragment fragment,
                                    final int containerViewId) {
        fragmentManager.beginTransaction()
                .replace(containerViewId, fragment)
                .commit();
    }


    public static void showLossFragment(final FragmentManager fragmentManager, final Fragment fragment,
                                        final int containerViewId) {
        fragmentManager.beginTransaction()
                .replace(containerViewId, fragment)
                .commitAllowingStateLoss();
    }

    public static void showFragment(final FragmentManager fragmentManager, final Fragment fragment,
                                    final int containerViewId, final String backStackArg) {
        fragmentManager.beginTransaction()
                .replace(containerViewId, fragment)
                .addToBackStack(backStackArg)   //transition
                .commit();
    }


    /**
     * 移除Fragment
     */
    public static void removeFragment(final FragmentManager fragmentManager, final Fragment fragment) {
        fragmentManager.beginTransaction().remove(fragment).commit();
    }

    /**
     * 隐藏Fragment
     *
     * @param fragmentManager
     * @param fragment
     */
    public static void hideFragment(final FragmentManager fragmentManager, final Fragment fragment) {
        fragmentManager.beginTransaction().hide(fragment).commit();
    }

    /**
     * 显示Fragment  ---->和隐藏相对
     *
     * @param fragmentManager
     * @param fragment
     * @return
     */
    public static void disPlayFragment(final FragmentManager fragmentManager, final Fragment fragment) {
        fragmentManager.beginTransaction().show(fragment).commit();
    }

    public static boolean hasFragment(final FragmentManager fragmentManager, final int fragmentId) {
        return null != fragmentManager.findFragmentById(fragmentId);
    }

    public static boolean popEntireFragmentBackStack(final FragmentManager fragmentManager) {
        final int backStackCount = fragmentManager.getBackStackEntryCount();
        for (int i = 0; i < backStackCount; i++) {
            fragmentManager.popBackStack();
        }
        return backStackCount > 0;
    }
}

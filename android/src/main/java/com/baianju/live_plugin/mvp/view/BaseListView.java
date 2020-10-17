package com.baianju.live_plugin.mvp.view;

import java.util.List;

/**
 * Created by amosnail on 2015/6/18.
 */
public interface BaseListView<T> extends IView {
    void renderData(final List<T> data);

    void renderDataFromLocal(final List<T> data);
}

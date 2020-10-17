package com.baianju.live_plugin.mvp.presenter;


import com.baianju.live_plugin.mvp.view.IView;

/**
 * abstract presenter
 * <p/>
 * Created by Amosnail on 2015/5/26.
 */
public abstract class BasePresenter<T extends IView> {
    private T view;

    public void setView(T view) {
        this.view = view;
    }

    public T getView() {
        return view;
    }

    public abstract void initialize();

    public abstract void resume();

    public abstract void pause();

    public abstract void destroy();
}

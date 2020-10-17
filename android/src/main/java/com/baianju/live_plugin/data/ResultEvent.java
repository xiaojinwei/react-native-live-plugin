package com.baianju.live_plugin.data;

import com.google.gson.Gson;

public
class ResultEvent {
    public int what;//哪个按钮事件
    public Object data;//数据

    public ResultEvent(int what, Object data) {
        this.what = what;
        this.data = data;
    }

    public static ResultEvent newEvent(int what, Object data){
        return new ResultEvent(what,data);
    }

    public String toJson(){
        return new Gson().toJson(this);
    }
}

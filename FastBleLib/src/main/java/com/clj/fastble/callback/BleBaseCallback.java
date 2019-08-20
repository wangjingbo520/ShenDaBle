package com.clj.fastble.callback;


import android.os.Handler;

public abstract class BleBaseCallback {

    private String strMac;
    private String key;
    private Handler handler;

    public String getMac() {
        return strMac;
    }

    public void setMac(String _Mac) {
        this.strMac = _Mac;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Handler getHandler() {
        return handler;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

}

package com.clj.fastble.callback;


import com.clj.fastble.exception.BleException;

public abstract class BleNotifyCallback extends BleBaseCallback {

    public abstract void onNotifySuccess(String strMac);

    public abstract void onNotifyFailure(String strMac, BleException exception);

    public abstract void onCharacteristicChanged(String strMac, byte[] data);

}

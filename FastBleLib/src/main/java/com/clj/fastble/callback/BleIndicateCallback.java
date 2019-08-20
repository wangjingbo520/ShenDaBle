package com.clj.fastble.callback;


import com.clj.fastble.exception.BleException;

public abstract class BleIndicateCallback extends BleBaseCallback{

    public abstract void onIndicateSuccess(String strMac);

    public abstract void onIndicateFailure(String strMac, BleException exception);

    public abstract void onCharacteristicChanged(String strMac, byte[] data);
}

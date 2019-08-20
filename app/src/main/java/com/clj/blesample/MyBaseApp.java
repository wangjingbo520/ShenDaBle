package com.clj.blesample;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.SystemClock;

/**
 * Created by Administrator on 2017/8/6.
 */

public class MyBaseApp extends Application {

    public static final String TAG = MyBaseApp.class.getName();;

    public static Context application;


    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        application = getApplicationContext();

        //注册全局异常处理
        BaseUncaughtExceptionHandler catchExcep = new BaseUncaughtExceptionHandler(this);
        Thread.setDefaultUncaughtExceptionHandler(catchExcep);

    }

    public static void init(){

    }


}

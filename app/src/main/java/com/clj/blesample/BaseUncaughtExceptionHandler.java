package com.clj.blesample;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Looper;
import android.util.Base64;
import android.widget.Toast;

import com.clj.blesample.util.LogUtil;

import org.json.JSONObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Administrator on 2017/10/25.
 */

public class BaseUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

    public final static String TAG = BaseUncaughtExceptionHandler.class.getName();

    private Thread.UncaughtExceptionHandler mDefaultHandler;

    /**
     * 存储设备信息和异常信息
     **/
    private Map<String, String> mInfos = new HashMap<String, String>();

    MyBaseApp application;

    public BaseUncaughtExceptionHandler(MyBaseApp application) {
        //获取系统默认的UncaughtException处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        this.application = application;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (!handleException(ex) && mDefaultHandler != null) {
            //如果用户没有处理则让系统默认的异常处理器来处理
            mDefaultHandler.uncaughtException(thread, ex);
        } else {


            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                LogUtil.log(TAG, "[" + TAG + "]  sleep " + e.toString());
            }

            //完成后，设置安装完成状态
            Intent intent = new Intent(application.getApplicationContext(), MyBaseApp.class);
            PendingIntent restartIntent = PendingIntent.getActivity(application.getApplicationContext(), 0, intent, Intent.FLAG_ACTIVITY_NEW_TASK);
            //退出程序
            AlarmManager mgr = (AlarmManager) application.getSystemService(Context.ALARM_SERVICE);
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, restartIntent); // 1秒钟后重启应用

            android.os.Process.killProcess(android.os.Process.myPid());

            LogUtil.log(TAG, "[" + TAG + "]" + "app_UpLoadCenterRun  killProcess");
        }
    }

    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
     *
     * @param ex
     * @return true:如果处理了该异常信息;否则返回false.
     */
    private boolean handleException(Throwable ex) {
        if (ex == null) {
            return false;
        }
        //使用Toast来显示异常信息
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                Toast.makeText(application.getApplicationContext(), "很抱歉,程序出现异常,即将退出.",
                        Toast.LENGTH_LONG).show();
                Looper.loop();
            }
        }.start();


        // 5.3 保存log和crash到文件
        saveLogAndCrash(ex);
        // 5.4 发送log和crash到服务器
        //sendLogAndCrash();


        return true;
    }


    /**
     * 5.3 保存log和crash到文件
     *
     * @param ex
     */
    protected void saveLogAndCrash(Throwable ex) {
        LogUtil.log(TAG, "[" + TAG + "]  saveLogAndCrash ");

        StringBuffer sb = new StringBuffer();
        //sb.append("[DateTime: " + DateUtil.date2String(new Date()) + "]\n");
        // 遍历infos
        for (Map.Entry<String, String> entry : mInfos.entrySet()) {
            String key = entry.getKey().toLowerCase(Locale.getDefault());
            String value = entry.getValue();
            sb.append("  " + key + ": " + value + "\n");
        }
        // 将错误手机到writer中
        Writer writer = new StringWriter();
        PrintWriter pw = new PrintWriter(writer);
        ex.printStackTrace(pw);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(pw);
            cause = cause.getCause();
        }
        pw.close();
        String result = writer.toString();
        sb.append("[Excetpion: ]\n");
        sb.append(result);

        // 将异常写入日志文件
        //log.error(result);

        LogUtil.log(TAG, "[" + TAG + "]  saveLogAndCrash " + sb.toString());

        try {
            if (sb!=null) {
                if (sb.toString().length()>0) {

                }
            }
        }
        catch(Exception e)
        {
            LogUtil.log(TAG, "[" + TAG + "]" + "saveLogAndCrash app_ExceptionUpLoadRun " + e.toString());
        }
    }
}

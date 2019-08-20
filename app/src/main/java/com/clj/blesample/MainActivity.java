package com.clj.blesample;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.clj.blesample.DbHelper.BleLogsItem;
import com.clj.blesample.DbHelper.BleLogsItemDao;
import com.clj.blesample.DbHelper.BleSaveItem;
import com.clj.blesample.DbHelper.BleSaveItemDao;
import com.clj.blesample.adapter.DeviceAdapter;
import com.clj.blesample.comm.ObserverManager;
import com.clj.blesample.filedirchoose.ChooseFileActivity;
import com.clj.blesample.operation.OperationActivity;
import com.clj.blesample.util.LogUtil;
import com.clj.blesample.util.StringUtil;
import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleMtuChangedCallback;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleRssiCallback;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.scan.BleScanRuleConfig;
import com.clj.fastble.utils.HexUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getName();
    private static final int REQUEST_CODE_OPEN_GPS = 1;
    private static final int REQUEST_CODE_PERMISSION_LOCATION = 2;

    private Button btn_device_status;
    private Button btn_delete_data;
    private Button btn_end_uploading;

    private Button btn_start_uploading;
    private Button btn_end_collection;
    private Button btn_start_collection;

    private Button btn_start_search;
    private Button btn_show_pin;
    private Button btn_clean_screen;

    private Button btn_select_dev;
    private Button btn_select_dir;
    private Button btn_start_save;

    private AlertDialog alertDialog2;

    private ImageView img_loading;

    private Animation operatingAnim;
    private DeviceAdapter mDeviceAdapter;
    private ProgressDialog progressDialog;

    private ListView listView_device;
    private TextView text_logs;
    private ScrollView scrollView_log;

    private SimpleDateFormat LogSdf = new SimpleDateFormat("HH:mm:ss");
    private String macFilter ="";

    private Hashtable<String, String> NotifyMsg=new Hashtable<>();

    public static int bolSaveType=0;
    public static String strSelectMac="";
    public static String strFileName="test.data";
    public static String strdirName="/storage/emulated/0/";

    public  EditText edtxt_filename;
    public EditText edtxt_select_dir;

    public int iMaxDataLen;
    public int iMaxDataCount;

    public int iDataLen;
    public int iDataCount;
    public int iCurrCount;
    public int iMinCount;
    public long lgCurrTime;

    public static final int PATHREQUESTCODE = 44;

        Handler handler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            //设置进度条当前值，并将线程对象放入线程队列
            LogUtil.log(TAG, "[" + TAG + "]  handleMessage" + msg.arg1 );
            LogUtil.log(TAG, "[" + TAG + "]  handleMessage" + msg.arg2 );

            if(msg.arg1==0)
            {
                progressDialog.setMax(iMaxDataLen);
                progressDialog.setProgress(iMaxDataLen - iDataLen);

            }
        }

    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

        BleManager.getInstance().init(getApplication());
        BleManager.getInstance()
                .enableLog(true)
                .setReConnectCount(1, 5000)
                .setConnectOverTime(20000)
                .setOperateTimeout(5000);

        LogUtil.log(TAG, "[" + TAG + "] onCreate ");
    }

    @Override
    protected void onResume() {
        super.onResume();
        showConnectedDevice();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BleManager.getInstance().disconnectAllDevice();
        BleManager.getInstance().destroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start_search:
                macFilter = "";
                if (btn_start_search.getText().equals(getString(R.string.start_search))) {
                    checkPermissions();
                    listView_device.setVisibility(View.VISIBLE);
                    text_logs.setVisibility(View.GONE);
                    scrollView_log.setVisibility(View.GONE);
                } else if (btn_start_search.getText().equals(getString(R.string.end_search))) {
                    BleManager.getInstance().cancelScan();
                }
                break;
            case R.id.btn_clean_screen:
                LogUtil.log(TAG, "[" + TAG + "] btn_clean_screen ");
                listView_device.setVisibility(View.VISIBLE);
                text_logs.setVisibility(View.GONE);
                scrollView_log.setVisibility(View.GONE);
                //notifyConnectedDevice();
                BleLogsItemDao _BleLogsItemDao = new BleLogsItemDao(MainActivity.this);
                _BleLogsItemDao.DeleteAll();
                showLogs();
                break;
            case R.id.btn_show_pin:
                LogUtil.log(TAG, "[" + TAG + "] btn_show_pin ");
                if (bolSaveType==1)
                {
                    Toast.makeText(MainActivity.this, "正在保存数据！", Toast.LENGTH_SHORT).show();
                    return;
                }
                sendConnectedDevice("aafb");
                listView_device.setVisibility(View.GONE);
                text_logs.setVisibility(View.VISIBLE);
                scrollView_log.setVisibility(View.VISIBLE);
                break;

            case R.id.btn_start_collection:
                LogUtil.log(TAG, "[" + TAG + "] btn_show_mac ");
                if (bolSaveType==1)
                {
                    Toast.makeText(MainActivity.this, "正在保存数据！", Toast.LENGTH_SHORT).show();
                    return;
                }
                bolSaveType =2;
                sendConnectedDevice("aa01");
                listView_device.setVisibility(View.GONE);
                text_logs.setVisibility(View.VISIBLE);
                scrollView_log.setVisibility(View.VISIBLE);
                break;

            case R.id.btn_end_collection:
                LogUtil.log(TAG, "[" + TAG + "] btn_show_mac ");
                if (bolSaveType==1)
                {
                    Toast.makeText(MainActivity.this, "正在保存数据！", Toast.LENGTH_SHORT).show();
                    return;
                }
                bolSaveType=0;
                sendConnectedDevice("aa02");
                listView_device.setVisibility(View.GONE);
                text_logs.setVisibility(View.VISIBLE);
                scrollView_log.setVisibility(View.VISIBLE);
                break;

            case R.id.btn_start_uploading:
                LogUtil.log(TAG, "[" + TAG + "] btn_start_uploading ");
                if (bolSaveType==1)
                {
                    Toast.makeText(MainActivity.this, "正在保存数据！", Toast.LENGTH_SHORT).show();
                    return;
                }
                bolSaveType=2;
                sendConnectedDevice("aa03");
                listView_device.setVisibility(View.GONE);
                text_logs.setVisibility(View.VISIBLE);
                scrollView_log.setVisibility(View.VISIBLE);
                break;

            case R.id.btn_end_uploading:
                LogUtil.log(TAG, "[" + TAG + "] btn_end_uploading ");
                if (bolSaveType==1)
                {
                    Toast.makeText(MainActivity.this, "正在保存数据！", Toast.LENGTH_SHORT).show();
                    return;
                }
                bolSaveType=0;
                sendConnectedDevice("aa04");
                listView_device.setVisibility(View.GONE);
                text_logs.setVisibility(View.VISIBLE);
                scrollView_log.setVisibility(View.VISIBLE);
                break;

            case R.id.btn_delete_data:
                LogUtil.log(TAG, "[" + TAG + "] btn_delete_data ");
                if (bolSaveType==1)
                {
                    Toast.makeText(MainActivity.this, "正在保存数据！", Toast.LENGTH_SHORT).show();
                    return;
                }
                //sendConnectedDevice("aafd");
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.notifyTitle))
                        .setMessage(getString(R.string.delNotifyMsg))
                        .setNegativeButton(getString(R.string.cancel),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                        .setPositiveButton(getString(R.string.confirm),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        sendConnectedDevice("aafd");
                                    }
                                })

                        .setCancelable(false)
                        .show();
                listView_device.setVisibility(View.GONE);
                text_logs.setVisibility(View.VISIBLE);
                scrollView_log.setVisibility(View.VISIBLE);
                break;

            case R.id.btn_device_status:
                LogUtil.log(TAG, "[" + TAG + "] btn_device_status ");
                if (bolSaveType==1)
                {
                    Toast.makeText(MainActivity.this, "正在保存数据！", Toast.LENGTH_SHORT).show();
                    return;
                }
                sendConnectedDevice("aafc");
                listView_device.setVisibility(View.GONE);
                text_logs.setVisibility(View.VISIBLE);
                scrollView_log.setVisibility(View.VISIBLE);
                break;

            case R.id.btn_select_dev:
                LogUtil.log(TAG, "[" + TAG + "] btn_select_dev ");
                ArrayList<String> list = new ArrayList<String>();
                List<BleDevice> deviceList = BleManager.getInstance().getAllConnectedDevice();
                for (BleDevice bleDevice : deviceList) {
                    LogUtil.log(TAG, "[" + TAG + "] bleDevice " + bleDevice.getMac());
                    Collections.addAll(list, bleDevice.getMac());
                }
                //final String[] items = {"设备1", "设备2", "设备3", "设备4"};
                final String[] items = list.toArray(new String[list.size()]);
                if (items.length > 0) {
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
                    alertBuilder.setTitle("选择设备");
                    alertBuilder.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //Toast.makeText(MainActivity.this, items[i], Toast.LENGTH_SHORT).show();
                            strSelectMac = items[i];
                            Toast.makeText(MainActivity.this, "选择的是：" + items[i], Toast.LENGTH_SHORT).show();
                            LogUtil.log(TAG, "[" + TAG + "] bleDevice " + items[i]);
                        }
                    });

                    alertBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            alertDialog2.dismiss();
                        }
                    });

                    alertBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            alertDialog2.dismiss();
                        }
                    });
                    alertDialog2 = alertBuilder.create();
                    alertDialog2.show();
                } else {
                    Toast.makeText(MainActivity.this, "未连接到任何设备", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_select_dir:
                LogUtil.log(TAG, "[" + TAG + "] btn_select_dir ");

                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                dialog.setTitle("选择目录");

                // 取得自定义View
                LayoutInflater layoutInflater = LayoutInflater.from(this);
                final View myLoginView = layoutInflater.inflate(
                        R.layout.layout_select_dir, null);
                dialog.setView(myLoginView);

                edtxt_filename  = myLoginView.findViewById(R.id.edtxt_filename);
                edtxt_select_dir = myLoginView.findViewById(R.id.edtxt_select_dir);
                edtxt_select_dir.setInputType(InputType.TYPE_NULL);
                // 设置颜色
                edtxt_select_dir.setTextColor(Color.GRAY);
                dialog.setNegativeButton("取消",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });

                dialog.setPositiveButton("确定",null);
                /*
                dialog.setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //EditText loginAccountEt = (EditText) myLoginView
                                //        .findViewById(R.id.my_login_account_et);
                                //EditText loginPasswordEt = (EditText) myLoginView
                                //        .findViewById(R.id.my_login_password_et);
                                //Log.d("MyLogin Dialog", "输入的用户名是："
                                //        + loginAccountEt.getText().toString());
                                //Log.d("MyLogin Dialog", "输入的密码是："
                                //        + loginPasswordEt.getText().toString());
                                if (edtxt_filename.getText().toString().trim().equalsIgnoreCase(""))
                                {

                                }

                                if (edtxt_select_dir.getText().toString().trim().equalsIgnoreCase(""))
                                {

                                }

                            }
                        });
                */

                //dialog.setCancelable(false);
                //dialog.show();
                //创建dialog
                final AlertDialog pwdDialog=dialog.create();
                //dialog点击其他地方不关闭
                pwdDialog.setCancelable(false);
                pwdDialog.show();
                //创建dialog点击监听OnClickListener
                pwdDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //如果想关闭dialog直接加上下面这句代码就行
                        //pwdDialog.cancel();
                        if (edtxt_filename.getText().toString().trim().equalsIgnoreCase(""))
                        {
                            Toast.makeText(MainActivity.this, "文件名不能为空", Toast.LENGTH_LONG).show();
                            return;
                        }

                        strFileName = edtxt_filename.getText().toString().trim();

                        if (edtxt_select_dir.getText().toString().trim().equalsIgnoreCase(""))
                        {
                            Toast.makeText(MainActivity.this, "目录不能为空", Toast.LENGTH_LONG).show();
                            return;
                        }

                        strdirName = edtxt_select_dir.getText().toString().trim();

                        pwdDialog.dismiss();
                    }
                });


                //TextView tv_select_dir = myLoginView.findViewById(R.id.tv_select_dir);
                edtxt_select_dir.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //dialog.dismiss();
                        LogUtil.log(TAG, "[" + TAG + "] edtxt_select_dir ");

                        initData();
                    }
                });


                break;
            case R.id.btn_start_save:
                LogUtil.log(TAG, "[" + TAG + "] btn_start_save ");
                if (bolSaveType==2)
                {
                    Toast.makeText(MainActivity.this, "正在进行通讯！", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (strSelectMac.equalsIgnoreCase(""))
                {
                    Toast.makeText(MainActivity.this, "请选择需要保存的设备", Toast.LENGTH_LONG).show();
                    return;
                }
                if (strFileName.equalsIgnoreCase(""))
                {
                    Toast.makeText(MainActivity.this, "文件名不能为空", Toast.LENGTH_LONG).show();
                    return;
                }
                if (strdirName.equalsIgnoreCase(""))
                {
                    Toast.makeText(MainActivity.this, "目录不能为空", Toast.LENGTH_LONG).show();
                    return;
                }

                BleSaveItemDao _BleSaveItemDao = new BleSaveItemDao(MainActivity.this);
                _BleSaveItemDao.DeleteAll();

                lgCurrTime = System.currentTimeMillis();
                //mHandler.postDelayed(rCheckStop, 1 * 1000);

                if (bolSaveType==0) {
                    bolSaveType=1;
                    btn_start_save.setText(getString(R.string.stop_save));
                    sendConnectedDevice("aa05");

                    progressDialog=new ProgressDialog(MainActivity.this);
                    progressDialog.setIcon(R.mipmap.ic_launcher);

                    progressDialog.setButton("取消",new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            progressDialog.dismiss();
                        }
                    });

                    progressDialog.setTitle("正在保存");
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);//设置进度条对话框//样式（水平，旋转）
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                }
                else {
                    bolSaveType=0;
                    btn_start_save.setText(getString(R.string.start_save));
                    sendConnectedDevice("aa06");
                }
                break;
        }
    }


    private void initData() {
        ChooseFileActivity.enterActivityForResult(this, PATHREQUESTCODE);
    }


    private void showLogs() {
        //LogUtil.log(TAG, "[" + TAG + "] showLogs ");
        if (bolSaveType!=1) {
            BleLogsItemDao _BleLogsItemDao = new BleLogsItemDao(MainActivity.this);

            StringBuilder sb = new StringBuilder();
            List<BleLogsItem> _List;
            if (macFilter.equalsIgnoreCase("")) {
                _List = _BleLogsItemDao.GetList();
            } else {
                _List = _BleLogsItemDao.GetListByMac(macFilter);
            }

            for (int i = 0; i < _List.size(); i++) {
                String logTime = LogSdf.format(_List.get(i).getLogTime());
                sb.append(_List.get(i).getLogMac() + " " + logTime + " " + _List.get(i).getLogText() + "\n");
            }
            text_logs.setText(sb.toString());
        }
    }

    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        btn_device_status = (Button) findViewById(R.id.btn_device_status);
        btn_device_status.setText(getString(R.string.device_status));
        btn_device_status.setOnClickListener(this);

        btn_delete_data = (Button) findViewById(R.id.btn_delete_data);
        btn_delete_data.setText(getString(R.string.delete_data));
        btn_delete_data.setOnClickListener(this);

        btn_end_uploading = (Button) findViewById(R.id.btn_end_uploading);
        btn_end_uploading.setText(getString(R.string.end_uploading));
        btn_end_uploading.setOnClickListener(this);


        btn_start_uploading = (Button) findViewById(R.id.btn_start_uploading);
        btn_start_uploading.setText(getString(R.string.start_uploading));
        btn_start_uploading.setOnClickListener(this);

        btn_end_collection = (Button) findViewById(R.id.btn_end_collection);
        btn_end_collection.setText(getString(R.string.end_collection));
        btn_end_collection.setOnClickListener(this);

        btn_start_collection = (Button) findViewById(R.id.btn_start_collection);
        btn_start_collection.setText(getString(R.string.start_collection));
        btn_start_collection.setOnClickListener(this);

        btn_start_search = (Button) findViewById(R.id.btn_start_search);
        btn_start_search.setText(getString(R.string.start_search));
        btn_start_search.setOnClickListener(this);

        btn_show_pin = (Button) findViewById(R.id.btn_show_pin);
        btn_show_pin.setText(getString(R.string.show_pin));
        btn_show_pin.setOnClickListener(this);

        btn_clean_screen = (Button) findViewById(R.id.btn_clean_screen);
        btn_clean_screen.setText(getString(R.string.clean_screen));
        btn_clean_screen.setOnClickListener(this);


        btn_select_dev = (Button) findViewById(R.id.btn_select_dev);
        btn_select_dev.setText(getString(R.string.select_dev));
        btn_select_dev.setOnClickListener(this);

        btn_select_dir = (Button) findViewById(R.id.btn_select_dir);
        btn_select_dir.setText(getString(R.string.select_dir));
        btn_select_dir.setOnClickListener(this);

        btn_start_save = (Button) findViewById(R.id.btn_start_save);
        btn_start_save.setText(getString(R.string.start_save));
        btn_start_save.setOnClickListener(this);

        img_loading = (ImageView) findViewById(R.id.img_loading);
        operatingAnim = AnimationUtils.loadAnimation(this, R.anim.rotate);
        operatingAnim.setInterpolator(new LinearInterpolator());
        progressDialog = new ProgressDialog(this);

        mDeviceAdapter = new DeviceAdapter(this);
        mDeviceAdapter.setOnDeviceClickListener(new DeviceAdapter.OnDeviceClickListener() {
            @Override
            public void onConnect(BleDevice bleDevice) {
                if (!BleManager.getInstance().isConnected(bleDevice)) {
                    LogUtil.log(TAG, "[" + TAG + "] onConnect " + bleDevice.getMac());

                    BleManager.getInstance().cancelScan();
                    connect(bleDevice);
                }
            }

            @Override
            public void onDisConnect(final BleDevice bleDevice) {
                if (BleManager.getInstance().isConnected(bleDevice)) {
                    BleManager.getInstance().disconnect(bleDevice);
                }
            }

            @Override
            public void onDetail(BleDevice bleDevice) {
                /*
                if (BleManager.getInstance().isConnected(bleDevice)) {
                    Intent intent = new Intent(MainActivity.this, OperationActivity.class);
                    intent.putExtra(OperationActivity.KEY_DATA, bleDevice);
                    startActivity(intent);
                }
                */
                if (BleManager.getInstance().isConnected(bleDevice)) {
                    //过滤日志显示
                    macFilter = bleDevice.getMac();
                    listView_device.setVisibility(View.GONE);
                    text_logs.setVisibility(View.VISIBLE);
                    scrollView_log.setVisibility(View.VISIBLE);
                    showLogs();
                }
            }
        });
        listView_device = (ListView) findViewById(R.id.list_device);
        listView_device.setAdapter(mDeviceAdapter);


        text_logs = (TextView) findViewById(R.id.text_logs);
        scrollView_log = (ScrollView) findViewById(R.id.scrollview_logs);

        listView_device.setVisibility(View.GONE);
        text_logs.setVisibility(View.GONE);
        scrollView_log.setVisibility(View.GONE);
    }

    private void showConnectedDevice() {
        List<BleDevice> deviceList = BleManager.getInstance().getAllConnectedDevice();
        mDeviceAdapter.clearConnectedDevice();
        for (BleDevice bleDevice : deviceList) {
            mDeviceAdapter.addDevice(bleDevice);

            LogUtil.log(TAG, "[" + TAG + "] bleDevice " + bleDevice.getMac());
        }
        mDeviceAdapter.notifyDataSetChanged();
    }


    private void sendConnectedDevice(String strSendCmd) {
        List<BleDevice> deviceList = BleManager.getInstance().getAllConnectedDevice();
        for (BleDevice bleDevice : deviceList) {

            LogUtil.log(TAG, "[" + TAG + "] sendConnectedDevice " + bleDevice.getName());
            LogUtil.log(TAG, "[" + TAG + "] sendConnectedDevice " + bleDevice.getMac());
            if (strSendCmd.equalsIgnoreCase("aa05")||strSendCmd.equalsIgnoreCase("aa06"))
            {
                //只对选中的设备进行命令发送
                if (!strSelectMac.equalsIgnoreCase(bleDevice.getMac())) {
                    continue;
                }
            }

            BluetoothGatt gatt = BleManager.getInstance().getBluetoothGatt(bleDevice);

            for (BluetoothGattService service : gatt.getServices()) {

                LogUtil.log(TAG, "[" + TAG + "] BluetoothGattService service " + service.getUuid().toString());
                String UUID_slice = service.getUuid().toString().substring(4, 8);
                LogUtil.log(TAG, "[" + TAG + "] BluetoothGattService service " + UUID_slice);
                if (UUID_slice.equalsIgnoreCase("ffe0")) {

                    for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                        LogUtil.log(TAG, "[" + TAG + "] BluetoothGattService service characteristic " + characteristic.getUuid().toString());
                        UUID_slice = characteristic.getUuid().toString().substring(4, 8);
                        LogUtil.log(TAG, "[" + TAG + "] BluetoothGattService service " + UUID_slice);
                        if (UUID_slice.equalsIgnoreCase("ffe1")) {
                            int charaProp = characteristic.getProperties();
                            if ((charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
                                LogUtil.log(TAG, "[" + TAG + "] BluetoothGattService service characteristic  Write ");
                                BluetoothGattCharacteristicwrite(bleDevice, characteristic,strSendCmd);
                            }
                        }
                    }
                }
            }

        }
    }

    private void notifyConnectedDevice() {
        List<BleDevice> deviceList = BleManager.getInstance().getAllConnectedDevice();
        for (BleDevice bleDevice : deviceList) {

            LogUtil.log(TAG, "[" + TAG + "] sendConnectedDevice " + bleDevice.getName());
            LogUtil.log(TAG, "[" + TAG + "] sendConnectedDevice " + bleDevice.getMac());

            BluetoothGatt gatt = BleManager.getInstance().getBluetoothGatt(bleDevice);

            for (BluetoothGattService service : gatt.getServices()) {

                LogUtil.log(TAG, "[" + TAG + "] BluetoothGattService service " + service.getUuid().toString());
                String UUID_slice = service.getUuid().toString().substring(4, 8);
                LogUtil.log(TAG, "[" + TAG + "] BluetoothGattService service " + UUID_slice);
                if (UUID_slice.equalsIgnoreCase("ffe0")) {

                    for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                        LogUtil.log(TAG, "[" + TAG + "] BluetoothGattService service characteristic " + characteristic.getUuid().toString());
                        UUID_slice = characteristic.getUuid().toString().substring(4, 8);
                        LogUtil.log(TAG, "[" + TAG + "] BluetoothGattService service " + UUID_slice);
                        if (UUID_slice.equalsIgnoreCase("ffe1")) {

                            int charaProp = characteristic.getProperties();
                            if ((charaProp & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                                LogUtil.log(TAG, "[" + TAG + "] BluetoothGattService service characteristic  Notify ");
                                BluetoothGattCharacteristicNotify(bleDevice, characteristic);
                            }
                        }
                    }
                }
            }

        }
    }

    private void BluetoothGattCharacteristicwrite(BleDevice bleDevice,BluetoothGattCharacteristic characteristic,String hex) {
        final BluetoothGattCharacteristic characteristicinner = characteristic;
        final BleDevice bleDeviceinner = bleDevice;
        final String sendHex = hex;
        BleManager.getInstance().write(
                bleDeviceinner,
                characteristicinner.getService().getUuid().toString(),
                characteristicinner.getUuid().toString(),
                HexUtil.hexStringToBytes(sendHex),
                new BleWriteCallback() {

                    @Override
                    public void onWriteSuccess(final int current, final int total, final byte[] justWrite) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //addText(txt, "write success, current: " + current
                                //        + " total: " + total
                                //        + " justWrite: " + HexUtil.formatHexString(justWrite, true));
                                LogUtil.log(TAG, "[" + TAG + "] " + bleDeviceinner.getMac() + " BluetoothGattCharacteristicwrite Success " + HexUtil.formatHexString(justWrite, true));
                                //text_logs.append(bleDeviceinner.getMac() + " " + sendHex +" 数据发送成功\n");
                                BleLogsItemDao _BleLogsItemDao = new BleLogsItemDao(MainActivity.this);
                                BleLogsItem _BleLogsItem = new BleLogsItem();
                                _BleLogsItem.setLogMac(bleDeviceinner.getMac());
                                _BleLogsItem.setLogText(sendHex +" 数据发送成功");
                                _BleLogsItem.setLogTime(System.currentTimeMillis());
                                _BleLogsItemDao.add(_BleLogsItem);
                                showLogs();
                            }
                        });
                    }

                    @Override
                    public void onWriteFailure(final BleException exception) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //addText(txt, exception.toString());
                                LogUtil.log(TAG, "[" + TAG + "] " + bleDeviceinner.getMac() + " BluetoothGattCharacteristicwrite Failure " + exception.toString());
                                //text_logs.append(bleDeviceinner.getMac() + " " + sendHex +" 数据发送失败\n");
                                BleLogsItemDao _BleLogsItemDao = new BleLogsItemDao(MainActivity.this);
                                BleLogsItem _BleLogsItem = new BleLogsItem();
                                _BleLogsItem.setLogMac(bleDeviceinner.getMac());
                                _BleLogsItem.setLogText(sendHex +" 数据发送失败");
                                _BleLogsItem.setLogTime(System.currentTimeMillis());
                                _BleLogsItemDao.add(_BleLogsItem);
                                showLogs();
                            }
                        });
                    }
                });
    }

    private void BluetoothGattCharacteristicNotify(BleDevice bleDevice,BluetoothGattCharacteristic characteristic)
    {
        //final BluetoothGattCharacteristic characteristicinner = characteristic;
        //final BleDevice bleDeviceinner  = bleDevice;
        BleManager.getInstance().notify(
                bleDevice,
                characteristic.getService().getUuid().toString(),
                characteristic.getUuid().toString(),
                new BleNotifyCallback() {

                    @Override
                    public void onNotifySuccess(String strMac) {
                        LogUtil.log(TAG, "[" + TAG + "] " + strMac + " BluetoothGattCharacteristicNotify notify success ");
                        //text_logs.append(bleDeviceinner.getMac() + " " + " 设备监听成功\n");
                        BleLogsItemDao _BleLogsItemDao = new BleLogsItemDao(MainActivity.this);
                        BleLogsItem _BleLogsItem = new BleLogsItem();
                        _BleLogsItem.setLogMac(strMac);
                        _BleLogsItem.setLogText("设备监听成功");
                        _BleLogsItem.setLogTime(System.currentTimeMillis());
                        _BleLogsItemDao.add(_BleLogsItem);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //addText(txt, "notify success");
                                showLogs();
                            }
                        });
                    }

                    @Override
                    public void onNotifyFailure(String strMac,final BleException exception) {
                        LogUtil.log(TAG, "[" + TAG + "] " + strMac + "BluetoothGattCharacteristicNotify onNotifyFailure " + exception.toString());
                        //text_logs.append(bleDeviceinner.getMac() + " " + " 设备监听失败\n");
                        BleLogsItemDao _BleLogsItemDao = new BleLogsItemDao(MainActivity.this);
                        BleLogsItem _BleLogsItem = new BleLogsItem();
                        _BleLogsItem.setLogMac(strMac);
                        _BleLogsItem.setLogText("设备监听失败");
                        _BleLogsItem.setLogTime(System.currentTimeMillis());
                        _BleLogsItemDao.add(_BleLogsItem);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //addText(txt, exception.toString());
                                showLogs();
                            }
                        });
                    }

                    @Override
                    public void onCharacteristicChanged(String strMac,byte[] data) {
                        //LogUtil.log(TAG, "[" + TAG + "] " + strMac + " BluetoothGattCharacteristicNotify onCharacteristicChanged " + HexUtil.formatHexString(data, true));
                        //text_logs.append(bleDeviceinner.getMac() + " " + " 设备接收数据" + HexUtil.formatHexString(characteristicinner.getValue(), true) + "\n");

                        //_BleLogsItem.setLogText("设备接收数据 " + HexUtil.formatHexString(characteristicinner.getValue(), true));
                        String strRet = HexUtil.formatHexString(data, true);
                        //LogUtil.log(TAG, "[" + TAG + "] " + strMac + " BluetoothGattCharacteristicNotify onCharacteristicChanged 11 " + strRet);

                        String strCmd;
                        if (strRet.length()>=5) {
                            strCmd = strRet.substring(0, 5);
                        }
                        else
                        {
                            strCmd="";
                        }

                        if (bolSaveType==1)
                        {
                            lgCurrTime = System.currentTimeMillis();
                            //保存数据
                            if (strCmd.equalsIgnoreCase("bb 05")) {
                                iCurrCount = 0;
                                iMinCount =0;
                                //计算数据长度
                                String strLen = strRet.substring(6, 17);
                                strLen = strLen.replace(" ","");
                                LogUtil.log(TAG, "[" + TAG + "] " + strMac + " BluetoothGattCharacteristicNotify onCharacteristicChanged  数据长度：" + strLen);
                                iDataLen = Integer.valueOf(strLen,16);
                                iMaxDataLen = iDataLen;
                                LogUtil.log(TAG, "[" + TAG + "] " + strMac + " BluetoothGattCharacteristicNotify onCharacteristicChanged  数据长度：" + strLen);
                                LogUtil.log(TAG, "[" + TAG + "] " + strMac + " BluetoothGattCharacteristicNotify onCharacteristicChanged  数据长度：" + iDataLen);


                                strLen = strRet.substring(18, 26);
                                strLen = strLen.replace(" ","");
                                LogUtil.log(TAG, "[" + TAG + "] " + strMac + " BluetoothGattCharacteristicNotify onCharacteristicChanged  数据记录：" + strLen);
                                iDataCount = Integer.valueOf(strLen,16);
                                iMaxDataCount = iDataCount;
                                LogUtil.log(TAG, "[" + TAG + "] " + strMac + " BluetoothGattCharacteristicNotify onCharacteristicChanged  数据记录：" + iDataCount);


                                byte[] dataTmp = StringUtil.subBytes(data,9,data.length - 9);
                                LogUtil.log(TAG, "[" + TAG + "] " + strMac + " BluetoothGattCharacteristicNotify onCharacteristicChanged  数据：" + HexUtil.formatHexString(dataTmp, true));

                                //byteSave = StringUtil.byteMerger(byteSave, dataTmp);
                                iDataLen = iDataLen -(data.length - 9);
                                LogUtil.log(TAG, "[" + TAG + "] " + strMac + " BluetoothGattCharacteristicNotify onCharacteristicChanged  数据长度：" + iDataLen);

                                BleSaveItemDao _BleSaveItemDao = new BleSaveItemDao(MainActivity.this);
                                BleSaveItem _BleSaveItem = new BleSaveItem();
                                _BleSaveItem.setLogMac(strMac);
                                _BleSaveItem.setLogOrder(0);
                                _BleSaveItem.setLogText(HexUtil.formatHexString(dataTmp, false));
                                _BleSaveItem.setLogTime(System.currentTimeMillis());
                                _BleSaveItemDao.add(_BleSaveItem);

                                LogUtil.log(TAG, "[" + TAG + "] " + strMac + " BluetoothGattCharacteristicNotify onCharacteristicChanged  保存记录数：" + _BleSaveItem.getLogOrder());


                                BleLogsItemDao _BleLogsItemDao = new BleLogsItemDao(MainActivity.this);
                                BleLogsItem  _BleLogsItem = new BleLogsItem();
                                _BleLogsItem.setLogMac(strMac);
                                _BleLogsItem.setLogText("总数据长度：" + iMaxDataLen + ",总数据个数："+ iDataCount + ",剩余数据长度："+ iDataLen);
                                _BleLogsItem.setLogTime(System.currentTimeMillis());
                                _BleLogsItemDao.add(_BleLogsItem);

                                Message msg = new Message();
                                msg.arg1 = 0;
                                handler.sendMessage(msg);


                                if (iDataLen>0)
                                {
                                    //获取下一条数据
                                    iCurrCount = iCurrCount + 1;
                                    String strCount = StringUtil.bytesToHexString(StringUtil.intToBytes2(iCurrCount));
                                    strCount = strCount.substring(2);
                                    LogUtil.log(TAG, "[" + TAG + "] " + strMac + " BluetoothGattCharacteristicNotify onCharacteristicChanged  下一条记录数：" + strCount);
                                    sendConnectedDevice("aa15"+strCount);
                                }
                                else
                                {
                                    //没有多余的数据,应该是异常数据
                                    bolSaveType = 0;
                                    btn_start_save.setText(getString(R.string.start_save));
                                    sendConnectedDevice("aa06");
                                }

                            }
                            else if (strCmd.equalsIgnoreCase("bb 15")) {
                                {
                                    iMinCount = 0;
                                    //判断数据是否已经读完
                                    String strLen = strRet.substring(6, 14);
                                    strLen = strLen.replace(" ", "");
                                    //LogUtil.log(TAG, "[" + TAG + "] " + strMac + " BluetoothGattCharacteristicNotify onCharacteristicChanged  数据序号：" + strLen);

                                    int iDataOrder = Integer.valueOf(strLen, 16);
                                    //LogUtil.log(TAG, "[" + TAG + "] " + strMac + " BluetoothGattCharacteristicNotify onCharacteristicChanged  数据序号：" + strLen);
                                    //LogUtil.log(TAG, "[" + TAG + "] " + strMac + " BluetoothGattCharacteristicNotify onCharacteristicChanged  数据序号：" + iDataOrder);

                                    byte[] dataTmp = StringUtil.subBytes(data, 5, data.length - 5);
                                    //LogUtil.log(TAG, "[" + TAG + "] " + strMac + " BluetoothGattCharacteristicNotify onCharacteristicChanged  数据：" + HexUtil.formatHexString(dataTmp, true));

                                    //byteSave = StringUtil.byteMerger(byteSave, dataTmp);
                                    iDataLen = iDataLen - (data.length - 5);
                                    //LogUtil.log(TAG, "[" + TAG + "] " + strMac + " BluetoothGattCharacteristicNotify onCharacteristicChanged  数据长度：" + iDataLen);
                                    //LogUtil.log(TAG, "[" + TAG + "] " + strMac + " BluetoothGattCharacteristicNotify onCharacteristicChanged  数据长度：" + HexUtil.formatHexString(byteSave, true));

                                    BleSaveItemDao _BleSaveItemDao = new BleSaveItemDao(MainActivity.this);
                                    BleSaveItem _BleSaveItem = new BleSaveItem();
                                    _BleSaveItem.setLogMac(strMac);
                                    _BleSaveItem.setLogOrder((iCurrCount +1) * 100);
                                    _BleSaveItem.setLogText(HexUtil.formatHexString(dataTmp, false));
                                    _BleSaveItem.setLogTime(System.currentTimeMillis());
                                    _BleSaveItemDao.add(_BleSaveItem);

                                    //LogUtil.log(TAG, "[" + TAG + "] " + strMac + " BluetoothGattCharacteristicNotify onCharacteristicChanged  保存记录数：" + _BleSaveItem.getLogOrder());

                                    /*
                                    BleLogsItemDao _BleLogsItemDao = new BleLogsItemDao(MainActivity.this);
                                    BleLogsItem  _BleLogsItem = new BleLogsItem();
                                    _BleLogsItem.setLogMac(strMac);
                                    _BleLogsItem.setLogText("总数据长度：" + iMaxDataLen + ",总数据个数："+ iDataCount + ",剩余数据长度："+ iDataLen);
                                    _BleLogsItem.setLogTime(System.currentTimeMillis());
                                    _BleLogsItemDao.add(_BleLogsItem);
                                    */

                                    Message msg = new Message();
                                    msg.arg1 = 0;
                                    handler.sendMessage(msg);

                                    if (iDataLen>0)
                                    {
                                        //获取下一条数据
                                        iCurrCount = iCurrCount + 1;
                                        String strCount = StringUtil.bytesToHexString(StringUtil.intToBytes2(iCurrCount));
                                        strCount = strCount.substring(2);
                                        //LogUtil.log(TAG, "[" + TAG + "] " + strMac + " BluetoothGattCharacteristicNotify onCharacteristicChanged  下一条记录数：" + strCount);
                                        sendConnectedDevice("aa15"+strCount);
                                    }
                                    else
                                    {
                                        QuitAndSave();
                                    }
                                }
                            }
                            else {
                                //处理多次返回的数据
                                iMinCount = iMinCount +1;
                                //LogUtil.log(TAG, "[" + TAG + "] " + strMac + " BluetoothGattCharacteristicNotify onCharacteristicChanged  处理多次返回的数据：" + iCurrCount);
                                BleSaveItemDao _BleSaveItemDao = new BleSaveItemDao(MainActivity.this);
                                BleSaveItem _BleSaveItem = new BleSaveItem();
                                _BleSaveItem.setLogMac(strMac);
                                _BleSaveItem.setLogOrder(iCurrCount * 100 + iMinCount);
                                _BleSaveItem.setLogText(HexUtil.formatHexString(data, false));
                                _BleSaveItem.setLogTime(System.currentTimeMillis());
                                _BleSaveItemDao.add(_BleSaveItem);

                                //LogUtil.log(TAG, "[" + TAG + "] " + strMac + " BluetoothGattCharacteristicNotify onCharacteristicChanged  保存记录数：" + _BleSaveItem.getLogOrder());

                                iDataLen = iDataLen - (data.length);
                                //LogUtil.log(TAG, "[" + TAG + "] " + strMac + " BluetoothGattCharacteristicNotify onCharacteristicChanged  数据长度：" + iDataLen);

                                /*
                                BleLogsItemDao _BleLogsItemDao = new BleLogsItemDao(MainActivity.this);
                                BleLogsItem  _BleLogsItem = new BleLogsItem();
                                _BleLogsItem.setLogMac(strMac);
                                _BleLogsItem.setLogText("总数据长度：" + iMaxDataLen + ",总数据个数："+ iDataCount + ",剩余数据长度："+ iDataLen);
                                _BleLogsItem.setLogTime(System.currentTimeMillis());
                                _BleLogsItemDao.add(_BleLogsItem);
                                */

                                if (iDataLen>0)
                                {

                                }
                                else
                                {
                                    QuitAndSave();
                                }
                            }
                        }
                        else {
                            if (strCmd.equalsIgnoreCase("bb fc")) {
                                BleLogsItemDao _BleLogsItemDao = new BleLogsItemDao(MainActivity.this);
                                BleLogsItem _BleLogsItem = new BleLogsItem();
                                //_BleLogsItem.setLogMac(strMac);
                                StringBuilder sb = new StringBuilder();
                                for (int i = 3; i < data.length; i++) {
                                    if (data[i] != 10) {
                                        sb.append(((char) data[i]));
                                    }
                                }
                                if (!NotifyMsg.containsKey(strMac)) {
                                    NotifyMsg.put(strMac, sb.toString());
                                } else {
                                    NotifyMsg.put(strMac, NotifyMsg.get(strMac) + sb.toString());
                                }

                                //_BleLogsItem.setLogText("设备接收数据 " + sb.toString());
                                LogUtil.log(TAG, "[" + TAG + "] " + strMac + " BluetoothGattCharacteristicNotify onCharacteristicChanged  设备接收数据：" + sb.toString());

                                //_BleLogsItem.setLogTime(System.currentTimeMillis());
                                //_BleLogsItemDao.add(_BleLogsItem);

                                if (data.length < 20) {
                                    //数据结束
                                    _BleLogsItem = new BleLogsItem();
                                    _BleLogsItem.setLogMac(strMac);
                                    _BleLogsItem.setLogText("设备接收数据 " + NotifyMsg.get(strMac).toString());
                                    _BleLogsItem.setLogTime(System.currentTimeMillis());
                                    _BleLogsItemDao.add(_BleLogsItem);
                                    NotifyMsg.put(strMac, "");
                                }
                            } else if (strCmd.equalsIgnoreCase("bb 01") ||
                                    strCmd.equalsIgnoreCase("bb 02") ||
                                    strCmd.equalsIgnoreCase("bb 03") ||
                                    strCmd.equalsIgnoreCase("bb 04") ||
                                    strCmd.equalsIgnoreCase("bb fa") ||
                                    strCmd.equalsIgnoreCase("bb fb") ||
                                    strCmd.equalsIgnoreCase("bb fd")) {
                                BleLogsItemDao _BleLogsItemDao = new BleLogsItemDao(MainActivity.this);
                                BleLogsItem _BleLogsItem = new BleLogsItem();
                                _BleLogsItem.setLogMac(strMac);
                                if (strCmd.equalsIgnoreCase("bb 01")) {
                                    //_BleLogsItem.setLogText("设备接收数据 " + HexUtil.formatHexString(data, true));
                                    _BleLogsItem.setLogText(" 采集中");
                                } else {
                                    _BleLogsItem.setLogText("设备接收数据 " + HexUtil.formatHexString(data, true));
                                }
                                _BleLogsItem.setLogTime(System.currentTimeMillis());
                                _BleLogsItemDao.add(_BleLogsItem);
                            } else {
                                //处理多次返回的数据

                                StringBuilder sb = new StringBuilder();
                                for (int i = 0; i < data.length; i++) {
                                    //if (data[i]!=10) {
                                    sb.append((char) data[i]);
                                    //}
                                }
                                if (!NotifyMsg.containsKey(strMac)) {
                                    NotifyMsg.put(strMac, sb.toString());
                                } else {
                                    NotifyMsg.put(strMac, NotifyMsg.get(strMac) + sb.toString());
                                }

                                LogUtil.log(TAG, "[" + TAG + "] " + strMac + " BluetoothGattCharacteristicNotify onCharacteristicChanged  设备接收数据：" + sb.toString());

                                BleLogsItemDao _BleLogsItemDao = new BleLogsItemDao(MainActivity.this);
                                BleLogsItem _BleLogsItem = new BleLogsItem();
                                //_BleLogsItem.setLogMac(strMac);
                                //_BleLogsItem.setLogText("设备接收数据 " + sb.toString());

                                //_BleLogsItem.setLogTime(System.currentTimeMillis());
                                //_BleLogsItemDao.add(_BleLogsItem);

                                if (data.length < 20) {
                                    //数据结束
                                    _BleLogsItem = new BleLogsItem();
                                    _BleLogsItem.setLogMac(strMac);
                                    _BleLogsItem.setLogText("设备接收数据 " + NotifyMsg.get(strMac).toString());
                                    _BleLogsItem.setLogTime(System.currentTimeMillis());
                                    _BleLogsItemDao.add(_BleLogsItem);

                                    NotifyMsg.put(strMac, "");
                                }
                            }
                        }



                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //addText(txt, HexUtil.formatHexString(characteristic.getValue(), true));
                                showLogs();
                            }
                        });
                    }
                });
    }

    private void setScanRule() {
        UUID[] serviceUuids = null;
        String[] names = null;
        String mac = "";
        boolean isAutoConnect = false;
        BleScanRuleConfig scanRuleConfig = new BleScanRuleConfig.Builder()
                .setServiceUuids(serviceUuids)      // 只扫描指定的服务的设备，可选
                .setDeviceName(true, names)   // 只扫描指定广播名的设备，可选
                .setDeviceMac(mac)                  // 只扫描指定mac的设备，可选
                .setAutoConnect(isAutoConnect)      // 连接时的autoConnect参数，可选，默认false
                .setScanTimeOut(10000)              // 扫描超时时间，可选，默认10秒
                .build();
        BleManager.getInstance().initScanRule(scanRuleConfig);
    }

    private void startScan() {
        BleManager.getInstance().scan(new BleScanCallback() {
            @Override
            public void onScanStarted(boolean success) {
                mDeviceAdapter.clearScanDevice();
                mDeviceAdapter.notifyDataSetChanged();
                img_loading.startAnimation(operatingAnim);
                img_loading.setVisibility(View.VISIBLE);
                btn_start_search.setText(getString(R.string.end_search));
            }

            @Override
            public void onLeScan(BleDevice bleDevice) {
                super.onLeScan(bleDevice);
            }

            @Override
            public void onScanning(BleDevice bleDevice) {
                mDeviceAdapter.addDevice(bleDevice);
                mDeviceAdapter.notifyDataSetChanged();
            }

            @Override
            public void onScanFinished(List<BleDevice> scanResultList) {
                img_loading.clearAnimation();
                img_loading.setVisibility(View.INVISIBLE);
                btn_start_search.setText(getString(R.string.start_search));
            }
        });
    }

    private void connect(final BleDevice bleDevice) {
        BleManager.getInstance().connect(bleDevice, new BleGattCallback() {
            @Override
            public void onStartConnect() {
                progressDialog.show();
            }

            @Override
            public void onConnectFail(BleDevice bleDevice, BleException exception) {
                img_loading.clearAnimation();
                img_loading.setVisibility(View.INVISIBLE);
                btn_start_search.setText(getString(R.string.start_search));
                progressDialog.dismiss();
                Toast.makeText(MainActivity.this, bleDevice.getMac() +" " + getString(R.string.connect_fail), Toast.LENGTH_LONG).show();

                BleLogsItemDao _BleLogsItemDao = new BleLogsItemDao(MainActivity.this);
                BleLogsItem _BleLogsItem = new BleLogsItem();
                _BleLogsItem.setLogMac(bleDevice.getMac());
                _BleLogsItem.setLogText(getString(R.string.connect_fail));
                _BleLogsItem.setLogTime(System.currentTimeMillis());
                _BleLogsItemDao.add(_BleLogsItem);
                showLogs();
            }

            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                progressDialog.dismiss();
                mDeviceAdapter.addDevice(bleDevice);
                mDeviceAdapter.notifyDataSetChanged();

                //BleManager.getInstance().requestConnectionPriority(bleDevice,BluetoothGatt.CONNECTION_PRIORITY_HIGH);

                //strLogs.append(bleDevice.getMac() +" " + "连接成功\n");
                BleLogsItemDao _BleLogsItemDao = new BleLogsItemDao(MainActivity.this);
                BleLogsItem _BleLogsItem = new BleLogsItem();
                _BleLogsItem.setLogMac(bleDevice.getMac());
                _BleLogsItem.setLogText("连接成功");
                _BleLogsItem.setLogTime(System.currentTimeMillis());

                _BleLogsItemDao.add(_BleLogsItem);

                //设置数据包大小
                //setMtu(bleDevice, 50);
                strSelectMac = bleDevice.getMac();

                //连接成功后,进行监听
                notifyConnectedDevice();
            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice bleDevice, BluetoothGatt gatt, int status) {
                progressDialog.dismiss();

                mDeviceAdapter.removeDevice(bleDevice);
                mDeviceAdapter.notifyDataSetChanged();

                if (isActiveDisConnected) {
                    Toast.makeText(MainActivity.this, bleDevice.getMac() +" " + getString(R.string.active_disconnected), Toast.LENGTH_LONG).show();
                    BleLogsItemDao _BleLogsItemDao = new BleLogsItemDao(MainActivity.this);
                    BleLogsItem _BleLogsItem = new BleLogsItem();
                    _BleLogsItem.setLogMac(bleDevice.getMac());
                    _BleLogsItem.setLogText(getString(R.string.active_disconnected));
                    _BleLogsItem.setLogTime(System.currentTimeMillis());
                    _BleLogsItemDao.add(_BleLogsItem);
                    showLogs();

                } else {
                    Toast.makeText(MainActivity.this, bleDevice.getMac() +" " + getString(R.string.disconnected), Toast.LENGTH_LONG).show();

                    BleLogsItemDao _BleLogsItemDao = new BleLogsItemDao(MainActivity.this);
                    BleLogsItem _BleLogsItem = new BleLogsItem();
                    _BleLogsItem.setLogMac(bleDevice.getMac());
                    _BleLogsItem.setLogText(getString(R.string.disconnected));
                    _BleLogsItem.setLogTime(System.currentTimeMillis());
                    _BleLogsItemDao.add(_BleLogsItem);
                    showLogs();
                    ObserverManager.getInstance().notifyObserver(bleDevice);
                }

            }
        });
    }

    private void readRssi(BleDevice bleDevice) {
        BleManager.getInstance().readRssi(bleDevice, new BleRssiCallback() {
            @Override
            public void onRssiFailure(BleException exception) {
                Log.i(TAG, "onRssiFailure" + exception.toString());
            }

            @Override
            public void onRssiSuccess(int rssi) {
                Log.i(TAG, "onRssiSuccess: " + rssi);
            }
        });
    }

    private void setMtu(BleDevice bleDevice, int mtu) {
        BleManager.getInstance().setMtu(bleDevice, mtu, new BleMtuChangedCallback() {
            @Override
            public void onSetMTUFailure(BleException exception) {
                //Log.i(TAG, "onsetMTUFailure" + exception.toString());
                LogUtil.log(TAG, "[" + TAG + "] " + " onSetMTUFailure " + exception.toString());
            }

            @Override
            public void onMtuChanged(int mtu) {
                //Log.i(TAG, "onMtuChanged: " + mtu);
                LogUtil.log(TAG, "[" + TAG + "] " + " onMtuChanged " + mtu);
            }
        });
    }

    @Override
    public final void onRequestPermissionsResult(int requestCode,
                                                 @NonNull String[] permissions,
                                                 @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE_PERMISSION_LOCATION:
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            onPermissionGranted(permissions[i]);
                        }
                    }
                }
                break;
        }
    }

    private void checkPermissions() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, getString(R.string.please_open_blue), Toast.LENGTH_LONG).show();
            return;
        }

        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
        List<String> permissionDeniedList = new ArrayList<>();
        for (String permission : permissions) {
            int permissionCheck = ContextCompat.checkSelfPermission(this, permission);
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                onPermissionGranted(permission);
            } else {
                permissionDeniedList.add(permission);
            }
        }
        if (!permissionDeniedList.isEmpty()) {
            String[] deniedPermissions = permissionDeniedList.toArray(new String[permissionDeniedList.size()]);
            ActivityCompat.requestPermissions(this, deniedPermissions, REQUEST_CODE_PERMISSION_LOCATION);
        }
    }

    private void onPermissionGranted(String permission) {
        switch (permission) {
            case Manifest.permission.ACCESS_FINE_LOCATION:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !checkGPSIsOpen()) {
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.notifyTitle)
                            .setMessage(R.string.gpsNotifyMsg)
                            .setNegativeButton(R.string.cancel,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            finish();
                                        }
                                    })
                            .setPositiveButton(R.string.setting,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                            startActivityForResult(intent, REQUEST_CODE_OPEN_GPS);
                                        }
                                    })

                            .setCancelable(false)
                            .show();
                } else {
                    setScanRule();
                    startScan();
                }
                break;
        }
    }

    private boolean checkGPSIsOpen() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null)
            return false;
        return locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_OPEN_GPS) {
            if (checkGPSIsOpen()) {
                setScanRule();
                startScan();
            }
        }
        if (requestCode == PATHREQUESTCODE && resultCode == ChooseFileActivity.RESULTCODE) {
            ArrayList<String> resPath = data.getStringArrayListExtra(ChooseFileActivity.SELECTPATH);
            //Log.d("ZWW", resPath.toString());

            LogUtil.log(TAG, "[" + TAG + "] " + " onActivityResult " + resPath.toString());
            if (resPath.size()>0)
            {
                edtxt_select_dir.setText(resPath.get(0));
            }
        }
    }

    public void QuitAndSave() {

        progressDialog.dismiss();

        bolSaveType = 0;
        btn_start_save.setText(getString(R.string.start_save));
        sendConnectedDevice("aa06");

        StringBuilder sb = new StringBuilder();
        File f = new File(strdirName + "/" + strFileName);

        //保存数据
        try {
            FileOutputStream fos =  new FileOutputStream(f);
            LogUtil.log(TAG, "[" + TAG + "] " + " QuitAndSave " + f.getAbsolutePath());

            BleSaveItemDao _BleSaveItemDao = new BleSaveItemDao(MainActivity.this);
            List<BleSaveItem> _listSave = _BleSaveItemDao.GetListByMac(strSelectMac);
            for (int i = 0; i < _listSave.size(); i++) {
                //String logTime = LogSdf.format(_List.get(i).getLogTime());
                //sb.append(_List.get(i).getLogMac() + " " + logTime + " " + _List.get(i).getLogText() + "\n");
                //LogUtil.log(TAG, "[" + TAG + "] " + " QuitAndSave " + _listSave.get(i).getLogOrder());
                //LogUtil.log(TAG, "[" + TAG + "] " + " QuitAndSave " + _listSave.get(i).getLogText());

                byte[] bytesSave = HexUtil.hexStringToBytes(_listSave.get(i).getLogText());
                //LogUtil.log(TAG, "[" + TAG + "] " + " QuitAndSave " + HexUtil.formatHexString(bytesSave));
                //sb.append(_listSave.get(i).getLogText());
                fos.write(bytesSave, 0, bytesSave.length);

            }
            fos.flush();
            fos.close();

            BleLogsItemDao _BleLogsItemDao = new BleLogsItemDao(MainActivity.this);
            BleLogsItem _BleLogsItem = new BleLogsItem();
            _BleLogsItem.setLogMac(strSelectMac);
            _BleLogsItem.setLogText(f.getAbsolutePath()+" 保存成功");
            _BleLogsItem.setLogTime(System.currentTimeMillis());
            _BleLogsItemDao.add(_BleLogsItem);
            showLogs();

            Toast.makeText(MainActivity.this, "保存成功", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            //e.printStackTrace();
            LogUtil.log(TAG, "[" + TAG + "] " + " QuitAndSave " + e.toString());
            Toast.makeText(MainActivity.this, "保存失败", Toast.LENGTH_LONG).show();
            BleLogsItemDao _BleLogsItemDao = new BleLogsItemDao(MainActivity.this);
            BleLogsItem _BleLogsItem = new BleLogsItem();
            _BleLogsItem.setLogMac(strSelectMac);
            _BleLogsItem.setLogText(" 保存失败");
            _BleLogsItem.setLogTime(System.currentTimeMillis());
            _BleLogsItemDao.add(_BleLogsItem);
            showLogs();
        }


    }

    //超时判断
    /*
    Handler mHandler = new Handler();
    Runnable rCheckStop = new Runnable() {
        @Override
        public void run() {
            //do something
            LogUtil.log(TAG, "[" + TAG + "] " + " mHandler " + lgCurrTime);
            LogUtil.log(TAG, "[" + TAG + "] " + " mHandler " + System.currentTimeMillis());

            if (System.currentTimeMillis() - lgCurrTime > 1000 * 10 )
            {
                //传输完成.超过10秒没有接收到数据
                LogUtil.log(TAG, "[" + TAG + "] " + " mHandler " + "超过10秒没有接收到数据");
                //保存数据并退出
                //QuitAndSave();
                bolSaveType = 0;
                btn_start_save.setText(getString(R.string.start_save));
                sendConnectedDevice("aa06");
                Toast.makeText(MainActivity.this, "超过10秒没有接收到数据", Toast.LENGTH_LONG).show();
            }
            else {
                //每隔1s循环执行run方法
                mHandler.postDelayed(this, 7 * 1000);
            }
        }
    };
    */

}

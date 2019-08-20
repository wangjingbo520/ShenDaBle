package com.clj.blesample.DbHelper;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.clj.blesample.util.LogUtil;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;



public class DatabaseHelper extends OrmLiteSqliteOpenHelper {
    public static final String TAG = DatabaseHelper.class.getName();

    private static final String TABLE_NAME = "DatabaseHelper";
    public static final int DB_VERSION = 1;

    private Map<String, Dao> daos = new HashMap<String, Dao>();

    private DatabaseHelper(Context context) {
        super(context, TABLE_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database,
                         ConnectionSource connectionSource) {
        try {

            //请不要修改FIRST_DATABASE_VERSION的值，其为第一个数据库版本大小
            final int FIRST_DATABASE_VERSION = 1;

            TableUtils.createTable(connectionSource, BleLogsItem.class);
            TableUtils.createTable(connectionSource, BleSaveItem.class);

            // 若不是第一个版本安装，直接执行数据库升级
            onUpgrade(database, FIRST_DATABASE_VERSION, DB_VERSION);

        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.log(TAG, "[" + TAG + "]  " + e.toString());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database,
                          ConnectionSource connectionSource, int oldVersion, int newVersion) {
        LogUtil.log(TAG, "[" + TAG + "]  onUpgrade oldVersion " + oldVersion);
        LogUtil.log(TAG, "[" + TAG + "]  onUpgrade newVersion " + newVersion);

        // 使用for实现跨版本升级数据库
        try {
            for (int i = oldVersion; i < newVersion; i++) {
                switch (i) {
                    case 1:
                        //upgradeToVersion2(connectionSource, database);
                        break;
                    default:
                        break;
                }
            }



        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.log(TAG, "[" + TAG + "]  " + e.toString());
        }
    }

    private static DatabaseHelper instance;

    /**
     * �����ȡ��Helper
     *
     * @param context
     * @return
     */
    public static synchronized DatabaseHelper getHelper(Context context) {
        context = context.getApplicationContext();
        if (instance == null) {
            synchronized (DatabaseHelper.class) {
                if (instance == null)
                    instance = new DatabaseHelper(context);
            }
        }

        return instance;
    }

    public synchronized Dao getDao(Class clazz) throws SQLException {
        Dao dao = null;
        String className = clazz.getSimpleName();

        if (daos.containsKey(className)) {
            dao = daos.get(className);
        }
        if (dao == null) {
            try {
                dao = (Dao) super.getDao(clazz);
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                LogUtil.log(TAG, "[" + TAG + "]  " + e.toString());
            }
            daos.put(className, dao);
        }
        return dao;
    }

    /**
     * �ͷ���Դ
     */
    @Override
    public void close() {
        super.close();

        for (String key : daos.keySet()) {
            Dao dao = daos.get(key);
            dao = null;
        }
    }


    /*
    private void upgradeToVersion2(ConnectionSource connectionSource, SQLiteDatabase database) {
        // 第一版本到第二版本,LocationItem增加了表
        try {
            TableUtils.createTable(connectionSource, LocationItem.class);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.log(TAG, "[" + TAG + "]  " + e.toString());
        }
    }
    */


    /*
    private void upgradeToVersion3(ConnectionSource connectionSource, SQLiteDatabase database) {
        // 第一版本到第二版本,LocationItem增加了表
        try {
            String sql1 = "ALTER TABLE t_PhotoItem ADD COLUMN phototype VARCHAR";
            database.execSQL(sql1);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.log(TAG, "[" + TAG + "]  " + e.toString());
        }
    }
    */


}

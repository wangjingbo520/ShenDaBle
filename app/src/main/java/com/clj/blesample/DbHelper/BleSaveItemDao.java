package com.clj.blesample.DbHelper;

import android.content.Context;

import com.clj.blesample.util.LogUtil;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;
import java.util.List;

public class BleSaveItemDao {
    public static final String TAG = BleSaveItemDao.class.getName();

    private Context context;
    private Dao<BleSaveItem, String> BleSaveItemOpe;
    private DatabaseHelper helper;

    public BleSaveItemDao(Context context)
    {
        this.context = context;
        try
        {
            helper = DatabaseHelper.getHelper(context);
            BleSaveItemOpe = helper.getDao(BleSaveItem.class);
        } catch (SQLException e)
        {
            e.printStackTrace();
            LogUtil.log(TAG, "[" + TAG + "]  " + e.toString());
        }
    }

    /**
     * 增加一个
     * @param //user
     */
    public void add(BleSaveItem bleSaveItem)
    {
        try
        {
            BleSaveItemOpe.create(bleSaveItem);
        } catch (SQLException e)
        {
            //helper.getWritableDatabase();
            e.printStackTrace();
            LogUtil.log(TAG, "[" + TAG + "]  " + e.toString());
        }

    }
    /**
     * 修改一条数据
     * @param //messageItem
     */
    public void update(BleSaveItem bleSaveItem){
        try {
            BleSaveItemOpe.update(bleSaveItem);
        } catch (SQLException e) {
            e.printStackTrace();
            LogUtil.log(TAG, "[" + TAG + "]  " + e.toString());
        }
    }


    /**
     * 删除一条数据
     * @param //messageItem
     */
    public void Delete(BleSaveItem bleSaveItem){
        try {
            //CallLogItemOpe.delete(callLogItem);
            BleSaveItemOpe.executeRawNoArgs("delete from t_BleSaveItem where guid='" + bleSaveItem.getGuid() + "'");
        } catch (SQLException e) {
            e.printStackTrace();
            LogUtil.log(TAG, "[" + TAG + "]  " + e.toString());
        }
    }

    /**
     * 删除所有数据
     * @param //messageItem
     */
    public void DeleteAll(){
        try {
            //CallLogItemOpe.delete(callLogItem);
            BleSaveItemOpe.executeRawNoArgs("delete from t_BleSaveItem");
        } catch (SQLException e) {
            e.printStackTrace();
            LogUtil.log(TAG, "[" + TAG + "]  " + e.toString());
        }
    }

    public List<BleSaveItem> GetList()
    {
        try
        {
            // List<BleLogsItem> bleLogsItems = BleLogsItemOpe.queryForAll();
            QueryBuilder builder = BleSaveItemOpe.queryBuilder();
            builder.orderBy("logorder", true);
            List<BleSaveItem> bleSaveItems = builder.query();
            return bleSaveItems;
        } catch (SQLException e)
        {
            e.printStackTrace();
            LogUtil.log(TAG, "[" + TAG + "]  " + e.toString());
            return null;
        }

    }


    public List<BleSaveItem> GetListByMac(String strMac)
    {
        try
        {
            QueryBuilder builder = BleSaveItemOpe.queryBuilder();
            builder.orderBy("logorder", true).where().eq("logmac",strMac);
            List<BleSaveItem> bleSaveItems = builder.query();
            return bleSaveItems;
        } catch (SQLException e)
        {
            e.printStackTrace();
            LogUtil.log(TAG, "[" + TAG + "]  " + e.toString());
            return null;
        }

    }
}

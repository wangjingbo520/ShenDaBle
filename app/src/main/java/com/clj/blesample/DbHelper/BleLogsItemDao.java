package com.clj.blesample.DbHelper;

import android.content.Context;
import java.sql.SQLException;
import java.util.List;

import com.clj.blesample.util.LogUtil;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

public class BleLogsItemDao {


    public static final String TAG = BleLogsItemDao.class.getName();

    private Context context;
    private Dao<BleLogsItem, String> BleLogsItemOpe;
    private DatabaseHelper helper;

    public BleLogsItemDao(Context context)
    {
        this.context = context;
        try
        {
            helper = DatabaseHelper.getHelper(context);
            BleLogsItemOpe = helper.getDao(BleLogsItem.class);
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
    public void add(BleLogsItem bleLogsItem)
    {
        try
        {
            BleLogsItemOpe.create(bleLogsItem);
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
    public void update(BleLogsItem bleLogsItem){
        try {
            BleLogsItemOpe.update(bleLogsItem);
        } catch (SQLException e) {
            e.printStackTrace();
            LogUtil.log(TAG, "[" + TAG + "]  " + e.toString());
        }
    }


    /**
     * 删除一条数据
     * @param //messageItem
     */
    public void Delete(BleLogsItem bleLogsItem){
        try {
            //CallLogItemOpe.delete(callLogItem);
            BleLogsItemOpe.executeRawNoArgs("delete from t_BleLogsItem where guid='" + bleLogsItem.getGuid() + "'");
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
            BleLogsItemOpe.executeRawNoArgs("delete from t_BleLogsItem");
        } catch (SQLException e) {
            e.printStackTrace();
            LogUtil.log(TAG, "[" + TAG + "]  " + e.toString());
        }
    }

    public List<BleLogsItem> GetList()
    {
        try
        {
           // List<BleLogsItem> bleLogsItems = BleLogsItemOpe.queryForAll();
            QueryBuilder builder = BleLogsItemOpe.queryBuilder();
            builder.orderBy("guid", false);
            List<BleLogsItem> bleLogsItems = builder.query();
            return bleLogsItems;
        } catch (SQLException e)
        {
            e.printStackTrace();
            LogUtil.log(TAG, "[" + TAG + "]  " + e.toString());
            return null;
        }

    }


    public List<BleLogsItem> GetListByMac(String strMac)
    {
        try
        {
            QueryBuilder builder = BleLogsItemOpe.queryBuilder();
            builder.orderBy("guid", false).where().eq("logmac",strMac);
            List<BleLogsItem> bleLogsItems = builder.query();
            return bleLogsItems;
        } catch (SQLException e)
        {
            e.printStackTrace();
            LogUtil.log(TAG, "[" + TAG + "]  " + e.toString());
            return null;
        }

    }



}

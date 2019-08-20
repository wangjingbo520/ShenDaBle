package com.clj.blesample.DbHelper;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "t_BleSaveItem")
public class BleSaveItem {

    public BleSaveItem() {

    }

    @DatabaseField(columnName = "guid", generatedId = true)
    private long guid;

    public long getGuid() {
        return guid;
    }

    public void setGuid(long _guid) {
        this.guid = _guid;
    }


    @DatabaseField(columnName = "logorder")
    private long logorder;

    public long getLogOrder() {
        return logorder;
    }

    public void setLogOrder(long _logorder) {
        this.logorder = _logorder;
    }

    @DatabaseField(columnName = "logmac")
    private String logmac;

    public String getLogMac() {
        return logmac;
    }

    public void setLogMac(String _logmac) {
        this.logmac = _logmac;
    }

    @DatabaseField(columnName = "logtext")
    private String logtext;

    public String getLogText() {
        return logtext;
    }

    public void setLogText(String _logtext) {
        this.logtext = _logtext;
    }

    @DatabaseField(columnName = "logtime")
    private long logtime;

    public long getLogTime() {
        return logtime;
    }

    public void setLogTime(long _logtime) {
        this.logtime = _logtime;
    }

    @Override
    public String toString() {
        return "guid = " + guid + ";" +
                "logmac = " + logmac + ";" +
                "logorder = " + logorder + ";" +
                "logtext = " + logtext +
                "logtime = " + logtime;
    }
}

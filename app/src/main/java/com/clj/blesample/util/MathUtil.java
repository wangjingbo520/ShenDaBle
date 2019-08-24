package com.clj.blesample.util;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MathUtil {

    public static String mathMode(int postion) {
        switch (postion) {
            case 0:
                return "03";
            case 1:
                return "01";
            case 2:
                return "02";
            default:
                return "01";
        }
    }

    public static String mathPinlv(int position) {
        String data = "";
        if (0 == position) {
            data = convertDecimalToBinary(50);
        } else if (1 == position) {
            data = convertDecimalToBinary(100);
        } else if (2 == position) {
            data = convertDecimalToBinary(150);
        } else if (3 == position) {
            data = convertDecimalToBinary(200);
        } else if (4 == position) {
            data = convertDecimalToBinary(250);
        } else if (5 == position) {
            data = convertDecimalToBinary(300);
        } else if (6 == position) {
            data = convertDecimalToBinary(350);
        } else if (7 == position) {
            data = convertDecimalToBinary(400);
        } else if (8 == position) {
            data = convertDecimalToBinary(450);
        } else if (9 == position) {
            data = convertDecimalToBinary(500);
        }

        if (data.length() == 1) {
            return "000" + data;
        } else if (data.length() == 2) {
            return "00" + data;
        } else if (data.length() == 3) {
            return "0" + data;
        } else {
            return data;
        }
    }

    public static String mains() {
        String s = convertDecimalToBinary(450);
        Log.i("------>", "mains: " + s);
        return s;
    }


    private static String convertDecimalToBinary(int value) {
        String str;
        String a = Integer.toHexString(value);
        if (a.length() == 1) {
            str = "0" + a;
        } else {
            str = a;
        }
        return str;
    }

    public static String getDate() {
        Date dt = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
        return sdf.format(dt);
    }


}

package com.clj.blesample.util;

public class StringUtil {

    private static final String TAG = StringUtil.class.getName();

    public static byte getXor(byte[] data) {
        //LogUtil.log(TAG, "[" + TAG + "] " + " getXor: " + bytesToHexString(data));
        byte temp = data[0];

        int v = temp & 0xFF;
        String hv = Integer.toHexString(v);

        //LogUtil.log(TAG, "[" + TAG + "] " + " getXor 0: " + hv);
        for (int i = 1; i < data.length; i++) {
            v = data[i] & 0xFF;
            hv = Integer.toHexString(v);
            //LogUtil.log(TAG, "[" + TAG + "] " + " getXor "+i+" 1: " + hv);

            temp ^= data[i];

            v = temp & 0xFF;
            hv = Integer.toHexString(v);
            //LogUtil.log(TAG, "[" + TAG + "] " + " getXor "+i+" 2: " + hv);
        }
        return temp;
    }

    public static String bytesToHexString(byte[] src){
        StringBuilder stringBuilder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }

        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }


    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }


    public static String encodeHex(String msg) {
        byte[] bytes = null;
        try {
            bytes = msg.getBytes("GBK");
        } catch (java.io.UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        StringBuffer buff = new StringBuffer(bytes.length * 4);
        String b;
        char a;
        int n = 0;
        int m = 0;
        for (int i = 0; i < bytes.length; i++) {
            b = Integer.toHexString(bytes[i]);
            if (bytes[i] > 0) {
                buff.append("00");
                buff.append(b);
                n = n + 1;
            } else {
                a = msg.charAt((i - n) / 2 + n);
                m = a;
                b = Integer.toHexString(m);
                buff.append(b.substring(0, 4));

                i = i + 1;
            }
        }
        return buff.toString();
    }


    /**
     * 将int类型的数据转换为byte数组
     * @param n int数据
     * @return 生成的byte数组
     */
    public static byte[] intToBytes(int n){
        String s = String.valueOf(n);
        return s.getBytes();
    }

    /**
     * 将byte数组转换为int数据
     * @param b 字节数组
     * @return 生成的int数据
     */
    public static int bytesToInt(byte[] b){
        String s = new String(b);
        return Integer.parseInt(s);
    }

    /**
     * 将int类型的数据转换为byte数组
     * 原理：将int数据中的四个byte取出，分别存储
     * @param n int数据
     * @return 生成的byte数组
     */
    public static byte[] intToBytes2(int n){
        byte[] b = new byte[4];
        for(int i = 0;i < 4;i++){
            b[i] = (byte)(n >> (24 - i * 8));
        }
        return b;
    }

    /**
     * 将byte数组转换为int数据
     * @param b 字节数组
     * @return 生成的int数据
     */
    public static int byteToInt2(byte[] b){
        return (((int)b[0]) << 24) + (((int)b[1]) << 16) + (((int)b[2]) << 8) + b[3];
    }


    //java 合并两个byte数组
    public static byte[] byteMerger(byte[] byte_1, byte[] byte_2) {
        byte[] byte_3 = new byte[byte_1.length + byte_2.length];
        System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
        System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);
        return byte_3;
    }

    /**
     * 从一个byte[]数组中截取一部分
     * @param src
     * @param begin
     * @param count
     * @return
     */
    public static byte[] subBytes(byte[] src, int begin, int count) {
        byte[] bs = new byte[count];
        for (int i=begin; i<begin+count; i++) bs[i-begin] = src[i];
        return bs;
    }
}

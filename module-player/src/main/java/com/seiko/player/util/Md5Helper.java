package com.seiko.player.util;

import java.io.InputStream;
import java.security.MessageDigest;

class Md5Helper {
    private static char[] hexChar = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
    private static String MD5     = "MD5";

    static String md5ForInputStream(InputStream fis) {
        MessageDigest md5 = null;
        try {
            byte[] buffer = new byte[1024 * 8];
            md5 = MessageDigest.getInstance(MD5);
            int numRead;
            while ((numRead = fis.read(buffer)) > 0) {
                md5.update(buffer, 0, numRead);
            }
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return toHexString(md5.digest());
    }

    static String md5ForInputStream(InputStream fis, int maxLength) {
        MessageDigest md5 = null;
        try {
            byte[] buffer = new byte[1024 * 8];
            md5 = MessageDigest.getInstance(MD5);
            int numRead;
            int currentNum = 0;
            while ((numRead = fis.read(buffer)) > 0) {
                currentNum += numRead;
                if (currentNum >= maxLength) {
                    md5.update(buffer, 0, numRead - (currentNum - maxLength));
                    break;
                } else {
                    md5.update(buffer, 0, numRead);
                }
            }
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return toHexString(md5.digest());
    }

    private static String toHexString(byte[] b) {
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (byte value : b) {
            sb.append(hexChar[(value & 0xf0) >>> 4]);
            sb.append(hexChar[value & 0x0f]);
        }
        return sb.toString();
    }

}

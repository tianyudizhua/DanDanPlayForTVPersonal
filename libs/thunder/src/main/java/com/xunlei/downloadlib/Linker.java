package com.xunlei.downloadlib;

public class Linker {
    public static void loadCacheDown() {
        System.loadLibrary("ghostdo");
        System.loadLibrary("ghostu");
        System.loadLibrary("ghostcachedown");
    }

    public static void loadBDPlayer() {
        System.loadLibrary("xl_stat");
        System.loadLibrary("xluagc");
        System.loadLibrary("xl_thunder_sdk");
        System.loadLibrary("xl_thunder_iface");
    }
}

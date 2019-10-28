package com.xunlei.downloadlib;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.xunlei.downloadlib.parameter.TorrentInfo;

import java.io.File;
import java.io.IOException;

public class TaskService extends Service {
    private XLTaskHelper xlTaskHelper;

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            XLDownloadManager.getInstance().uninit();
            XLTaskHelper.init(this, 2);
            xlTaskHelper = XLTaskHelper.getInstance();
            String url = intent.getStringExtra("url");
            String file = intent.getStringExtra("file");
            int index = intent.getIntExtra("index", -1);
            if (index != -1) {
                addTorrentTask(url, file, index);
            } else {
                String name = intent.getStringExtra("name");
                addUrlTask(url, file, name);
            }
            Log.e("info", "onStartCommand: ");
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void addUrlTask(String url, String file, String name) {
       final long taskId = xlTaskHelper.addThunderTask(url, file, name);
        Log.e("info", "playUrl: task -> " + taskId);
        if (taskId == -1) {
            XLDownloadManager.getInstance().uninit();
            XLTaskHelper.init(this, 2);
            xlTaskHelper = XLTaskHelper.getInstance();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                for (; ; ) {
                    if (xlTaskHelper.getTaskInfo(taskId).mDownloadSize > 0) {
                        xlTaskHelper.stopTask(taskId);
                        break;
                    }
                    // Log.e("info", "speed: "+xlTaskHelper.getTaskInfo(taskId).mDownloadSize);
                }
            }
        }).start();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                xlTaskHelper.stopTask(taskId);
            }
        }, 10000);
    }

    public void addTorrentTask(final String torrentFile, final String downloadFile, final int index) {
      final   long taskId = xlTaskHelper.addTorrentTaskBySelect(torrentFile, downloadFile, new int[]{index});
        Log.e("info", "playTorrent: task -> " + taskId);
        if (taskId == -1) {
            XLDownloadManager.getInstance().uninit();
            XLTaskHelper.init(this, 2);
            xlTaskHelper = XLTaskHelper.getInstance();
        }
        TorrentInfo torrentInfo = xlTaskHelper.getTorrentInfo(torrentFile);
        String subFile = torrentInfo.mSubFileInfo[index].mSubPath;
        String realFile = downloadFile + "/" + (subFile.isEmpty() ? "" : subFile + "/") + torrentInfo.mSubFileInfo[index].mFileName;//多层文件夹
        Log.e("info", "TaskService: " + realFile);
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (; ; ) {
                    if (xlTaskHelper.getTaskInfo(taskId).mDownloadSize > 0) {
                        xlTaskHelper.stopTask(taskId);
                        break;
                    }
                    //Log.e("info", "speed: "+xlTaskHelper.getTaskInfo(taskId).mDownloadSize);
                }
            }
        }).start();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                xlTaskHelper.stopTask(taskId);
            }
        }, 10000);
    }

}

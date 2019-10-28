package com.xunlei.downloadlib;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.xunlei.downloadlib.parameter.TorrentInfo;

import java.io.File;
import java.lang.ref.WeakReference;

public class TaskCreater {
    private Context context;
    private Intent taskService;
    private boolean isOk = false;
    private XLTaskHelper xlTaskHelper;
    private TaskCallBack taskCallBack;
    private NoLeakHandler handler = new NoLeakHandler(this);

    public interface TaskCallBack {
        void onResult(boolean result);
    }

    private static class NoLeakHandler extends Handler {
        private WeakReference<TaskCreater> magnetParserWeakReference;

        private NoLeakHandler(TaskCreater magnetParser) {
            this.magnetParserWeakReference = new WeakReference<>(magnetParser);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            TaskCreater request = magnetParserWeakReference.get();
            request.isOk = true;
            if (request.taskService != null) {
                request.context.stopService(request.taskService);
            }
            switch (msg.what) {
                case 0:
                    if (request.taskCallBack != null) {
                        request.taskCallBack.onResult(true);
                    }
                    break;
                case 1:
                    if (request.taskCallBack != null) {
                        request.taskCallBack.onResult(false);
                    }
                    break;
            }
        }
    }

    public TaskCreater with(Context context, XLTaskHelper xlTaskHelper) {
        this.context = context;
        this.xlTaskHelper = xlTaskHelper;
        return this;
    }

    public TaskCreater setTaskCallBack(TaskCallBack taskCallBack) {
        this.taskCallBack = taskCallBack;
        return this;
    }

    public void start(final String url, final String downloadFile, final String name) {
        startService(url, downloadFile, name);
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (; ; ) {
                    if (isOk) {
                        break;
                    }
                    String path = downloadFile + name;//判断文件是否创建成功
                    File file = new File(path);
                    if (file.exists() /*&& file.length() > 0*/) {//文件创建成功，启动主程序任务
                        isOk = true;
                        try {
                            Thread.sleep(500L);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        /*xlTaskHelper.stopTask(taskId);
                        taskId = xlTaskHelper.addThunderTask(url, downloadFile, name);*/
                        handler.sendEmptyMessage(0);
                    }
                }
            }
        }).start();
    }

    private void startService(String url, String file, String name) {
        taskService = new Intent(context, TaskService.class);
        taskService.putExtra("url", url);
        taskService.putExtra("file", file);
        taskService.putExtra("name", name);
        context.startService(taskService);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isOk) {
                    return;
                }
                handler.sendEmptyMessage(1);
            }
        }, 12000);
    }

    public void startIndexTask(final String torrent, final String downloadFile, final int index) {
        startIndexService(torrent, downloadFile, index);
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (; ; ) {
                    if (isOk) {
                        break;
                    }
                    TorrentInfo torrentInfo = xlTaskHelper.getTorrentInfo(torrent);
                    String subFile = torrentInfo.mSubFileInfo[index].mSubPath;
                    String realFile = downloadFile + "/" + (subFile.isEmpty() ? "" : subFile + "/") + torrentInfo.mSubFileInfo[index].mFileName;//多层文件夹
                    File file = new File(realFile);
                    Log.e("info", "TaskCreater: " + realFile);
                    if (file.exists() /*&& file.length() > 0*/) {//文件创建成功，启动主程序任务
                        isOk = true;
                       /* xlTaskHelper.stopTask(taskId);
                        taskId = xlTaskHelper.addTorrentTaskBySelect(torrent, downloadFile, new int[]{index});*/
                        try {
                            Thread.sleep(500L);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        handler.sendEmptyMessage(0);
                    }
                }
            }
        }).start();
    }

    private void startIndexService(String torrentFile, String file, int index) {
        taskService = new Intent(context, TaskService.class);
        taskService.putExtra("url", torrentFile);
        taskService.putExtra("file", file);
        taskService.putExtra("index", index);
        context.startService(taskService);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isOk) {
                    return;
                }
                handler.sendEmptyMessage(1);
            }
        }, 12000);
    }
}

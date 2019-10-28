package com.xunlei.downloadlib;

import android.content.Context;
import android.os.Build.VERSION;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.xunlei.downloadlib.android.XLUtil;
import com.xunlei.downloadlib.parameter.BtIndexSet;
import com.xunlei.downloadlib.parameter.BtSubTaskDetail;
import com.xunlei.downloadlib.parameter.BtTaskParam;
import com.xunlei.downloadlib.parameter.EmuleTaskParam;
import com.xunlei.downloadlib.parameter.GetFileName;
import com.xunlei.downloadlib.parameter.GetTaskId;
import com.xunlei.downloadlib.parameter.InitParam;
import com.xunlei.downloadlib.parameter.MagnetTaskParam;
import com.xunlei.downloadlib.parameter.P2spTaskParam;
import com.xunlei.downloadlib.parameter.TorrentFileInfo;
import com.xunlei.downloadlib.parameter.TorrentInfo;
import com.xunlei.downloadlib.parameter.XLConstant.XLCreateTaskMode;
import com.xunlei.downloadlib.parameter.XLConstant.XLErrorCode;
import com.xunlei.downloadlib.parameter.XLTaskInfo;
import com.xunlei.downloadlib.parameter.XLTaskLocalUrl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class XLTaskHelper {
    private static final String TAG = "XLTaskHelper";
    private static volatile XLTaskHelper instance = null;
    private AtomicInteger seq = new AtomicInteger(0);

    private XLTaskHelper() {
    }

    public static XLTaskHelper getInstance() {
        if (instance == null) {
            synchronized (XLTaskHelper.class) {
                try {
                    if (instance == null) {
                        instance = new XLTaskHelper();
                    }
                } catch (Throwable th) {
                    while (true) {
                        Class cls = XLTaskHelper.class;
                    }
                }
            }
        }
        return instance;
    }


    public static void init(Context context, int i) {
        XLDownloadManager instance = XLDownloadManager.getInstance();
        InitParam initParam = new InitParam();
        initParam.mAppKey = XLUtil.generateAppKey("com.xunlei.downloadprovider", (short) 0, (byte) 1);
        //XLUtil.generateAppKey("com.xunlei.downloadprovider", (short) 0, (byte) 1);
        if (i == 1) {
            Linker.loadCacheDown();
        } else {
            Linker.loadBDPlayer();
        }
        Log.i(TAG, "initAppKey = " + initParam.mAppKey);
        initParam.mAppVersion = "5.51.2.5220";
        initParam.mStatSavePath = context.getFilesDir().getPath();
        initParam.mStatCfgSavePath = context.getFilesDir().getPath();
        initParam.mPermissionLevel = 2;
        if (i == 1) {
            int init = instance.init(context, initParam);
            Log.i(TAG, "initXLEngine(" + i + ") ret = " + init);
        } else {
            int init = instance.init2(context, initParam);
            Log.i(TAG, "initXLEngine(" + i + ") ret = " + init);
        }
        instance.setOSVersion(VERSION.INCREMENTAL);
        instance.setSpeedLimit(-1, -1);
        instance.setUserId("0");
    }

    /**
     * 添加磁力链任务
     * @param url 磁力链接 magnet:? 开头
     * @param savePath 下载文件保存路径
     * @param fileName 下载文件名 可以通过 getFileName(url) 获取到,为空默认为getFileName(url)的值
     * @return 任务id
     */
    public long addMagnetTask(String url, String savePath, String fileName) {
        long taskId = -1;
        synchronized (this) {
            if (url.startsWith("magnet:?")) {
                XLDownloadManager instance = XLDownloadManager.getInstance();
                if (TextUtils.isEmpty(fileName)) {
                    GetFileName getFileName = new GetFileName();
                    instance.getFileNameFromUrl(url, getFileName);
                    fileName = getFileName.getFileName();
                }
                MagnetTaskParam magnetTaskParam = new MagnetTaskParam();
                magnetTaskParam.setFileName(fileName);
                magnetTaskParam.setFilePath(savePath);
                magnetTaskParam.setUrl(url);
                GetTaskId getTaskId = new GetTaskId();
                XLDownloadManager.getInstance().createBtMagnetTask(magnetTaskParam, getTaskId);
                instance.setTaskLxState(getTaskId.getTaskId(), 0, 1);
                instance.startDcdn(getTaskId.getTaskId(), 0, "", "611", "");
                instance.startTask(getTaskId.getTaskId(), false);
                taskId = getTaskId.getTaskId();
            }
        }
        return taskId;
    }

    public long addTorrentTaskBySelect(String str, String str2, int[] iArr) {
        long taskId;
        int i = 0;
        synchronized (this) {
            int i2;
            TorrentInfo torrentInfo = new TorrentInfo();
            XLDownloadManager.getInstance().getTorrentInfo(str, torrentInfo);
            TorrentFileInfo[] torrentFileInfoArr = torrentInfo.mSubFileInfo;
            BtTaskParam btTaskParam = new BtTaskParam();
            btTaskParam.setCreateMode(1);
            btTaskParam.setFilePath(str2);
            btTaskParam.setMaxConcurrent(3);
            btTaskParam.setSeqId(this.seq.incrementAndGet());
            btTaskParam.setTorrentPath(str);
            GetTaskId getTaskId = new GetTaskId();
            XLDownloadManager.getInstance().createBtTask(btTaskParam, getTaskId);
            List<Integer> arrayList = new ArrayList();
            for (i2 = 0; i2 < torrentFileInfoArr.length; i2++) {
                arrayList.add(Integer.valueOf(i2));
            }
            for (i2 = 0; i2 < iArr.length; i2++) {
                if (arrayList.contains(Integer.valueOf(iArr[i2]))) {
                    arrayList.remove(arrayList.indexOf(Integer.valueOf(iArr[i2])));
                }
            }
            if (torrentFileInfoArr.length > 1 && arrayList != null && arrayList.size() > 0) {
                BtIndexSet btIndexSet = new BtIndexSet(arrayList.size());
                int i3 = 0;
                for (Integer intValue : arrayList) {
                    btIndexSet.mIndexSet[i3] = intValue.intValue();
                    i3++;
                }
                Log.d(TAG, "selectBtSubTask return = " + ((long) XLDownloadManager.getInstance().deselectBtSubTask(getTaskId.getTaskId(), btIndexSet)));
            }
            while (i < torrentInfo.mFileCount) {
                XLDownloadManager.getInstance().startDcdn(getTaskId.getTaskId(), i, "", "", "");
                i++;
            }
            XLDownloadManager.getInstance().setTaskLxState(getTaskId.getTaskId(), 0, 1);
            XLDownloadManager.getInstance().startTask(getTaskId.getTaskId(), false);
            taskId = getTaskId.getTaskId();
        }
        return taskId;
    }

    /**
     * 添加迅雷链接任务 支持thunder:// ftp:// ed2k:// http:// https:// 协议
     * @param url 下载链接
     * @param savePath 下载文件保存路径
     * @param fileName 下载文件名 可以通过 getFileName(url) 获取到,为空默认为getFileName(url)的值
     * @return 任务id
     */
    public long addThunderTask(String url, String savePath, String fileName) {
        long taskId;
        synchronized (this) {
            int createP2spTask;
            StringBuilder stringBuilder;
            if (url.startsWith("thunder://")) {
                url = XLDownloadManager.getInstance().parserThunderUrl(url);
            }
            GetTaskId getTaskId = new GetTaskId();
            if (TextUtils.isEmpty(fileName)) {
                GetFileName getFileName = new GetFileName();
                XLDownloadManager.getInstance().getFileNameFromUrl(url, getFileName);
                fileName = getFileName.getFileName();
            }
            if (url.startsWith("ftp://") || url.startsWith("http://") || url.startsWith("https://")) {
                P2spTaskParam p2spTaskParam = new P2spTaskParam();
                p2spTaskParam.setCreateMode(XLCreateTaskMode.CONTINUE_TASK.ordinal());
                p2spTaskParam.setFileName(fileName);
                p2spTaskParam.setFilePath(savePath);
                p2spTaskParam.setUrl(url);
                p2spTaskParam.setSeqId(this.seq.incrementAndGet());
                p2spTaskParam.setCookie("");
                p2spTaskParam.setRefUrl("");
                p2spTaskParam.setUser("");
                p2spTaskParam.setPass("");
                createP2spTask = XLDownloadManager.getInstance().createP2spTask(p2spTaskParam, getTaskId);
                if (createP2spTask != XLErrorCode.NO_ERROR) {
                    stringBuilder = new StringBuilder();
                    stringBuilder.append("create task failed:");
                    stringBuilder.append(XLDownloadManager.getInstance().getErrorCodeMsg(createP2spTask));
                    Log.e(TAG, stringBuilder.toString());
                }
            } else if (url.startsWith("ed2k://")) {
                EmuleTaskParam emuleTaskParam = new EmuleTaskParam();
                emuleTaskParam.setFilePath(savePath);
                emuleTaskParam.setFileName(fileName);
                emuleTaskParam.setUrl(url);
                emuleTaskParam.setSeqId(this.seq.incrementAndGet());
                emuleTaskParam.setCreateMode(XLCreateTaskMode.CONTINUE_TASK.ordinal());
                createP2spTask = XLDownloadManager.getInstance().createEmuleTask(emuleTaskParam, getTaskId);
                if (createP2spTask != XLErrorCode.NO_ERROR) {
                    stringBuilder = new StringBuilder();
                    stringBuilder.append("create task failed:");
                    stringBuilder.append(XLDownloadManager.getInstance().getErrorCodeMsg(createP2spTask));
                    Log.e(TAG, stringBuilder.toString());
                }
            }
            XLDownloadManager.getInstance().setDownloadTaskOrigin(getTaskId.getTaskId(), "out_app/out_app_paste");
            XLDownloadManager.getInstance().setOriginUserAgent(getTaskId.getTaskId(), "AndroidDownloadManager/5.41.2.4980 (Linux; U; Android 4.4.4; Build/KTU84Q)");
            createP2spTask = XLDownloadManager.getInstance().startTask(getTaskId.getTaskId(), false);
            if (createP2spTask != XLErrorCode.NO_ERROR) {
                stringBuilder = new StringBuilder();
                stringBuilder.append("start task failed: ");
                stringBuilder.append(createP2spTask);
                Log.e(TAG, stringBuilder.toString());
            }
            taskId = getTaskId.getTaskId();
        }
        return taskId;
    }

    /**
     * 添加种子下载任务,如果是磁力链需要先通过addMagentTask将种子下载下来
     * @param torrentPath 种子地址
     * @param savePath 保存路径
     * @param deselectIndexArray 不需要下载的文件索引
     * @return 任务id
     */
    public long addTorrentTask(String torrentPath, String savePath, @NonNull int[] deselectIndexArray) {
        long deselectBtSubTask;
        int i = 0;
        synchronized (this) {
            TorrentInfo torrentInfo = new TorrentInfo();
            XLDownloadManager.getInstance().getTorrentInfo(torrentPath, torrentInfo);
            TorrentFileInfo[] torrentFileInfoArr = torrentInfo.mSubFileInfo;
            BtTaskParam btTaskParam = new BtTaskParam();
            btTaskParam.setCreateMode(1);
            btTaskParam.setFilePath(savePath);
            btTaskParam.setMaxConcurrent(3);
            btTaskParam.setSeqId(this.seq.incrementAndGet());
            btTaskParam.setTorrentPath(torrentPath);
            GetTaskId getTaskId = new GetTaskId();
            XLDownloadManager.getInstance().createBtTask(btTaskParam, getTaskId);
            if (torrentFileInfoArr.length > 1 && deselectIndexArray.length > 0) {
                BtIndexSet btIndexSet = new BtIndexSet(deselectIndexArray.length);
                int length = deselectIndexArray.length;
                int i2 = 0;
                while (i2 < length) {
                    btIndexSet.mIndexSet[i] = deselectIndexArray[i2];
                    i2++;
                    i++;
                }
                deselectBtSubTask = (long) XLDownloadManager.getInstance().deselectBtSubTask(getTaskId.getTaskId(), btIndexSet);
                String stringBuilder = "selectBtSubTask return = " + deselectBtSubTask;
                Log.d(TAG, stringBuilder);
            }
            XLDownloadManager.getInstance().setTaskLxState(getTaskId.getTaskId(), 0, 1);
            if (deselectIndexArray.length > 0) {
                XLDownloadManager.getInstance().startDcdn(getTaskId.getTaskId(), deselectIndexArray[0], "", "", "");
                Log.e(TAG, "deselectIndexArray length > 0");
            }
            XLDownloadManager.getInstance().startTask(getTaskId.getTaskId(), false);
            deselectBtSubTask = getTaskId.getTaskId();
        }
        return deselectBtSubTask;
    }

    public synchronized long startTask(BtTaskParam taskParam, BtIndexSet selectIndexSet, BtIndexSet deSelectIndexSet) {
        GetTaskId getTaskId = new GetTaskId();
        XLDownloadManager.getInstance().createBtTask(taskParam, getTaskId);
        XLDownloadManager.getInstance().selectBtSubTask(getTaskId.getTaskId(), selectIndexSet);
        XLDownloadManager.getInstance().deselectBtSubTask(getTaskId.getTaskId(), deSelectIndexSet);
        XLDownloadManager.getInstance().setTaskLxState(getTaskId.getTaskId(), 0, 1);
        XLDownloadManager.getInstance().startTask(getTaskId.getTaskId(), false);
        return getTaskId.getTaskId();
    }

    /**
     * 通过链接获取文件名
     * @param url
     * @return
     */
    public String getFileName(String url) {
        String fileName;
        synchronized (this) {
            if (url.startsWith("thunder://")) {
                url = XLDownloadManager.getInstance().parserThunderUrl(url);
            }
            GetFileName getFileName = new GetFileName();
            XLDownloadManager.getInstance().getFileNameFromUrl(url, getFileName);
            fileName = getFileName.getFileName();
        }
        return fileName;
    }

    /**
     * 获取某个文件的本地proxy url,如果是音视频文件可以实现变下边播
     * @param filePath 文件路径
     * @return 本地代理url
     */
    public synchronized String getLocalUrl(String filePath) {
        XLTaskLocalUrl localUrl = new XLTaskLocalUrl();
        XLDownloadManager.getInstance().getLocalUrl(filePath,localUrl);
        return localUrl.mStrUrl;
    }

    /**
     * 删除一个任务，会把文件也删掉
     * @param taskId 任务id
     * @param savePath 储存路径
     */
    public static synchronized void deleteTask(long taskId, final String savePath) {
        stopTask(taskId);
        new Handler(Daemon.looper()).post(() -> {
            try {
                new LinuxFileCommand(Runtime.getRuntime()).deleteDirectory(savePath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

//    public void deleteTask(long j, final String str) {
//        synchronized (this) {
//            stopTask(j);
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    deleteFile(str);
//                }
//            }, 1000);
//        }
//    }
//
//    /**
//     * 删除某个文件夹下的所有文件夹和文件
//     */
//    public static boolean deleteFile(String filePath) {
//        try {
//            File file = new File(filePath);
//            // 当且仅当此抽象路径名表示的文件存在且 是一个目录时，返回 true
//            if (!file.isDirectory()) {
//                file.delete();
//            } else if (file.isDirectory()) {
//                String[] filelist = file.list();
//                for (int i = 0; i < filelist.length; i++) {
//                    File delfile = new File(filePath + "/" + filelist[i]);
//                    if (!delfile.isDirectory()) {
//                        boolean r= delfile.delete();
//                        System.out.println(delfile.getAbsolutePath() + "删除文件"+(r?"成功":"失败"));
//                    } else if (delfile.isDirectory()) {
//                        deleteFile(filePath + "/" + filelist[i]);
//                        System.out.println(file + "ssss");
//                    }
//                }
//                file.delete();
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return true;
//    }

    /**
     * 停止任务 文件保留
     * @param taskId 任务id
     */
    public static synchronized void stopTask(long taskId) {
        XLDownloadManager.getInstance().stopTask(taskId);
        XLDownloadManager.getInstance().releaseTask(taskId);
    }

    /**
     * 获取种子详情
     * @param torrentPath 种子路径
     * @return 种子详情
     */
    public TorrentInfo getTorrentInfo(String torrentPath) {
        TorrentInfo torrentInfo;
        synchronized (this) {
            torrentInfo = new TorrentInfo();
            XLDownloadManager.getInstance().getTorrentInfo(torrentPath, torrentInfo);
            torrentInfo.mDataPath = torrentPath;
        }
        return torrentInfo;
    }

    /**
     * 获取任务详情， 包含当前状态，下载进度，下载速度，文件大小
     * mDownloadSize:已下载大小  mDownloadSpeed:下载速度 mFileSize:文件总大小 mTaskStatus:当前状态，0连接中1下载中 2下载完成 3失败 mAdditionalResDCDNSpeed DCDN加速 速度
     * @param taskId 任务id
     * @return 任务详情
     */
    public synchronized XLTaskInfo getTaskInfo(long taskId) {
        XLTaskInfo taskInfo = new XLTaskInfo();
        XLDownloadManager.getInstance().getTaskInfo(taskId,1,taskInfo);
        return taskInfo;
    }

    /**
     * 获取种子文件子任务的详情
     * @param taskId 任务id
     * @param fileIndex 文件位置
     * @return
     */
    public synchronized BtSubTaskDetail getBtSubTaskInfo(long taskId,int fileIndex) {
        BtSubTaskDetail subTaskDetail = new BtSubTaskDetail();
        XLDownloadManager.getInstance().getBtSubTaskInfo(taskId,fileIndex,subTaskDetail);
        return subTaskDetail;
    }

    /**
     * 开启dcdn加速
     * @param taskId 任务id
     * @param btFileIndex 需要加速的种子文件index
     */
    public synchronized void startDcdn(long taskId,int btFileIndex) {
        XLDownloadManager.getInstance().startDcdn(taskId, btFileIndex, "", "", "");
    }

    /**
     * 停止dcdn加速
     * @param taskId 任务id
     * @param btFileIndex 需要加速的种子文件index
     */
    public synchronized void stopDcdn(long taskId,int btFileIndex) {
        XLDownloadManager.getInstance().stopDcdn(taskId, btFileIndex);
    }
}
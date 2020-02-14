package com.xunlei.downloadlib;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.ghost.thunder.BuildConfig;
import com.xunlei.downloadlib.android.XLLog;
import com.xunlei.downloadlib.android.XLUtil;
import com.xunlei.downloadlib.android.XLUtil.GUID_TYPE;
import com.xunlei.downloadlib.android.XLUtil.GuidInfo;
import com.xunlei.downloadlib.parameter.BtIndexSet;
import com.xunlei.downloadlib.parameter.BtSubTaskDetail;
import com.xunlei.downloadlib.parameter.BtTaskParam;
import com.xunlei.downloadlib.parameter.BtTaskStatus;
import com.xunlei.downloadlib.parameter.CIDTaskParam;
import com.xunlei.downloadlib.parameter.EmuleTaskParam;
import com.xunlei.downloadlib.parameter.ErrorCodeToMsg;
import com.xunlei.downloadlib.parameter.GetDownloadHead;
import com.xunlei.downloadlib.parameter.GetDownloadLibVersion;
import com.xunlei.downloadlib.parameter.GetFileName;
import com.xunlei.downloadlib.parameter.GetTaskId;
import com.xunlei.downloadlib.parameter.InitParam;
import com.xunlei.downloadlib.parameter.MagnetTaskParam;
import com.xunlei.downloadlib.parameter.MaxDownloadSpeedParam;
import com.xunlei.downloadlib.parameter.P2spTaskParam;
import com.xunlei.downloadlib.parameter.PeerResourceParam;
import com.xunlei.downloadlib.parameter.ServerResourceParam;
import com.xunlei.downloadlib.parameter.ThunderUrlInfo;
import com.xunlei.downloadlib.parameter.TorrentInfo;
import com.xunlei.downloadlib.parameter.UrlQuickInfo;
import com.xunlei.downloadlib.parameter.XLConstant;
import com.xunlei.downloadlib.parameter.XLConstant.XLErrorCode;
import com.xunlei.downloadlib.parameter.XLConstant.XLManagerStatus;
import com.xunlei.downloadlib.parameter.XLProductInfo;
import com.xunlei.downloadlib.parameter.XLSessionInfo;
import com.xunlei.downloadlib.parameter.XLTaskInfo;
import com.xunlei.downloadlib.parameter.XLTaskInfoEx;
import com.xunlei.downloadlib.parameter.XLTaskLocalUrl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class XLDownloadManager {
    private static final int GET_GUID_FIRST_TIME = 5000;
    private static final int GET_GUID_INTERVAL_TIME = 60000;
    private static final int QUERY_GUID_COUNT = 5;
    private static final String TAG = "XLDownloadManager";
    private static boolean mAllowExecution = true;
    public static XLManagerStatus mDownloadManagerState = XLManagerStatus.MANAGER_UNINIT;
    private static Map<String, Object> mErrcodeStringMap = null;
    private static XLDownloadManager mInstance = null;
    private static boolean mIsLoadErrcodeMsg = false;
    private static int mRunningRefCount = 0;
    private XLAppKeyChecker mAppkeyChecker;
    private Context mContext;
    private Timer mGetGuidTimer;
    private TimerTask mGetGuidTimerTask;
    private XLLoader mLoader;
    private int mQueryGuidCount;
    private NetworkChangeReceiver mReceiver;

    class MyTimerTask extends TimerTask {
        final XLDownloadManager this$0;

        MyTimerTask(XLDownloadManager xLDownloadManager) {
            this.this$0 = xLDownloadManager;
        }

        public void run() {
            if (XLDownloadManager.this.mQueryGuidCount < 5) {
                GuidInfo guidInfo = new GuidInfo();
                guidInfo = XLUtil.generateGuid(XLDownloadManager.this.mContext);
                if (guidInfo.mType == GUID_TYPE.ALL) {
                }
                if (guidInfo.mType != GUID_TYPE.DEFAULT) {
                    XLDownloadManager.this.setLocalProperty("Guid", guidInfo.mGuid);
                }
            }
        }
    }

    class NetworkChangeHandlerThread implements Runnable {
        private boolean m_allow_execution = true;
        private Context m_context = null;
        private XLLoader m_loader = null;
        final XLDownloadManager this$0;

        public NetworkChangeHandlerThread(XLDownloadManager xLDownloadManager, Context context, XLLoader xLLoader, boolean z) {
            this.this$0 = xLDownloadManager;
            this.m_context = context;
            this.m_loader = xLLoader;
            this.m_allow_execution = z;
        }

        public void run() {
            if (this.m_allow_execution) {
                int networkTypeComplete = XLUtil.getNetworkTypeComplete(this.m_context);
                XLLog.d(XLDownloadManager.TAG, "NetworkChangeHandlerThread nettype=" + networkTypeComplete);
                this.this$0.notifyNetWorkType(networkTypeComplete, this.m_loader);
                String bssid = XLUtil.getBSSID(this.m_context);
                XLLog.d(XLDownloadManager.TAG, "NetworkChangeHandlerThread bssid=" + bssid);
                this.this$0.notifyWifiBSSID(bssid, this.m_loader);
                XLUtil.NetWorkCarrier netWorkCarrier = XLUtil.getNetWorkCarrier(this.m_context);
                XLLog.d(XLDownloadManager.TAG, "NetworkChangeHandlerThread NetWorkCarrier=" + netWorkCarrier);
                this.this$0.notifyNetWorkCarrier(netWorkCarrier.ordinal());
            }
        }
    }

    class NetworkChangeReceiver extends BroadcastReceiver {
        private static final String TAG = "TAG_DownloadReceiver";
        final XLDownloadManager this$0;

        public NetworkChangeReceiver(XLDownloadManager xLDownloadManager) {
            this.this$0 = xLDownloadManager;
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && action.equals("android.net.conn.CONNECTIVITY_CHANGE")) {
                new Thread(new NetworkChangeHandlerThread(this.this$0, context, XLDownloadManager.this.mLoader, XLDownloadManager.mAllowExecution)).start();
            }
        }
    }

    public static synchronized XLDownloadManager getInstance() {
        XLDownloadManager xLDownloadManager;
        synchronized (XLDownloadManager.class) {
            synchronized (XLDownloadManager.class) {
                if (mInstance == null) {
                    mInstance = new XLDownloadManager();
                }
                xLDownloadManager = mInstance;
            }
        }
        return xLDownloadManager;
    }

    private XLDownloadManager() {
        this.mLoader = null;
        this.mContext = null;
        this.mReceiver = null;
        this.mAppkeyChecker = null;
        this.mQueryGuidCount = 0;
        this.mLoader = new XLLoader();
        XLLog.init(new File(Environment.getExternalStorageDirectory().getPath(), "xunlei_ds_log.ini").getPath());
    }

    public XLManagerStatus getManagerStatus() {
        return mDownloadManagerState;
    }

    private void doMonitorNetworkChange() {
        XLLog.i(TAG, "doMonitorNetworkChange()");
        if (this.mContext != null && this.mReceiver == null) {
            this.mReceiver = new NetworkChangeReceiver(this);
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            XLLog.i(TAG, "register Receiver");
            this.mContext.registerReceiver(this.mReceiver, intentFilter);
        }
    }

    private void undoMonitorNetworkChange() {
        XLLog.i(TAG, "undoMonitorNetworkChange()");
        if (this.mContext != null && this.mReceiver != null) {
            try {
                this.mContext.unregisterReceiver(this.mReceiver);
                XLLog.i(TAG, "unregister Receiver");
            } catch (IllegalArgumentException e) {
                XLLog.e(TAG, "Receiver not registered");
            }
            this.mReceiver = null;
        }
    }

    private synchronized void increRefCount() {
        mRunningRefCount++;
    }

    private synchronized void decreRefCount() {
        mRunningRefCount--;
    }

    public synchronized int init(Context context, InitParam initParam) {
        return init(context, initParam, true);
    }

    public synchronized int init2(Context context, InitParam initParam) {
        return init2(context, initParam, true);
    }
    public synchronized int init(Context context, InitParam initParam, boolean z) {
        int i;
        i = XLErrorCode.DOWNLOAD_MANAGER_ERROR;
        int i2 = 0;
        synchronized (this) {
            if (!mIsLoadErrcodeMsg) {
                loadErrcodeString(context);
                mIsLoadErrcodeMsg = true;
            }
            if (!(context == null || initParam == null || !initParam.checkMemberVar())) {
                this.mContext = context;
                mAllowExecution = z;
                if (mDownloadManagerState == XLManagerStatus.MANAGER_RUNNING) {
                    XLLog.i(TAG, "XLDownloadManager is already init");
                } else if (this.mLoader != null) {
                    String peerid = getPeerid();
                    String guid = getGuid();
                    XLLog.i(TAG, "Peerid:" + new String(Base64.encode(peerid.getBytes(), 0)));
                    XLLog.i(TAG, "Guid:" + new String(Base64.encode(guid.getBytes(), 0)));
                    if (mAllowExecution) {
                        i2 = XLUtil.getNetworkTypeComplete(context);
                    }
                    try {
                        i = this.mLoader.init(context, initParam.mAppVersion, "", peerid, guid, initParam.mStatSavePath, initParam.mStatCfgSavePath, i2, initParam.mPermissionLevel);
//                        i = this.mLoader.init(context, initParam.mAppVersion, BuildConfig.FLAVOR, peerid, guid, initParam.mStatSavePath, initParam.mStatCfgSavePath, i2, initParam.mPermissionLevel);
                        Log.e(TAG, "loader init end: + " + i);
                        if (i != XLErrorCode.NO_ERROR) {
                            mDownloadManagerState = XLManagerStatus.MANAGER_INIT_FAIL;
                            XLLog.e(TAG, "XLDownloadManager init failed ret=" + i);
                        } else {
                            mDownloadManagerState = XLManagerStatus.MANAGER_RUNNING;
                            doMonitorNetworkChange();
                            setLocalProperty("PhoneModel", Build.MODEL);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return i;
    }

    public int init2(Context context, InitParam initParam, boolean z) {
        int i;
        int i2 = 0;
        synchronized (this) {
            i = XLErrorCode.DOWNLOAD_MANAGER_ERROR;
            synchronized (this) {
                if (!mIsLoadErrcodeMsg) {
                    loadErrcodeString(context);
                    mIsLoadErrcodeMsg = true;
                }
                if (!(context == null || initParam == null || !initParam.checkMemberVar())) {
                    this.mContext = context;
                    mAllowExecution = z;
                    if (mDownloadManagerState == XLManagerStatus.MANAGER_RUNNING) {
                        XLLog.i(TAG, "XLDownloadManager is already init");
                    } else if (this.mLoader != null) {
                        String peerid = getPeerid();
                        String guid = getGuid();
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("Peerid:");
                        stringBuilder.append(new String(Base64.encode(peerid.getBytes(), 0)));
                        XLLog.i(TAG, stringBuilder.toString());
                        stringBuilder = new StringBuilder();
                        stringBuilder.append("Guid:");
                        stringBuilder.append(new String(Base64.encode(guid.getBytes(), 0)));
                        XLLog.i(TAG, stringBuilder.toString());
                        if (mAllowExecution) {
                            i2 = XLUtil.getNetworkTypeComplete(context);
                        }
                        i = this.mLoader.init(initParam.mAppKey, "com.xunlei.downloadprovider", initParam.mAppVersion, "", peerid, guid, initParam.mStatSavePath, initParam.mStatCfgSavePath, i2, initParam.mPermissionLevel);
                        if (i != XLErrorCode.NO_ERROR) {
                            mDownloadManagerState = XLManagerStatus.MANAGER_INIT_FAIL;
                        } else {
                            mDownloadManagerState = XLManagerStatus.MANAGER_RUNNING;
                            doMonitorNetworkChange();
                            setLocalProperty("PhoneModel", Build.MODEL);
                        }
                    }
                }
            }
        }
        return i;
    }



    public synchronized int uninit() {
        int i;
        i = XLErrorCode.DOWNLOAD_MANAGER_ERROR;
        synchronized (this) {
            if (mRunningRefCount != 0) {
                XLLog.i(TAG, "some function of XLDownloadManager is running, uninit failed!");
            } else if (!(mDownloadManagerState == XLManagerStatus.MANAGER_UNINIT || this.mLoader == null)) {
                if (mDownloadManagerState == XLManagerStatus.MANAGER_RUNNING) {
                    undoMonitorNetworkChange();
                }
                stopGetGuidTimer();
                i = this.mLoader.unInit();
                mDownloadManagerState = XLManagerStatus.MANAGER_UNINIT;
                this.mContext = null;
            }
        }
        return i;
    }

    int notifyNetWorkType(int i, XLLoader xLLoader) {
        int i2 = XLErrorCode.DOWNLOAD_MANAGER_ERROR;
        if (mDownloadManagerState != XLManagerStatus.MANAGER_RUNNING || xLLoader == null) {
            return i2;
        }
        try {
            return xLLoader.notifyNetWorkType(i);
        } catch (Error e) {
            XLLog.e(TAG, "notifyNetWorkType failed," + e.getMessage());
            return i2;
        }
    }

    public int createP2spTask(P2spTaskParam p2spTaskParam, GetTaskId getTaskId) {
        int i = XLErrorCode.DOWNLOAD_MANAGER_ERROR;
        if (!(p2spTaskParam == null || getTaskId == null || !p2spTaskParam.checkMemberVar())) {
            increRefCount();
            if (mDownloadManagerState == XLManagerStatus.MANAGER_RUNNING && this.mLoader != null) {
                i = this.mLoader.createP2spTask(p2spTaskParam.mUrl, p2spTaskParam.mRefUrl, p2spTaskParam.mCookie, p2spTaskParam.mUser, p2spTaskParam.mPass, p2spTaskParam.mFilePath, p2spTaskParam.mFileName, p2spTaskParam.mCreateMode, p2spTaskParam.mSeqId, getTaskId);
            }
            decreRefCount();
        }
        return i;
    }

    public int releaseTask(long j) {
        int i = XLErrorCode.DOWNLOAD_MANAGER_ERROR;
        increRefCount();
        if (mDownloadManagerState == XLManagerStatus.MANAGER_RUNNING && this.mLoader != null) {
            i = this.mLoader.releaseTask(j);
        }
        decreRefCount();
        return i;
    }

    int setTaskAppInfo(long j, String str, String str2, String str3) {
        if (mDownloadManagerState != XLManagerStatus.MANAGER_RUNNING || this.mLoader == null || str == null || str2 == null || str3 == null) {
            return XLErrorCode.DOWNLOAD_MANAGER_ERROR;
        }
        return this.mLoader.setTaskAppInfo(j, str, str2, str3);
    }

    public int setTaskAllowUseResource(long j, int i) {
        int i2 = XLErrorCode.DOWNLOAD_MANAGER_ERROR;
        increRefCount();
        if (mDownloadManagerState == XLManagerStatus.MANAGER_RUNNING && this.mLoader != null) {
            i2 = this.mLoader.setTaskAllowUseResource(j, i);
        }
        decreRefCount();
        return i2;
    }

    public int setTaskUid(long j, int i) {
        int i2 = XLErrorCode.DOWNLOAD_MANAGER_ERROR;
        increRefCount();
        if (mDownloadManagerState == XLManagerStatus.MANAGER_RUNNING && this.mLoader != null) {
            i2 = this.mLoader.setTaskUid(j, i);
        }
        decreRefCount();
        return i2;
    }

    public int startTask(long j, boolean z) {
        int i = XLErrorCode.DOWNLOAD_MANAGER_ERROR;
        increRefCount();
        if (mDownloadManagerState == XLManagerStatus.MANAGER_RUNNING && this.mLoader != null) {
            i = this.mLoader.startTask(j, z);
        }
        decreRefCount();
        return i;
    }

    public int startTask(long j) {
        return startTask(j, false);
    }

    int switchOriginToAllResDownload(long j) {
        if (mDownloadManagerState != XLManagerStatus.MANAGER_RUNNING || this.mLoader == null) {
            return XLErrorCode.DOWNLOAD_MANAGER_ERROR;
        }
        return this.mLoader.switchOriginToAllResDownload(j);
    }

    public int stopTask(long j) {
        int i = XLErrorCode.DOWNLOAD_MANAGER_ERROR;
        increRefCount();
        if (mDownloadManagerState == XLManagerStatus.MANAGER_RUNNING && this.mLoader != null) {
            i = this.mLoader.stopTask(j);
        }
        XLLog.i(TAG, "XLStopTask()----- ret=" + i);
        decreRefCount();
        return i;
    }

    public int stopTaskWithReason(long j, int i) {
        int i2 = XLErrorCode.DOWNLOAD_MANAGER_ERROR;
        increRefCount();
        if (mDownloadManagerState == XLManagerStatus.MANAGER_RUNNING && this.mLoader != null) {
            i2 = this.mLoader.stopTaskWithReason(j, i);
        }
        XLLog.i(TAG, "XLStopTask()----- ret=" + i2);
        decreRefCount();
        return i2;
    }

    public int getTaskInfo(long j, int i, XLTaskInfo xLTaskInfo) {
        int i2 = XLErrorCode.DOWNLOAD_MANAGER_ERROR;
        increRefCount();
        if (!(mDownloadManagerState != XLManagerStatus.MANAGER_RUNNING || this.mLoader == null || xLTaskInfo == null)) {
            i2 = this.mLoader.getTaskInfo(j, i, xLTaskInfo);
        }
        decreRefCount();
        return i2;
    }

    public int getTaskInfoEx(long j, XLTaskInfoEx xLTaskInfoEx) {
        int i = XLErrorCode.DOWNLOAD_MANAGER_ERROR;
        increRefCount();
        if (!(mDownloadManagerState != XLManagerStatus.MANAGER_RUNNING || this.mLoader == null || xLTaskInfoEx == null)) {
            i = this.mLoader.getTaskInfoEx(j, xLTaskInfoEx);
        }
        decreRefCount();
        return i;
    }

    public int getLocalUrl(String str, XLTaskLocalUrl xLTaskLocalUrl) {
        int i = XLErrorCode.DOWNLOAD_MANAGER_ERROR;
        increRefCount();
        if (!(mDownloadManagerState != XLManagerStatus.MANAGER_RUNNING || this.mLoader == null || xLTaskLocalUrl == null)) {
            i = this.mLoader.getLocalUrl(str, xLTaskLocalUrl);
        }
        decreRefCount();
        return i;
    }

    public int addServerResource(long j, ServerResourceParam serverResourceParam) {
        int i = XLErrorCode.DOWNLOAD_MANAGER_ERROR;
        if (serverResourceParam != null && serverResourceParam.checkMemberVar()) {
            increRefCount();
            if (mDownloadManagerState == XLManagerStatus.MANAGER_RUNNING && this.mLoader != null) {
                XLLog.i(TAG, "respara.mUrl=" + serverResourceParam.mUrl);
                i = this.mLoader.addServerResource(j, serverResourceParam.mUrl, serverResourceParam.mRefUrl, serverResourceParam.mCookie, serverResourceParam.mResType, serverResourceParam.mStrategy);
            }
            decreRefCount();
        }
        return i;
    }

    public int addPeerResource(long j, PeerResourceParam peerResourceParam) {
        int i = XLErrorCode.DOWNLOAD_MANAGER_ERROR;
        if (peerResourceParam == null || !peerResourceParam.checkMemberVar()) {
            return XLErrorCode.DOWNLOAD_MANAGER_ERROR;
        }
        increRefCount();
        if (mDownloadManagerState == XLManagerStatus.MANAGER_RUNNING && this.mLoader != null) {
            i = this.mLoader.addPeerResource(j, peerResourceParam.mPeerId, peerResourceParam.mUserId, peerResourceParam.mJmpKey, peerResourceParam.mVipCdnAuth, peerResourceParam.mInternalIp, peerResourceParam.mTcpPort, peerResourceParam.mUdpPort, peerResourceParam.mResLevel, peerResourceParam.mResPriority, peerResourceParam.mCapabilityFlag, peerResourceParam.mResType);
        }
        decreRefCount();
        return i;
    }

    public int removeServerResource(long j, int i) {
        int i2 = XLErrorCode.DOWNLOAD_MANAGER_ERROR;
        increRefCount();
        if (mDownloadManagerState == XLManagerStatus.MANAGER_RUNNING && this.mLoader != null) {
            i2 = this.mLoader.removeAddedServerResource(j, i);
        }
        decreRefCount();
        return i2;
    }

    int requeryTaskIndex(long j) {
        if (mDownloadManagerState != XLManagerStatus.MANAGER_RUNNING || this.mLoader == null) {
            return XLErrorCode.DOWNLOAD_MANAGER_ERROR;
        }
        return this.mLoader.requeryIndex(j);
    }

    public int setOriginUserAgent(long j, String str) {
        int i = XLErrorCode.DOWNLOAD_MANAGER_ERROR;
        increRefCount();
        if (!(mDownloadManagerState != XLManagerStatus.MANAGER_RUNNING || this.mLoader == null || str == null)) {
            i = this.mLoader.setOriginUserAgent(j, str);
        }
        decreRefCount();
        return i;
    }

    public int setUserId(String str) {
        if (mDownloadManagerState != XLManagerStatus.MANAGER_RUNNING || this.mLoader == null || str == null) {
            return XLErrorCode.DOWNLOAD_MANAGER_ERROR;
        }
        return this.mLoader.setUserId(str);
    }

    public int getDownloadHeader(long j, GetDownloadHead getDownloadHead) {
        int i = XLErrorCode.DOWNLOAD_MANAGER_ERROR;
        increRefCount();
        if (!(mDownloadManagerState != XLManagerStatus.MANAGER_RUNNING || this.mLoader == null || getDownloadHead == null)) {
            i = this.mLoader.getDownloadHeader(j, getDownloadHead);
        }
        decreRefCount();
        return i;
    }

    public int setFileName(long j, String str) {
        int i = XLErrorCode.DOWNLOAD_MANAGER_ERROR;
        increRefCount();
        if (!(mDownloadManagerState != XLManagerStatus.MANAGER_RUNNING || this.mLoader == null || str == null)) {
            i = this.mLoader.setFileName(j, str);
        }
        decreRefCount();
        return i;
    }

    int notifyNetWorkCarrier(int i) {
        if (mDownloadManagerState != XLManagerStatus.MANAGER_RUNNING || this.mLoader == null) {
            return XLErrorCode.DOWNLOAD_MANAGER_ERROR;
        }
        return this.mLoader.setNotifyNetWorkCarrier(i);
    }

    int notifyWifiBSSID(String str, XLLoader xLLoader) {
        int i = XLErrorCode.DOWNLOAD_MANAGER_ERROR;
        if (mDownloadManagerState != XLManagerStatus.MANAGER_RUNNING || xLLoader == null) {
            return i;
        }
        if (str == null || str.length() == 0 || str == "<unknown ssid>") {
            str = "";
        }
        try {
            return xLLoader.setNotifyWifiBSSID(str);
        } catch (Error e) {
            XLLog.e(TAG, "setNotifyWifiBSSID failed," + e.getMessage());
            return i;
        }
    }

    public int setDownloadTaskOrigin(long j, String str) {
        int i = XLErrorCode.DOWNLOAD_MANAGER_ERROR;
        increRefCount();
        if (!(mDownloadManagerState != XLManagerStatus.MANAGER_RUNNING || this.mLoader == null || str == null)) {
            i = this.mLoader.setDownloadTaskOrigin(j, str);
        }
        decreRefCount();
        return i;
    }

    int setMac(String str) {
        if (mDownloadManagerState != XLManagerStatus.MANAGER_RUNNING || this.mLoader == null || str == null) {
            return XLErrorCode.DOWNLOAD_MANAGER_ERROR;
        }
        return this.mLoader.setMac(str);
    }

    int setImei(String str) {
        if (mDownloadManagerState != XLManagerStatus.MANAGER_RUNNING || this.mLoader == null || str == null) {
            return XLErrorCode.DOWNLOAD_MANAGER_ERROR;
        }
        return this.mLoader.setImei(str);
    }

    private int setLocalProperty(String str, String str2) {
        if (mDownloadManagerState != XLManagerStatus.MANAGER_RUNNING || this.mLoader == null || str == null || str2 == null) {
            return XLErrorCode.DOWNLOAD_MANAGER_ERROR;
        }
        return this.mLoader.setLocalProperty(str, str2);
    }

    public int setOSVersion(String str) {
        int i = XLErrorCode.DOWNLOAD_MANAGER_ERROR;
        increRefCount();
        if (!(mDownloadManagerState != XLManagerStatus.MANAGER_RUNNING || this.mLoader == null || str == null)) {
            i = this.mLoader.setMiUiVersion(str);
        }
        decreRefCount();
        return i;
    }

    public int setHttpHeaderProperty(long j, String str, String str2) {
        int i = XLErrorCode.DOWNLOAD_MANAGER_ERROR;
        increRefCount();
        if (!(mDownloadManagerState != XLManagerStatus.MANAGER_RUNNING || this.mLoader == null || str == null || str2 == null)) {
            i = this.mLoader.setHttpHeaderProperty(j, str, str2);
        }
        decreRefCount();
        return i;
    }

    public int getDownloadLibVersion(GetDownloadLibVersion getDownloadLibVersion) {
        int i = XLErrorCode.DOWNLOAD_MANAGER_ERROR;
        increRefCount();
        if (!(mDownloadManagerState != XLManagerStatus.MANAGER_RUNNING || this.mLoader == null || getDownloadLibVersion == null)) {
            i = this.mLoader.getDownloadLibVersion(getDownloadLibVersion);
        }
        decreRefCount();
        return i;
    }

    public int getProductInfo(XLProductInfo xLProductInfo) {
        increRefCount();
        if (mDownloadManagerState != XLManagerStatus.MANAGER_RUNNING || this.mContext == null || xLProductInfo == null) {
            decreRefCount();
            return XLErrorCode.DOWNLOAD_MANAGER_ERROR;
        }
        xLProductInfo.mProductKey = this.mAppkeyChecker.getSoAppKey();
        xLProductInfo.mProductName = this.mContext.getPackageName();
        return XLErrorCode.NO_ERROR;
    }

    private String getPeerid() {
        if (!mAllowExecution) {
            return "000000000000000V";
        }
        String peerid = XLUtil.getPeerid(this.mContext);
        if (peerid == null) {
            return "000000000000000V";
        }
        return peerid;
    }

    private String getGuid() {
        if (!mAllowExecution) {
            return "00000000000000_000000000000";
        }
        GuidInfo guidInfo = new GuidInfo();
        guidInfo = XLUtil.generateGuid(this.mContext);
        if (guidInfo.mType != GUID_TYPE.ALL) {
            XLLog.i(TAG, "Start the GetGuidTimer");
            startGetGuidTimer();
        }
        return guidInfo.mGuid;
    }

    private void startGetGuidTimer() {
        this.mGetGuidTimer = new Timer();
        this.mGetGuidTimerTask = new MyTimerTask(this);
        this.mGetGuidTimer.schedule(this.mGetGuidTimerTask, 5000, 60000);
    }

    private void stopGetGuidTimer() {
        if (this.mGetGuidTimer instanceof Timer) {
            this.mGetGuidTimer.cancel();
            this.mGetGuidTimer.purge();
            this.mGetGuidTimer = null;
            XLLog.i(TAG, "stopGetGuidTimer");
        }
        if (this.mGetGuidTimerTask instanceof TimerTask) {
            this.mGetGuidTimerTask.cancel();
            this.mGetGuidTimerTask = null;
        }
    }

    public int enterPrefetchMode(long j) {
        int i = XLErrorCode.DOWNLOAD_MANAGER_ERROR;
        increRefCount();
        if (mDownloadManagerState == XLManagerStatus.MANAGER_RUNNING && this.mLoader != null) {
            i = this.mLoader.enterPrefetchMode(j);
        }
        decreRefCount();
        return i;
    }

    public int setTaskLxState(long j, int i, int i2) {
        int i3 = XLErrorCode.DOWNLOAD_MANAGER_ERROR;
        increRefCount();
        if (mDownloadManagerState == XLManagerStatus.MANAGER_RUNNING && this.mLoader != null) {
            i3 = this.mLoader.setTaskLxState(j, i, i2);
        }
        decreRefCount();
        return i3;
    }

    public int setTaskGsState(long j, int i, int i2) {
        int i3 = XLErrorCode.DOWNLOAD_MANAGER_ERROR;
        increRefCount();
        if (mDownloadManagerState == XLManagerStatus.MANAGER_RUNNING && this.mLoader != null) {
            i3 = this.mLoader.setTaskGsState(j, i, i2);
        }
        decreRefCount();
        return i3;
    }

    public int setReleaseLog(boolean z, String str, int i, int i2) {
        int i3 = XLErrorCode.DOWNLOAD_MANAGER_ERROR;
        increRefCount();
        if (mDownloadManagerState == XLManagerStatus.MANAGER_RUNNING && this.mLoader != null) {
            i3 = z ? this.mLoader.setReleaseLog(1, str, i, i2) : this.mLoader.setReleaseLog(0, null, 0, 0);
        }
        decreRefCount();
        return i3;
    }

    public int setReleaseLog(boolean z, String str) {
        return setReleaseLog(z, str, 0, 0);
    }

    public boolean isLogTurnOn() {
        boolean z = false;
        increRefCount();
        if (mDownloadManagerState == XLManagerStatus.MANAGER_RUNNING && this.mLoader != null) {
            z = this.mLoader.isLogTurnOn();
        }
        decreRefCount();
        return z;
    }

    public int setStatReportSwitch(boolean z) {
        int i = XLErrorCode.DOWNLOAD_MANAGER_ERROR;
        increRefCount();
        if (mDownloadManagerState == XLManagerStatus.MANAGER_RUNNING && this.mLoader != null) {
            i = this.mLoader.setStatReportSwitch(z);
        }
        decreRefCount();
        return i;
    }

    public int createBtMagnetTask(MagnetTaskParam magnetTaskParam, GetTaskId getTaskId) {
        int i = XLErrorCode.DOWNLOAD_MANAGER_ERROR;
        if (!(magnetTaskParam == null || getTaskId == null || !magnetTaskParam.checkMemberVar())) {
            increRefCount();
            if (mDownloadManagerState == XLManagerStatus.MANAGER_RUNNING && this.mLoader != null) {
                i = this.mLoader.createBtMagnetTask(magnetTaskParam.mUrl, magnetTaskParam.mFilePath, magnetTaskParam.mFileName, getTaskId);
            }
            decreRefCount();
        }
        return i;
    }

    public int createEmuleTask(EmuleTaskParam emuleTaskParam, GetTaskId getTaskId) {
        int i = XLErrorCode.DOWNLOAD_MANAGER_ERROR;
        if (!(emuleTaskParam == null || getTaskId == null || !emuleTaskParam.checkMemberVar())) {
            increRefCount();
            if (mDownloadManagerState == XLManagerStatus.MANAGER_RUNNING && this.mLoader != null) {
                i = this.mLoader.createEmuleTask(emuleTaskParam.mUrl, emuleTaskParam.mFilePath, emuleTaskParam.mFileName, emuleTaskParam.mCreateMode, emuleTaskParam.mSeqId, getTaskId);
            }
            decreRefCount();
        }
        return i;
    }

    public int createBtTask(BtTaskParam btTaskParam, GetTaskId getTaskId) {
        int i = XLErrorCode.DOWNLOAD_MANAGER_ERROR;
        if (!(btTaskParam == null || getTaskId == null || !btTaskParam.checkMemberVar())) {
            increRefCount();
            if (mDownloadManagerState == XLManagerStatus.MANAGER_RUNNING && this.mLoader != null) {
                i = this.mLoader.createBtTask(btTaskParam.mTorrentPath, btTaskParam.mFilePath, btTaskParam.mMaxConcurrent, btTaskParam.mCreateMode, btTaskParam.mSeqId, getTaskId);
            }
            decreRefCount();
        }
        return i;
    }

    public int getTorrentInfo(String str, TorrentInfo torrentInfo) {
        int i = XLErrorCode.DOWNLOAD_MANAGER_ERROR;
        increRefCount();
        if (!(this.mLoader == null || str == null || torrentInfo == null)) {
            i = this.mLoader.getTorrentInfo(str, torrentInfo);
        }
        decreRefCount();
        return i;
    }

    public int getBtSubTaskStatus(long j, BtTaskStatus btTaskStatus, int i, int i2) {
        int i3 = XLErrorCode.DOWNLOAD_MANAGER_ERROR;
        increRefCount();
        if (!(mDownloadManagerState != XLManagerStatus.MANAGER_RUNNING || this.mLoader == null || btTaskStatus == null)) {
            i3 = this.mLoader.getBtSubTaskStatus(j, btTaskStatus, i, i2);
        }
        decreRefCount();
        return i3;
    }

    public int getBtSubTaskInfo(long j, int i, BtSubTaskDetail btSubTaskDetail) {
        int i2 = XLErrorCode.DOWNLOAD_MANAGER_ERROR;
        increRefCount();
        if (!(mDownloadManagerState != XLManagerStatus.MANAGER_RUNNING || this.mLoader == null || btSubTaskDetail == null)) {
            i2 = this.mLoader.getBtSubTaskInfo(j, i, btSubTaskDetail);
        }
        decreRefCount();
        return i2;
    }

    public int selectBtSubTask(long j, BtIndexSet btIndexSet) {
        int i = XLErrorCode.DOWNLOAD_MANAGER_ERROR;
        increRefCount();
        if (!(mDownloadManagerState != XLManagerStatus.MANAGER_RUNNING || this.mLoader == null || btIndexSet == null)) {
            i = this.mLoader.selectBtSubTask(j, btIndexSet);
        }
        decreRefCount();
        return i;
    }

    public int deselectBtSubTask(long j, BtIndexSet btIndexSet) {
        int i = XLErrorCode.DOWNLOAD_MANAGER_ERROR;
        increRefCount();
        if (!(mDownloadManagerState != XLManagerStatus.MANAGER_RUNNING || this.mLoader == null || btIndexSet == null)) {
            i = this.mLoader.deselectBtSubTask(j, btIndexSet);
        }
        decreRefCount();
        return i;
    }


    public int btRemoveAddedResource(long j, int i, int i2) {
        int i3 = XLErrorCode.DOWNLOAD_MANAGER_ERROR;
        increRefCount();
        if (mDownloadManagerState == XLManagerStatus.MANAGER_RUNNING && this.mLoader != null) {
            i3 = this.mLoader.btRemoveAddedResource(j, i, i2);
        }
        decreRefCount();
        return i3;
    }

    private void loadErrcodeString(Context context) {
        if (context == null) {
            XLLog.e(TAG, "loadErrcodeString, context invalid");
        } else {
            mErrcodeStringMap = XLUtil.parseJSONString(ErrorCodeToMsg.ErrCodeToMsg);
        }
    }

    public String getErrorCodeMsg(int i) {
        String str = null;
        String num = Integer.toString(i);
        if (!(mErrcodeStringMap == null || num == null)) {
            Object obj = mErrcodeStringMap.get(num);
            if (obj != null) {
                str = obj.toString().trim();
            }
            XLLog.i(TAG, "errcode:" + i + ", errcodeMsg:" + str);
        }
        return str;
    }

    public int getUrlQuickInfo(long j, UrlQuickInfo urlQuickInfo) {
        int i = XLErrorCode.DOWNLOAD_MANAGER_ERROR;
        increRefCount();
        if (!(mDownloadManagerState != XLManagerStatus.MANAGER_RUNNING || this.mLoader == null || urlQuickInfo == null)) {
            i = this.mLoader.getUrlQuickInfo(j, urlQuickInfo);
        }
        decreRefCount();
        return i;
    }

    public int createCIDTask(CIDTaskParam cIDTaskParam, GetTaskId getTaskId) {
        int i = XLErrorCode.DOWNLOAD_MANAGER_ERROR;
        if (!(cIDTaskParam == null || getTaskId == null || !cIDTaskParam.checkMemberVar())) {
            increRefCount();
            if (mDownloadManagerState == XLManagerStatus.MANAGER_RUNNING && this.mLoader != null) {
                i = this.mLoader.createCIDTask(cIDTaskParam.mCid, cIDTaskParam.mGcid, cIDTaskParam.mBcid, cIDTaskParam.mFilePath, cIDTaskParam.mFileName, cIDTaskParam.mFileSize, cIDTaskParam.mCreateMode, cIDTaskParam.mSeqId, getTaskId);
            }
            decreRefCount();
        }
        return i;
    }

    public String parserThunderUrl(String str) {
        int i = XLErrorCode.DOWNLOAD_MANAGER_ERROR;
        ThunderUrlInfo thunderUrlInfo = new ThunderUrlInfo();
        if (!(this.mLoader == null || str == null)) {
            i = this.mLoader.parserThunderUrl(str, thunderUrlInfo);
        }
        if (XLConstant.XLErrorCode.NO_ERROR == i) {
            return thunderUrlInfo.mUrl;
        }
        return null;
    }

    public int getFileNameFromUrl(String str, GetFileName getFileName) {
        if (this.mLoader == null || str == null || getFileName == null) {
            return XLErrorCode.DOWNLOAD_MANAGER_ERROR;
        }
        return this.mLoader.getFileNameFromUrl(str, getFileName);
    }

    public int getNameFromUrl(String str, String str2) {
        if (this.mLoader == null || str == null || str2 == null) {
            return XLErrorCode.DOWNLOAD_MANAGER_ERROR;
        }
        return this.mLoader.getNameFromUrl(str, str2);
    }

    public int setSpeedLimit(long j, long j2) {
        XLLog.d(TAG, "debug: XLDownloadManager::setSpeedLimit beg, maxDownloadSpeed=[" + j + "] maxUploadSpeed=[" + j2 + "]");
        if (this.mLoader == null) {
            XLLog.e(TAG, "error: XLDownloadManager::setSpeedLimit mLoader is null, maxDownloadSpeed=[" + j + "] maxUploadSpeed=[" + j2 + "] ret=[9900]");
            return XLErrorCode.DOWNLOAD_MANAGER_ERROR;
        }
        int speedLimit = this.mLoader.setSpeedLimit(j, j2);
        XLLog.d(TAG, "debug: XLDownloadManager::setSpeedLimit end, maxDownloadSpeed=[" + j + "] maxUploadSpeed=[" + j2 + "] ret=[" + speedLimit + "]");
        return speedLimit;
    }

    public int setBtPriorSubTask(long j, int i) {
        XLLog.d(TAG, "XLDownloadManager::setBtPriorSubTask beg, taskId=[" + j + "] fileIndex=[" + i + "]");
        if (this.mLoader == null) {
            XLLog.e(TAG, "XLDownloadManager::setBtPriorSubTask mLoader is null, taskId=[" + j + "] fileIndex=[" + i + "]");
            return XLErrorCode.DOWNLOAD_MANAGER_ERROR;
        }
        int btPriorSubTask = this.mLoader.setBtPriorSubTask(j, i);
        if (XLErrorCode.NO_ERROR != btPriorSubTask) {
            XLLog.e(TAG, "XLDownloadManager::setBtPriorSubTask end, taskId=[" + j + "] fileIndex=[" + i + "] ret=[" + btPriorSubTask + "]");
            return btPriorSubTask;
        }
        XLLog.d(TAG, " XLDownloadManager::setBtPriorSubTask end, taskId=[" + j + "] fileIndex=[" + i + "]");
        return XLErrorCode.NO_ERROR;
    }

    public int getMaxDownloadSpeed(MaxDownloadSpeedParam maxDownloadSpeedParam) {
        if (this.mLoader == null) {
            XLLog.e(TAG, "XLDownloadManager::getMaxDownloadSpeed mLoader is null");
            return XLErrorCode.DOWNLOAD_MANAGER_ERROR;
        }
        int maxDownloadSpeed = this.mLoader.getMaxDownloadSpeed(maxDownloadSpeedParam);
        if (XLErrorCode.NO_ERROR != maxDownloadSpeed) {
            XLLog.e(TAG, "XLDownloadManager::getMaxDownloadSpeed end, ret=[" + maxDownloadSpeed + "]");
            return maxDownloadSpeed;
        }
        XLLog.d(TAG, "XLDownloadManager::getMaxDownloadSpeed end, speed=[" + maxDownloadSpeedParam.mSpeed + "] ret=[" + maxDownloadSpeed + "]");
        return maxDownloadSpeed;
    }

    public int statExternalInfo(long j, int i, String str, String str2) {
        XLLog.d(TAG, "XLDownloadManager::statExternalInfo beg, taskId=[" + j + "] fileIndex=[" + i + "] key=[" + str + "] value=[" + str2 + "]");
        if (this.mLoader == null) {
            XLLog.e(TAG, "XLDownloadManager::statExternalInfo mLoader is null, taskId=[" + j + "] fileIndex=[" + i + "]");
            return XLErrorCode.DOWNLOAD_MANAGER_ERROR;
        }
        int statExternalInfo = this.mLoader.statExternalInfo(j, i, str, str2);
        if (XLErrorCode.NO_ERROR != statExternalInfo) {
            XLLog.e(TAG, "XLDownloadManager::statExternalInfo end, taskId=[" + j + "] fileIndex=[" + i + "] ret=[" + statExternalInfo + "]");
            return statExternalInfo;
        }
        XLLog.d(TAG, "XLDownloadManager::statExternalInfo end, taskId=[" + j + "] fileIndex=[" + i + "] ret=[" + statExternalInfo + "]");
        return statExternalInfo;
    }

    public int statExternalInfo(long j, int i, String str, int i2) {
        return statExternalInfo(j, i, str, String.valueOf(i2));
    }

    public int clearTaskFile(String str) {
        XLLog.d(TAG, "XLDownloadManager::clearTaskFile filePath=[" + str + "]");
        if (TextUtils.isEmpty(str)) {
            return XLErrorCode.DOWNLOAD_MANAGER_ERROR;
        }
        if (this.mLoader == null) {
            XLLog.e(TAG, "XLDownloadManager::clearTaskFile mLoader is null");
            return XLErrorCode.DOWNLOAD_MANAGER_ERROR;
        }
        int clearTaskFile = this.mLoader.clearTaskFile(str);
        if (XLErrorCode.NO_ERROR == clearTaskFile) {
            return XLErrorCode.NO_ERROR;
        }
        XLLog.e(TAG, "XLDownloadManager::clearTaskFile end, ret=[" + clearTaskFile + "]");
        return clearTaskFile;
    }

    public int startDcdn(long j, int i, String str, String str2, String str3) {
        int i2 = XLErrorCode.DOWNLOAD_MANAGER_ERROR;
        increRefCount();
        if (mDownloadManagerState == XLManagerStatus.MANAGER_RUNNING && this.mLoader != null) {
            i2 = this.mLoader.startDcdn(j, i, str, str2, str3);
        }
        decreRefCount();
        XLLog.d(TAG, String.format("XLDownloadManager::startDcdn ret=[%d] taskId=[%d] subIndex=[%d] sessionId=[%s] productType=[%s] verifyInfo=[%s]", new Object[]{Integer.valueOf(i2), Long.valueOf(j), Integer.valueOf(i), str, str2, str3}));
        return i2;
    }

    public int stopDcdn(long j, int i) {
        int i2 = XLErrorCode.DOWNLOAD_MANAGER_ERROR;
        increRefCount();
        if (mDownloadManagerState == XLManagerStatus.MANAGER_RUNNING && this.mLoader != null) {
            i2 = this.mLoader.stopDcdn(j, i);
        }
        decreRefCount();
        XLLog.d(TAG, String.format("XLDownloadManager::stopDcdn ret=[%d] taskId=[%d] subIndex=[%d]", new Object[]{Integer.valueOf(i2), Long.valueOf(j), Integer.valueOf(i)}));
        return i2;
    }

    public int createShortVideoTask(String str, String str2, String str3, String str4, int i, int i2, int i3, GetTaskId getTaskId) {
        XLLog.d(TAG, "XLDownloadManager::createShortVideoTask beg, url=[" + str + "] path=[" + str2 + "] filename=[" + str3 + "] title=[" + str4 + "]");
        if (this.mLoader == null) {
            XLLog.e(TAG, "XLDownloadManager::createShortVideoTask mLoader is null");
            return XLErrorCode.DOWNLOAD_MANAGER_ERROR;
        }
        String str5;
        if (str4 == null) {
            str5 = "default Title";
        } else {
            str5 = str4;
        }
        int createShortVideoTask = this.mLoader.createShortVideoTask(str, str2, str3, str5, i, i2, i3, getTaskId);
        if (XLErrorCode.NO_ERROR != createShortVideoTask) {
            XLLog.e(TAG, "XLDownloadManager::createShortVideoTask end, ret=[" + createShortVideoTask + "]");
            return createShortVideoTask;
        }
        XLLog.d(TAG, "XLDownloadManager::createShortVideoTask end, taskId=[" + getTaskId.getTaskId() + "] ret=[" + createShortVideoTask + "]");
        return createShortVideoTask;
    }

    public int playShortVideoBegin(long j) {
        XLLog.d(TAG, "XLDownloadManager::playShortVideoBegin beg, taskId=[" + j + "]");
        if (this.mLoader == null) {
            XLLog.e(TAG, "XLDownloadManager::playShortVideoBegin mLoader is null");
            return XLErrorCode.DOWNLOAD_MANAGER_ERROR;
        }
        int playShortVideoBegin = this.mLoader.playShortVideoBegin(j);
        if (XLErrorCode.NO_ERROR != playShortVideoBegin) {
            XLLog.e(TAG, "XLDownloadManager::playShortVideoBegin end, ret=[" + playShortVideoBegin + "]");
            return playShortVideoBegin;
        }
        XLLog.d(TAG, "XLDownloadManager::playShortVideoBegin end, taskId=[" + j + "] ret=[" + playShortVideoBegin + "]");
        return playShortVideoBegin;
    }

    public int getSessionInfoByUrl(String str, XLSessionInfo xLSessionInfo) {
        if (this.mLoader == null) {
            XLLog.e(TAG, "XLDownloadManager::getSessionInfoByUrl mLoader is null");
            return XLErrorCode.DOWNLOAD_MANAGER_ERROR;
        }
        int sessionInfoByUrl = this.mLoader.getSessionInfoByUrl(str, xLSessionInfo);
        if (XLErrorCode.NO_ERROR == sessionInfoByUrl) {
            return sessionInfoByUrl;
        }
        XLLog.e(TAG, "XLDownloadManager::getSessionInfoByUrl end, ret=[" + sessionInfoByUrl + "]");
        return sessionInfoByUrl;
    }
}
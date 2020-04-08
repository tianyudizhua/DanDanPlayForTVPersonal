package com.dandanplay.tv

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.os.StrictMode
import androidx.multidex.MultiDex
import com.alibaba.android.arouter.launcher.ARouter
import com.seiko.common.app.AppDelegate
import com.seiko.common.app.AppSetupDelegate
import com.seiko.common.util.timber.NanoDebugTree
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

class App : Application(), AppDelegate by AppSetupDelegate() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate() {
        super.onCreate()
        // 日志
        if (BuildConfig.DEBUG) {
            Timber.plant(NanoDebugTree())
        }
        // 路由
        if (BuildConfig.DEBUG) {
            // 打印日志
            ARouter.openLog()
            //开启调试模式
            ARouter.openDebug()
        }
        ARouter.init(this)
        // 注解
        startKoin {
            androidContext(this@App)
        }

        setupApplication()

        strictMode()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        configurationChanged(newConfig)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        clearOnLowMemory()
    }

    /**
     * 严格模式
     */
    private fun strictMode() {
        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder()
            .detectDiskReads()
            .detectDiskWrites()
            .detectNetwork()
            .penaltyLog()
            .build())
        StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder()
            .detectLeakedSqlLiteObjects()
//            .detectLeakedClosableObjects()
            .penaltyLog()
            .penaltyDeath()
            .build())
    }

}
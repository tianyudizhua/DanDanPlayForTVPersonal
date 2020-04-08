package com.seiko.common.app

import android.app.Application
import android.content.res.Configuration
import com.seiko.common.di.moshiModule
import com.seiko.common.di.networkModule
import com.seiko.common.util.helper.providerAppManager
import com.seiko.common.util.prefs.initMMKV
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.jessyan.autosize.AutoSizeConfig
import org.koin.core.context.loadKoinModules
import timber.log.Timber

/**
 * @description：插件自动加载该类，实现服务注册，需要指定在gradle.properties中application
 */
class ProviderApplication : InitComponent {

    override fun onCreate(application: Application) {
        Timber.tag("Provider").d("start register common.")

        // 关闭AutoSize日志
        AutoSizeConfig.getInstance().setLog(false)

        loadKoinModules( listOf(
            // JSON
            moshiModule,
            // 本地存储、网络请求
            networkModule
        ))

        application.providerAppManager()

        GlobalScope.launch(Dispatchers.IO) {
            // 初始化MMKV
            application.initMMKV()
        }
    }

    override fun onLowMemory() {

    }

    override fun onConfigurationChanged(newConfig: Configuration) {

    }

}
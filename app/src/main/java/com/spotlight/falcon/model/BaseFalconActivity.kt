package com.spotlight.falcon.model

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.shadowsocks.preference.DataStore
import com.spotlight.falcon.activity.MainActivity
import com.spotlight.falcon.contract.FalconApplication
import com.spotlight.falcon.datas.LanguageEvent
import com.spotlight.falcon.resid.FalconResId
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

open class BaseFalconActivity: AppCompatActivity() {
    protected var falconResId: FalconResId = FalconResId()

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase)
        falconResId.setLocale(this, DataStore.falconStorageLanguage)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
        falconResId.setLocale(this, DataStore.falconStorageLanguage)
    }

    override fun onResume() {
        super.onResume()
        falconResId.setLocale(this, DataStore.falconStorageLanguage)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        falconResId.setLocale(this, DataStore.falconStorageLanguage)
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun switchLanguage(language: LanguageEvent) {
        falconResId.setLocale(this, language.language)
        recreate()
    }
}
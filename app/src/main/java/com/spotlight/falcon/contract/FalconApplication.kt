package com.spotlight.falcon.contract

import android.app.Application
import android.widget.Toast
import com.github.shadowsocks.Core
import com.spotlight.falcon.BuildConfig
import com.spotlight.falcon_ui.ui.FalconUIDialog
import timber.log.Timber
import kotlin.properties.Delegates

class FalconApplication: Application() {
    companion object {
        var falconAPP: FalconApplication by Delegates.notNull()
        fun String.logFalcon() {
            if (BuildConfig.DEBUG) Timber.tag("falcon").e(this)
        }

        fun String.toastFalcon() {
            Toast.makeText(falconAPP, this, Toast.LENGTH_SHORT).show()
        }
    }
    val falconUtils: FalconUtils by lazy {
        FalconUtils(this)
    }
    val falconUIDialog: FalconUIDialog by lazy {
        FalconUIDialog()
    }
    override fun onCreate() {
        super.onCreate()
        falconAPP = this
        Core.init(this)
        falconUtils.checkFalconProcess {
            when(it) {
                false -> falconUtils.finishInFalse()
                true -> falconUtils.finishInTrue()
            }
        }
    }
}
package com.spotlight.falcon.contract

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.VpnService
import androidx.activity.result.contract.ActivityResultContract
import com.github.shadowsocks.preference.DataStore
import com.github.shadowsocks.utils.Key
import timber.log.Timber

class MainResultContracts : ActivityResultContract<Void?, Boolean>() {
    private var cachedIntent: Intent? = null

    override fun getSynchronousResult(context: Context, input: Void?): SynchronousResult<Boolean>? {
        if (DataStore.serviceMode == Key.modeVpn) VpnService.prepare(context)?.let { intent ->
            cachedIntent = intent
            //弹窗展示
            return null
        }
        return SynchronousResult(true)
    }

    override fun createIntent(context: Context, input: Void?) = cachedIntent!!.also { cachedIntent = null }

    override fun parseResult(resultCode: Int, intent: Intent?): Boolean {
        val result = if (resultCode == Activity.RESULT_OK) {
            true
        } else {
            Timber.e("Failed to start VpnService: $intent")
            false
        }
        return result
    }
}
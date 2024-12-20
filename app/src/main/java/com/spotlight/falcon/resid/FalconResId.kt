package com.spotlight.falcon.resid

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.github.shadowsocks.Core
import com.github.shadowsocks.database.ProfileManager
import com.github.shadowsocks.preference.DataStore
import com.spotlight.falcon.R
import com.spotlight.falcon.contract.FalconApplication
import com.spotlight.falcon.databinding.ActivityAboutUsBinding
import com.spotlight.falcon.databinding.ActivityDispositionBinding
import com.spotlight.falcon.databinding.ActivityFalconWebBinding
import com.spotlight.falcon.databinding.ActivityIntroductionBinding
import com.spotlight.falcon.databinding.ActivityJoinResultBinding
import com.spotlight.falcon.databinding.ActivityLanguageBinding
import com.spotlight.falcon.databinding.ActivityMainBinding
import com.spotlight.falcon.databinding.ActivityMoreDetailBinding
import com.spotlight.falcon.datas.FalconContent
import com.spotlight.falcon.datas.FalconContent.falconServerMap
import com.spotlight.falcon_ui.info.DispositionChildNode
import java.util.Locale

class FalconResId {
    fun getResId(context: AppCompatActivity, resId: Int, back: () -> Unit, next: (Any) -> Unit) {
        context.onBackPressedDispatcher.addCallback(context, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                back()
            }
        })
        return when (resId) {
            R.layout.activity_about_us -> next.invoke(ActivityAboutUsBinding.inflate(context.layoutInflater))
            R.layout.activity_disposition -> next.invoke(ActivityDispositionBinding.inflate(context.layoutInflater))
            R.layout.activity_introduction -> next.invoke(
                ActivityIntroductionBinding.inflate(
                    context.layoutInflater
                )
            )

            R.layout.activity_join_result -> next.invoke(ActivityJoinResultBinding.inflate(context.layoutInflater))
            R.layout.activity_language -> next.invoke(ActivityLanguageBinding.inflate(context.layoutInflater))
            R.layout.activity_main -> next.invoke(ActivityMainBinding.inflate(context.layoutInflater))
            R.layout.activity_more_detail -> next.invoke(ActivityMoreDetailBinding.inflate(context.layoutInflater))
            R.layout.activity_falcon_web -> next.invoke(ActivityFalconWebBinding.inflate(context.layoutInflater))
            else -> {}
        }
    }

    fun <T> falconToPage(
        context: AppCompatActivity,
        clazz: Class<T>,
        finishEnable: Boolean = false
    ) {
        context.startActivity(Intent(context, clazz))
        if (finishEnable) context.finish()
    }

    fun <T> falconToPageFlag(
        context: AppCompatActivity,
        clazz: Class<T>,
        flag: Int,
        finishEnable: Boolean = false
    ) {
        context.startActivity(Intent(context, clazz).apply {
            addFlags(flag)
        })
        if (finishEnable) context.finish()
    }

    fun <T> falconToPageOwnParams(
        context: AppCompatActivity,
        clazz: Class<T>,
        bundle: Bundle,
        finishEnable: Boolean = false
    ) {
        context.startActivity(Intent(context, clazz).apply { putExtras(bundle) })
        if (finishEnable) context.finish()
    }

    fun falconCountToString(count: Long): String {
        val hours = count / 3600
        val minutes = count % 3600 / 60
        val seconds = count % 3600 % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    fun falconNowNode(): DispositionChildNode? {
        val profile = ProfileManager.getProfile(DataStore.profileId)
        var node: DispositionChildNode? = null
        if (profile != null) {
            node = DispositionChildNode(
                profile.host,
                profile.remotePort,
                profile.method,
                profile.password,
                profile.nodeParentName,
                profile.nodeChildName,
                profile.nodeParentCode,
                profile.nodeInFast
            )
        }
        return node
    }

    fun falconMainConnectVpn(
        connectType: String = "fast",
        connectNode: DispositionChildNode? = null,
        connectRefuse: () -> Unit
    ) {
        if (falconServerMap.isEmpty()) {
            connectRefuse.invoke()
            return
        }
        val fasts = falconServerMap["fast"]
        val games = falconServerMap["game"]
        val servers = falconServerMap["server"]
        if (fasts.isNullOrEmpty() && (servers.isNullOrEmpty() || games.isNullOrEmpty())) {
            connectRefuse.invoke()
            return
        }
        val random: DispositionChildNode? = when (connectType) {
            "fast" -> {
                (if (fasts!!.isNotEmpty()) fasts.random() else servers!!.random()).apply {
                    nodeInFast = "fast"
                }
            }

            "game" -> {
                games!!.random().apply {
                    nodeInFast = "game"
                }
            }

            else -> {
                null
            }
        }
        val profileNode: DispositionChildNode? = falconNowNode()
        if (connectType == "server" && connectNode == null && (profileNode == null || profileNode.nodeInFast != "server")) {
            connectRefuse.invoke()
            return
        }

        val node: DispositionChildNode? = if (connectType == "fast" || connectType == "game") {
            random
        } else if (connectType == "server") {
            connectNode ?: profileNode
        } else {
            null
        }
        if (node == null || node.nodeInFast != connectType) {
            connectRefuse.invoke()
            return
        }
        val end = ProfileManager.createProfile()
        end.host = node.nodeIp
        end.remotePort = node.nodePort
        end.method = node.nodeMethod
        end.password = node.nodePassword
        end.nodeParentName = node.nodeParentName
        end.nodeChildName = node.nodeChildName
        end.nodeParentCode = node.nodeParentCode
        end.nodeInFast = node.nodeInFast
        end.name = node.nodeChildName
        DataStore.profileId = end.id
        ProfileManager.updateProfile(end)
        Core.startService()
    }

    fun falconMainDisconnectVpn() {
        Core.stopService()
    }

    fun falconNoNet(context: Context?): Boolean {
        if (context == null) return true
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.activeNetwork?.let {
            connectivityManager.getNetworkCapabilities(it)
        }
        return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) != true
    }

    fun setLocale(context: AppCompatActivity, languageCode: String) {
        val resource = context.resources
        val config = resource.configuration
        val locale = Locale(languageCode)
        config.setLocale(locale)
        resource.updateConfiguration(config, resource.displayMetrics)
        context.createConfigurationContext(config)
        FalconApplication.falconAPP.applicationContext.createConfigurationContext(config)
    }

}
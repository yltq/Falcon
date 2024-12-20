package com.spotlight.falcon.datas

import com.github.shadowsocks.bg.BaseService
import com.spotlight.falcon.BuildConfig
import com.spotlight.falcon_ui.info.DispositionChildNode
import java.util.Locale
import kotlin.random.Random

object FalconContent {
    val falconServerUrl: String = if (BuildConfig.DEBUG) "https://test.securelinkvpn.com" else "https://api.securelinkvpn.com"
    val falconEmail: String = if (BuildConfig.DEBUG) "xxx@qq.com" else "support@falconlinkvpn.com"
    val webPolicyUrl: String = if (BuildConfig.DEBUG) "https://www.google.com/" else "https://falconlinkvpn.com/privacy-policy/ "
    val webTermsUrl: String = if (BuildConfig.DEBUG) "https://www.google.com/" else "https://falconlinkvpn.com/terms-of-service/"

    fun updateFalconServerList(type: String, list: MutableList<DispositionChildNode>) {
        if (list.isEmpty()) return
        when(type) {
            "fast" -> {
                falconFasts.clear()
                falconFasts.addAll(list)
            }
            "game" -> {
                falconGame.clear()
                falconGame.addAll(list)
            }
            "server" -> {
                falconServer.clear()
                falconServer.addAll(list)
            }
        }
    }

    private val falconFasts: MutableList<DispositionChildNode> = mutableListOf()
    private val falconGame: MutableList<DispositionChildNode> = mutableListOf()
    private val falconServer: MutableList<DispositionChildNode> = mutableListOf()
    val falconServerMap: MutableMap<String, MutableList<DispositionChildNode>> = mutableMapOf(
        "fast" to falconFasts,
        "game" to falconGame,
        "server" to falconServer
    )
    var falconConnect: Boolean = false
    var falconConnectService: BaseService.State = BaseService.State.Stopped
    fun falconConnecting() = falconConnectService == BaseService.State.Connecting
    fun falconDisconnecting() = falconConnectService == BaseService.State.Stopping
    var falconConnectTotal: Long = 0 //总连接时长
    var falconCountryCode: String = Locale.getDefault().country

}
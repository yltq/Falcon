package com.spotlight.falcon.model

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.text.format.Formatter
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceDataStore
import com.github.shadowsocks.aidl.IShadowsocksService
import com.github.shadowsocks.aidl.ShadowsocksConnection
import com.github.shadowsocks.aidl.TrafficStats
import com.github.shadowsocks.bg.BaseService
import com.github.shadowsocks.core.R
import com.github.shadowsocks.preference.DataStore
import com.github.shadowsocks.preference.OnPreferenceDataStoreChangeListener
import com.github.shadowsocks.utils.Key
import com.github.shadowsocks.utils.StartService
import com.spotlight.falcon.activity.JoinResultActivity
import com.spotlight.falcon.activity.MainActivity
import com.spotlight.falcon.contract.DispositionActivityResultContract
import com.spotlight.falcon.contract.FalconApplication
import com.spotlight.falcon.contract.FalconApplication.Companion.logFalcon
import com.spotlight.falcon.contract.FalconApplication.Companion.toastFalcon
import com.spotlight.falcon.datas.FalconConnectEvent
import com.spotlight.falcon.datas.FalconConnectSpeedEvent
import com.spotlight.falcon.datas.FalconContent
import com.spotlight.falcon.datas.FalconContent.falconConnect
import com.spotlight.falcon.datas.FalconContent.falconConnectService
import com.spotlight.falcon.datas.FalconContent.falconConnectTotal
import com.spotlight.falcon.resid.FalconResId
import com.spotlight.falcon_ui.info.DispositionChildNode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import java.lang.ref.WeakReference

class MainViewModel(private var activity: WeakReference<MainActivity>?, private val falconResId: FalconResId): ViewModel(),
    OnPreferenceDataStoreChangeListener, ShadowsocksConnection.Callback {
    private var vpnJob: Job? = null
    val uiLiveData: MutableLiveData<String> = MutableLiveData()
    val uiConnectCountLiveData: MutableLiveData<String> = MutableLiveData()
    val uiTrafficLiveData: MutableLiveData<Pair<String, String>> = MutableLiveData()
    val uiBottomServerLiveData: MutableLiveData<Pair<String, DispositionChildNode?>> = MutableLiveData()
    private lateinit var connection: ShadowsocksConnection

    private var falconConnectType: String = "fast"
    private var falconConnectNode: DispositionChildNode? = null
    private var falconDisconnectThroughDisposition: Boolean = false
    private var falconDisconnectThroughNode: DispositionChildNode? = null
    private var falconDisconnectThroughType: String = ""

    companion object {
        private var falconConnectStartCountJob: Job? = null
    }


    private val serverRegister = activity?.get()?.registerForActivityResult(StartService()) {
        "startJob contracts is $it".logFalcon()
        if (it) {
            activity?.get()?.apply {
                getString(com.spotlight.falcon_language.R.string.main_permission_not_granted).toastFalcon()
            }
            return@registerForActivityResult
        }
        vpnJob?.cancel()
        vpnJob = viewModelScope.launch {
            uiLiveData.postValue("connecting")
            falconConnectService = BaseService.State.Connecting
            delay(2000)
            falconResId.falconMainConnectVpn(falconConnectType, falconConnectNode) {
                uiLiveData.postValue("disconnected")
            }
        }
    }

    private val dispositionForResult = activity?.get()?.registerForActivityResult(DispositionActivityResultContract()) { result ->
        val type: String = result["type"]?:""
        val nodeIp: String? = result["nodeIp"]
        val nodePort: Int = result["nodePort"]?.toIntOrNull()?:8800
        val nodeMethod: String? = result["nodeMethod"]
        val nodePassword: String? = result["nodePassword"]
        val nodeParentName: String? = result["nodeParentName"]
        val nodeChildName: String? = result["nodeChildName"]
        val nodeParentCode: String? = result["nodeParentCode"]
        val nodeInFast: String? = result["nodeInFast"]

        var node: DispositionChildNode? = null
        if (!TextUtils.isEmpty(nodeIp) && !TextUtils.isEmpty(nodeMethod) && !TextUtils.isEmpty(nodePassword) &&
            !TextUtils.isEmpty(nodeParentCode) && !TextUtils.isEmpty(nodeInFast)) {
            node = DispositionChildNode(
                nodeIp!!, nodePort, nodeMethod!!, nodePassword!!, nodeParentName?:"",
                nodeChildName?:"", nodeParentCode!!, nodeInFast!!
            )
        }

        if (type.isEmpty()) return@registerForActivityResult
        if (falconConnect) {
            falconDisconnectThroughDisposition = true
            falconDisconnectThroughNode = if (type == "fast") null else if (type == "game") null else node
            falconDisconnectThroughType = type
            falconResId.falconMainDisconnectVpn()
        } else {
            startJob(type, if (type == "fast") null else if (type == "game") null else node)
        }
    }


    init {
        DataStore.publicStore.registerChangeListener(this)
        connection = ShadowsocksConnection(true).apply {
            bandwidthTimeout = 500L
            activity?.get()?.apply {
                connect(this, this@MainViewModel)
            }
        }
    }

    fun falconToPageDisposition() {
        dispositionForResult?.launch(null)
    }

    //分析可能得参数类型
    //首页-"none", null
    //服务器列表："fast", null -> 点击fast
    //"game", null -> 点击game
    //"server", not null -> 触发指定服务器配置
    fun startJob(connectType: String= "none", connectNode: DispositionChildNode? = null) {
        "startJob serverRegister is $serverRegister".logFalcon()
        if (falconResId.falconNoNet(activity?.get())) {
            activity?.get()?.apply {
                FalconApplication.falconAPP.falconUIDialog.falconNoNet(this)
            }
            return
        }
        if (mutableListOf<String>(FalconContent.falconCountryCode).any { it == "CN" || it == "IR" || it == "HK" || it == "MO" }) {
            activity?.get()?.apply {
                FalconApplication.falconAPP.falconUIDialog.falconRegionLimit(this, confirm = {
                    this.finish()
                })
            }
            return
        }
        viewModelScope.launch {
            val server = FalconContent.falconServerMap["server"]
            if (server.isNullOrEmpty()) {
                if (!FalconApplication.falconAPP.falconUtils.falconLoadingServer) {
                    FalconApplication.falconAPP.falconUtils.falconServer()
                }
                activity?.get()?.apply {
                    val dialog =
                        FalconApplication.falconAPP.falconUIDialog.falconLoadingDialog(this)
                    delay(2000)
                    dialog?.dismiss()
                }
            } else {
                val profileNode: DispositionChildNode? = falconResId.falconNowNode()
                if (connectType == "none") {
                    if (profileNode == null || profileNode.nodeInFast == "fast") {
                        falconConnectType = "fast"
                    } else if (profileNode.nodeInFast == "game") {
                        falconConnectType = "game"
                    } else if (profileNode.nodeInFast == "server") {
                        falconConnectType = "server"
                    }
                } else if (connectType == "fast") {
                    falconConnectType = "fast"
                    uiBottomServerLiveData.postValue(Pair(falconConnectType, null))
                } else if (connectType == "game") {
                    falconConnectType = "game"
                    uiBottomServerLiveData.postValue(Pair(falconConnectType, null))
                } else if (connectType == "server" && connectNode != null) {
                    falconConnectType = "server"
                    falconConnectNode = connectNode
                    uiBottomServerLiveData.postValue(Pair(falconConnectType, connectNode))
                }
                serverRegister?.launch(null)
            }
        }
    }

    fun stopJob() {
        vpnJob?.cancel()
        vpnJob = viewModelScope.launch {
            uiLiveData.postValue("disconnecting")
            falconConnectService = BaseService.State.Stopping
            delay(2000)
            falconResId.falconMainDisconnectVpn()
        }
    }

    fun cancelJob() {
        vpnJob?.cancel()
    }

    fun destroy() {
        activity?.get()?.apply {
            connection.disconnect(this)
        }
        DataStore.publicStore.unregisterChangeListener(this)
    }

    override fun stateChanged(state: BaseService.State, profileName: String?, msg: String?) {
        "state is $state".logFalcon()
        when(state) {
            BaseService.State.Connected -> {
                falconConnect = true
                falconConnectService = state
                falconDisconnectThroughDisposition = false
                falconDisconnectThroughType = ""
                falconDisconnectThroughNode = null
                uiLiveData.postValue("connected")
                falconStartCount()
            }
            BaseService.State.Connecting -> {
                falconDisconnectThroughDisposition = false
                falconDisconnectThroughType = ""
                falconDisconnectThroughNode = null
                uiLiveData.postValue("connecting")
            }
            BaseService.State.Stopping -> {
                uiLiveData.postValue("disconnecting")
            }
            BaseService.State.Stopped -> {
                falconConnect = false
                if (falconConnectService == BaseService.State.Connecting) {
                    falconConnectService = state
                    //连接失败
                    activity?.get()?.apply {
                        FalconApplication.falconAPP.falconUIDialog.falconConnectionFail(this, retry = {
                            startJob(falconConnectType, falconConnectNode)
                        }, feedback = {
                            fun sendEmail(email: String) {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:$email")
                                }
                                if (intent.resolveActivity(packageManager) != null) {
                                    startActivity(intent)
                                } else {
                                    getString(com.spotlight.falcon_language.R.string.more_detail_no_email).toastFalcon()
                                }
                            }
                            sendEmail(FalconContent.falconEmail)
                        })
                    }
                } else {
                    falconConnectService = state
                    val time  = falconResId.falconCountToString(falconConnectTotal)
                    falconEndCount()
                    uiLiveData.postValue("disconnected")
                    if (!falconDisconnectThroughDisposition) {
                        falconDisconnectThroughType = ""
                        falconDisconnectThroughNode = null
                        viewModelScope.launch {
                            activity?.get()?.apply {
                                delay(280)
                                if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                                    val node = falconResId.falconNowNode()
                                    val bundle = Bundle()
                                    bundle.putString("time", time)
                                    if (node?.nodeInFast != "game") {
                                        bundle.putString("ip", node?.nodeIp)
                                    }
                                    falconResId.falconToPageOwnParams(this, JoinResultActivity::class.java, bundle)
                                }
                            }
                        }
                    } else {
                        falconDisconnectThroughDisposition = false
                        startJob(falconDisconnectThroughType, falconDisconnectThroughNode)
                    }
                }
            }
            else -> {}
        }
    }

    override fun onServiceConnected(service: IShadowsocksService) {

    }

    override fun onPreferenceDataStoreChanged(store: PreferenceDataStore, key: String) {
        if (key != Key.serviceMode) return
        activity?.get()?.apply {
            connection.disconnect(this)
            connection.connect(this, this@MainViewModel)
        }
    }

    override fun trafficUpdated(profileId: Long, stats: TrafficStats) {
        super.trafficUpdated(profileId, stats)
        activity?.get()?.apply {
            val pair = Pair(getString(R.string.speed, Formatter.formatFileSize(this, stats.rxRate)),
                    getString(R.string.speed, Formatter.formatFileSize(this, stats.txRate)))
//            uiTrafficLiveData.postValue(pair)
            EventBus.getDefault().post(FalconConnectSpeedEvent(pair))
        }
    }

    private fun falconStartCount() {
        falconConnectStartCountJob?.cancel()
        falconConnectStartCountJob = CoroutineScope(Dispatchers.IO).launch {
            while (falconConnect) {
                delay(1000)
                falconConnectTotal += 1
                EventBus.getDefault().post(FalconConnectEvent(falconResId.falconCountToString(falconConnectTotal)))
//                uiConnectCountLiveData.postValue()
            }
        }
    }

    private fun falconEndCount() {
        falconConnectStartCountJob?.cancel()
        falconConnectTotal = 0
    }
}
package com.spotlight.falcon.activity

import android.animation.ObjectAnimator
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.github.shadowsocks.bg.BaseService
import com.github.shadowsocks.database.ProfileManager
import com.github.shadowsocks.preference.DataStore
import com.spotlight.falcon.R
import com.spotlight.falcon.contract.FalconApplication
import com.spotlight.falcon.contract.FalconApplication.Companion.toastFalcon
import com.spotlight.falcon.databinding.ActivityMainBinding
import com.spotlight.falcon.datas.FalconConnectEvent
import com.spotlight.falcon.datas.FalconConnectSpeedEvent
import com.spotlight.falcon.datas.FalconContent
import com.spotlight.falcon.datas.FalconContent.falconConnecting
import com.spotlight.falcon.datas.FalconContent.falconDisconnecting
import com.spotlight.falcon.gesture.FalconGesture
import com.spotlight.falcon.gesture.VpnUIGesture
import com.spotlight.falcon.model.BaseFalconActivity
import com.spotlight.falcon.model.MainViewModel
import com.spotlight.falcon.model.MainViewModelFactory
import com.spotlight.falcon.resid.FalconResId
import com.spotlight.falcon_ui.info.DispositionChildNode
import com.spotlight.falcon_ui.info.FalconTopBarUI
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.lang.ref.WeakReference

class MainActivity : BaseFalconActivity(), FalconGesture, VpnUIGesture {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var factory: MainViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        falconResId.getResId(this, R.layout.activity_main, back = {
            gestureBack()
        }) {
            binding = it as ActivityMainBinding
            setContentView(binding.root)
            gestureInit(this)
        }
    }

    override fun gestureBack() {
        if (falconConnecting()) {
            getString(com.spotlight.falcon_language.R.string.disposition_connecting).toastFalcon()
            return
        }
        if (falconDisconnecting()) {
            getString(com.spotlight.falcon_language.R.string.disposition_disconnecting).toastFalcon()
            return
        }
        finish()
    }

    override fun gestureInit(context: Context) {
        context.apply {
            factory = MainViewModelFactory(WeakReference(this@MainActivity), falconResId)
            viewModel = ViewModelProvider(this@MainActivity, factory)[MainViewModel::class.java]
            binding.mainTopBar.initIconAction(FalconTopBarUI(
                com.spotlight.falcon_ui.R.mipmap.main_more,
                com.spotlight.falcon_ui.R.mipmap.main_server,
                getString(com.spotlight.falcon_language.R.string.app_name),
                clickLeft = {
                    if (falconConnecting()) {
                        getString(com.spotlight.falcon_language.R.string.disposition_connecting).toastFalcon()
                        return@FalconTopBarUI
                    }
                    if (falconDisconnecting()) {
                        getString(com.spotlight.falcon_language.R.string.disposition_disconnecting).toastFalcon()
                        return@FalconTopBarUI
                    }
                    falconResId.falconToPage(this@MainActivity, MoreDetailActivity::class.java)
                }, clickRight = {
                    if (falconConnecting()) {
                        getString(com.spotlight.falcon_language.R.string.disposition_connecting).toastFalcon()
                        return@FalconTopBarUI
                    }
                    if (falconDisconnecting()) {
                        getString(com.spotlight.falcon_language.R.string.disposition_disconnecting).toastFalcon()
                        return@FalconTopBarUI
                    }
                    lifecycleScope.launch {
                        val server = FalconContent.falconServerMap["server"]
                        if (server.isNullOrEmpty()) {
                            if (!FalconApplication.falconAPP.falconUtils.falconLoadingServer) {
                                FalconApplication.falconAPP.falconUtils.falconServer()
                            }
                            val dialog =
                                FalconApplication.falconAPP.falconUIDialog.falconLoadingDialog(this@MainActivity)
                            delay(2000)
                            dialog?.dismiss()
                        }
                        viewModel.falconToPageDisposition()
                    }
                }
            ))
            val node: DispositionChildNode? = falconResId.falconNowNode()
            when (node?.nodeInFast) {
                "fast" -> {
                    binding.mainLocationName.text =
                        getString(com.spotlight.falcon_language.R.string.disposition_auto)
                    binding.mainLocationIcon.initIconRes("fast")
                }

                "game" -> {
                    binding.mainLocationName.text =
                        getString(com.spotlight.falcon_language.R.string.disposition_game)
                    binding.mainLocationIcon.initIconRes("game")
                }

                else -> {
                    binding.mainLocationName.text = if (TextUtils.isEmpty(node?.nodeChildName))
                        getString(com.spotlight.falcon_language.R.string.disposition_auto) else node?.nodeChildName
                    binding.mainLocationIcon.initIconRes(node?.nodeParentCode ?: "")
                }
            }
            binding.mainBottomLocation.setOnClickListener {
                if (falconConnecting()) {
                    getString(com.spotlight.falcon_language.R.string.disposition_connecting).toastFalcon()
                    return@setOnClickListener
                }
                if (falconDisconnecting()) {
                    getString(com.spotlight.falcon_language.R.string.disposition_disconnecting).toastFalcon()
                    return@setOnClickListener
                }
                lifecycleScope.launch {
                    val server = FalconContent.falconServerMap["server"]
                    if (server.isNullOrEmpty()) {
                        if (!FalconApplication.falconAPP.falconUtils.falconLoadingServer) {
                            FalconApplication.falconAPP.falconUtils.falconServer()
                        }
                        val dialog =
                            FalconApplication.falconAPP.falconUIDialog.falconLoadingDialog(this@MainActivity)
                        delay(2000)
                        dialog?.dismiss()
                    }
                    viewModel.falconToPageDisposition()
                }
            }
            binding.mainCenterStartGroup.setOnClickListener {
                viewModel.startJob()
            }
            binding.mainCenterBtnDisconnect.setOnClickListener {
                FalconApplication.falconAPP.falconUIDialog.falconDisconnectDialog(
                    this@MainActivity,
                    disconnect = {
                        viewModel.stopJob()
                    })
            }

            viewModel.uiLiveData.observe(this@MainActivity) {
                when (it) {
                    "connecting" -> {
                        uiConnecting()
                    }

                    "disconnecting" -> {
                        uiDisconnecting()
                    }

                    "connected" -> {
                        uiConnected()
                    }

                    "disconnected" -> {
                        uiDisconnected()
                    }
                }
            }
            viewModel.uiBottomServerLiveData.observe(this@MainActivity) {
                val node = it.second
                when (it.first) {
                    "fast" -> {
                        binding.mainLocationName.text =
                            getString(com.spotlight.falcon_language.R.string.disposition_auto)
                        binding.mainLocationIcon.initIconRes("fast")
                    }

                    "game" -> {
                        binding.mainLocationName.text =
                            getString(com.spotlight.falcon_language.R.string.disposition_game)
                        binding.mainLocationIcon.initIconRes("game")
                    }

                    else -> {
                        binding.mainLocationName.text = if (TextUtils.isEmpty(node?.nodeChildName))
                            getString(com.spotlight.falcon_language.R.string.disposition_auto) else node?.nodeChildName
                        binding.mainLocationIcon.initIconRes(node?.nodeParentCode ?: "")
                    }
                }
            }
//            viewModel.uiConnectCountLiveData.observe(this@MainActivity) {
//                binding.mainConnectTime.text = it
//            }
//            viewModel.uiTrafficLiveData.observe(this@MainActivity) {
//                binding.mainBottomBpsDownload.text = it.first
//                binding.mainBottomBpsUpload.text = it.second
//            }
            if (FalconContent.falconConnect) {
                uiConnected()
            } else {
                uiDisconnected()
                if (DataStore.falconStorageAutomatic) {
                    //触发自动连接
                    viewModel.startJob()
                } else {
                    startWobbleAnimation()
                }
            }
        }
    }

    private fun startWobbleAnimation() {
        // 创建 ObjectAnimator，设置旋转动画
        if (!FalconContent.falconConnect) {
            lifecycleScope.launch {
                val animator = ObjectAnimator.ofFloat(binding.mainCenterStart, "translationY", 0f, 10f, 0f)
                animator.duration = 500 // 动画持续时间
                animator.repeatCount = ObjectAnimator.INFINITE // 无限重复
                animator.repeatMode = ObjectAnimator.REVERSE // 反向播放
                animator.start() // 启动动画
                delay(5000)
                animator.cancel()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        viewModel.cancelJob()
        if (FalconContent.falconConnectService == BaseService.State.Stopping && FalconContent.falconConnect) {
            FalconContent.falconConnectService = BaseService.State.Connected
            uiConnected()
        } else if (FalconContent.falconConnectService == BaseService.State.Connecting && !FalconContent.falconConnect) {
            FalconContent.falconConnectService = BaseService.State.Stopped
            uiDisconnected()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.destroy()
    }

    override fun uiConnecting() {
        binding.mainCenterConnecting.visibility = View.VISIBLE
        binding.mainCenterUnConnect.visibility = View.GONE
        binding.mainBottomBpsGroup.visibility = View.GONE
        binding.mainCenterConnected.visibility = View.GONE
        binding.mainCenterGesture.text =
            getText(com.spotlight.falcon_language.R.string.main_connecting)
    }

    override fun uiConnected() {
        binding.mainCenterConnected.visibility = View.VISIBLE
        binding.mainBottomBpsGroup.visibility = View.VISIBLE
        binding.mainCenterUnConnect.visibility = View.GONE
        binding.mainCenterConnecting.visibility = View.GONE
        binding.mainIp.text = falconResId.falconNowNode()?.nodeIp ?: "8.8.8.8"
    }

    override fun uiDisconnected() {
        binding.mainCenterUnConnect.visibility = View.VISIBLE
        binding.mainBottomBpsGroup.visibility = View.GONE
        binding.mainCenterConnected.visibility = View.GONE
        binding.mainCenterConnecting.visibility = View.GONE
    }

    override fun uiDisconnecting() {
        binding.mainCenterConnecting.visibility = View.VISIBLE
        binding.mainCenterUnConnect.visibility = View.GONE
        binding.mainBottomBpsGroup.visibility = View.GONE
        binding.mainCenterConnected.visibility = View.GONE
        binding.mainCenterGesture.text =
            getText(com.spotlight.falcon_language.R.string.main_disconnecting)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun countUpdate(event: FalconConnectEvent) {
        binding.mainConnectTime.text = event.time
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun speedUpdate(event: FalconConnectSpeedEvent) {
        binding.mainBottomBpsDownload.text = event.pair.first
        binding.mainBottomBpsUpload.text = event.pair.second
    }
}